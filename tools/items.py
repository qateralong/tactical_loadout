#!/usr/bin/env python3
import json
import math
import sys
from pathlib import Path

import numpy as np
from PIL import Image

from textures import hex_rgb

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/tactical_loadout"
MODEL_DIR = ASSETS / "models/item"
GEO_DIR = ASSETS / "geo/armor"
TEX_DIR = ASSETS / "textures"
ATLAS = ROOT / "src/main/resources/assets/minecraft/atlases/blocks.json"
NS = "tactical_loadout"

FORMS = {
    "ct": ["ct"], "t": ["t"], "ru": ["ru_emr", "ru_flora"], "swat": ["swat"],
    "us": ["us_woodland", "us_desert", "us_green", "us_multicam", "us_snow", "us_ucp", "us_tiger"],
    "ghillie": ["ghillie", "ghillie_snow", "ghillie_desert"], "eod": ["eod"],
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
    "nvg_thermal_pano": ["nvg_thermal_pano"],
    "gas_mask": ["gas_mask"],
}
ANGLES = [-45.0, -22.5, 0.0, 22.5, 45.0]

SWATCH = 8
SWATCHES = [
    "frag_side", "frag_top", "fuze", "spoon", "ring", "he_side", "he_band", "flash_side",
    "flash_top", "smoke_side", "smoke_top", "battery_side", "battery_top", "battery_bottom", "filter_side", "filter_top",
    "metal_dark", "glass", "glass_top", "rag", "incendiary_side", "incendiary_top", "decoy_side", "decoy_top",
    "impact_side", "impact_top", "can_side", "can_top", "shield_face", "shield_edge", "shield_window",
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
    elif name == "glass":
        rgb = flat("3f5c3c")
        rgb[:, 1] = hex_rgb("7fa07a")
        rgb[:, 2] *= 1.15
        rgb[-2:] *= 0.8
        rgb[2:4] = hex_rgb("6e4a2a")
    elif name == "glass_top":
        rgb = flat("38533a")
        rgb[3:5, 3:5] = hex_rgb("6e4a2a")
    elif name == "rag":
        rgb = flat("d6ccb4")
        rgb[ys % 3 == 2] *= 0.86
        rgb[rng.random((SWATCH, SWATCH)) < 0.18] = hex_rgb("8a6a4a")
    elif name == "incendiary_side":
        rgb = flat("4a4d50")
        rgb[2:4] = hex_rgb("a83a2a")
        rgb[6, (xs[0] % 2 == 0)] = hex_rgb("b9bcbf")
        rgb[0] *= 1.12
    elif name == "incendiary_top":
        rgb = flat("45484b")
        rgb[3:5, 3:5] *= 0.75
    elif name == "decoy_side":
        rgb = flat("62675a")
        rgb[1:3] = hex_rgb("c9a227")
        holes = (ys >= 4) & (ys <= 6) & (xs % 2 == 1) & (ys % 2 == 0)
        rgb[holes] = hex_rgb("23251f")
    elif name == "decoy_top":
        rgb = flat("5a5f52")
        rgb[3:5, 3:5] = hex_rgb("23251f")
    elif name == "impact_side":
        rgb = flat("5c6236")
        rgb[1:3] = hex_rgb("9e2b25")
        rgb[SWATCH // 2 + 1] *= 0.82
        rgb[0] *= 1.12
    elif name == "impact_top":
        rgb = flat("565c32")
        rgb[3:5, 3:5] = hex_rgb("9e2b25")
    elif name == "can_side":
        rgb = flat("a7abae")
        rgb[ys % 2 == 0] *= 0.88
        rgb[3:6] = hex_rgb("b5482f")
        rgb[4, 2:6] = hex_rgb("d9c9a3")
    elif name == "can_top":
        rgb = flat("bcc0c3")
        rgb[1:7, 1:7] *= 0.85
        rgb[3:5, 3:5] *= 0.8
    elif name == "shield_face":
        rgb = flat("222426")
        rgb[(xs + ys) % 4 == 0] *= 1.12
    elif name == "shield_edge":
        rgb = flat("141516")
        rgb[ys % 2 == 0] *= 1.2
    elif name == "shield_window":
        rgb = flat("1c3038")
        rgb[1:3, 1:3] = hex_rgb("7fa6b3")
        rgb[-2:] *= 0.75
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
    g.cylinder(8, 8, 2, 1, 8, "glass", "glass_top")
    g.cylinder(8, 8, 1.4, 8, 9.5, "glass", "glass_top")
    g.cylinder(8, 8, 0.75, 9.5, 12.5, "glass", "glass_top")
    g.box((7.1, 11, 7.1), (8.9, 13, 8.9), "rag")
    g.box((7.4, 13, 7.4), (8.6, 14, 8.6), "rag")
    g.box((8.7, 8.5, 7.5), (9.3, 11.5, 8.5), "rag")
    models["molotov"] = (g, 1.3)

    g = Gear(size)
    g.cylinder(8, 8, 1.75, 2.5, 9.5, "incendiary_side", "incendiary_top")
    g.fuze(9.5, 1.75, 6.25)
    models["incendiary_grenade"] = (g, 1.4)

    g = Gear(size)
    g.cylinder(8, 8, 1.5, 3, 9, "decoy_side", "decoy_top")
    g.fuze(9, 1.5, 6.5)
    models["decoy_grenade"] = (g, 1.45)

    g = Gear(size)
    g.cylinder(8, 8, 2.25, 4, 8.5, "impact_side", "impact_top")
    g.cylinder(8, 8, 1.6, 3, 4, "impact_side", "impact_top")
    g.cylinder(8, 8, 1.6, 8.5, 9.5, "impact_side", "impact_top")
    g.fuze(9.5, 1.6, 6.4)
    models["impact_grenade"] = (g, 1.4)

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
                if form == "ghillie" and level > 0 or form == "eod" and tier != "heavy":
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


# Two tin cans tied to a wire across the block; the wire itself uses the vanilla tripwire texture.
def tripwire_alarm_model(size):
    g = Gear(size)
    for cx in (2, 14):
        g.cylinder(cx, 8, 1.5, 0, 4, "can_side", "can_top")
    wire = {"up": {"uv": [0, 0, 16, 2], "texture": "#wire"}, "down": {"uv": [0, 2, 16, 0], "texture": "#wire"}}
    side = {"north": {"uv": [0, 0, 16, 2], "texture": "#wire"}, "south": {"uv": [0, 0, 16, 2], "texture": "#wire"}}
    model = {
        "render_type": "minecraft:cutout",
        "ambientocclusion": False,
        "textures": {"0": f"{NS}:item/gear_parts", "particle": f"{NS}:item/gear_parts", "wire": "minecraft:block/tripwire"},
        "elements": g.elements + [
            {"from": [3.5, 1.5, 8], "to": [12.5, 1.5, 8], "shade": False, "faces": wire},
            {"from": [3.5, 1, 8], "to": [12.5, 2, 8], "shade": False, "faces": side},
        ],
    }
    path = ASSETS / "models/block/tripwire_alarm.json"
    path.write_text(json.dumps(model, indent=2) + "\n")


# A full-height SWAT ballistic shield with an open viewport at eye level. Raised in first person, the viewport sits in the
# middle of the screen; in third person the shield stands upright in front of the body. It has its own texture for the lettering.
SHIELD_SLIT = (-4.5, 4.5, 10, 15)
SHIELD_X = (-9, 9)
SHIELD_Y = (-15, 21)
SHIELD_TEXELS = 2
SHIELD_TEX = 128
LETTERS = {
    "S": ["01111", "10000", "10000", "01110", "00001", "00001", "11110"],
    "W": ["10001", "10001", "10001", "10101", "10101", "11011", "10001"],
    "A": ["01110", "10001", "10001", "11111", "10001", "10001", "10001"],
    "T": ["11111", "00100", "00100", "00100", "00100", "00100", "00100"],
}


def shield_texture():
    width = (SHIELD_X[1] - SHIELD_X[0]) * SHIELD_TEXELS
    height = (SHIELD_Y[1] - SHIELD_Y[0]) * SHIELD_TEXELS
    rng = np.random.default_rng(21)
    img = np.zeros((SHIELD_TEX, SHIELD_TEX, 4))
    face = np.ones((height, width, 3)) * hex_rgb("1c1d1f") * (1.0 + (rng.random((height, width)) - 0.5) * 0.08)[..., None]
    back = face * 0.85
    text_top = (SHIELD_Y[1] - 4) * SHIELD_TEXELS
    x = (width - (len("SWAT") * 6 - 1)) // 2
    for letter in "SWAT":
        for row, bits in enumerate(LETTERS[letter]):
            for col, bit in enumerate(bits):
                if bit == "1":
                    face[text_top + row * 2:text_top + row * 2 + 2, x + col] = hex_rgb("e8e8e8")
        x += 6
    stripe = (SHIELD_Y[1] - 8) * SHIELD_TEXELS
    face[stripe:stripe + 1, 4:width - 4] = hex_rgb("c9c9c9")
    img[:height, :width, :3] = face
    img[:height, width:width * 2, :3] = back
    img[:height, :width * 2, 3] = 255
    img[height:height + 8, :8, :3] = hex_rgb("111213")
    img[height:height + 8, :8, 3] = 255
    Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), "RGBA").save(TEX_DIR / "item/riot_shield.png")
    return width, height


