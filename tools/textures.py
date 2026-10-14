#!/usr/bin/env python3
import sys
from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ITEM_DIR = ROOT / "src/main/resources/assets/tacz_camo_armor/textures/item"

SETS = ["ct", "t", "us_woodland", "us_desert", "us_green"]
TIERS = ["light", "medium", "heavy"]
PIECES = ["helmet", "plate_carrier", "leggings", "boots"]
FABRIC_IDS = {"ct": "blue", "t": "beige", "us_woodland": "woodland", "us_desert": "sand", "us_green": "olive"}


def hex_rgb(value):
    value = value.lstrip("#")
    return np.array([int(value[i:i + 2], 16) for i in (0, 2, 4)], dtype=np.float64)


def value_noise(w, h, cell, rng):
    gw, gh = w // cell + 2, h // cell + 2
    grid = rng.random((gh, gw))
    ys = np.arange(h) / cell
    xs = np.arange(w) / cell
    y0 = ys.astype(int)
    x0 = xs.astype(int)
    fy = ys - y0
    fx = xs - x0
    fy = fy * fy * (3 - 2 * fy)
    fx = fx * fx * (3 - 2 * fx)
    a = grid[y0][:, x0]
    b = grid[y0][:, x0 + 1]
    c = grid[y0 + 1][:, x0]
    d = grid[y0 + 1][:, x0 + 1]
    top = a + (b - a) * fx[None, :]
    bottom = c + (d - c) * fx[None, :]
    return top + (bottom - top) * fy[:, None]


