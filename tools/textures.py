#!/usr/bin/env python3
import sys
from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
ITEM_DIR = ROOT / "src/main/resources/assets/tactical_loadout/textures/item"

SETS = ["ct", "t", "us_woodland", "us_desert", "us_green", "us_multicam", "us_snow", "us_ucp", "us_tiger", "ru_emr", "ru_flora",
        "ghillie", "ghillie_snow", "ghillie_desert", "swat"]
FABRIC_IDS = {"ct": "blue", "t": "beige", "us_woodland": "woodland", "us_desert": "sand", "us_green": "olive",
              "us_multicam": "multicam", "us_snow": "snow", "ru_emr": "emr", "ghillie": "ghillie",
              "ghillie_snow": "snow_ghillie", "swat": "black", "us_ucp": "ucp", "us_tiger": "tiger_stripe",
              "ru_flora": "flora", "ghillie_desert": "desert_ghillie"}


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


def stretched(w, h, cell, rng, sx, sy):
    small = fbm(max(2, w // sx), max(2, h // sy), max(1, cell // max(sx, sy)), rng)
    img = Image.fromarray((small * 255).astype(np.uint8), "L").resize((w, h), Image.BILINEAR)
    return np.asarray(img, dtype=np.float64) / 255.0


def striped(w, h, scale, rng, palette, thresholds, sx, sy):
    out = np.empty((h, w, 3))
    out[:] = palette[0]
    for color, threshold in zip(palette[1:], thresholds):
        field = stretched(w, h, scale * 2, rng, sx, sy)
        out[field >= np.quantile(field, threshold)] = color
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
    if set_id == "us_multicam":
        palette = [hex_rgb("a8996f"), hex_rgb("7d7a54"), hex_rgb("8a6a4a"), hex_rgb("4b3b2b"), hex_rgb("cbbf98")]
        return layered(w, h, scale, rng, palette, [0.5, 0.7, 0.88, 0.94])
    if set_id == "us_snow":
        palette = [hex_rgb("e9ebeb"), hex_rgb("c3c8ca"), hex_rgb("8f989c")]
        return layered(w, h, scale, rng, palette, [0.72, 0.92])
    if set_id == "ru_emr":
        palette = [hex_rgb("7a7a52"), hex_rgb("525b3a"), hex_rgb("6e5639"), hex_rgb("2b2b25")]
        small = layered(max(1, w // 2), max(1, h // 2), max(1, scale // 2), rng, palette, [0.5, 0.72, 0.88])
        return np.repeat(np.repeat(small, 2, axis=0), 2, axis=1)[:h, :w]
    if set_id == "us_ucp":
        palette = [hex_rgb("c9c8b8"), hex_rgb("9a9d8c"), hex_rgb("7f876f")]
        small = layered(max(1, w // 2), max(1, h // 2), max(1, scale // 2), rng, palette, [0.55, 0.8])
        return np.repeat(np.repeat(small, 2, axis=0), 2, axis=1)[:h, :w]
    if set_id == "us_tiger":
        palette = [hex_rgb("8a875a"), hex_rgb("5a5a36"), hex_rgb("5e4630"), hex_rgb("1f1f19")]
        return striped(w, h, scale, rng, palette, [0.5, 0.72, 0.86], 4, 1)
    if set_id == "ru_flora":
        palette = [hex_rgb("8f9b62"), hex_rgb("5b6e3c"), hex_rgb("6e5a3c")]
        return striped(w, h, scale, rng, palette, [0.5, 0.78], 1, 3)
    if set_id == "ghillie_desert":
        palette = [hex_rgb("c4ae82"), hex_rgb("a8915f"), hex_rgb("8a7b52"), hex_rgb("6e6446")]
        return layered(w, h, max(1, scale // 2), rng, palette, [0.45, 0.68, 0.86])
    if set_id == "ghillie":
        palette = [hex_rgb("525c33"), hex_rgb("6d6b3d"), hex_rgb("3a4224"), hex_rgb("6b583a")]
        return layered(w, h, max(1, scale // 2), rng, palette, [0.45, 0.65, 0.85])
    if set_id == "ghillie_snow":
        palette = [hex_rgb("e6eaea"), hex_rgb("cdd3d5"), hex_rgb("b2babd"), hex_rgb("a3ab9c")]
        return layered(w, h, max(1, scale // 2), rng, palette, [0.45, 0.68, 0.88])
    if set_id == "swat":
        return speckle(w, h, rng, hex_rgb("26282b"), hex_rgb("1f2023"), 0.3)
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




REPAIR_KIT = [
    "................",
    "......mmmm......",
    ".....m....m.....",
    "..############..",
    "..#ffffffffff#..",
    "..#ffffffffff#..",
    "..#####bb#####..",
    "..#ooooBBoooo#..",
    "..#oyyyyyyyyo#..",
    "..#oyKyKyKyyo#..",
    "..#oyyyyyyyyo#..",
    "..#oyKyKyKyyo#..",
    "..#oyyyyyyyyo#..",
    "..#oooooooooo#..",
    "..############..",
    "................",
]
REPAIR_KIT_COLORS = {"#": "26291d", "o": "56603a", "f": "636e43", "b": "8f9295", "B": "5d6063", "m": "3a3c3e",
                     "y": "c2a94a", "K": "9e8838"}


def pixel_icon(rows, colors):
    img = np.zeros((16, 16, 4))
    for y, row in enumerate(rows):
        for x, c in enumerate(row):
            if c != ".":
                img[y, x, :3] = hex_rgb(colors[c]) * (1.1 - 0.2 * y / 15)
                img[y, x, 3] = 255
    return Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), "RGBA")


def colorize(rows, fill):
    img = np.zeros((16, 16, 4))
    mean = fill.reshape(-1, 3).mean(axis=0)
    for y in range(16):
        shade = 1.12 - 0.24 * y / 15
        for x in range(16):
            c = rows[y][x]
            if c == ".":
                continue
            above = rows[y - 1][x] if y > 0 else "."
            rgb = mean * 0.3 if c == "#" else fill[y, x] * shade * (1.12 if above in ".#" else 1.0)
            img[y, x, :3] = rgb
            img[y, x, 3] = 255
    return Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), "RGBA")


def overlays():
    misc = ROOT / "src/main/resources/assets/tactical_loadout/textures/misc"
    misc.mkdir(parents=True, exist_ok=True)
    Image.new("RGBA", (16, 16), (255, 255, 255, 255)).save(misc / "white.png")
    rng = np.random.default_rng(9)
    noise = (rng.random((256, 256)) * 255).astype(np.uint8)
    Image.fromarray(np.dstack([noise, noise, noise, np.full_like(noise, 255)]), "RGBA").save(misc / "nvg_noise.png")

    def vignette(w, h, centers, radius):
        ys, xs = np.mgrid[0:h, 0:w].astype(np.float64)
        dist = np.full((h, w), np.inf)
        for cx, cy in centers:
            dist = np.minimum(dist, np.hypot(xs - cx, ys - cy))
        alpha = np.clip((dist - radius * 0.86) / (radius * 0.14), 0, 1)
        ring = np.exp(-((dist - radius * 0.97) / (radius * 0.03)) ** 2) * 0.35
        alpha = np.clip(alpha + ring, 0, 1)
        img = np.zeros((h, w, 4), dtype=np.uint8)
        img[..., 3] = (alpha * 255).astype(np.uint8)
        return Image.fromarray(img, "RGBA")

    vignette(256, 256, [(128, 128)], 124).save(misc / "nvg_pvs14.png")
    vignette(512, 256, [(186, 128), (326, 128)], 122).save(misc / "nvg_gpnvg18.png")

    w, h, r = 384, 216, 40
    ys, xs = np.mgrid[0:h, 0:w].astype(np.float64)
    qx = np.maximum(np.abs(xs - w / 2) - (w / 2 - r - 8), 0)
    qy = np.maximum(np.abs(ys - h / 2) - (h / 2 - r - 8), 0)
    dist = np.hypot(qx, qy) - r
    alpha = np.clip((dist + 6) / 10, 0, 1)
    img = np.zeros((h, w, 4), dtype=np.uint8)
    img[..., 3] = (alpha * 255).astype(np.uint8)
    Image.fromarray(img, "RGBA").save(misc / "nvg_thermal.png")


def smoke_puffs():
    out = ROOT / "src/main/resources/assets/tactical_loadout/textures/particle"
    out.mkdir(parents=True, exist_ok=True)
    size = 16
    ys, xs = np.mgrid[0:size, 0:size].astype(np.float64)
    dist = np.hypot(xs - 7.5, ys - 7.5) / 8.0
    tones = [hex_rgb("e8e8e8"), hex_rgb("d2d2d2"), hex_rgb("bababa")]
    for i in range(4):
        rng = np.random.default_rng(40 + i)
        noise = fbm(size, size, 4, rng, octaves=2)
        noise = (noise - noise.min()) / (noise.max() - noise.min())
        inside = dist + (noise - 0.5) * 0.5 < 0.9
        img = np.zeros((size, size, 4), dtype=np.uint8)
        shade = np.where(noise > 0.62, 0, np.where(noise > 0.38, 1, 2))
        shade = np.where(dist > 0.7, np.minimum(shade + 1, 2), shade)
        for t, color in enumerate(tones):
            mask = inside & (shade == t)
            img[mask, :3] = color.astype(np.uint8)
        img[inside, 3] = 255
        Image.fromarray(img, "RGBA").save(out / f"smoke_puff_{i}.png")


def save(img, name):
    ITEM_DIR.mkdir(parents=True, exist_ok=True)
    img.save(ITEM_DIR / f"{name}.png")


def main():
    for set_id in SETS:
        fill = pattern(set_id, 16, 16, 4, seed=7)
        save(colorize(FABRIC_MASK, fill), FABRIC_IDS[set_id] + "_fabric")

    kevlar = solid("c2a94a")
    for y in range(16):
        for x in range(16):
            if (x + y) % 2 == 0:
                kevlar[y, x] *= 0.86
    save(colorize(PLATE_MASK, kevlar), "kevlar_insert")
    save(colorize(PLATE_MASK, solid("9a8f86")), "raw_ceramic_plate")
    save(colorize(PLATE_MASK, solid("ddd8cc")), "ceramic_plate")
    save(pixel_icon(REPAIR_KIT, REPAIR_KIT_COLORS), "armor_repair_kit")
    overlays()
    smoke_puffs()
    print(f"wrote {len(list(ITEM_DIR.glob('*.png')))} textures to {ITEM_DIR.relative_to(ROOT)}")


if __name__ == "__main__":
    sys.exit(main())
