#!/usr/bin/env python3
import json
import math
import sys
from pathlib import Path

import numpy as np
from PIL import Image

from textures import hex_rgb

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/tacz_camo_armor"
MODEL_DIR = ASSETS / "models/item"
GEO_DIR = ASSETS / "geo/armor"
TEX_DIR = ASSETS / "textures"
ATLAS = ROOT / "src/main/resources/assets/minecraft/atlases/blocks.json"
NS = "tacz_camo_armor"

FORMS = {
    "ct": ["ct"], "t": ["t"], "ru": ["ru_emr"], "swat": ["swat"],
    "us": ["us_woodland", "us_desert", "us_green", "us_multicam", "us_snow"],
    "ghillie": ["ghillie", "ghillie_snow"],
}
TIERS = ["light", "medium", "heavy"]
PIECES = {
    "helmet": ["armorHead"],
    "plate_carrier": ["armorBody", "armorRightArm", "armorLeftArm"],
    "leggings": ["armorRightLeg", "armorLeftLeg"],
    "boots": ["armorRightBoot", "armorLeftBoot"],
}
DEVICES = {
    "nvg_pvs14": ["nvg_pvs14"],
    "nvg_gpnvg18": ["nvg_gpnvg18"],
    "nvg_gpnvg18_green": ["nvg_gpnvg18_green"],
    "nvg_thermal": ["nvg_thermal"],
    "gas_mask": ["gas_mask"],
}
ANGLES = [-45.0, -22.5, 0.0, 22.5, 45.0]

SWATCH = 8
SWATCHES = [
    "frag_side", "frag_top", "fuze", "spoon", "ring", "he_side", "he_band", "flash_side",
    "flash_top", "smoke_side", "smoke_top", "battery_side", "battery_top", "battery_bottom", "filter_side", "filter_top",
    "metal_dark",
]


def paint_swatch(name, rng):
    ys, xs = np.mgrid[0:SWATCH, 0:SWATCH]
    def flat(color):
        return np.ones((SWATCH, SWATCH, 3)) * hex_rgb(color)
    if name == "frag_side":
        rgb = flat("4b5320")
        rgb[SWATCH // 2] *= 0.82
        rgb[0] *= 1.12
    elif name == "frag_top":
        rgb = flat("535b26")
    elif name == "fuze":
        rgb = flat("6d7073")
        rgb[0] *= 1.3
        rgb[-1] *= 0.8
    elif name == "spoon":
        rgb = flat("8a8d8f")
        rgb[:, 0] *= 1.2
    elif name == "ring":
        rgb = flat("b5b8bb")
    elif name == "he_side":
        rgb = flat("3d4527")
        rgb[(ys % 3 == 2) | (xs % 3 == 2)] *= 0.72
    elif name == "he_band":
        rgb = flat("c9a227")
        rgb[:, xs[0] % 4 == 3] *= 0.85
    elif name == "flash_side":
        rgb = flat("9a9c8c")
        holes = ((ys == 2) | (ys == 5)) & (xs % 3 == 1)
        rgb[holes] = hex_rgb("2c2d28")
        rgb[0] *= 1.1
    elif name == "flash_top":
        rgb = flat("8d8f80")
        rgb[3:5, 3:5] = hex_rgb("3a3b35")
    elif name == "smoke_side":
        rgb = flat("59613f")
        rgb[:2] = hex_rgb("c9c9bf")
        rgb[1, (xs[0] % 2 == 0) & (xs[0] > 0) & (xs[0] < SWATCH - 1)] = hex_rgb("6e6e66")
        rgb[-1] *= 0.8
    elif name == "smoke_top":
        rgb = flat("4f5638")
        rgb[2:6, 2:6] *= 0.75
    elif name == "battery_side":
        rgb = flat("262626")
        rgb[:3] = hex_rgb("c27c3a")
        rgb[3] *= 0.7
        rgb[5, 3:5] = hex_rgb("d9d9d9")
    elif name == "battery_top":
        rgb = flat("c27c3a")
        rgb[3:5, 3:5] = hex_rgb("d0d3d6")
    elif name == "battery_bottom":
        rgb = flat("3a3a3a")
    elif name == "filter_side":
        rgb = flat("4b5233")
        rgb[ys % 2 == 1] *= 0.78
    elif name == "filter_top":
        rgb = flat("4b5233")
        rgb[(ys % 2 == 1) & (xs % 2 == 1)] = hex_rgb("23261a")
    elif name == "metal_dark":
        rgb = flat("3a3c3e")
        rgb[ys % 2 == 0] *= 1.25
    else:
        raise ValueError(name)
    return rgb * (1.0 + (rng.random((SWATCH, SWATCH)) - 0.5) * 0.06)[..., None]


def gear_texture():
    cols = 8
    rows = math.ceil(len(SWATCHES) / cols)
    size = 1 << max(cols * SWATCH, rows * SWATCH).bit_length() - 1
    img = np.zeros((size, size, 4))
    rng = np.random.default_rng(9)
    for i, name in enumerate(SWATCHES):
        x, y = (i % cols) * SWATCH, (i // cols) * SWATCH
        img[y:y + SWATCH, x:x + SWATCH, :3] = paint_swatch(name, rng)
        img[y:y + SWATCH, x:x + SWATCH, 3] = 255
    Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), "RGBA").save(TEX_DIR / "item/gear_parts.png")
    return size


