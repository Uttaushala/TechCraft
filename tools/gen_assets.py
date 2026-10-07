#!/usr/bin/env python3
"""Regenerates blockstates, models and crafting recipes. Usage: tools/gen_assets.py [repo root]"""
import json, os, sys

root = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(__file__), "..")
A = os.path.join(root, "src/main/resources/assets/techcraft")


def w(path, obj):
    p = os.path.join(A, path)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


# name: (side, top, front, front_on)
machines = {
    "coal_generator": ("machine_side", "machine_top", "coal_generator_front", "coal_generator_front_on"),
    "electric_furnace": ("machine_side", "machine_top", "electric_furnace_front", "electric_furnace_front_on"),
    "crusher": ("machine_side", "machine_top", "crusher_front", "crusher_front_on"),
    "battery_box": ("battery_box_side", "machine_top", "battery_box_front", "battery_box_front"),
    "battery_box_advanced": ("battery_box_advanced_side", "machine_top", "battery_box_advanced_front", "battery_box_advanced_front"),
    "battery_box_ultimate": ("battery_box_ultimate_side", "machine_top", "battery_box_ultimate_front", "battery_box_ultimate_front"),
    "compressor": ("machine_side", "machine_top", "compressor_front", "compressor_front_on"),
    "alloy_furnace": ("machine_side", "machine_top", "alloy_furnace_front", "alloy_furnace_front_on"),
    "charger": ("machine_side", "machine_top", "charger_front", "charger_front_on"),
    "lava_generator": ("machine_side", "machine_top", "lava_generator_front", "lava_generator_front_on"),
    "fluid_tank": ("fluid_tank_side", "machine_top", "fluid_tank_front", "fluid_tank_front"),
    "wind_turbine": ("machine_side", "machine_top", "wind_turbine_front", "wind_turbine_front_on"),
    "solar_panel": ("machine_side", "solar_top_basic", "solar_front", "solar_front"),
    "solar_panel_advanced": ("machine_side", "solar_top_advanced", "solar_front", "solar_front"),
    "solar_panel_ultimate": ("machine_side", "solar_top_ultimate", "solar_front", "solar_front"),
}
rot = {"north": 0, "east": 90, "south": 180, "west": 270}
for name, (side, top, front, front_on) in machines.items():
    for suffix, fr in (("", front), ("_on", front_on)):
        w(f"models/block/{name}{suffix}.json", {"parent": "minecraft:block/orientable", "textures": {
            "top": f"techcraft:blocks/{top}", "front": f"techcraft:blocks/{fr}", "side": f"techcraft:blocks/{side}"}})
    variants = {}
    for f, y in rot.items():
        for active in ("false", "true"):
            m = {"model": f"techcraft:{name}" + ("_on" if active == "true" else "")}
            if y:
                m["y"] = y
            variants[f"active={active},facing={f}"] = m
    w(f"blockstates/{name}.json", {"variants": variants})
    w(f"models/item/{name}.json", {"parent": f"techcraft:block/{name}"})

plates = ["iron", "gold", "copper", "tin", "lead", "silver", "bronze", "steel"]
generated = ["iron_dust", "gold_dust", "circuit", "machine_frame"] + [f"plate_{p}" for p in plates] + \
            ["battery_basic", "battery_advanced", "battery_ultimate"]
handheld = ["electric_drill", "chainsaw", "tech_wrench", "energy_meter"]
for item in generated:
    w(f"models/item/{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": f"techcraft:items/{item}"}})
for item in handheld:
    w(f"models/item/{item}.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": f"techcraft:items/{item}"}})


def ore(name):
    return {"type": "forge:ore_dict", "ore": name}


def item(name, data=None):
    d = {"item": name}
    if data is not None:
        d["data"] = data
    return d


T = lambda n: item(f"techcraft:{n}")
IRON, REDSTONE, GLASS, DIAMOND = ore("ingotIron"), ore("dustRedstone"), ore("blockGlass"), ore("gemDiamond")
P_IRON, P_GOLD, P_COPPER = ore("plateIron"), ore("plateGold"), ore("plateCopper")
CIRCUIT, FRAME = T("circuit"), T("machine_frame")

