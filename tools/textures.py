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
        palette = [hex_rgb("d2c29d"), hex_rgb("a8916a"), hex_rgb("8a7756")]
        return layered(w, h, scale, rng, palette, [0.6, 0.82])
    if set_id == "us_green":
        return speckle(w, h, rng, hex_rgb("57592f"), hex_rgb("4b4d28"), 0.25)
    if set_id == "ct":
        return speckle(w, h, rng, hex_rgb("2f3d58"), hex_rgb("26324a"), 0.3)
    if set_id == "t":
        return speckle(w, h, rng, hex_rgb("b29b74"), hex_rgb("a08a64"), 0.3)
    raise ValueError(set_id)


def solid(color, w=16, h=16):
    out = np.empty((h, w, 3))
    out[:] = hex_rgb(color)
    return out


MASKS = {
    "helmet": [
        "................",
        "................",
        ".....######.....",
        "...##oooooo##...",
        "..#oooooooooo#..",
        "..#oooooooooo#..",
        ".#oooooooooooo#.",
        ".#oBBBBBBBBBBo#.",
        ".#oooooooooooo#.",
        "x#oooooooooooo#x",
        "xa############ax",
        "xax..........xax",
        "xxx..........xxx",
        "................",
        "................",
        "................",
    ],
    "balaclava": [
        "................",
        ".....######.....",
        "....#oooooo#....",
        "...#oooooooo#...",
        "...#oBBBBBBo#...",
        "...#oooooooo#...",
        "..x#o######o#x..",
        ".xa#o#eeee#o#ax.",
        ".xa#o######o#ax.",
        ".xa#oooooooo#ax.",
        "..x#oooooooo#x..",
        "....#oooooo#....",
        ".....#oooo#.....",
        "...##oooooo##...",
        "..#oooooooooo#..",
        "..############..",
    ],
    "plate_carrier": [
        "................",
        "yyy###yyyy###yyy",
        "ybb#o#bbbb#o#bby",
        "ybb#o#yyyy#o#bby",
        "yyy#o######o#yyy",
        "..#oooooooooo#..",
        "..#AoPPPPPPoA#..",
        "..#AoPPPPPPoA#..",
        "..#AoPPPPPPoA#..",
        "..#AooooooooA#..",
        "..#ommommommo#..",
        "..#ommommommo#..",
        "..#oooooooooo#..",
        "..############..",
        "................",
        "................",
    ],
    "leggings": [
        "................",
        "...##########...",
        "...#oBBBBBBo#...",
        "...#oooooooo#...",
        "...#AAo##oAA#...",
        "...#AAo##oAA#...",
        "...#ooo##ooo#...",
        "...#kkk##kkk#...",
        "...#kkk##kkk#...",
        "...#ooo##ooo#...",
        "...#ooo##ooo#...",
        "...#ooo##ooo#...",
        "...#ooo##ooo#...",
        "...#ooo##ooo#...",
        "...####..####...",
        "................",
    ],
    "boots": [
        "................",
        "................",
        "...#####........",
        "...#ooo#........",
        "...#ooo#........",
        "...#BBB#........",
        "...#BBB#........",
        "...#ooo#........",
        "...#ooo#####....",
        "...#oooooooo##..",
        "...#ooooooAAAA#.",
        "...#ooooooAAAA#.",
        "...#kkkkkkkkkkk#",
        "...#############",
        "................",
        "................",
    ],
    "fabric": [
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
    ],
    "plate": [
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
    ],
}

for name, rows in MASKS.items():
    assert len(rows) == 16 and all(len(r) == 16 for r in rows), name

GEAR_BLACK = hex_rgb("26282a")
SKIN = hex_rgb("c99a78")


def resolve(ch, tier_level):
    if ch in "aA" and tier_level < 1 or ch in "bB" and tier_level < 2:
        return "o" if ch.isupper() else "."
    if ch in "xy" and (ch == "x" and tier_level < 1 or ch == "y" and tier_level < 2):
        return "."
    return ch


def centered(rows):
    filled = [(y, x) for y, r in enumerate(rows) for x, c in enumerate(r) if c != "."]
    ys = [p[0] for p in filled]
    xs = [p[1] for p in filled]
    dy = (16 - (max(ys) - min(ys) + 1)) // 2 - min(ys)
    dx = (16 - (max(xs) - min(xs) + 1)) // 2 - min(xs)
    grid = [["."] * 16 for _ in range(16)]
    for y, x in filled:
        grid[y + dy][x + dx] = rows[y][x]
    return ["".join(r) for r in grid]


def render(mask_name, fill, tier_level=2):
    rows = centered(MASKS[mask_name])
    rows = [[resolve(c, tier_level) for c in r] for r in rows]
    img = np.zeros((16, 16, 4))
    base_mean = fill.reshape(-1, 3).mean(axis=0)
    outline = base_mean * 0.35
    for y in range(16):
        shade = 1.12 - 0.24 * y / 15
        for x in range(16):
            c = rows[y][x]
            if c == ".":
                continue
            if c in "#xy":
                rgb = outline
            elif c == "k":
                rgb = GEAR_BLACK * shade
            elif c == "e":
                rgb = SKIN
            elif c == "m":
                rgb = fill[y, x] * 0.78 * shade
            elif c == "P":
                rgb = fill[y, x] * 0.9 * shade
            elif c in "aAbB":
                rgb = (base_mean * 0.55 + hex_rgb("5a5d60") * 0.45) * shade
            else:
                rgb = fill[y, x] * shade
            img[y, x, :3] = rgb
            img[y, x, 3] = 255
    return Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), "RGBA")


def save(img, name):
    ITEM_DIR.mkdir(parents=True, exist_ok=True)
    img.save(ITEM_DIR / f"{name}.png")


def main():
    for set_id in SETS:
        fill = pattern(set_id, 16, 16, 4, seed=7)
        save(render("fabric", fill), FABRIC_IDS[set_id] + "_fabric")
        for level, tier in enumerate(TIERS):
            for piece in PIECES:
                mask = "balaclava" if piece == "helmet" and set_id == "t" else piece
                save(render(mask, fill, level), f"{set_id}_{tier}_{piece}")

    kevlar = solid("c2a94a")
    for y in range(16):
        for x in range(16):
            if (x + y) % 2 == 0:
                kevlar[y, x] *= 0.86
    save(render("plate", kevlar), "kevlar_insert")
    save(render("plate", solid("9a8f86")), "raw_ceramic_plate")
    save(render("plate", solid("ddd8cc")), "ceramic_plate")
    print(f"wrote {len(list(ITEM_DIR.glob('*.png')))} textures to {ITEM_DIR.relative_to(ROOT)}")


if __name__ == "__main__":
    sys.exit(main())
