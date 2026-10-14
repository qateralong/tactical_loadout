#!/usr/bin/env python3
import json
import math
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/tacz_camo_armor"
SCALE = 7
BODY = [
    ((-4, 24, -4), (4, 32, 4)), ((-4, 12, -2), (4, 24, 2)),
    ((-8, 12, -2), (-4, 24, 2)), ((4, 12, -2), (8, 24, 2)),
    ((-3.9, 0, -2), (0.1, 12, 2)), ((-0.1, 0, -2), (3.9, 12, 2)),
]


def rot_matrix(rx, ry, rz):
    rx, ry, rz = (math.radians(a) for a in (rx, ry, rz))
    mx = np.array([[1, 0, 0], [0, math.cos(rx), -math.sin(rx)], [0, math.sin(rx), math.cos(rx)]])
    my = np.array([[math.cos(ry), 0, math.sin(ry)], [0, 1, 0], [-math.sin(ry), 0, math.cos(ry)]])
    mz = np.array([[math.cos(rz), -math.sin(rz), 0], [math.sin(rz), math.cos(rz), 0], [0, 0, 1]])
    return mz @ my @ mx


def faces_of(lo, hi):
    lo, hi = np.array(lo, float), np.array(hi, float)
    w, h, d = hi - lo
    return {
        "north": ((lo[0], hi[1], lo[2]), (w, 0, 0), (0, -h, 0), (0, 0, -1)),
        "south": ((hi[0], hi[1], hi[2]), (-w, 0, 0), (0, -h, 0), (0, 0, 1)),
        "east": ((hi[0], hi[1], lo[2]), (0, 0, d), (0, -h, 0), (1, 0, 0)),
        "west": ((lo[0], hi[1], hi[2]), (0, 0, -d), (0, -h, 0), (-1, 0, 0)),
        "up": ((lo[0], hi[1], hi[2]), (w, 0, 0), (0, 0, -d), (0, 1, 0)),
        "down": ((lo[0], lo[1], lo[2]), (w, 0, 0), (0, 0, d), (0, -1, 0)),
    }


def quads(geo, tex, tiers, shown=None):
    tex = np.asarray(tex)
    out = []
    for lo, hi in BODY:
        for name, (o, u, v, n) in faces_of(lo, hi).items():
            out.append((np.array(o), np.array(u), np.array(v), np.array(n, float), None, None, None))
    for bone in geo["bones"]:
        tier = bone["name"].split("_")[0]
        if tier in ("medium", "heavy") and tier not in tiers:
            continue
        if shown is not None and "parent" in bone and not bone["name"].startswith(("medium_", "heavy_")) and bone["name"] not in shown:
            continue
        for cube in bone["cubes"]:
            inf = cube.get("inflate", 0)
            lo = np.array(cube["origin"], float) - inf
            hi = np.array(cube["origin"], float) + np.array(cube["size"], float) + inf
            m = rot_matrix(*cube["rotation"]) if "rotation" in cube else None
            piv = np.array(cube.get("pivot", [0, 0, 0]), float)
            for name, (o, u, v, n) in faces_of(lo, hi).items():
                o, u, v, n = (np.array(a, float) for a in (o, u, v, n))
                if m is not None:
                    o, u, v, n = m @ (o - piv) + piv, m @ u, m @ v, m @ n
                face = cube["uv"][name]
                out.append((o, u, v, n, tex, face["uv"], face["uv_size"]))
    return out


def render(geo, tex, tiers, yaw, size=(130, 260), scale=SCALE, background=(235, 235, 235, 255), body=(150, 150, 150, 255), shown=None, pitch=0):
    img = Image.new("RGBA", size, background)
    draw = ImageDraw.Draw(img)
    m = rot_matrix(pitch, 0, 0) @ rot_matrix(0, yaw, 0)
    cx, cy = size[0] / 2, size[1] - 20
    polys = []
    for o, u, v, n, tex, uv, uvs in quads(geo, tex, tiers, shown):
        nn = m @ n
        if nn[2] >= -1e-6:
            continue
        light = 0.65 + 0.35 * max(0.0, float(-nn[2]) * 0.8 + float(nn[1]) * 0.4)
        if tex is None:
            gw, gh = max(1, int(np.linalg.norm(u) * 2)), max(1, int(np.linalg.norm(v) * 2))
            grid = [(i / gw, j / gh, (i + 1) / gw, (j + 1) / gh, body) for j in range(gh) for i in range(gw)]
        else:
            gw, gh = int(uvs[0]), int(uvs[1])
            grid = []
            for j in range(gh):
                for i in range(gw):
                    c = tex[int(uv[1]) + j, int(uv[0]) + i]
                    if c[3] < 128:
                        continue
                    grid.append((i / gw, j / gh, (i + 1) / gw, (j + 1) / gh, tuple(int(x) for x in c)))
        for a0, b0, a1, b1, col in grid:
            pts3 = [o + u * a + v * b for a, b in ((a0, b0), (a1, b0), (a1, b1), (a0, b1))]
            pts = [m @ p for p in pts3]
            depth = sum(p[2] for p in pts) / 4
            shade = tuple(int(c * light) for c in col[:3]) + (255,)
            polys.append((depth, [(cx + p[0] * scale, cy - p[1] * scale) for p in pts], shade))
    polys.sort(key=lambda t: -t[0])
    for _, pts, col in polys:
        draw.polygon(pts, fill=col)
    return img


def main():
    form = sys.argv[1] if len(sys.argv) > 1 else "us"
    set_id = sys.argv[2] if len(sys.argv) > 2 else {"us": "us_woodland"}.get(form, form)
    out = Path(sys.argv[3]) if len(sys.argv) > 3 else Path(f"preview_{set_id}.png")
    geo = json.loads((ASSETS / f"geo/armor/{form}.geo.json").read_text())["minecraft:geometry"][0]
    tex = Image.open(ASSETS / f"textures/armor/{set_id}.png").convert("RGBA")
    tier_sets = [(), ("medium",), ("medium", "heavy")]
    yaws = [0, 180, 90, 35]
    sheet = Image.new("RGBA", (130 * len(yaws), 260 * len(tier_sets)), (255, 255, 255, 255))
    for r, tiers in enumerate(tier_sets):
        for c, yaw in enumerate(yaws):
            sheet.paste(render(geo, tex, tiers, yaw), (c * 130, r * 260))
    sheet.save(out)
    print(out)


if __name__ == "__main__":
    sys.exit(main())
