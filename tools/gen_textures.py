#!/usr/bin/env python3
"""Draws all block and item textures. Usage: tools/gen_textures.py [repo root]  (needs Pillow)"""
import os, sys, random
from PIL import Image

root = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(__file__), "..")
T = os.path.join(root, "src/main/resources/assets/techcraft/textures")
os.makedirs(T + "/blocks", exist_ok=True)
os.makedirs(T + "/items", exist_ok=True)
rnd = random.Random(42)


def shade(c, d):
    return tuple(max(0, min(255, v + d)) for v in c[:3]) + (255,)


def rect(im, x0, y0, x1, y1, c):
    for x in range(x0, x1 + 1):
        for y in range(y0, y1 + 1):
            im.putpixel((x, y), c)


def frame(im, x0, y0, x1, y1, base):
    rect(im, x0, y0, x1, y1, shade(base, -60))
    for x in range(x0, x1 + 1):
        im.putpixel((x, y1), shade(base, 35))
    for y in range(y0, y1 + 1):
        im.putpixel((x1, y), shade(base, 35))


def casing(base=(120, 124, 130)):
    im = Image.new("RGBA", (16, 16))
    for x in range(16):
        for y in range(16):
            im.putpixel((x, y), shade(base, rnd.randint(-6, 6)))
    for i in range(16):
        im.putpixel((i, 0), shade(base, 40))
        im.putpixel((0, i), shade(base, 40))
        im.putpixel((i, 15), shade(base, -45))
        im.putpixel((15, i), shade(base, -45))
    for (x, y) in ((2, 2), (13, 2), (2, 13), (13, 13)):
        im.putpixel((x, y), shade(base, 55))
        im.putpixel((x + 1, y + 1), shade(base, -50))
    return im


def save_block(im, name):
    im.save(f"{T}/blocks/{name}.png")


def save_item(im, name):
    im.save(f"{T}/items/{name}.png")


BASE = (120, 124, 130)

# ---- shared casing ----
save_block(casing(), "machine_side")
top = casing((105, 108, 114))
rect(top, 5, 5, 10, 10, (70, 72, 76, 255))
for i in range(5, 11, 2):
    rect(top, i, 6, i, 9, (45, 46, 50, 255))
save_block(top, "machine_top")


# ---- original machines ----
def front_generator(on):
    im = casing()
    frame(im, 3, 7, 12, 12, BASE)
    for x in range(4, 12):
        for y in range(8, 12):
            if on:
                h = rnd.random()
                c = (255, 220, 60, 255) if h > 0.6 else (240, 120, 20, 255) if h > 0.25 else (180, 40, 10, 255)
            else:
                c = (30, 30, 32, 255) if rnd.random() > 0.3 else (55, 55, 58, 255)
            im.putpixel((x, y), c)
    for x in range(4, 12, 2):
        rect(im, x, 3, x, 4, (60, 62, 66, 255))
    return im


def front_furnace(on):
    im = casing()
    frame(im, 3, 3, 12, 12, BASE)
    rect(im, 4, 4, 11, 11, (40, 40, 44, 255))
    coil = (255, 110, 40, 255) if on else (110, 80, 60, 255)
    for y in (5, 8, 10):
        rect(im, 4, y, 11, y, coil)
    if on:
        for x in range(4, 12):
            im.putpixel((x, 6), (255, 190, 90, 255))
    return im


def front_crusher(on):
    im = casing()
    frame(im, 2, 3, 13, 12, BASE)
    rect(im, 3, 4, 12, 11, (40, 40, 44, 255))
    tooth = (200, 200, 205, 255) if on else (150, 150, 155, 255)
    for i, x in enumerate(range(3, 13)):
        h = (i + (1 if on else 0)) % 2
        rect(im, x, 4, x, 5 + h, tooth)
        rect(im, x, 10 - h, x, 11, tooth)
    if on:
        for _ in range(6):
            im.putpixel((rnd.randint(4, 11), rnd.randint(7, 8)), (150, 120, 90, 255))
    return im


for on in (False, True):
    sfx = "_front_on" if on else "_front"
    save_block(front_generator(on), "coal_generator" + sfx)
    save_block(front_furnace(on), "electric_furnace" + sfx)
    save_block(front_crusher(on), "crusher" + sfx)


