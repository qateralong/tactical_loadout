#!/usr/bin/env python3
import json
import math
import sys
from pathlib import Path

import numpy as np
from PIL import Image

from textures import hex_rgb, pattern

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/tacz_camo_armor"
GEO_DIR = ASSETS / "geo/armor"
TEX_DIR = ASSETS / "textures/armor"
ANIM_DIR = ASSETS / "animations"

TEXELS = 2
ATLAS_W = 256

FORMS = {
    "ct": ["ct"],
    "t": ["t"],
    "us": ["us_woodland", "us_desert", "us_green", "us_multicam", "us_snow"],
    "ru": ["ru_emr"],
    "ghillie": ["ghillie"],
    "swat": ["swat"],
}

PIVOTS = {
    "armorHead": [0, 24, 0],
    "armorBody": [0, 24, 0],
    "armorRightArm": [-5, 22, 0],
    "armorLeftArm": [5, 22, 0],
    "armorRightLeg": [-1.9, 12, 0],
    "armorLeftLeg": [1.9, 12, 0],
    "armorRightBoot": [-1.9, 12, 0],
    "armorLeftBoot": [1.9, 12, 0],
}
MIRROR = {"armorRightArm": "armorLeftArm", "armorRightLeg": "armorLeftLeg", "armorRightBoot": "armorLeftBoot"}
SHORT = {
    "armorHead": "head", "armorBody": "body", "armorRightArm": "right_arm", "armorLeftArm": "left_arm",
    "armorRightLeg": "right_leg", "armorLeftLeg": "left_leg", "armorRightBoot": "right_boot", "armorLeftBoot": "left_boot",
}
TIERS = ["light", "medium", "heavy"]


class Cube:
    def __init__(self, lo, hi, mat, inflate=0.0, rotation=None, pivot=None):
        self.lo = [float(v) for v in lo]
        self.hi = [float(v) for v in hi]
        self.mat = mat
        self.inflate = inflate
        self.rotation = rotation
        self.pivot = pivot

    def mirrored(self):
        lo = [-self.hi[0], self.lo[1], self.lo[2]]
        hi = [-self.lo[0], self.hi[1], self.hi[2]]
        rot = None if self.rotation is None else [self.rotation[0], -self.rotation[1], -self.rotation[2]]
        piv = None if self.pivot is None else [-self.pivot[0], self.pivot[1], self.pivot[2]]
        return Cube(lo, hi, self.mat, self.inflate, rot, piv)


class Model:
    def __init__(self):
        self.parts = {(bone, tier): [] for bone in PIVOTS for tier in TIERS}

    def add(self, bone, tier, lo, hi, mat, inflate=0.0, rotation=None, pivot=None, mirror=False, sym=False):
        cube = Cube(lo, hi, mat, inflate, rotation, pivot)
        self.parts[(bone, tier)].append(cube)
        if mirror:
            self.parts[(MIRROR[bone], tier)].append(cube.mirrored())
        if sym:
            self.parts[(bone, tier)].append(cube.mirrored())


def helmet_us(m):
    h = "armorHead"
    m.add(h, "light", (-4.75, 32, -4.75), (4.75, 33.5, 4.75), "shell")
    m.add(h, "light", (-5, 28.5, -5), (5, 32, 5), "shell")
    m.add(h, "light", (-5, 26, -1), (-4.5, 28.5, 5), "shell", sym=True)
    m.add(h, "light", (-4.5, 26.5, 4.5), (4.5, 28.5, 5), "shell")
    m.add(h, "light", (-4.75, 24.5, -0.5), (-4.5, 26, 0.5), "strap", sym=True)
    m.add(h, "light", (-1, 30.5, -5.5), (1, 32, -5), "metal")
    m.add(h, "medium", (-5.5, 29, -3.5), (-5, 30, 3), "polymer", sym=True)
    m.add(h, "medium", (-5.75, 25.5, -1.5), (-5, 28.5, 1.5), "polymer", sym=True)
    m.add(h, "medium", (-5.25, 28.5, -0.5), (-5, 29, 0.5), "polymer", sym=True)
    m.add(h, "heavy", (-4.75, 23.75, -5.25), (4.75, 26.25, -4.5), "shell")
    m.add(h, "heavy", (-5.25, 23.75, -4.75), (-4.5, 26.25, -1.5), "shell", sym=True)
    m.add(h, "heavy", (-2, 29, 5), (2, 31, 5.75), "pouch")


