#!/usr/bin/env python3
import json
import subprocess
import sys
import tempfile
import wave
from pathlib import Path

import numpy as np

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/tactical_loadout"
SOUND_DIR = ASSETS / "sounds/grenade"
RATE = 44100
NS = "tactical_loadout"


def lowpass(signal, cutoff):
    alpha = 1.0 - np.exp(-2.0 * np.pi * cutoff / RATE)
    out = np.empty_like(signal)
    acc = 0.0
    for i, x in enumerate(signal):
        acc += alpha * (x - acc)
        out[i] = acc
    return out


def envelope(n, attack, decay):
    t = np.arange(n) / RATE
    return np.minimum(1.0, t / attack) * np.exp(-t / decay)


# A suppressed-rifle-like crack: a sharp noise transient, a low body thump and a short room tail.
def gunshot(seed):
    rng = np.random.default_rng(seed)
    n = int(RATE * 0.7)
    t = np.arange(n) / RATE
    crack = rng.standard_normal(n) * envelope(n, 0.0005, 0.018)
    body = lowpass(rng.standard_normal(n), 900 + 200 * rng.random()) * envelope(n, 0.001, 0.06) * 3.0
    thump = np.sin(2 * np.pi * (70 + 15 * rng.random()) * t) * envelope(n, 0.002, 0.05)
    tail = lowpass(rng.standard_normal(n), 500) * envelope(n, 0.02, 0.22) * 0.9
    signal = crack * 0.8 + body + thump * 0.9 + tail
    return signal / np.max(np.abs(signal)) * 0.95


# Pin pulled: a soft metal scrape, a click, then the ring of the pin swinging free.
def pin():
    rng = np.random.default_rng(3)
    n = int(RATE * 0.45)
    t = np.arange(n) / RATE
    scrape = (rng.standard_normal(n) - lowpass(rng.standard_normal(n), 3000)) * envelope(n, 0.01, 0.03) * 0.25
    click_at = int(RATE * 0.05)
    click = np.zeros(n)
    m = n - click_at
    click[click_at:] = (rng.standard_normal(m) - lowpass(rng.standard_normal(m), 4000)) * envelope(m, 0.0003, 0.004)
    ring = np.zeros(n)
    for freq, amp in ((2350, 1.0), (4120, 0.6), (6890, 0.35)):
        ring[click_at:] += amp * np.sin(2 * np.pi * freq * t[:m]) * envelope(m, 0.001, 0.09)
    signal = scrape + click * 1.2 + ring * 0.25
    return signal / np.max(np.abs(signal)) * 0.8


# Tin cans on a tripwire: a burst of tinny clanks that rattle on and slowly die down.
def can_rattle(seed):
    rng = np.random.default_rng(seed)
    n = int(RATE * 1.6)
    signal = np.zeros(n)
    at = 0.0
    while at < 1.3:
        start = int(at * RATE)
        m = n - start
        t = np.arange(m) / RATE
        loud = (1.0 - at / 1.4) ** 1.5 * (0.6 + 0.4 * rng.random())
        hit = (rng.standard_normal(m) - lowpass(rng.standard_normal(m), 2500)) * envelope(m, 0.0004, 0.006)
        for freq in (850, 1630, 2580, 3970):
            f = freq * (0.9 + 0.2 * rng.random())
            hit += 0.45 * rng.random() * np.sin(2 * np.pi * f * t) * envelope(m, 0.0008, 0.04 + 0.08 * rng.random())
        signal[start:] += hit * loud
        at += 0.025 + 0.12 * rng.random() * (0.4 + at)
    return signal / np.max(np.abs(signal)) * 0.95


def write_ogg(path, signal):
    path.parent.mkdir(parents=True, exist_ok=True)
    pcm = (np.clip(signal, -1, 1) * 32767).astype(np.int16)
    with tempfile.NamedTemporaryFile(suffix=".wav") as tmp:
        with wave.open(tmp.name, "wb") as wav:
            wav.setnchannels(1)
            wav.setsampwidth(2)
            wav.setframerate(RATE)
            wav.writeframes(pcm.tobytes())
        subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", tmp.name, "-c:a", "libvorbis", "-q:a", "5", str(path)],
                       check=True)


def main():
    shots = []
    for i in range(3):
        write_ogg(SOUND_DIR / f"decoy_shot{i + 1}.ogg", gunshot(i + 1))
        shots.append(f"{NS}:grenade/decoy_shot{i + 1}")
    write_ogg(SOUND_DIR / "pin.ogg", pin())
    rattles = []
    for i in range(2):
        write_ogg(ASSETS / f"sounds/gear/can_rattle{i + 1}.ogg", can_rattle(10 + i))
        rattles.append(f"{NS}:gear/can_rattle{i + 1}")
    sounds = {
        "grenade.decoy_shot": {"subtitle": f"subtitles.{NS}.grenade.decoy_shot", "sounds": shots},
        "grenade.pin": {"subtitle": f"subtitles.{NS}.grenade.pin", "sounds": [f"{NS}:grenade/pin"]},
        "tripwire_alarm.rattle": {"subtitle": f"subtitles.{NS}.tripwire_alarm.rattle", "sounds": rattles},
    }
    (ASSETS / "sounds.json").write_text(json.dumps(sounds, indent=2) + "\n")
    print(f"wrote {len(shots) + 1 + len(rattles)} sounds")


if __name__ == "__main__":
    sys.exit(main())
