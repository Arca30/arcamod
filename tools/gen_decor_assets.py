#!/usr/bin/env python3
"""Genere les ressources des escaliers, dalles et blocs de chaume.

Usage : python3 tools/gen_decor_assets.py

Ce script est le pendant de ModDecorBlocks : la liste des matieres doit etre
la meme des deux cotes. Il ecrit blockstates, modeles, definitions d'items,
tables de butin, recettes, tags et traductions.

Il lit le blockstate vanilla des escaliers de chene comme gabarit (40 entrees
avec leurs rotations) : inutile de le reecrire a la main.
"""

import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/arcamod"
DATA = ROOT / "src/main/resources/data"

CLIENT_JAR = Path.home() / ".gradle/caches/fabric-loom/26.2/minecraft-client.jar"

DYE_COLORS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
              "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"]

# Les blocs vanilla qui recoivent escaliers et dalles.
VANILLA_BASES = (["terracotta"]
                 + [c + "_terracotta" for c in DYE_COLORS]
                 + [c + "_concrete_powder" for c in DYE_COLORS]
                 + ["moss_block", "hay_block"])

# Les quatre etapes du chaume, de la plus jaune a la plus grise.
THATCH_STAGES = ["thatch_block", "weathered_thatch_block", "aged_thatch_block", "gray_thatch_block"]

# Blocs "colonne" : leur dessus n'a pas la meme texture que leurs cotes.
PILLARS = {"hay_block"} | set(THATCH_STAGES)

# Outil attendu, par famille (pour les tags mineable/*).
def tool_for(base):
    if base.endswith("terracotta") or base.endswith("concrete_powder"):
        return "pickaxe"
    if base == "moss_block":
        return "hoe"
    return "hoe"  # paille et chaume


def textures_of(base):
    if base in PILLARS:
        namespace = "arcamod" if base in THATCH_STAGES else "minecraft"
        top = f"{namespace}:block/{base}_top"
        side = f"{namespace}:block/{base}_side"
        return {"bottom": top, "top": top, "side": side}

    full = f"minecraft:block/{base}"
    return {"bottom": full, "top": full, "side": full}


def full_block_model(base):
    """Le modele du bloc plein, utilise par la double dalle."""
    if base in THATCH_STAGES:
        return f"arcamod:block/{base}"
    return f"minecraft:block/{base}"


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent="\t") + "\n")


def retarget(template, old, new):
    """Recopie un blockstate vanilla en changeant le chemin des modeles."""
    text = json.dumps(template)
    return json.loads(text.replace(old, new))


def add_waxed_states(blockstate):
    """Duplique chaque variante en version ciree et non ciree.

    La cire ne change rien a l'apparence, mais Minecraft exige que TOUTES les
    combinaisons d'etats soient decrites, sinon il rale au chargement.
    """
    variants = {}

    for key, value in blockstate["variants"].items():
        for waxed in ("false", "true"):
            suffix = f"waxed={waxed}"
            variants[f"{key},{suffix}" if key else suffix] = value

    return {"variants": variants}


def load_vanilla_blockstate(name):
    with zipfile.ZipFile(CLIENT_JAR) as jar:
        with jar.open(f"assets/minecraft/blockstates/{name}.json") as handle:
            return json.load(handle)