def helmet_ct(m):
    h = "armorHead"
    m.add(h, "light", (-4.75, 32, -4.75), (4.75, 33.25, 4.75), "shell")
    m.add(h, "light", (-5, 29, -5), (5, 32, 5), "shell")
    m.add(h, "light", (-5, 27, 1), (-4.5, 29, 5), "shell", sym=True)
    m.add(h, "light", (-4.5, 27, 4.5), (4.5, 29, 5), "shell")
    m.add(h, "light", (-5.25, 30, -5.25), (5.25, 30.75, 5.25), "strap")
    m.add(h, "light", (-3.5, 30.75, -5.75), (3.5, 32.25, -5), "polymer")
    m.add(h, "light", (-3.25, 31, -5.85), (-0.25, 32, -5.75), "lens", sym=True)
    m.add(h, "medium", (-5.5, 29.25, -3.5), (-5, 30, 3), "polymer", sym=True)
    m.add(h, "medium", (-5.75, 25.5, -1.5), (-5, 28.5, 1.5), "polymer", sym=True)
    m.add(h, "medium", (-5.25, 28.5, -0.5), (-5, 29.25, 0.5), "polymer", sym=True)
    m.add(h, "heavy", (-4.75, 23.75, -5.25), (4.75, 26.25, -4.5), "shell")
    m.add(h, "heavy", (-5.25, 23.75, -4.75), (-4.5, 26.25, -1.5), "shell", sym=True)
    m.add(h, "heavy", (-4.5, 25, 4.5), (4.5, 27, 5.25), "shell")


def balaclava_t(m):
    h = "armorHead"
    m.add(h, "light", (-4, 24, -4), (4, 32, 4), "knit", inflate=0.65)
    m.add(h, "medium", (-4.9, 30.5, -4.9), (4.9, 31.25, 4.9), "strap")
    m.add(h, "medium", (-3.5, 30.25, -5.5), (3.5, 31.75, -4.75), "polymer")
    m.add(h, "medium", (-3.25, 30.5, -5.6), (-0.25, 31.5, -5.5), "lens", sym=True)
    m.add(h, "heavy", (-4.95, 30.75, -4.95), (4.95, 33.25, 4.95), "shell")


def carrier_common(m, front_panel=True):
    b = "armorBody"
    if front_panel:
        m.add(b, "light", (-4.25, 14, -3.25), (4.25, 23, -2.25), "fabric")
    m.add(b, "light", (-4.25, 14, 2.25), (4.25, 23, 3.25), "fabric")
    m.add(b, "light", (-3.5, 23, -3), (-1.5, 24.5, 3), "webbing", sym=True)
    m.add(b, "light", (-4.5, 14, -2.5), (4.5, 18, 2.5), "webbing")
    m.add(b, "medium", (-5.25, 14.5, -1.75), (-4.5, 19.5, 1.75), "plate", sym=True)
    m.add(b, "heavy", (-4.75, 22.5, -3.75), (4.75, 24.75, 3.75), "plate")
    m.add(b, "heavy", (-2.5, 9.5, -3), (2.5, 13.5, -2.25), "plate")
    m.add("armorRightArm", "heavy", (-8.75, 19, -2.75), (-3.75, 24.5, 2.75), "plate", mirror=True)
    m.add("armorRightArm", "heavy", (-8.85, 21, -2.85), (-3.65, 21.5, 2.85), "strap", mirror=True)


