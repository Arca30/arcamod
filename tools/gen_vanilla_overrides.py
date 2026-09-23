#!/usr/bin/env python3
"""Genere les fichiers qui remplacent des donnees vanilla.

Usage : python3 tools/gen_vanilla_overrides.py [--jar chemin/minecraft.jar]

PRINCIPE (change depuis 26.3) : au lieu de recopier a la main une version
figee du fichier vanilla, le script LIT le fichier dans le jar du jeu et n'y
change QUE les reglages ci-dessous. Tout le reste (nouvelles cibles de
minerai, nouveaux effets, nouveaux couts...) suit donc automatiquement la
version de Minecraft, meme apres une mise a jour.

Le jar est cherche dans le cache de Loom, pour la version indiquee dans
gradle.properties. Relancer ce script apres chaque montee de version.

Trois choses ici :
  1. l'enchantement Fente (lunge) des lances, pour qu'il propulse aussi vers
     le haut ;
  2. Puissance et Recul, pour elargir les objets qui les acceptent ;
  3. les filons de diamant, pour en avoir moins a l'air libre dans les
     grottes.
"""

import argparse
import json
import re
import sys
import zipfile
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

# Puissance de la propulsion. None = garder la valeur vanilla.
LUNGE_MAGNITUDE_BASE = None
LUNGE_MAGNITUDE_PER_LEVEL = None

# Cout en nourriture (exhaustion) par niveau. None = valeur vanilla.
LUNGE_EXHAUSTION_BASE = None
LUNGE_EXHAUSTION_PER_LEVEL = None

# Conditions d'utilisation. Vanilla interdit les trois.
LUNGE_ALLOW_WHILE_RIDING = False
LUNGE_ALLOW_WHILE_GLIDING = True    # True = utilisable en elytres
LUNGE_ALLOW_IN_WATER = False

# Le drapeau d'entite qui correspond a chaque autorisation : c'est lui qu'on
# retire des conditions vanilla quand l'action devient permise.
LUNGE_FLAG_BY_ALLOW = {
    "is_fall_flying": LUNGE_ALLOW_WHILE_GLIDING,
    "is_in_water": LUNGE_ALLOW_IN_WATER,
}

# ----------------------------------------------------------------------
# 2. Objets acceptes par des enchantements vanilla
# ----------------------------------------------------------------------

# Remplace le tag "supported_items". Le reste de l'enchantement ne bouge pas.
ENCHANTMENT_SUPPORTED_ITEMS = {
    "power": "#arcamod:enchantable/power",
    "knockback": "#arcamod:enchantable/knockback",
}

# ----------------------------------------------------------------------
# 3. Diamants a l'air libre
# ----------------------------------------------------------------------

# Reduction de la quantite de minerai de diamant expose a l'air dans les
# grottes. 0.35 = -35%. 0 = comportement vanilla, 1 = plus aucun diamant
# visible sans creuser.
DIAMOND_AIR_EXPOSURE_REDUCTION = 0.35

# Multiplicateur applique a la QUANTITE de diamant generee, toutes veines
# confondues (visible ou enterree).
#   1.0 = vanilla (aucun fichier de placement n'est alors ecrit)
#   0.5 = deux fois moins de diamant
#   2.0 = deux fois plus
# Les veines "count" se multiplient directement, les veines "rarity_filter"
# fonctionnent a l'envers (une chance sur N) : le script fait la conversion.
DIAMOND_FREQUENCY_MULTIPLIER = 1.0

# Les filons retouches. "ore_diamond_buried" est deja a 1.0 (jamais visible),
# il n'a pas besoin d'etre touche.
DIAMOND_VEINS = ["ore_diamond_small", "ore_diamond_medium", "ore_diamond_large"]

# Les placements de diamant, uniquement utiles si le multiplicateur n'est
# pas a 1.0.
DIAMOND_PLACEMENTS = ["ore_diamond", "ore_diamond_buried", "ore_diamond_medium", "ore_diamond_large"]

ROOT = Path(__file__).resolve().parent.parent
DATA = ROOT / "src/main/resources/data/minecraft"


# ----------------------------------------------------------------------
# Lecture du jar vanilla
# ----------------------------------------------------------------------

def minecraft_version():
    text = (ROOT / "gradle.properties").read_text()
    match = re.search(r"^minecraft_version\s*=\s*(\S+)", text, re.M)

    if not match:
        sys.exit("minecraft_version introuvable dans gradle.properties")

    return match.group(1)


def find_jar(explicit):
    if explicit:
        return Path(explicit)

    version = minecraft_version()
    cache = Path.home() / ".gradle/caches/fabric-loom" / version

    for name in ("minecraft-client.jar", "minecraft-common.jar", "minecraft-server.jar"):
        jar = cache / name

        if jar.exists():
            return jar

    sys.exit(f"Jar de Minecraft {version} introuvable dans {cache}.\n"
             "Lance une fois le jeu (ou une tache Gradle) pour remplir le cache,\n"
             "ou passe --jar chemin/vers/minecraft.jar")


class Vanilla:
    """Les fichiers de donnees du jeu, lus dans le jar."""

    def __init__(self, jar):
        self.jar = zipfile.ZipFile(jar)
        self.name = jar

    def load(self, path):
        try:
            return json.loads(self.jar.read(f"data/minecraft/{path}"))
        except KeyError:
            sys.exit(f"{path} n'existe pas dans {self.name} : le nom a change avec la version ?")


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent="\t") + "\n")
    print("ecrit", path.relative_to(ROOT))


