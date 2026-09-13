#!/usr/bin/env python3
"""Genere les fichiers qui remplacent des donnees vanilla.

Usage : python3 tools/gen_vanilla_overrides.py

Ces fichiers ecrasent ceux du jeu (meme identifiant, namespace minecraft) : ils
sont recopies en entier, pas fusionnes. Les reglages sont en tete de fichier.

Deux choses ici :
  1. l'enchantement Fente (lunge) des lances, pour qu'il propulse aussi vers
     le haut ;
  2. les filons de diamant, pour en avoir moins a l'air libre dans les grottes.
"""

import json
from pathlib import Path

# ----------------------------------------------------------------------
# 1. Enchantement Fente (lunge) des lances
# ----------------------------------------------------------------------

# Multiplicateurs appliques a la direction du regard du joueur.
# Vanilla : [1.0, 0.0, 1.0] - la composante verticale est ecrasee, d'ou une
# propulsion toujours horizontale. Mettre 1.0 en Y suit le regard : viser le
# ciel projette en l'air, viser le sol plaque au sol.
LUNGE_SCALE_X = 1.0
LUNGE_SCALE_Y = 1.0
LUNGE_SCALE_Z = 1.0

# Poussee verticale ajoutee en plus, quel que soit le regard (0 = aucune).
# 0.35 donne un petit saut meme en visant droit devant.
LUNGE_UPWARD_BIAS = 0.35

# Puissance de la propulsion. Vanilla : 0.458 au niveau I, +0.458 par niveau.
LUNGE_MAGNITUDE_BASE = 0.458
LUNGE_MAGNITUDE_PER_LEVEL = 0.458

# Conditions d'utilisation. Vanilla interdit les trois.
LUNGE_ALLOW_WHILE_RIDING = False
LUNGE_ALLOW_WHILE_GLIDING = True    # True = utilisable en elytres
LUNGE_ALLOW_IN_WATER = False

# Cout en nourriture (exhaustion) par niveau.
LUNGE_EXHAUSTION_BASE = 4.0
LUNGE_EXHAUSTION_PER_LEVEL = 4.0

# ----------------------------------------------------------------------
# 2. Diamants a l'air libre
# ----------------------------------------------------------------------

# Reduction de la quantite de minerai de diamant expose a l'air dans les
# grottes. 0.35 = -35%. 0 = comportement vanilla, 1 = plus aucun diamant
# visible sans creuser.
DIAMOND_AIR_EXPOSURE_REDUCTION = 0.35

# Multiplicateur applique a la QUANTITE de diamant generee, toutes veines
# confondues (visible ou enterree).
#   1.0 = vanilla
#   0.5 = deux fois moins de diamant
#   2.0 = deux fois plus
# Attention : les veines "count" se multiplient directement, les veines
# "rarity_filter" fonctionnent a l'envers (une chance sur N), le script fait
# la conversion tout seul.
DIAMOND_FREQUENCY_MULTIPLIER = 1.0

# Les filons vanilla et leur taux d'abandon d'origine. "buried" est deja a
# 1.0 (jamais visible), il n'a pas besoin d'etre touche.
DIAMOND_VEINS = {
    "ore_diamond_small": (0.5, 4),
    "ore_diamond_medium": (0.5, 8),
    "ore_diamond_large": (0.7, 12),
}

# Les placements vanilla des quatre veines de diamant : soit un nombre de
# tentatives par chunk (count), soit une chance sur N (rarity_filter).
DIAMOND_PLACEMENTS = {
    "ore_diamond": ("count", 7, "ore_diamond_small", ("trapezoid", 80, -80)),
    "ore_diamond_buried": ("count", 4, "ore_diamond_buried", ("trapezoid", 80, -80)),
    "ore_diamond_medium": ("count", 2, "ore_diamond_medium", ("uniform", -4, -64)),
    "ore_diamond_large": ("rarity_filter", 9, "ore_diamond_large", ("trapezoid", 80, -80)),
}

ROOT = Path(__file__).resolve().parent.parent
DATA = ROOT / "src/main/resources/data/minecraft"


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent="\t") + "\n")
    print("ecrit", path.relative_to(ROOT))


def entity_flag(flag, value):
    return {
        "condition": "minecraft:entity_properties",
        "entity": "this",
        "predicate": {"minecraft:flags": {flag: value}},
    }