def carrier_us(m):
    b = "armorBody"
    carrier_common(m)
    for i in range(3):
        x0 = -3.75 + i * 2.6
        m.add(b, "light", (x0, 15, -4.5), (x0 + 2.3, 18.5, -3.25), "pouch")
    m.add(b, "light", (-2, 19, -3.75), (2, 21.5, -3.25), "pouch")
    m.add(b, "light", (-2.5, 15, 3.25), (2.5, 21, 4.25), "pouch")


def carrier_ct(m):
    b = "armorBody"
    carrier_common(m)
    m.add(b, "light", (-3.75, 15, -4.25), (-0.25, 18, -3.25), "pouch", sym=True)
    m.add(b, "light", (1, 19.5, -3.9), (3.5, 22.5, -3.25), "pouch")
    m.add(b, "light", (-3.5, 19.75, -3.5), (-0.5, 21.75, -3.25), "patch")
    m.add(b, "light", (-2.25, 16, 3.25), (2.25, 20, 3.75), "patch")


def rig_t(m):
    b = "armorBody"
    carrier_common(m, front_panel=False)
    m.add(b, "light", (-4.25, 14, -3), (4.25, 19, -2.25), "webbing")
    for i in range(4):
        x0 = -4 + i * 2.05
        m.add(b, "light", (x0, 14.5, -4.25), (x0 + 1.85, 19.5, -3), "pouch")
    m.add(b, "light", (-3.5, 19, -2.75), (-2.25, 24, -2.25), "webbing", sym=True)
    for angle in (35, -35):
        m.add(b, "light", (-0.5, 13.5, 3.25), (0.5, 23.5, 3.75), "webbing", rotation=[0, 0, angle], pivot=[0, 18.5, 3.5])
    m.add(b, "heavy", (-4.25, 19, -3), (4.25, 23, -2.25), "plate")


def leggings(m):
    r = "armorRightLeg"
    m.add(r, "light", (-3.9, 1.5, -2), (0.1, 12, 2), "fabric", inflate=0.4, mirror=True)
    m.add(r, "light", (-3.65, 4.5, -2.9), (-0.15, 7.5, -2.25), "kneepad", mirror=True)
    m.add(r, "light", (-4.4, 5.5, -2.5), (0.6, 6.25, 2.5), "strap", mirror=True)
    m.add(r, "light", (-4.5, 10.75, -2.5), (0.6, 12, 2.5), "strap", mirror=True)
    m.add(r, "light", (-4.6, 7.5, -1.25), (-4.3, 10, 1.25), "pouch", mirror=True)
    m.add(r, "medium", (-4.75, 7, -1.75), (-4.3, 10.5, 1.75), "plate", mirror=True)
    m.add(r, "heavy", (-3.6, 7.75, -2.85), (-0.2, 10.5, -2.4), "plate", mirror=True)


def boots(m):
    r = "armorRightBoot"
    m.add(r, "light", (-4.4, 0, -2.5), (0.6, 5, 2.5), "boot", mirror=True)
    m.add(r, "light", (-4.3, 0, -3.25), (0.5, 2, -2.5), "boot", mirror=True)
    m.add(r, "light", (-4.5, 0, -3.4), (0.7, 0.6, 2.6), "sole", mirror=True)
    m.add(r, "medium", (-4.6, 1, -1.25), (-4.4, 3, 1.25), "kneepad", mirror=True)
    m.add(r, "medium", (0.6, 1, -1.25), (0.8, 3, 1.25), "kneepad", mirror=True)
    m.add(r, "heavy", (-3.9, 1.5, -2.85), (0.1, 4.5, -2.5), "kneepad", mirror=True)