def remove(path):
    if path.exists():
        path.unlink()
        print("retire (identique au vanilla)", path.relative_to(ROOT))


# ----------------------------------------------------------------------
# Retouches
# ----------------------------------------------------------------------

def find_effect(enchantment, effect_type):
    """Cherche un effet par son type, a n'importe quelle profondeur."""
    found = []

    def walk(node):
        if isinstance(node, dict):
            if node.get("type") == effect_type:
                found.append(node)

            for value in node.values():
                walk(value)
        elif isinstance(node, list):
            for value in node:
                walk(value)

    walk(enchantment)
    return found


def is_flag_term(term, flag):
    """Vrai si la condition vanilla teste ce drapeau d'entite a false."""
    predicate = term.get("predicate", {}) if isinstance(term, dict) else {}
    return flag in predicate.get("minecraft:flags", {})


def lunge(vanilla):
    data = vanilla.load("enchantment/lunge.json")

    for impulse in find_effect(data, "minecraft:apply_impulse"):
        impulse["coordinate_scale"] = [LUNGE_SCALE_X, LUNGE_SCALE_Y, LUNGE_SCALE_Z]
        direction = impulse.get("direction", [0.0, 0.0, 1.0])
        impulse["direction"] = [direction[0], LUNGE_UPWARD_BIAS, direction[2]]

        if LUNGE_MAGNITUDE_BASE is not None:
            impulse["magnitude"]["base"] = LUNGE_MAGNITUDE_BASE

        if LUNGE_MAGNITUDE_PER_LEVEL is not None:
            impulse["magnitude"]["per_level_above_first"] = LUNGE_MAGNITUDE_PER_LEVEL

    for exhaustion in find_effect(data, "minecraft:apply_exhaustion"):
        if LUNGE_EXHAUSTION_BASE is not None:
            exhaustion["amount"]["base"] = LUNGE_EXHAUSTION_BASE

        if LUNGE_EXHAUSTION_PER_LEVEL is not None:
            exhaustion["amount"]["per_level_above_first"] = LUNGE_EXHAUSTION_PER_LEVEL

    # Les conditions : on RETIRE celles que le mod autorise, sans toucher aux
    # autres (nourriture, creatif, et tout ce que le vanilla ajoutera).
    for entry in data["effects"].get("minecraft:post_piercing_attack", []):
        requirements = entry.get("requirements")

        if not isinstance(requirements, dict) or "terms" not in requirements:
            continue

        kept = []

        for term in requirements["terms"]:
            if any(allowed and is_flag_term(term, flag) for flag, allowed in LUNGE_FLAG_BY_ALLOW.items()):
                continue

            if LUNGE_ALLOW_WHILE_RIDING and term.get("type") == "minecraft:inverted" \
                    and "minecraft:vehicle" in term.get("term", {}).get("predicate", {}):
                continue

            kept.append(term)

        requirements["terms"] = kept

    return data


def supported_items(vanilla, name, tag):
    data = vanilla.load(f"enchantment/{name}.json")
    data["supported_items"] = tag
    return data


def diamond_vein(vanilla, name):
    """Le filon vanilla, avec le seul taux d'abandon retouche."""
    data = vanilla.load(f"worldgen/feature/{name}.json")
    discard = data.get("discard_chance_on_air_exposure", 0.0)

    # On garde (1 - discard) du minerai expose ; on reduit cette part.
    kept = (1.0 - discard) * (1.0 - DIAMOND_AIR_EXPOSURE_REDUCTION)
    data["discard_chance_on_air_exposure"] = round(1.0 - kept, 4)
    return data


def diamond_placement(vanilla, name):
    """Le placement vanilla, avec la seule frequence retouchee."""
    data = vanilla.load(f"worldgen/placed_feature/{name}.json")

    for step in data.get("placement", []):
        if step.get("type") == "minecraft:count" and isinstance(step.get("count"), int):
            # Plus de tentatives = plus de minerai.
            step["count"] = max(1, round(step["count"] * DIAMOND_FREQUENCY_MULTIPLIER))
        elif step.get("type") == "minecraft:rarity_filter":
            # une chance sur N, donc N plus GRAND = plus rare.
            step["chance"] = max(1, round(step["chance"] / max(0.01, DIAMOND_FREQUENCY_MULTIPLIER)))

    return data


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", help="jar de Minecraft a lire (defaut : cache de Loom)")
    args = parser.parse_args()

    jar = find_jar(args.jar)
    print("vanilla lu dans", jar)
    vanilla = Vanilla(jar)

    write_json(DATA / "enchantment/lunge.json", lunge(vanilla))

    for name, tag in ENCHANTMENT_SUPPORTED_ITEMS.items():
        write_json(DATA / f"enchantment/{name}.json", supported_items(vanilla, name, tag))

    for name in DIAMOND_VEINS:
        write_json(DATA / f"worldgen/feature/{name}.json", diamond_vein(vanilla, name))

    for name in DIAMOND_PLACEMENTS:
        path = DATA / f"worldgen/placed_feature/{name}.json"

        # A 1.0, le fichier serait une copie conforme du vanilla : autant ne
        # pas l'ecrire du tout, sinon il fige la version du jeu.
        if DIAMOND_FREQUENCY_MULTIPLIER == 1.0:
            remove(path)
        else:
            write_json(path, diamond_placement(vanilla, name))


if __name__ == "__main__":
    main()