def fbm(w, h, cell, rng, octaves=3):
    total = np.zeros((h, w))
    amp = 1.0
    norm = 0.0
    for _ in range(octaves):
        total += value_noise(w, h, max(1, cell), rng) * amp
        norm += amp
        amp *= 0.5
        cell = max(1, cell // 2)
    return total / norm


def layered(w, h, scale, rng, palette, thresholds):
    out = np.empty((h, w, 3))
    out[:] = palette[0]
    for color, threshold in zip(palette[1:], thresholds):
        field = fbm(w, h, scale, rng)
        lo, hi = np.quantile(field, threshold), 1.0
        out[(field >= lo) & (field <= hi)] = color
    return out


def speckle(w, h, rng, base, dark, amount):
    out = np.empty((h, w, 3))
    out[:] = base
    noise = fbm(w, h, 2, rng, octaves=2)
    out[noise > np.quantile(noise, 1 - amount)] = dark
    return out


def pattern(set_id, w, h, scale, seed=1):
    rng = np.random.default_rng(seed)
    if set_id == "us_woodland":
        palette = [hex_rgb("6b7a45"), hex_rgb("6b5038"), hex_rgb("2f3b26"), hex_rgb("1c1c1a")]
        return layered(w, h, scale, rng, palette, [0.62, 0.68, 0.84])
    if set_id == "us_desert":
        palette = [hex_rgb("d6c7a1"), hex_rgb("b39a6e"), hex_rgb("7a6146")]
        return layered(w, h, scale, rng, palette, [0.55, 0.85])
    if set_id == "us_green":
        return speckle(w, h, rng, hex_rgb("57592f"), hex_rgb("4b4d28"), 0.25)
    if set_id == "ct":
        return speckle(w, h, rng, hex_rgb("2f3d58"), hex_rgb("26324a"), 0.3)
    if set_id == "t":
        return speckle(w, h, rng, hex_rgb("b8a27a"), hex_rgb("a99269"), 0.3)
    raise ValueError(set_id)


def solid(color, w=16, h=16):
    out = np.empty((h, w, 3))
    out[:] = hex_rgb(color)
    return out


FABRIC_MASK = [
    "................",
    "................",
    "...##########...",
    "..#oooooooooo#..",
    "..#oooooooooo#..",
    "..#oooooooooo#..",
    "..############..",
    "..#oooooooooo#..",
    "..#oooooooooo#..",
    "..#oooooooooo#..",
    "..############..",
    "..#oooooooooo#..",
    "..#oooooooooo#..",
    "..#oooooooooo#..",
    "...##########...",
    "................",
]
PLATE_MASK = [
    "................",
    "................",
    "....########....",
    "...#oooooooo#...",
    "..#oooooooooo#..",
    "..#oooooooooo#..",
    "..#oooooooooo#..",
    "..#oooooooooo#..",
    "..#oooooooooo#..",
    "..#oooooooooo#..",
    "..#oooooooooo#..",
    "..#oooooooooo#..",
    "..#oooooooooo#..",
    "...#oooooooo#...",
    "....########....",
    "................",
]


class Sprite:
    def __init__(self):
        self.grid = [["."] * 16 for _ in range(16)]

    def rect(self, x0, y0, x1, y1, ch):
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.grid[y][x] = ch
        return self

    def outlined(self):
        out = [row[:] for row in self.grid]
        for y in range(16):
            for x in range(16):
                if self.grid[y][x] != ".":
                    continue
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < 16 and 0 <= ny < 16 and self.grid[ny][nx] != ".":
                        out[y][x] = "#"
                        break
        return out


def helmet(set_id, level):
    s = Sprite()
    s.rect(5, 1, 10, 1, "o").rect(3, 2, 12, 3, "o").rect(2, 4, 13, 8, "o")
    s.rect(2, 9, 3, 11, "o").rect(12, 9, 13, 11, "o")
    if set_id == "ct":
        s.rect(3, 5, 12, 6, "k").rect(4, 5, 6, 6, "l").rect(9, 5, 11, 6, "l")
    else:
        s.rect(7, 3, 8, 5, "m")
    if level >= 1:
        s.rect(1, 8, 2, 11, "k").rect(13, 8, 14, 11, "k").rect(2, 7, 13, 7, "k")
    if level >= 2:
        s.rect(3, 12, 12, 13, "M").rect(2, 11, 3, 12, "M").rect(12, 11, 13, 12, "M")
    return s


def balaclava(level):
    s = Sprite()
    s.rect(5, 1, 10, 1, "o").rect(4, 2, 11, 12, "o").rect(5, 13, 10, 13, "o").rect(3, 14, 12, 14, "o")
    s.rect(5, 7, 10, 8, "e").rect(6, 7, 6, 8, "E").rect(9, 7, 9, 8, "E")
    if level >= 1:
        s.rect(3, 4, 12, 4, "k").rect(4, 3, 7, 5, "k").rect(8, 3, 11, 5, "k").rect(5, 4, 6, 4, "l").rect(9, 4, 10, 4, "l")
    if level >= 2:
        s.rect(4, 0, 11, 0, "c").rect(3, 1, 12, 2, "c")
    return s


def carrier(set_id, level):
    s = Sprite()
    if set_id == "t":
        s.rect(4, 1, 5, 8, "w").rect(10, 1, 11, 8, "w").rect(2, 8, 13, 13, "w")
        for x0 in (3, 6, 9, 12):
            s.rect(x0, 9, min(x0 + 1, 13), 13, "p")
        if level >= 2:
            s.rect(4, 4, 11, 7, "P")
    else:
        s.rect(4, 1, 5, 4, "w").rect(10, 1, 11, 4, "w").rect(3, 5, 12, 13, "o")
        if set_id == "ct":
            s.rect(4, 8, 6, 11, "p").rect(9, 8, 11, 11, "p").rect(4, 6, 7, 6, "x").rect(10, 5, 11, 7, "p")
        else:
            for x0 in (4, 7, 10):
                s.rect(x0, 8, x0 + 1, 11, "p")
            s.rect(6, 6, 9, 6, "p")
        s.rect(3, 12, 12, 12, "w")
    if level >= 1:
        y0, y1 = (9, 12) if set_id == "t" else (7, 11)
        s.rect(1, y0, 2, y1, "P").rect(13, y0, 14, y1, "P")
    if level >= 2:
        s.rect(1, 2, 3, 5, "P").rect(12, 2, 14, 5, "P").rect(6, 3, 9, 4, "P").rect(6, 14, 9, 14, "P")
    return s


def leggings(level):
    s = Sprite()
    s.rect(3, 1, 12, 1, "w").rect(3, 2, 12, 4, "o").rect(3, 5, 6, 14, "o").rect(9, 5, 12, 14, "o")
    s.rect(3, 9, 6, 10, "K").rect(9, 9, 12, 10, "K")
    s.rect(2, 6, 2, 8, "p").rect(13, 6, 13, 8, "p")
    if level >= 1:
        s.rect(1, 5, 2, 8, "P").rect(13, 5, 14, 8, "P")
    if level >= 2:
        s.rect(4, 5, 5, 7, "P").rect(10, 5, 11, 7, "P")
    return s


def boots(level):
    s = Sprite()
    for x in (1, 8):
        s.rect(x + 1, 3, x + 3, 10, "b").rect(x + 1, 10, x + 5, 12, "b").rect(x + 1, 13, x + 5, 13, "s")
        s.grid[10][x + 5] = "."
        for y in (4, 6, 8):
            s.rect(x + 2, y, x + 3, y, "z")
        if level >= 1:
            s.rect(x, 9, x, 11, "P")
        if level >= 2:
            s.rect(x + 1, 1, x + 4, 7, "P")
    return s


GEAR_BLACK = hex_rgb("2c2e30")
SKIN = hex_rgb("c99a78")
BOOT = {"ct": "232323", "t": "5b4632", "us_woodland": "3b3a33", "us_desert": "a08560", "us_green": "514e36"}
KNEE = {"ct": "1e1f21", "t": "4a3a2a"}


def colorize(rows, fill, set_id=None):
    img = np.zeros((16, 16, 4))
    mean = fill.reshape(-1, 3).mean(axis=0)
    plate = mean * 0.5 + hex_rgb("6a6d70") * 0.5
    boot = hex_rgb(BOOT.get(set_id, "3b3a33"))
    knee = hex_rgb(KNEE[set_id]) if set_id in KNEE else mean * 0.6
    for y in range(16):
        shade = 1.12 - 0.24 * y / 15
        for x in range(16):
            c = rows[y][x]
            if c == ".":
                continue
            above = rows[y - 1][x] if y > 0 else "."
            if c == "#":
                rgb = mean * 0.3
            elif c == "o":
                rgb = fill[y, x] * shade
            elif c == "M":
                rgb = fill[y, x] * 0.82 * shade
            elif c == "p":
                rgb = fill[y, x] * (0.68 if above != "p" else 0.86) * shade
            elif c == "w":
                rgb = mean * 0.5 * shade
            elif c == "P":
                rgb = plate * shade
            elif c == "k":
                rgb = GEAR_BLACK * shade
            elif c == "K":
                rgb = knee * shade
            elif c == "l":
                rgb = hex_rgb("5f97a8") if above in ".#k" else hex_rgb("2e5562")
            elif c == "m":
                rgb = hex_rgb("7a7d80") * shade
            elif c == "b":
                rgb = boot * shade
            elif c == "z":
                rgb = boot * 0.55
            elif c == "s":
                rgb = hex_rgb("1c1c1c")
            elif c == "x":
                rgb = hex_rgb("c9ccd1")
            elif c == "e":
                rgb = SKIN
            elif c == "E":
                rgb = hex_rgb("2a2420")
            elif c == "c":
                rgb = hex_rgb("4a4a40") * shade
            else:
                raise ValueError(c)
            if c != "#" and above in ".#":
                rgb = rgb * 1.12
            img[y, x, :3] = rgb
            img[y, x, 3] = 255
    return Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), "RGBA")