def helmet_ru(m):
    h = "armorHead"
    m.add(h, "light", (-4.75, 32, -4.75), (4.75, 33.75, 4.75), "shell")
    m.add(h, "light", (-5, 28.25, -5), (5, 32, 5), "shell")
    m.add(h, "light", (-5, 25.5, -1.5), (-4.5, 28.25, 5), "shell", sym=True)
    m.add(h, "light", (-4.5, 25.5, 4.5), (4.5, 28.25, 5), "shell")
    m.add(h, "light", (-5.15, 28.25, -5.15), (5.15, 28.75, 5.15), "strap")
    m.add(h, "light", (-4.75, 24.5, 0), (-4.5, 25.5, 1), "strap", sym=True)
    m.add(h, "light", (-3, 29.5, 5), (3, 30.75, 5.4), "strap")
    m.add(h, "medium", (-5.5, 29, -3.5), (-5, 30, 3), "polymer", sym=True)
    m.add(h, "medium", (-5.75, 25.5, -1.5), (-5, 28.5, 1.5), "polymer", sym=True)
    m.add(h, "heavy", (-4.75, 23.75, -5.25), (4.75, 26.25, -4.5), "shell")
    m.add(h, "heavy", (-5.25, 23.75, -4.75), (-4.5, 26.25, -1.5), "shell", sym=True)
    m.add(h, "heavy", (-5, 23.5, 4.5), (5, 25.5, 5.5), "plate")


def vest_ru(m):
    b = "armorBody"
    m.add(b, "light", (-4.5, 13, -3.25), (4.5, 23.5, -2.25), "fabric")
    m.add(b, "light", (-4.5, 13, 2.25), (4.5, 23.5, 3.25), "fabric")
    m.add(b, "light", (-3.5, 23.5, -3), (-1.5, 24.5, 3), "webbing", sym=True)
    m.add(b, "light", (-4.6, 13.5, -2.5), (4.6, 19, 2.5), "fabric")
    for i in range(4):
        x0 = -4 + i * 2.05
        m.add(b, "light", (x0, 13.75, -4.5), (x0 + 1.85, 17.75, -3.25), "pouch")
    m.add(b, "light", (-3.5, 18.25, -4), (-1.5, 20, -3.25), "pouch", sym=True)
    m.add(b, "light", (-2.5, 14, 3.25), (2.5, 21.5, 4.5), "pouch")
    m.add(b, "medium", (-5.25, 14, -1.75), (-4.6, 19.5, 1.75), "plate", sym=True)
    m.add(b, "heavy", (-4.75, 22.75, -3.75), (4.75, 25, 3.75), "plate")
    m.add(b, "heavy", (-3, 9, -3.25), (3, 13.25, -2.25), "plate")
    m.add("armorRightArm", "heavy", (-8.75, 18.5, -2.75), (-3.75, 24.5, 2.75), "plate", mirror=True)
    m.add("armorRightArm", "heavy", (-8.85, 20.5, -2.85), (-3.65, 21, 2.85), "strap", mirror=True)


def strands(m, bone, count, box, seed, length=(1.5, 3.0), mirror=False):
    rng = np.random.default_rng(seed)
    (x0, y0, z0), (x1, y1, z1) = box
    for _ in range(count):
        face = rng.integers(0, 3)
        x = rng.uniform(x0, x1)
        y = rng.uniform(y0, y1)
        z = rng.uniform(z0, z1)
        if face == 0:
            z = z0 if rng.random() < 0.5 else z1
        elif face == 1:
            x = x0
        else:
            y = y1
        ln = round(rng.uniform(*length) * 2) / 2
        rx, rz = (round(float(a)) for a in rng.uniform(-20, 20, 2))
        m.add(bone, "light", (round(x * 4) / 4 - 0.25, round(y * 4) / 4 - ln, round(z * 4) / 4 - 0.25),
              (round(x * 4) / 4 + 0.25, round(y * 4) / 4, round(z * 4) / 4 + 0.25), "strand",
              rotation=[rx, 0, rz], pivot=[round(x * 4) / 4, round(y * 4) / 4, round(z * 4) / 4], mirror=mirror)