# ---- batteries (three tiers) ----
def battery_box(base, glow):
    side = casing(base)
    frame(side, 4, 3, 11, 12, base)
    for y in range(4, 12):
        rect(side, 5, y, 10, y, glow if y > 6 else shade(glow, -150))
    front = casing(base)
    frame(front, 5, 5, 10, 10, base)
    rect(front, 6, 6, 9, 9, glow)
    rect(front, 7, 7, 8, 8, shade(glow, 90))
    return side, front


for name, base, glow in (("battery_box", (95, 105, 120), (230, 50, 40, 255)),
                         ("battery_box_advanced", (110, 100, 70), (250, 200, 50, 255)),
                         ("battery_box_ultimate", (80, 70, 110), (90, 220, 255, 255))):
    s, f = battery_box(base, glow)
    save_block(s, name + "_side")
    save_block(f, name + "_front")


# ---- compressor: a piston pressing down ----
def front_compressor(on):
    im = casing()
    frame(im, 3, 3, 12, 12, BASE)
    rect(im, 4, 4, 11, 11, (40, 40, 44, 255))
    press = 8 if on else 5
    rect(im, 5, 4, 10, press, (170, 172, 178, 255))
    rect(im, 5, press, 10, press, (110, 112, 118, 255))
    rect(im, 4, 10, 11, 11, (90, 92, 98, 255))
    return im


# ---- alloy furnace: two input hoppers and a fire ----
def front_alloy(on):
    im = casing()
    rect(im, 3, 2, 5, 4, (60, 62, 68, 255))
    rect(im, 10, 2, 12, 4, (60, 62, 68, 255))
    frame(im, 3, 6, 12, 13, BASE)
    rect(im, 4, 7, 11, 12, (40, 40, 44, 255))
    for x in range(4, 12):
        for y in range(9, 13):
            if on:
                h = rnd.random()
                im.putpixel((x, y), (255, 220, 60, 255) if h > 0.6 else (240, 120, 20, 255) if h > 0.25 else (190, 50, 10, 255))
    if not on:
        rect(im, 5, 11, 10, 12, (90, 60, 40, 255))
    return im


# ---- charger: lightning bolt ----
def front_charger(on):
    im = casing()
    frame(im, 3, 3, 12, 12, BASE)
    rect(im, 4, 4, 11, 11, (30, 32, 44, 255))
    c = (255, 235, 90, 255) if on else (150, 140, 70, 255)
    for (x, y) in ((8, 4), (7, 5), (7, 6), (6, 7), (7, 7), (8, 7), (8, 8), (7, 9), (7, 10), (6, 11)):
        im.putpixel((x, y), c)
    return im


# ---- lava generator: glowing window ----
def front_lava(on):
    im = casing()
    frame(im, 3, 3, 12, 12, BASE)
    for x in range(4, 12):
        for y in range(4, 12):
            h = rnd.random()
            if on:
                im.putpixel((x, y), (255, 210, 60, 255) if h > 0.7 else (250, 120, 20, 255) if h > 0.3 else (200, 60, 10, 255))
            else:
                im.putpixel((x, y), (150, 70, 20, 255) if h > 0.5 else (110, 45, 15, 255))
    return im


# ---- wind turbine: four blades ----
def front_wind(on):
    im = casing()
    frame(im, 2, 2, 13, 13, BASE)
    rect(im, 3, 3, 12, 12, (60, 66, 76, 255))
    blade = (225, 228, 235, 255)
    if on:
        pts = [(8, 4), (8, 5), (8, 6), (9, 8), (10, 8), (11, 8), (8, 9), (8, 10), (8, 11), (7, 8), (6, 8), (5, 8)]
        pts = [(x + (1 if y < 8 and x == 8 else 0), y) for x, y in pts]
    else:
        pts = [(8, 4), (8, 5), (8, 6), (9, 8), (10, 8), (11, 8), (7, 9), (7, 10), (7, 11), (4, 7), (5, 7), (6, 7)]
    for p in pts:
        im.putpixel(p, blade)
    rect(im, 7, 7, 8, 8, (240, 80, 60, 255))
    return im