def generate_family(base, stairs_template, slab_template, lang_en, lang_fr, tags):
    textures = textures_of(base)

    # --- escaliers : trois modeles (droit, interieur, exterieur) ---
    for suffix, parent in (("", "minecraft:block/stairs"),
                           ("_inner", "minecraft:block/inner_stairs"),
                           ("_outer", "minecraft:block/outer_stairs")):
        write_json(ASSETS / f"models/block/{base}_stairs{suffix}.json",
                   {"parent": parent, "textures": textures})

    stairs_state = retarget(stairs_template, "minecraft:block/oak_stairs", f"arcamod:block/{base}_stairs")

    if base in THATCH_STAGES:
        stairs_state = add_waxed_states(stairs_state)

    write_json(ASSETS / f"blockstates/{base}_stairs.json", stairs_state)

    # --- dalles : basse, haute, et le bloc plein pour la double ---
    write_json(ASSETS / f"models/block/{base}_slab.json",
               {"parent": "minecraft:block/slab", "textures": textures})
    write_json(ASSETS / f"models/block/{base}_slab_top.json",
               {"parent": "minecraft:block/slab_top", "textures": textures})

    slab_state = retarget(slab_template, "minecraft:block/oak_slab", f"arcamod:block/{base}_slab")
    slab_state["variants"]["type=double"] = {"model": full_block_model(base)}

    if base in THATCH_STAGES:
        slab_state = add_waxed_states(slab_state)

    write_json(ASSETS / f"blockstates/{base}_slab.json", slab_state)

    for kind in ("stairs", "slab"):
        name = f"{base}_{kind}"
        write_json(ASSETS / f"models/item/{name}.json", {"parent": f"arcamod:block/{name}"})
        write_json(ASSETS / f"items/{name}.json",
                   {"model": {"type": "minecraft:model", "model": f"arcamod:block/{name}"}})

        # butin : les escaliers se ramassent tels quels, la double dalle rend
        # bien ses deux morceaux.
        entry = {"type": "minecraft:item", "name": f"arcamod:{name}"}
        if kind == "slab":
            entry["functions"] = [{
                "function": "minecraft:set_count",
                "count": 2.0,
                "add": False,
                "conditions": [{
                    "condition": "minecraft:block_state_property",
                    "block": f"arcamod:{name}",
                    "properties": {"type": "double"},
                }],
            }, {"function": "minecraft:explosion_decay"}]
        else:
            entry["functions"] = [{"function": "minecraft:explosion_decay"}]

        write_json(DATA / f"arcamod/loot_table/blocks/{name}.json", {
            "type": "minecraft:block",
            "pools": [{"rolls": 1.0, "entries": [entry]}],
            "random_sequence": f"arcamod:blocks/{name}",
        })

        tags[f"minecraft:{kind}"].append(f"arcamod:{name}")
        tags[f"mineable/{tool_for(base)}"].append(f"arcamod:{name}")

    # --- recettes : 6 blocs -> 4 escaliers, 3 blocs -> 6 dalles ---
    ingredient = f"arcamod:{base}" if base in THATCH_STAGES else f"minecraft:{base}"

    write_json(DATA / f"arcamod/recipe/{base}_stairs.json", {
        "type": "minecraft:crafting_shaped",
        "category": "building",
        "key": {"#": ingredient},
        "pattern": ["#  ", "## ", "###"],
        "result": {"id": f"arcamod:{base}_stairs", "count": 4},
    })
    write_json(DATA / f"arcamod/recipe/{base}_slab.json", {
        "type": "minecraft:crafting_shaped",
        "category": "building",
        "key": {"#": ingredient},
        "pattern": ["###"],
        "result": {"id": f"arcamod:{base}_slab", "count": 6},
    })

    pretty = base.replace("_", " ").title()
    lang_en[f"block.arcamod.{base}_stairs"] = f"{pretty} Stairs"
    lang_en[f"block.arcamod.{base}_slab"] = f"{pretty} Slab"

    pretty_fr = FRENCH.get(base, pretty)
    lang_fr[f"block.arcamod.{base}_stairs"] = f"Escalier en {pretty_fr}"
    lang_fr[f"block.arcamod.{base}_slab"] = f"Dalle de {pretty_fr}"


FRENCH_COLOURS = {
    "white": "blanche", "orange": "orange", "magenta": "magenta", "light_blue": "bleu clair",
    "yellow": "jaune", "lime": "vert clair", "pink": "rose", "gray": "grise",
    "light_gray": "gris clair", "cyan": "cyan", "purple": "violette", "blue": "bleue",
    "brown": "brune", "green": "verte", "red": "rouge", "black": "noire",
}

FRENCH = {"terracotta": "terre cuite"}
for colour, french in FRENCH_COLOURS.items():
    FRENCH[colour + "_terracotta"] = f"terre cuite {french}"
    FRENCH[colour + "_concrete_powder"] = f"poudre de beton {french}"
