#!/usr/bin/env python3
import json
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont

from preview import render
from textures import pattern

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/tactical_loadout"
OUT = ROOT / "src/main/resources/tactical_loadout_logo.png"
FONT = "/usr/share/fonts/noto/NotoSans-Black.ttf"
W, H = 512, 256
TITLE = "TACTICAL LOADOUT"
SOLDIERS = [
    ("t", "t", ("medium", "heavy"), {"gas_mask"}, 22),
    ("ru", "ru_emr", ("medium", "heavy"), {"nvg_mount", "nvg_thermal"}, 28),
    ("us", "us_woodland", ("medium", "heavy"), {"nvg_mount", "nvg_gpnvg18", "nvg_battery"}, 18),
    ("ct", "ct", ("medium", "heavy"), set(), -22),
    ("ghillie", "ghillie", (), set(), -30),
]


def pixel_text(text, size, color, shadow):
    font = ImageFont.truetype(FONT, size)
    box = font.getbbox(text)
    mask = Image.new("1", (box[2] - box[0] + 2, box[3] - box[1] + 2), 0)
    ImageDraw.Draw(mask).text((-box[0], -box[1]), text, font=font, fill=1)
    out = Image.new("RGBA", (mask.width + 1, mask.height + 1), (0, 0, 0, 0))
    out.paste(Image.new("RGBA", mask.size, shadow), (1, 1), mask)
    out.paste(Image.new("RGBA", mask.size, color), (0, 0), mask)
    return out


def main():
    camo = pattern("us_woodland", W // 8, H // 8, 4, seed=21)
    camo = np.repeat(np.repeat(camo, 8, axis=0), 8, axis=1)
    fade = np.linspace(0.42, 0.18, H)[:, None, None]
    bg = Image.fromarray(np.clip(camo * fade, 0, 255).astype(np.uint8), "RGB").convert("RGBA")

    figures = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    step = W / (len(SOLDIERS) + 0.4)
    order = sorted(range(len(SOLDIERS)), key=lambda i: abs(i - (len(SOLDIERS) - 1) / 2), reverse=True)
    for i in order:
        form, set_id, tiers, shown, yaw = SOLDIERS[i]
        geo = json.loads((ASSETS / f"geo/armor/{form}.geo.json").read_text())["minecraft:geometry"][0]
        tex = Image.open(ASSETS / f"textures/armor/{set_id}.png").convert("RGBA")
        scale = 5.2 if i == len(SOLDIERS) // 2 else 4.6
        img = render(geo, tex, tiers, yaw, size=(130, 200), scale=scale, background=(0, 0, 0, 0),
                     body=(38, 40, 36, 255), shown=shown)
        x = int(step * (i + 0.7) - 65)
        figures.alpha_composite(img, (x, H - 200 + 4))

    logo = Image.alpha_composite(bg, figures)
    title = pixel_text(TITLE, 13, (233, 222, 186, 255), (24, 26, 20, 255))
    factor = max(1, min(3, (W - 24) // title.width))
    title = title.resize((title.width * factor, title.height * factor), Image.NEAREST)
    band = Image.new("RGBA", (W, title.height + 16), (16, 18, 14, 200))
    logo.alpha_composite(band, (0, 0))
    logo.alpha_composite(title, ((W - title.width) // 2, 8))
    logo.save(OUT)
    print(OUT.relative_to(ROOT), logo.size)


if __name__ == "__main__":
    sys.exit(main())