def ghillie_suit(m):
    h, b, ra, rl = "armorHead", "armorBody", "armorRightArm", "armorRightLeg"
    m.add(h, "light", (-4, 24, -4), (4, 32, 4), "hood", inflate=0.7)
    strands(m, h, 26, ((-4.7, 26, -4.7), (4.7, 32.7, 4.7)), 1)
    m.add(b, "light", (-4, 12, -2), (4, 24, 2), "ghillie", inflate=0.55)
    strands(m, b, 22, ((-4.55, 13, -2.55), (4.55, 24.55, 2.55)), 2, length=(2.0, 4.0))
    m.add(ra, "light", (-8, 12, -2), (-4, 24, 2), "ghillie", inflate=0.55, mirror=True)
    strands(m, ra, 10, ((-8.55, 14, -2.55), (-3.9, 24.55, 2.55)), 3, mirror=True)
    m.add(rl, "light", (-3.9, 1.5, -2), (0.1, 12, 2), "ghillie", inflate=0.45, mirror=True)
    strands(m, rl, 10, ((-4.35, 3, -2.45), (0.2, 12, 2.45)), 4, mirror=True)


def helmet_swat(m):
    h = "armorHead"
    m.add(h, "light", (-4, 24, -4), (4, 32, 4), "knit", inflate=0.6)
    m.add(h, "light", (-4.75, 32, -4.75), (4.75, 33.5, 4.75), "shell")
    m.add(h, "light", (-5, 28.5, -5), (5, 32, 5), "shell")
    m.add(h, "light", (-5, 26, -1), (-4.5, 28.5, 5), "shell", sym=True)
    m.add(h, "light", (-4.5, 26.5, 4.5), (4.5, 28.5, 5), "shell")
    m.add(h, "medium", (-5.5, 29, -3.5), (-5, 30, 3), "polymer", sym=True)
    m.add(h, "medium", (-5.75, 25.5, -1.5), (-5, 28.5, 1.5), "polymer", sym=True)
    m.add(h, "heavy", (-4.75, 24.25, -5.75), (4.75, 31.25, -5.25), "visor")
    m.add(h, "heavy", (-5.5, 30, -5.6), (-5, 31.5, -4), "polymer", sym=True)


def vest_swat(m):
    b = "armorBody"
    carrier_common(m)
    m.add(b, "light", (-3.5, 15, -4.25), (-0.5, 18, -3.25), "pouch", sym=True)
    m.add(b, "light", (-3.5, 19, -3.6), (3.5, 21.5, -3.25), "police")
    m.add(b, "light", (-3.5, 17, 3.25), (3.5, 21, 3.75), "police")
    m.add(b, "light", (1.5, 21.5, -3.75), (3.5, 23.5, -3.25), "pouch")


BUILDERS = {
    "us": [helmet_us, carrier_us, leggings, boots],
    "ct": [helmet_ct, carrier_ct, leggings, boots],
    "t": [balaclava_t, rig_t, leggings, boots],
    "ru": [helmet_ru, vest_ru, leggings, boots],
    "ghillie": [ghillie_suit, boots],
    "swat": [helmet_swat, vest_swat, leggings, boots],
}


class Packer:
    def __init__(self, width):
        self.width = width
        self.x = 0
        self.y = 0
        self.row = 0

    def place(self, w, h):
        if self.x + w > self.width:
            self.x = 0
            self.y += self.row
            self.row = 0
        pos = (self.x, self.y)
        self.x += w
        self.row = max(self.row, h)
        return pos

    def height(self):
        return self.y + self.row


def face_sizes(cube):
    w, h, d = (cube.hi[i] - cube.lo[i] for i in range(3))
    texel = lambda v: max(1, int(math.ceil(v * TEXELS - 1e-6)))
    return {
        "north": (texel(w), texel(h)), "south": (texel(w), texel(h)),
        "east": (texel(d), texel(h)), "west": (texel(d), texel(h)),
        "up": (texel(w), texel(d)), "down": (texel(w), texel(d)),
    }


