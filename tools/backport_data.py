"""Converts the 1.21 data pack (generated + hand-written) into the 1.20.1 layout.

Run after datagen: python3 tools/backport_data.py
Output goes to src/generated/resources-1.20.1/data and replaces it completely.
"""

import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SOURCES = [ROOT / "src/generated/resources/data", ROOT / "src/main/resources/data"]
OUTPUT = ROOT / "src/generated/resources-1.20.1/data"

# 1.21 renamed these registry folders to singular names.
FOLDERS = {
    "recipe": "recipes",
    "advancement": "advancements",
    "loot_table": "loot_tables",
    "tags/item": "tags/items",
    "tags/block": "tags/blocks",
    "tags/entity_type": "tags/entity_types",
}
# Vanilla items that had other ids before 1.21.
ITEM_RENAMES = {
    "minecraft:short_grass": "minecraft:grass",
}
# NeoForge loot modifiers; Fabric and Forge add the same tables from code.
SKIPPED = ("neoforge/", "tactical_loadout/loot_modifiers/")


def target_path(relative: str) -> str:
    namespace, _, rest = relative.partition("/")
    for new, old in FOLDERS.items():
        if rest.startswith(new + "/"):
            return f"{namespace}/{old}{rest[len(new):]}"
    return relative


def item_stack(stack: dict) -> dict:
    stack = dict(stack)
    stack["item"] = stack.pop("id")
    return stack


def recipe(data: dict) -> dict:
    result = data.get("result")
    if isinstance(result, dict) and "id" in result:
        if data["type"] in ("minecraft:smelting", "minecraft:blasting", "minecraft:smoking", "minecraft:campfire_cooking"):
            data["result"] = result["id"]
        else:
            data["result"] = item_stack(result)
    return data


def item_predicate(predicate: dict) -> dict:
    items = predicate.get("items")
    if isinstance(items, str):
        predicate = dict(predicate)
        del predicate["items"]
        if items.startswith("#"):
            predicate["tag"] = items[1:]
        else:
            predicate["items"] = [items]
    return predicate


def advancement(data: dict) -> dict:
    display = data.get("display")
    if display and "id" in display.get("icon", {}):
        display["icon"] = item_stack(display["icon"])
    for criterion in data.get("criteria", {}).values():
        conditions = criterion.get("conditions", {})
        if criterion["trigger"] == "minecraft:inventory_changed" and "items" in conditions:
            conditions["items"] = [item_predicate(p) for p in conditions["items"]]
    return data


def biome_tag(data: dict) -> dict:
    values = []
    for entry in data["values"]:
        values.append(entry)
        ref = entry["id"] if isinstance(entry, dict) else entry
        if ref.startswith("#c:is_"):
            name = ref[len("#c:is_"):]
            # Fabric 1.20.1 convention tags drop the "is_" prefix; Forge 1.20.1 uses its own namespace.
            values.append({"id": f"#c:{name}", "required": False})
            values.append({"id": f"#forge:is_{name}", "required": False})
    data["values"] = values
    return data


def rename_items(value):
    if isinstance(value, dict):
        return {k: rename_items(v) for k, v in value.items()}
    if isinstance(value, list):
        return [rename_items(v) for v in value]
    return ITEM_RENAMES.get(value, value) if isinstance(value, str) else value


def convert(relative: str, data: dict) -> dict:
    data = rename_items(data)
    kind = relative.split("/", 2)[1] if "/" in relative else ""
    if kind == "recipe":
        return recipe(data)
    if kind == "advancement":
        return advancement(data)
    if relative.split("/", 1)[1].startswith("tags/worldgen/biome/"):
        return biome_tag(data)
    return data


def main() -> None:
    if OUTPUT.exists():
        shutil.rmtree(OUTPUT)
    count = 0
    for source in SOURCES:
        for path in sorted(source.rglob("*.json")):
            relative = path.relative_to(source).as_posix()
            if relative.startswith(SKIPPED):
                continue
            data = convert(relative, json.loads(path.read_text()))
            out = OUTPUT / target_path(relative)
            out.parent.mkdir(parents=True, exist_ok=True)
            out.write_text(json.dumps(data, indent=2) + "\n")
            count += 1
    print(f"wrote {count} files to {OUTPUT.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