recipes = {
    "circuit": ([" R ", "IGI", " R "], {"R": REDSTONE, "I": IRON, "G": ore("nuggetGold")}, "circuit", 2),
    "machine_frame": (["III", "IRI", "III"], {"I": IRON, "R": REDSTONE}, "machine_frame", 1),
    "coal_generator": (["ICI", "IMI", "IFI"], {"I": IRON, "C": CIRCUIT, "M": FRAME, "F": item("minecraft:furnace")}, "coal_generator", 1),
    "electric_furnace": (["ICI", "FMF", "IRI"], {"I": IRON, "C": CIRCUIT, "M": FRAME, "F": item("minecraft:furnace"), "R": REDSTONE}, "electric_furnace", 1),
    "crusher": (["IFI", "CMC", "IPI"], {"I": IRON, "C": CIRCUIT, "M": FRAME, "F": item("minecraft:flint"), "P": item("minecraft:piston")}, "crusher", 1),
    "battery_box": (["ICI", "RMR", "IRI"], {"I": IRON, "C": CIRCUIT, "M": FRAME, "R": ore("blockRedstone")}, "battery_box", 1),
    "compressor": (["IPI", "CMC", "ILI"], {"I": IRON, "P": item("minecraft:piston"), "C": CIRCUIT, "M": FRAME, "L": P_IRON}, "compressor", 1),
    "alloy_furnace": (["BFB", "FMF", "BCB"], {"B": item("minecraft:brick"), "F": item("minecraft:furnace"), "M": FRAME, "C": CIRCUIT}, "alloy_furnace", 1),
    "charger": (["GCG", "RMR", "GRG"], {"G": P_GOLD, "C": CIRCUIT, "R": REDSTONE, "M": FRAME}, "charger", 1),
    "lava_generator": (["IBI", "CMC", "IFI"], {"I": IRON, "B": item("minecraft:bucket"), "C": CIRCUIT, "M": FRAME, "F": item("minecraft:furnace")}, "lava_generator", 1),
    "fluid_tank": (["GPG", "GMG", "GPG"], {"G": GLASS, "P": P_IRON, "M": FRAME}, "fluid_tank", 1),
    "wind_turbine": (["PIP", "CMC", "PIP"], {"P": P_IRON, "I": IRON, "C": CIRCUIT, "M": FRAME}, "wind_turbine", 1),
    "solar_panel": (["GGG", "CLC", "IMI"], {"G": GLASS, "C": CIRCUIT, "L": item("minecraft:dye", 4), "I": IRON, "M": FRAME}, "solar_panel", 1),
    "solar_panel_advanced": (["SSS", "CMC", "GRG"], {"S": T("solar_panel"), "C": CIRCUIT, "M": FRAME, "G": P_GOLD, "R": REDSTONE}, "solar_panel_advanced", 1),
    "solar_panel_ultimate": (["AAA", "CMC", "DGD"], {"A": T("solar_panel_advanced"), "C": CIRCUIT, "M": FRAME, "D": DIAMOND, "G": P_GOLD}, "solar_panel_ultimate", 1),
    "battery_box_advanced": (["PCP", "BXB", "PCP"], {"P": P_GOLD, "C": CIRCUIT, "B": T("battery_advanced"), "X": T("battery_box")}, "battery_box_advanced", 1),
    "battery_box_ultimate": (["DCD", "BXB", "DCD"], {"D": DIAMOND, "C": CIRCUIT, "B": T("battery_ultimate"), "X": T("battery_box_advanced")}, "battery_box_ultimate", 1),
    "battery_basic": ([" C ", "PRP", "PRP"], {"C": CIRCUIT, "P": P_IRON, "R": REDSTONE}, "battery_basic", 1),
    "battery_advanced": (["GCG", "BBB", "GRG"], {"G": P_GOLD, "C": CIRCUIT, "B": T("battery_basic"), "R": REDSTONE}, "battery_advanced", 1),
    "battery_ultimate": (["DCD", "BBB", "DRD"], {"D": DIAMOND, "C": CIRCUIT, "B": T("battery_advanced"), "R": ore("blockRedstone")}, "battery_ultimate", 1),
    "electric_drill": (["PDP", "ICI", " B "], {"P": P_IRON, "D": DIAMOND, "I": IRON, "C": CIRCUIT, "B": T("battery_advanced")}, "electric_drill", 1),
    "chainsaw": (["PPD", "CMB", " I "], {"P": P_IRON, "D": DIAMOND, "C": CIRCUIT, "M": FRAME, "B": T("battery_advanced"), "I": IRON}, "chainsaw", 1),
    "tech_wrench": (["I I", "IMI", " I "], {"I": IRON, "M": FRAME}, "tech_wrench", 1),
    "energy_meter": (["R R", "ICI", " I "], {"R": REDSTONE, "I": IRON, "C": CIRCUIT}, "energy_meter", 1),
}
for name, (pattern, key, result, count) in recipes.items():
    w(f"recipes/{name}.json", {"type": "forge:ore_shaped", "pattern": pattern, "key": key,
                               "result": {"item": f"techcraft:{result}", "count": count}})