def lunge():
    requirements = []

    if not LUNGE_ALLOW_WHILE_RIDING:
        requirements.append({
            "condition": "minecraft:inverted",
            "term": {
                "condition": "minecraft:entity_properties",
                "entity": "this",
                "predicate": {"minecraft:vehicle": {}},
            },
        })

    if not LUNGE_ALLOW_WHILE_GLIDING:
        requirements.append(entity_flag("is_fall_flying", False))

    if not LUNGE_ALLOW_IN_WATER:
        requirements.append(entity_flag("is_in_water", False))

    # Vanilla : les mobs et les joueurs en creatif ne sont pas soumis a la
    # condition de nourriture.
    requirements.append({
        "condition": "minecraft:any_of",
        "terms": [
            {
                "condition": "minecraft:inverted",
                "term": {
                    "condition": "minecraft:entity_properties",
                    "entity": "this",
                    "predicate": {"minecraft:type_specific/player": {}},
                },
            },
            {
                "condition": "minecraft:entity_properties",
                "entity": "this",
                "predicate": {"minecraft:type_specific/player": {"gamemode": ["creative"]}},
            },
            {
                "condition": "minecraft:entity_properties",
                "entity": "this",
                "predicate": {"minecraft:type_specific/player": {"food": {"level": {"min": 7}}}},
            },
        ],
    })

    return {
        "anvil_cost": 2,
        "description": {"translate": "enchantment.minecraft.lunge"},
        "effects": {
            "minecraft:post_piercing_attack": [
                {
                    "effect": {
                        "type": "minecraft:all_of",
                        "effects": [
                            {"type": "minecraft:change_item_damage", "amount": 1.0},
                            {
                                "type": "minecraft:apply_exhaustion",
                                "amount": {
                                    "type": "minecraft:linear",
                                    "base": LUNGE_EXHAUSTION_BASE,
                                    "per_level_above_first": LUNGE_EXHAUSTION_PER_LEVEL,
                                },
                            },
                            {
                                "type": "minecraft:apply_impulse",
                                "coordinate_scale": [LUNGE_SCALE_X, LUNGE_SCALE_Y, LUNGE_SCALE_Z],
                                "direction": [0.0, LUNGE_UPWARD_BIAS, 1.0],
                                "magnitude": {
                                    "type": "minecraft:linear",
                                    "base": LUNGE_MAGNITUDE_BASE,
                                    "per_level_above_first": LUNGE_MAGNITUDE_PER_LEVEL,
                                },
                            },
                            {
                                "type": "minecraft:play_sound",
                                "pitch": 1.0,
                                "sound": [
                                    "minecraft:item.spear.lunge_1",
                                    "minecraft:item.spear.lunge_2",
                                    "minecraft:item.spear.lunge_3",
                                ],
                                "volume": 1.0,
                            },
                        ],
                    },
                    "requirements": {"condition": "minecraft:all_of", "terms": requirements},
                }
            ]
        },
        "max_cost": {"base": 25, "per_level_above_first": 8},
        "max_level": 3,
        "min_cost": {"base": 5, "per_level_above_first": 8},
        "slots": ["hand"],
        "supported_items": "#minecraft:enchantable/lunge",
        "weight": 5,
    }


def diamond_vein(discard, size):
    # On garde (1 - discard) du minerai expose ; on reduit cette part.
    kept = (1.0 - discard) * (1.0 - DIAMOND_AIR_EXPOSURE_REDUCTION)
    return {
        "type": "minecraft:ore",
        "config": {
            "discard_chance_on_air_exposure": round(1.0 - kept, 4),
            "size": size,
            "targets": [
                {
                    "state": {"Name": "minecraft:diamond_ore"},
                    "target": {
                        "predicate_type": "minecraft:tag_match",
                        "tag": "minecraft:stone_ore_replaceables",
                    },
                },
                {
                    "state": {"Name": "minecraft:deepslate_diamond_ore"},
                    "target": {
                        "predicate_type": "minecraft:tag_match",
                        "tag": "minecraft:deepslate_ore_replaceables",
                    },
                },
            ],
        },
    }


def diamond_placement(kind, value, feature, height):
    """Recopie le placement vanilla d'une veine en appliquant le multiplicateur."""
    if kind == "count":
        # Plus de tentatives = plus de minerai.
        amount = max(1, round(value * DIAMOND_FREQUENCY_MULTIPLIER))
        first = {"type": "minecraft:count", "count": amount}
    else:
        # rarity_filter : une chance sur N, donc N plus GRAND = plus rare.
        chance = max(1, round(value / max(0.01, DIAMOND_FREQUENCY_MULTIPLIER)))
        first = {"type": "minecraft:rarity_filter", "chance": chance}

    shape, top, bottom = height
    if shape == "trapezoid":
        range_ = {"type": "minecraft:trapezoid",
                  "max_inclusive": {"above_bottom": top},
                  "min_inclusive": {"above_bottom": bottom}}
    else:
        range_ = {"type": "minecraft:uniform",
                  "max_inclusive": {"absolute": top},
                  "min_inclusive": {"absolute": bottom}}

    return {
        "feature": f"minecraft:{feature}",
        "placement": [
            first,
            {"type": "minecraft:in_square"},
            {"type": "minecraft:height_range", "height": range_},
            {"type": "minecraft:biome"},
        ],
    }


def main():
    write_json(DATA / "enchantment/lunge.json", lunge())

    for name, (discard, size) in DIAMOND_VEINS.items():
        write_json(DATA / f"worldgen/configured_feature/{name}.json", diamond_vein(discard, size))

    for name, (kind, value, feature, height) in DIAMOND_PLACEMENTS.items():
        write_json(DATA / f"worldgen/placed_feature/{name}.json",
                   diamond_placement(kind, value, feature, height))


if __name__ == "__main__":
    main()