FRENCH["moss_block"] = "mousse"
FRENCH["hay_block"] = "paille"
FRENCH["thatch_block"] = "chaume"
FRENCH["weathered_thatch_block"] = "chaume patine"
FRENCH["aged_thatch_block"] = "chaume vieilli"
FRENCH["gray_thatch_block"] = "chaume gris"


def generate_thatch_blocks(lang_en, lang_fr, tags):
    """Les quatre blocs pleins de chaume (les escaliers sont faits a part)."""
    for stage in THATCH_STAGES:
        write_json(ASSETS / f"models/block/{stage}.json", {
            "parent": "minecraft:block/cube_column",
            "textures": {"end": f"arcamod:block/{stage}_top", "side": f"arcamod:block/{stage}_side"},
        })
        write_json(ASSETS / f"models/block/{stage}_horizontal.json", {
            "parent": "minecraft:block/cube_column_horizontal",
            "textures": {"end": f"arcamod:block/{stage}_top", "side": f"arcamod:block/{stage}_side"},
        })
        write_json(ASSETS / f"blockstates/{stage}.json", add_waxed_states({"variants": {
            "axis=x": {"model": f"arcamod:block/{stage}_horizontal", "x": 90, "y": 90},
            "axis=y": {"model": f"arcamod:block/{stage}"},
            "axis=z": {"model": f"arcamod:block/{stage}_horizontal", "x": 90},
        }}))
        write_json(ASSETS / f"models/item/{stage}.json", {"parent": f"arcamod:block/{stage}"})
        write_json(ASSETS / f"items/{stage}.json",
                   {"model": {"type": "minecraft:model", "model": f"arcamod:block/{stage}"}})
        write_json(DATA / f"arcamod/loot_table/blocks/{stage}.json", {
            "type": "minecraft:block",
            "pools": [{"rolls": 1.0, "entries": [{
                "type": "minecraft:item",
                "name": f"arcamod:{stage}",
                "functions": [{"function": "minecraft:explosion_decay"}],
            }]}],
            "random_sequence": f"arcamod:blocks/{stage}",
        })
        tags["mineable/hoe"].append(f"arcamod:{stage}")

        pretty = stage.replace("_", " ").title()
        lang_en[f"block.arcamod.{stage}"] = pretty
        lang_fr[f"block.arcamod.{stage}"] = FRENCH[stage].capitalize()


def main():
    stairs_template = load_vanilla_blockstate("oak_stairs")
    slab_template = load_vanilla_blockstate("oak_slab")

    lang_en, lang_fr = {}, {}
    tags = {"minecraft:stairs": [], "minecraft:slab": [], "mineable/pickaxe": [], "mineable/hoe": []}

    generate_thatch_blocks(lang_en, lang_fr, tags)

    for base in VANILLA_BASES + THATCH_STAGES:
        generate_family(base, stairs_template, slab_template, lang_en, lang_fr, tags)

    # --- tags vanilla auxquels on s'ajoute ---
    write_json(DATA / "minecraft/tags/block/stairs.json", {"values": tags["minecraft:stairs"]})
    write_json(DATA / "minecraft/tags/block/slabs.json", {"values": tags["minecraft:slab"]})
    write_json(DATA / "minecraft/tags/item/stairs.json", {"values": tags["minecraft:stairs"]})
    write_json(DATA / "minecraft/tags/item/slabs.json", {"values": tags["minecraft:slab"]})

    for tool in ("pickaxe", "hoe"):
        path = DATA / f"minecraft/tags/block/mineable/{tool}.json"
        existing = json.loads(path.read_text())["values"] if path.exists() else []
        merged = [v for v in existing if not v.startswith("arcamod:")] + sorted(set(tags[f"mineable/{tool}"]))
        write_json(path, {"values": merged})

    # --- traductions ---
    for name, extra in (("en_us.json", lang_en), ("fr_fr.json", lang_fr)):
        path = ASSETS / "lang" / name
        data = json.loads(path.read_text())
        data.update(extra)
        path.write_text(json.dumps(dict(sorted(data.items())), indent="\t", ensure_ascii=False) + "\n")

    print(f"{len(VANILLA_BASES + THATCH_STAGES)} familles d'escaliers et dalles")
    print(f"{len(THATCH_STAGES)} blocs de chaume")


if __name__ == "__main__":
    main()