for on in (False, True):
    sfx = "_front_on" if on else "_front"
    save_block(front_compressor(on), "compressor" + sfx)
    save_block(front_alloy(on), "alloy_furnace" + sfx)
    save_block(front_charger(on), "charger" + sfx)
    save_block(front_lava(on), "lava_generator" + sfx)
    save_block(front_wind(on), "wind_turbine" + sfx)

# ---- fluid tank: glass gauge ----
tank_side = casing((130, 140, 150))
for y in range(3, 13):
    for x in (6, 7, 8, 9):
        tank_side.putpixel((x, y), (70, 100, 130, 255) if y > 4 else (170, 200, 220, 255))
save_block(tank_side, "fluid_tank_side")
tank_front = casing((130, 140, 150))
frame(tank_front, 4, 3, 11, 12, (130, 140, 150))
rect(tank_front, 5, 4, 10, 11, (150, 190, 215, 255))
for y in range(4, 12):
    tank_front.putpixel((6, y), (230, 245, 255, 255))
for y in (5, 7, 9):
    rect(tank_front, 9, y, 10, y, (60, 80, 100, 255))
save_block(tank_front, "fluid_tank_front")


# ---- solar panels: grid of cells, deeper blue / gold / purple per tier ----
def solar_top(cell, line):
    im = Image.new("RGBA", (16, 16), shade(line, 0))
    for cx in range(2):
        for cy in range(2):
            x0, y0 = 1 + cx * 7, 1 + cy * 7
            for x in range(x0, x0 + 6):
                for y in range(y0, y0 + 6):
                    im.putpixel((x, y), shade(cell, rnd.randint(-8, 8) + (12 if x == x0 or y == y0 else 0)))
            for k in range(2, 6, 2):
                for y in range(y0, y0 + 6):
                    im.putpixel((x0 + k - 1, y), shade(line, 0))
    return im


save_block(solar_top((40, 80, 160), (190, 195, 205)), "solar_top_basic")
save_block(solar_top((30, 120, 190), (230, 190, 70)), "solar_top_advanced")
save_block(solar_top((100, 50, 170), (255, 215, 90)), "solar_top_ultimate")
solar_front = casing()
frame(solar_front, 5, 6, 10, 9, BASE)
rect(solar_front, 6, 7, 9, 8, (90, 220, 110, 255))
save_block(solar_front, "solar_front")

# ---- plain items ----
def dust(color):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for (y0, x0, x1) in [(5, 7, 8), (6, 6, 9), (7, 5, 10), (8, 4, 11), (9, 4, 11), (10, 3, 12), (11, 3, 12), (12, 2, 13)]:
        for x in range(x0, x1 + 1):
            im.putpixel((x, y0), shade(color, rnd.randint(-25, 25) + (20 if x < 7 else -10) - (y0 - 8) * 3))
    return im


save_item(dust((215, 205, 200)), "iron_dust")
save_item(dust((245, 205, 60)), "gold_dust")

c = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
rect(c, 2, 3, 13, 12, (30, 110, 50, 255))
for x in range(2, 14):
    c.putpixel((x, 3), (60, 150, 80, 255))
    c.putpixel((x, 12), (20, 70, 30, 255))
rect(c, 6, 6, 9, 9, (40, 40, 44, 255))
for x in (6, 9):
    c.putpixel((x, 5), (200, 200, 200, 255))
    c.putpixel((x, 10), (200, 200, 200, 255))
for x in range(3, 6):
    c.putpixel((x, 7), (230, 190, 60, 255))
for x in range(10, 13):
    c.putpixel((x, 8), (230, 190, 60, 255))
c.putpixel((4, 10), (220, 40, 30, 255))
c.putpixel((11, 5), (220, 40, 30, 255))
save_item(c, "circuit")

f = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
g = (150, 152, 158)
for i in range(1, 15):
    for (x, y) in ((i, 1), (i, 14), (1, i), (14, i)):
        f.putpixel((x, y), shade(g, 20))
    for (x, y) in ((i, 2), (2, i)):
        f.putpixel((x, y), shade(g, -10))
    for (x, y) in ((i, 13), (13, i)):
        f.putpixel((x, y), shade(g, -45))
for i in range(3, 13):
    f.putpixel((i, i), shade(g, 5))
    f.putpixel((15 - i, i), shade(g, -20))