def swatch_uv(name, size):
    i = SWATCHES.index(name)
    cell = SWATCH * 16 / size
    u, v = (i % 8) * cell, (i // 8) * cell
    return [round(u, 4), round(v, 4), round(u + cell, 4), round(v + cell, 4)]


class Gear:
    def __init__(self, size):
        self.size = size
        self.elements = []

    def box(self, lo, hi, side, top=None, bottom=None):
        top = top or side
        bottom = bottom or top
        faces = {}
        for face in ("north", "south", "east", "west", "up", "down"):
            mat = top if face == "up" else bottom if face == "down" else side
            faces[face] = {"uv": swatch_uv(mat, self.size), "texture": "#0"}
        self.elements.append({"from": list(lo), "to": list(hi), "faces": faces})

    def cylinder(self, cx, cz, radius, y0, y1, side, top=None, bottom=None):
        inner = round(radius * 0.7, 3)
        self.box((cx - radius, y0, cz - inner), (cx + radius, y1, cz + inner), side, top, bottom)
        self.box((cx - inner, y0, cz - radius), (cx + inner, y1, cz + radius), side, top, bottom)

    def fuze(self, top, half_x, front_z):
        self.box((7, top, 7), (9, top + 1.75, 9), "fuze")
        self.box((7.25, top + 1.75, 7.25), (8.75, top + 2.25, 8.75), "fuze")
        lever = 8 + half_x + 0.1
        self.box((8.75, top + 1.0, 7.4), (lever + 0.5, top + 1.6, 8.6), "spoon")
        self.box((lever, top - 4.5, 7.4), (lever + 0.5, top + 1.6, 8.6), "spoon")
        ring = front_z - 0.6
        self.box((7.75, top + 0.6, ring + 0.5), (8.25, top + 1.1, 7), "ring")
        self.box((6.8, top - 0.1, ring), (9.2, top + 0.4, ring + 0.5), "ring")
        self.box((6.8, top + 2.3, ring), (9.2, top + 2.8, ring + 0.5), "ring")
        self.box((6.8, top + 0.4, ring), (7.3, top + 2.3, ring + 0.5), "ring")
        self.box((8.7, top + 0.4, ring), (9.2, top + 2.3, ring + 0.5), "ring")


def grenade_display(gui_scale):
    return {
        "gui": {"rotation": [20, 225, 0], "translation": [0, 0, 0], "scale": [gui_scale] * 3},
        "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.5] * 3},
        "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [gui_scale * 0.9] * 3},
        "thirdperson_righthand": {"rotation": [10, 45, 0], "translation": [0, 2.5, 1.5], "scale": [0.6] * 3},
        "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0.5, 3, 0], "scale": [0.4] * 3},
        "head": {"rotation": [0, 0, 0], "translation": [0, 13, 0], "scale": [1.0] * 3},
    }


def gear_models(size):
    models = {}

    g = Gear(size)
    g.box((5.5, 4, 6.5), (10.5, 9, 9.5), "frag_side", "frag_top")
    g.box((6.5, 4, 5.5), (9.5, 9, 10.5), "frag_side", "frag_top")
    g.box((6, 3.5, 6), (10, 9.5, 10), "frag_side", "frag_top")
    g.fuze(9.5, 2.5, 5.5)
    models["frag_grenade"] = (g, 1.5)

    g = Gear(size)
    g.box((5.5, 3, 6.5), (10.5, 10, 9.5), "he_side", "frag_top")
    g.box((6.5, 3, 5.5), (9.5, 10, 10.5), "he_side", "frag_top")
    g.box((6, 2.5, 6), (10, 10.5, 10), "he_side", "frag_top")
    g.box((5.45, 6.25, 6.45), (10.55, 7.25, 9.55), "he_band")
    g.box((6.45, 6.25, 5.45), (9.55, 7.25, 10.55), "he_band")
    g.box((5.95, 6.25, 5.95), (10.05, 7.25, 10.05), "he_band")
    g.fuze(10.5, 2.5, 5.5)
    models["he_grenade"] = (g, 1.4)

    g = Gear(size)
    g.cylinder(8, 8, 1.5, 3, 9.5, "flash_side", "flash_top")
    g.fuze(9.5, 1.5, 6.5)
    models["flashbang"] = (g, 1.45)

    g = Gear(size)
    g.cylinder(8, 8, 1.75, 2.5, 9.5, "smoke_side", "smoke_top")
    g.fuze(9.5, 1.75, 6.25)
    models["smoke_grenade"] = (g, 1.4)

    g = Gear(size)
    g.cylinder(8, 8, 1.25, 4, 11, "battery_side", "battery_top", "battery_bottom")
    g.box((7.6, 11, 7.6), (8.4, 11.6, 8.4), "fuze")
    models["battery"] = (g, 1.5)

    g = Gear(size)
    g.cylinder(8, 8, 3, 5, 9, "filter_side", "filter_top")
    g.box((5.6, 5, 5.6), (10.4, 9, 10.4), "filter_side", "filter_top")
    g.box((7, 9, 7), (9, 10.25, 9), "metal_dark")
    models["gas_filter"] = (g, 1.6)

    for name, (gear, scale) in models.items():
        write_model(name, {
            "textures": {"0": f"{NS}:item/gear_parts", "particle": f"{NS}:item/gear_parts"},
            "gui_light": "side",
            "elements": gear.elements,
            "display": grenade_display(scale),
        })


def snap_rotation(rotation, pivot, offset):
    best = max(range(3), key=lambda i: abs(rotation[i]))
    angle = min(ANGLES, key=lambda a: abs(a - rotation[best]))
    if angle == 0:
        return None
    return {"angle": angle, "axis": "xyz"[best], "origin": [round(pivot[i] + offset[i], 4) for i in range(3)]}


def convert(cubes, tex_w, tex_h, texture):
    boxes = []
    for cube in cubes:
        inf = cube.get("inflate", 0)
        lo = np.array(cube["origin"], float) - inf
        hi = lo + np.array(cube["size"], float) + 2 * inf
        boxes.append((lo, hi, cube))
    lo_all = np.min([b[0] for b in boxes], axis=0)
    hi_all = np.max([b[1] for b in boxes], axis=0)
    offset = 8 - (lo_all + hi_all) / 2
    elements = []
    for lo, hi, cube in boxes:
        faces = {}
        for face, data in cube["uv"].items():
            u, v = data["uv"]
            w, h = data["uv_size"]
            faces[face] = {"uv": [round(u * 16 / tex_w, 4), round(v * 16 / tex_h, 4),
                                  round((u + w) * 16 / tex_w, 4), round((v + h) * 16 / tex_h, 4)], "texture": "#0"}
        element = {"from": [round(c, 4) for c in lo + offset], "to": [round(c, 4) for c in hi + offset], "faces": faces}
        if "rotation" in cube:
            rotation = snap_rotation(cube["rotation"], cube["pivot"], offset)
            if rotation:
                element["rotation"] = rotation
        elements.append(element)
    return {
        "textures": {"0": texture, "particle": texture},
        "gui_light": "side",
        "elements": elements,
        "display": armor_display(hi_all - lo_all),
    }


def armor_display(extent):
    yaw, pitch = math.radians(210), math.radians(20)
    width = abs(extent[0] * math.cos(yaw)) + abs(extent[2] * math.sin(yaw))
    depth = abs(extent[0] * math.sin(yaw)) + abs(extent[2] * math.cos(yaw))
    height = extent[1] * math.cos(pitch) + depth * math.sin(pitch)
    scale = round(min(1.6, 14.0 / max(width, height)), 3)
    return {
        "gui": {"rotation": [20, 210, 0], "translation": [0, 0, 0], "scale": [scale] * 3},
        "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [scale * 0.4] * 3},
        "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [scale * 0.9] * 3},
        "thirdperson_righthand": {"rotation": [75, 225, 0], "translation": [0, 2.5, 0], "scale": [scale * 0.45] * 3},
        "firstperson_righthand": {"rotation": [0, 225, 0], "translation": [0, 0, 0], "scale": [scale * 0.5] * 3},
        "head": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [scale] * 3},
    }