def shield_plate(lo, hi, size):
    width, height = size
    scale = 16 / SHIELD_TEX * SHIELD_TEXELS
    (x0, y0, z0), (x1, y1, z1) = lo, hi
    u0, u1 = (x0 - SHIELD_X[0]) * scale, (x1 - SHIELD_X[0]) * scale
    v0, v1 = (SHIELD_Y[1] - y1) * scale, (SHIELD_Y[1] - y0) * scale
    back = width / SHIELD_TEXELS * scale
    edge = [0, height / SHIELD_TEX * 16, 1, height / SHIELD_TEX * 16 + 1]
    faces = {
        "south": {"uv": [round(u0, 4), round(v0, 4), round(u1, 4), round(v1, 4)], "texture": "#1"},
        "north": {"uv": [round(back + back - u1, 4), round(v0, 4), round(back + back - u0, 4), round(v1, 4)], "texture": "#1"},
    }
    for face in ("east", "west", "up", "down"):
        faces[face] = {"uv": [round(v, 4) for v in edge], "texture": "#1"}
    return {"from": list(lo), "to": list(hi), "faces": faces}


def riot_shield_models(size):
    plate_size = shield_texture()
    g = Gear(size)
    x0, x1, y0, y1 = SHIELD_SLIT
    z0, z1 = 1, 2.5
    left, right = SHIELD_X
    bottom, top = SHIELD_Y
    plates = [
        ((left, bottom, z0), (right, y0, z1)),
        ((left, y1, z0), (right, top - 1, z1)),
        ((left + 1, top - 1, z0), (right - 1, top, z1)),
        ((left, y0, z0), (x0, y1, z1)),
        ((x1, y0, z0), (right, y1, z1)),
    ]
    elements = [shield_plate(lo, hi, plate_size) for lo, hi in plates]
    g.box((x0 - 0.6, y0 - 0.6, 0.6), (x1 + 0.6, y0, 2.9), "shield_edge")
    g.box((x0 - 0.6, y1, 0.6), (x1 + 0.6, y1 + 0.6, 2.9), "shield_edge")
    g.box((x0 - 0.6, y0, 0.6), (x0, y1, 2.9), "shield_edge")
    g.box((x1, y0, 0.6), (x1 + 0.6, y1, 2.9), "shield_edge")
    g.box((-1, -3, -4), (1, 3, 1), "metal_dark")
    g.box((-5, -1, -1), (5, 1, 1), "metal_dark")
    g.box((-6, 3, -1.5), (6, 4.5, 1), "metal_dark")
    elements += g.elements
    textures = {"0": f"{NS}:item/gear_parts", "1": f"{NS}:item/riot_shield", "particle": f"{NS}:item/riot_shield"}
    # Carried: upright at the side in third person, lowered at the bottom corner in first person (the left hand mirrors these).
    held = {
        "thirdperson_righthand": {"rotation": [-180, 90, -90], "translation": [7.5, 4.4, 7], "scale": [0.8, 0.8, 0.8]},
        "thirdperson_lefthand": {"rotation": [0, 90, 90], "translation": [7.5, -8.4, 7], "scale": [0.8, 0.8, 0.8]},
        "firstperson_righthand": {"rotation": [0, 180, 0], "translation": [-4.96, 1.72, -6.63], "scale": [0.6, 0.6, 0.6]},
        "firstperson_lefthand": {"rotation": [0, 180, 0], "translation": [4.64, 1.72, -6.63], "scale": [0.6, 0.6, 0.6]},
        "gui": {"rotation": [0, -20, 0], "translation": [2.15, 2.0, 0], "scale": [0.4, 0.4, 0.4]},
        "fixed": {"rotation": [0, 180, 0], "translation": [-3.04, 1.9, -1], "scale": [0.38, 0.38, 0.38]},
        "ground": {"rotation": [0, 0, 0], "translation": [1.6, 3.0, 0], "scale": [0.2, 0.2, 0.2]},
    }
    # Raised in first person: the viewport sits right in front of the eye. The item origin is 0.56 to the side and 0.52 below the eye,
    # the model's corner offset lands half a block to the right after the half turn, and the left hand mirrors the x translation. In third person the values put the shield upright in front of the body, solved for the blocking arm pose.
    slit_y = ((y0 + y1) / 2) / 16 - 0.5
    lift = round((0.52 - slit_y) * 16, 2)
    blocking = dict(held)
    blocking.update({
        "thirdperson_righthand": {"rotation": [-144, 30, -180], "translation": [-5.63, 9.37, -3.95], "scale": [1, 1, 1]},
        "thirdperson_lefthand": {"rotation": [-144, 30, 180], "translation": [8.22, 4.67, 2.52], "scale": [1, 1, 1]},
        "firstperson_righthand": {"rotation": [0, 180, 0], "translation": [-16.96, lift, -3.5], "scale": [1, 1, 1]},
        "firstperson_lefthand": {"rotation": [0, 180, 0], "translation": [-0.96, lift, -3.5], "scale": [1, 1, 1]},
    })
    write_model("riot_shield_blocking", {"textures": textures, "gui_light": "front", "elements": elements, "display": blocking})
    write_model("riot_shield", {
        "textures": textures, "gui_light": "front", "elements": elements, "display": held,
        "overrides": [{"predicate": {"minecraft:blocking": 1}, "model": f"{NS}:item/riot_shield_blocking"}],
    })


def main():
    size = gear_texture()
    gear_models(size)
    tripwire_alarm_model(size)
    riot_shield_models(size)
    print(f"wrote {armor_models() + 9} item models")


if __name__ == "__main__":
    sys.exit(main())