def build(form):
    model = Model()
    for builder in BUILDERS[form]:
        builder(model)

    packer = Packer(ATLAS_W)
    faces = []
    bones = []
    for bone, pivot in PIVOTS.items():
        bones.append({"name": bone, "pivot": pivot, "cubes": []})
        for tier in TIERS:
            cubes = model.parts[(bone, tier)]
            if not cubes:
                continue
            target = bones[-1] if tier == "light" else {"name": f"{tier}_{SHORT[bone]}", "parent": bone, "pivot": pivot, "cubes": []}
            if tier != "light":
                bones.append(target)
            for cube in sorted(cubes, key=lambda c: -max(face_sizes(c)["north"][1], face_sizes(c)["east"][1])):
                uv = {}
                for name, (w, h) in face_sizes(cube).items():
                    u, v = packer.place(w, h)
                    uv[name] = {"uv": [u, v], "uv_size": [w, h]}
                    faces.append((cube.mat, name, u, v, w, h))
                entry = {
                    "origin": [round(c, 4) for c in cube.lo],
                    "size": [round(cube.hi[i] - cube.lo[i], 4) for i in range(3)],
                    "uv": uv,
                }
                if cube.inflate:
                    entry["inflate"] = cube.inflate
                if cube.rotation:
                    entry["rotation"] = cube.rotation
                    entry["pivot"] = cube.pivot
                target["cubes"].append(entry)
    height = 1 << max(0, (packer.height() - 1).bit_length())
    geo = {
        "format_version": "1.12.0",
        "minecraft:geometry": [{
            "description": {
                "identifier": f"geometry.tacz_camo_armor.{form}",
                "texture_width": ATLAS_W,
                "texture_height": height,
                "visible_bounds_width": 3,
                "visible_bounds_height": 3.5,
                "visible_bounds_offset": [0, 1.25, 0],
            },
            "bones": bones,
        }],
    }
    return geo, faces, height


def palette(set_id):
    boot = {"ct": "232323", "t": "5b4632", "us_woodland": "3b3a33", "us_desert": "a08560", "us_green": "514e36",
            "us_multicam": "8b7355", "us_snow": "bfc1c2", "ru_emr": "2e2c28", "ghillie": "4a4232", "swat": "1d1d1f"}[set_id]
    shell = {"ct": "1f2a3d", "t": "4a4a40", "swat": "202124"}.get(set_id)
    knee = {"ct": "1e1f21", "t": "4a3a2a", "swat": "1b1b1d", "us_snow": "d9dcdc"}.get(set_id)
    knit = {"swat": "1a1a1c"}.get(set_id)
    return {"boot": hex_rgb(boot), "shell": None if shell is None else hex_rgb(shell),
            "knee": None if knee is None else hex_rgb(knee), "knit": None if knit is None else hex_rgb(knit)}