def write_model(name, model):
    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    (MODEL_DIR / f"{name}.json").write_text(json.dumps(model, separators=(",", ":")))


def armor_models():
    count = 0
    textures = []
    for form, sets in FORMS.items():
        geo = json.loads((GEO_DIR / f"{form}.geo.json").read_text())["minecraft:geometry"][0]
        desc = geo["description"]
        bones = {b["name"]: b for b in geo["bones"]}
        for set_id in sets:
            textures.append(f"{NS}:armor/{set_id}")
            texture = f"{NS}:armor/{set_id}"
            for level, tier in enumerate(TIERS):
                if form == "ghillie" and level > 0:
                    continue
                for piece, slot_bones in PIECES.items():
                    names = list(slot_bones)
                    for parent in slot_bones:
                        short = parent.replace("armor", "")
                        short = "".join("_" + c.lower() if c.isupper() else c for c in short).lstrip("_")
                        names += [f"{t}_{short}" for t in TIERS[1:level + 1]]
                    cubes = [c for n in names if n in bones for c in bones[n]["cubes"]]
                    if not cubes:
                        continue
                    write_model(f"{set_id}_{tier}_{piece}", convert(cubes, desc["texture_width"], desc["texture_height"], texture))
                    count += 1
        if form == "us":
            for device, device_bones in DEVICES.items():
                cubes = [c for n in device_bones for c in bones[n]["cubes"]]
                write_model(device, convert(cubes, desc["texture_width"], desc["texture_height"], f"{NS}:armor/us_woodland"))
                count += 1
    ATLAS.parent.mkdir(parents=True, exist_ok=True)
    ATLAS.write_text(json.dumps({"sources": [{"type": "single", "resource": t} for t in textures]}, indent=2) + "\n")
    return count


def main():
    size = gear_texture()
    gear_models(size)
    print(f"wrote {armor_models() + 6} item models")


if __name__ == "__main__":
    sys.exit(main())