def sprite(set_id, piece, level):
    if piece == "helmet":
        s = balaclava(level) if set_id == "t" else helmet(set_id, level)
    elif piece == "plate_carrier":
        s = carrier(set_id, level)
    elif piece == "leggings":
        s = leggings(level)
    else:
        s = boots(level)
    return s.outlined()


def save(img, name):
    ITEM_DIR.mkdir(parents=True, exist_ok=True)
    img.save(ITEM_DIR / f"{name}.png")


def main():
    for set_id in SETS:
        fill = pattern(set_id, 16, 16, 4, seed=7)
        save(colorize(FABRIC_MASK, fill), FABRIC_IDS[set_id] + "_fabric")
        icon_fill = pattern(set_id, 16, 16, 3, seed=3)
        for level, tier in enumerate(TIERS):
            for piece in PIECES:
                save(colorize(sprite(set_id, piece, level), icon_fill, set_id), f"{set_id}_{tier}_{piece}")

    kevlar = solid("c2a94a")
    for y in range(16):
        for x in range(16):
            if (x + y) % 2 == 0:
                kevlar[y, x] *= 0.86
    save(colorize(PLATE_MASK, kevlar), "kevlar_insert")
    save(colorize(PLATE_MASK, solid("9a8f86")), "raw_ceramic_plate")
    save(colorize(PLATE_MASK, solid("ddd8cc")), "ceramic_plate")
    print(f"wrote {len(list(ITEM_DIR.glob('*.png')))} textures to {ITEM_DIR.relative_to(ROOT)}")


if __name__ == "__main__":
    sys.exit(main())