def paint(set_id, faces, height):
    half = pattern(set_id, ATLAS_W // 2, height // 2, 5, seed=11)
    fill = np.repeat(np.repeat(half, 2, axis=0), 2, axis=1)
    mean = fill.reshape(-1, 3).mean(axis=0)
    pal = palette(set_id)
    img = np.zeros((height, ATLAS_W, 4))
    rng = np.random.default_rng(5)
    sides = ("north", "south", "east", "west")

    for mat, face, u, v, w, h in faces:
        region = fill[v:v + h, u:u + w].copy()
        alpha = np.full((h, w), 255.0)
        ys = np.arange(h)[:, None]
        xs = np.arange(w)[None, :]
        border = (ys == 0) | (ys == h - 1) | (xs == 0) | (xs == w - 1)
        outline = True

        if mat == "fabric":
            rgb = region * (1.0 - 0.03 * ((xs + ys) % 2))[..., None]
            outline = False
        elif mat == "knit":
            knit = region if pal["knit"] is None else pal["knit"] * np.ones_like(region)
            rgb = knit * (1.0 - 0.08 * (xs % 2))[..., None]
            outline = False
            if face == "north":
                y0, y1 = int(h * 0.42), int(h * 0.56)
                x0, x1 = int(w * 0.14), int(w * 0.86)
                alpha[y0:y1, x0:x1] = 0
                rgb[max(0, y0 - 1):y0, x0 - 1:x1 + 1] *= 0.6
                rgb[y1:y1 + 1, x0 - 1:x1 + 1] *= 0.6
                rgb[y0:y1, x0 - 1:x0] *= 0.6
                rgb[y0:y1, x1:x1 + 1] *= 0.6
        elif mat in ("ghillie", "hood"):
            tufts = rng.random((h, w))
            rgb = region * (0.85 + 0.3 * tufts)[..., None]
            outline = False
            if mat == "hood" and face == "north":
                y0, y1 = int(h * 0.3), int(h * 0.82)
                x0, x1 = int(w * 0.16), int(w * 0.84)
                alpha[y0:y1, x0:x1] = 0
        elif mat == "strand":
            tone = rng.choice([0.7, 0.85, 1.0, 1.15, 1.3])
            rgb = np.ones_like(region) * region.reshape(-1, 3).mean(axis=0) * tone
            outline = False
        elif mat == "visor":
            rgb = np.ones_like(region) * hex_rgb("16262c")
            if face == "north":
                rgb[1:3, 1:max(2, w // 3)] = hex_rgb("6f97a3")
                rgb[ys[:, 0] > h - 3] = hex_rgb("2a2c2e")
        elif mat == "police":
            rgb = np.ones_like(region) * hex_rgb("161618")
            if face in ("north", "south") and h >= 3 and w >= 6:
                row = slice(h // 2 - 1, h // 2 + 1)
                letters = (xs[0] % 3 != 2) & (xs[0] > 0) & (xs[0] < w - 1)
                rgb[row, letters] = hex_rgb("e6e6e6")
        elif mat == "webbing":
            rgb = region * 0.8
            rgb[(ys % 3 == 2).repeat(w, axis=1)] *= 0.62
            slits = (ys % 3 != 2) & (xs % 4 == 3)
            rgb[slits] *= 0.8
        elif mat == "pouch":
            rgb = region * 0.96
            if face in sides and h >= 4:
                flap = max(2, h // 3)
                rgb[:flap] *= 1.06
                rgb[flap - 1] *= 0.62
                if face in ("north", "south") and w >= 4:
                    cx = w // 2
                    rgb[flap - 1:flap + 1, cx - 1:cx + 1] = mean * 0.6
                    rgb[flap, cx - 1:cx + 1] = hex_rgb("3a3b3c")
        elif mat == "plate":
            rgb = region * 0.9
            if h >= 5 and w >= 5:
                stitch = ((ys == 1) | (ys == h - 2) | (xs == 1) | (xs == w - 2)) & ~border & ((xs + ys) % 2 == 0)
                rgb[stitch] *= 1.18
        elif mat == "shell":
            rgb = pal["shell"] * np.ones_like(region) if pal["shell"] is not None else region * 0.95
            scuff = rng.random((h, w)) < 0.04
            rgb[scuff] *= 1.15
        elif mat == "strap":
            rgb = np.ones_like(region) * mean * 0.45
            rgb[(xs % 4 == 0).repeat(h, axis=0)] *= 1.15
            outline = False
        elif mat == "polymer":
            rgb = np.ones_like(region) * hex_rgb("2c2e30")
            rgb[0] *= 1.35
        elif mat == "metal":
            rgb = np.ones_like(region) * hex_rgb("6d7073")
            rgb[0] *= 1.3
        elif mat == "lens":
            top = hex_rgb("5f97a8")
            bottom = hex_rgb("1f3a44")
            t = ys / max(1, h - 1)
            rgb = (top * (1 - t)[..., None] + bottom * t[..., None]) * np.ones((h, w, 1))
            rgb[:1, :2] = hex_rgb("d7eef3")
        elif mat == "patch":
            rgb = np.ones_like(region) * hex_rgb("1a1d24")
            if face in ("north", "south") and h >= 3 and w >= 5:
                row = h // 2 - (1 if h >= 6 else 0)
                letters = (xs[0] % 4 != 3) & (xs[0] > 0) & (xs[0] < w - 1)
                rgb[row, letters] = hex_rgb("c9ccd1")
                if h >= 6:
                    rgb[row + 2, letters & (xs[0] % 2 == 0)] = hex_rgb("8f949c")
        elif mat == "kneepad":
            base = pal["knee"] if pal["knee"] is not None else mean * 0.7
            rgb = np.ones_like(region) * base
            if face == "north" and h >= 3 and w >= 3:
                cy, cx = (h - 1) / 2, (w - 1) / 2
                d = ((ys - cy) / max(1, h / 2)) ** 2 + ((xs - cx) / max(1, w / 2)) ** 2
                rgb = rgb * (1.25 - 0.35 * np.clip(d, 0, 1))[..., None]
        elif mat == "boot":
            rgb = np.ones_like(region) * pal["boot"]
            if face == "north" and h > 6 and w >= 6:
                cx = w // 2
                for y in range(1, h - 2, 2):
                    rgb[y, cx - 2] = pal["boot"] * 0.55
                    rgb[y, cx + 1] = pal["boot"] * 0.55
                    rgb[y + 1, cx - 1:cx + 1] = pal["boot"] * 0.55
            elif face in ("east", "west") and h > 6:
                rgb[h // 3] *= 0.8
                rgb[-2:] *= 0.85
            elif face == "north":
                rgb *= 0.85
        elif mat == "sole":
            rgb = np.ones_like(region) * hex_rgb("1c1c1c")
            if face == "down":
                rgb[(ys % 2 == 0).repeat(w, axis=1)] = hex_rgb("2b2b2b")
            outline = False
        else:
            raise ValueError(mat)

        if face in sides:
            rgb = rgb * (1.06 - 0.12 * ys / max(1, h - 1))[..., None]
        elif face == "up":
            rgb = rgb * 1.08
        else:
            rgb = rgb * 0.8
        rgb = rgb * (1.0 + (rng.random((h, w)) - 0.5) * 0.05)[..., None]
        if outline and w > 2 and h > 2:
            rgb[0, :] *= 0.88
            rgb[-1, :] *= 0.78
            rgb[:, 0] *= 0.86
            rgb[:, -1] *= 0.86
        img[v:v + h, u:u + w, :3] = rgb
        img[v:v + h, u:u + w, 3] = alpha
    return Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), "RGBA")


def main():
    GEO_DIR.mkdir(parents=True, exist_ok=True)
    TEX_DIR.mkdir(parents=True, exist_ok=True)
    ANIM_DIR.mkdir(parents=True, exist_ok=True)
    for form, sets in FORMS.items():
        geo, faces, height = build(form)
        (GEO_DIR / f"{form}.geo.json").write_text(json.dumps(geo, separators=(",", ":")))
        for set_id in sets:
            paint(set_id, faces, height).save(TEX_DIR / f"{set_id}.png")
        print(f"{form}: {sum(len(b['cubes']) for b in geo['minecraft:geometry'][0]['bones'])} cubes, atlas {ATLAS_W}x{height}")
    (ANIM_DIR / "armor.animation.json").write_text(json.dumps({"format_version": "1.8.0", "animations": {}}, indent=1))


if __name__ == "__main__":
    sys.exit(main())