rect(f, 7, 7, 8, 8, (200, 40, 30, 255))
save_item(f, "machine_frame")

# ---- plates ----
plate_colors = {"iron": (200, 200, 205), "gold": (250, 215, 70), "copper": (215, 125, 75), "tin": (205, 215, 225),
                "lead": (115, 115, 160), "silver": (225, 235, 245), "bronze": (195, 135, 65), "steel": (135, 145, 158)}
for name, col in plate_colors.items():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for x in range(2, 14):
        for y in range(4, 13):
            im.putpixel((x, y), shade(col, rnd.randint(-7, 7)))
    for x in range(2, 14):
        im.putpixel((x, 4), shade(col, 45))
        im.putpixel((x, 12), shade(col, -55))
    for y in range(4, 13):
        im.putpixel((2, y), shade(col, 30))
        im.putpixel((13, y), shade(col, -40))
    for (x, y) in ((4, 6), (11, 6), (4, 10), (11, 10)):
        im.putpixel((x, y), shade(col, -60))
    save_item(im, f"plate_{name}")

# ---- batteries ----
for name, col in (("basic", (230, 60, 50)), ("advanced", (250, 200, 50)), ("ultimate", (80, 210, 255))):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rect(im, 6, 1, 9, 2, (200, 200, 205, 255))
    frame(im, 4, 3, 11, 14, (80, 85, 95))
    for y in range(4, 14):
        rect(im, 5, y, 10, y, shade(col, 0) if y > 6 else shade(col, -130))
    for y in range(4, 14):
        im.putpixel((5, y), shade(col, 70) if y > 6 else shade(col, -90))
    im.putpixel((7, 8), (255, 255, 255, 255))
    im.putpixel((8, 9), (255, 255, 255, 255))
    save_item(im, f"battery_{name}")


# ---- tools: drawn on the diagonal like vanilla tools ----
def diagonal(im, length, color, start=(2, 13)):
    x, y = start
    for i in range(length):
        im.putpixel((x + i, y - i), color)
        im.putpixel((x + i, y - i + 1), shade(color, -40))


drill = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
diagonal(drill, 8, (110, 112, 120, 255))
for (x, y) in ((9, 5), (10, 4), (11, 3), (12, 2), (13, 1)):
    drill.putpixel((x, y), (240, 240, 245, 255))
    drill.putpixel((x + 1, y), (170, 175, 185, 255))
rect(drill, 7, 6, 10, 9, (235, 140, 30, 255))
rect(drill, 7, 9, 10, 9, (170, 90, 20, 255))
drill.putpixel((3, 12), (80, 200, 255, 255))
save_item(drill, "electric_drill")

saw = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
rect(saw, 3, 8, 7, 11, (235, 140, 30, 255))
rect(saw, 3, 11, 7, 11, (170, 90, 20, 255))
for x in range(8, 15):
    saw.putpixel((x, 8), (210, 215, 225, 255))
    saw.putpixel((x, 9), (150, 155, 165, 255))
    if x % 2 == 0:
        saw.putpixel((x, 10), (230, 230, 235, 255))
rect(saw, 2, 6, 4, 7, (70, 72, 78, 255))
saw.putpixel((5, 9), (80, 200, 255, 255))
save_item(saw, "chainsaw")

wrench = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
diagonal(wrench, 9, (190, 60, 50, 255), start=(2, 13))
for (x, y) in ((9, 3), (10, 2), (11, 2), (12, 3), (12, 4), (11, 5), (10, 5)):
    wrench.putpixel((x, y), (205, 208, 215, 255))
wrench.putpixel((11, 3), (0, 0, 0, 0))
wrench.putpixel((10, 4), (160, 165, 175, 255))
save_item(wrench, "tech_wrench")

meter = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
frame(meter, 4, 2, 11, 14, (90, 95, 105))
rect(meter, 5, 3, 10, 8, (30, 80, 40, 255))
for y in range(4, 8):
    rect(meter, 6, y, 9, y, (110, 240, 130, 255) if y > 5 else (50, 140, 60, 255))
rect(meter, 5, 10, 6, 11, (220, 50, 40, 255))
rect(meter, 9, 10, 10, 11, (60, 110, 230, 255))
save_item(meter, "energy_meter")
print("textures ok")
