#!/usr/bin/env python3
"""Static checks on resources: every referenced model, texture, recipe item and lang key must exist."""
import json, os, re, sys

root = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(__file__), "..")
A = os.path.join(root, "src/main/resources/assets/techcraft")
errors = []


def load(path):
    with open(path) as f:
        return json.load(f)


def names(sub):
    d = os.path.join(A, sub)
    return {os.path.splitext(n)[0] for n in os.listdir(d)} if os.path.isdir(d) else set()


def texture_exists(ref):
    ns, _, path = ref.partition(":")
    if not path:
        ns, path = "minecraft", ns
    return ns != "techcraft" or os.path.isfile(os.path.join(A, "textures", path + ".png"))


def model_exists(ref):
    ns, _, path = ref.partition(":")
    if not path:
        ns, path = "minecraft", ns
    return ns != "techcraft" or os.path.isfile(os.path.join(A, "models", path + ".json")) or \
        os.path.isfile(os.path.join(A, "models/block", path + ".json"))


block_models = names("models/block")
item_models = names("models/item")
blockstates = names("blockstates")

for name in sorted(block_models):
    for key, ref in load(f"{A}/models/block/{name}.json").get("textures", {}).items():
        if not ref.startswith("#") and not texture_exists(ref):
            errors.append(f"models/block/{name}: missing texture {ref}")
for name in sorted(item_models):
    data = load(f"{A}/models/item/{name}.json")
    for ref in data.get("textures", {}).values():
        if not texture_exists(ref):
            errors.append(f"models/item/{name}: missing texture {ref}")
    parent = data.get("parent", "")
    if parent.startswith("techcraft:") and not model_exists(parent):
        errors.append(f"models/item/{name}: missing parent {parent}")
for name in sorted(blockstates):
    for variant, v in load(f"{A}/blockstates/{name}.json").get("variants", {}).items():
        models = v if isinstance(v, list) else [v]
        for m in models:
            ref = m["model"]
            if ref.startswith("techcraft:") and ref.split(":")[1] not in block_models:
                errors.append(f"blockstates/{name} {variant}: missing model {ref}")
    if name not in item_models:
        errors.append(f"blockstates/{name}: no item model")

lang_files = {}
for lang in ("en_us", "ru_ru"):
    keys = {}
    for line in open(f"{A}/lang/{lang}.lang", encoding="utf-8"):
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            k, v = line.split("=", 1)
            keys[k] = v
    lang_files[lang] = keys
    for name in sorted(item_models):
        key = ("tile" if name in blockstates else "item") + f".techcraft.{name}.name"
        if key not in keys:
            errors.append(f"lang/{lang}: missing {key}")
    for k, v in keys.items():
        if "%" in v and "%s" not in v and "%d" not in v:
            errors.append(f"lang/{lang}: suspicious format in {k}")
if set(lang_files["en_us"]) != set(lang_files["ru_ru"]):
    diff = set(lang_files["en_us"]) ^ set(lang_files["ru_ru"])
    errors.append(f"lang files differ in keys: {sorted(diff)}")

for name in sorted(names("recipes")):
    r = load(f"{A}/recipes/{name}.json")
    result = r["result"]["item"] if isinstance(r["result"], dict) else r["result"]
    refs = [result]
    for ing in r.get("key", {}).values():
        for i in (ing if isinstance(ing, list) else [ing]):
            if "item" in i:
                refs.append(i["item"])
    for ing in r.get("ingredients", []):
        if "item" in ing:
            refs.append(ing["item"])
    for ref in refs:
        if ref.startswith("techcraft:") and ref.split(":")[1] not in item_models:
            errors.append(f"recipes/{name}: unknown item {ref}")
    for row in r.get("pattern", []):
        if len(row) > 3:
            errors.append(f"recipes/{name}: row too wide {row!r}")
    used = set("".join(r.get("pattern", []))) - {" "}
    if used != set(r.get("key", {})):
        errors.append(f"recipes/{name}: pattern symbols {sorted(used)} don't match keys {sorted(r.get('key', {}))}")

if errors:
    print("\n".join(errors))
    sys.exit(1)
print(f"assets ok: {len(block_models)} block models, {len(item_models)} item models, "
      f"{len(blockstates)} blockstates, {len(names('recipes'))} recipes")
