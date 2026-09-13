#!/usr/bin/env python3
"""Genere les modeles 3D des cailloux et des branches au sol.

Usage : python3 tools/gen_ground_clutter_models.py

Le script ecrase toujours ses propres fichiers (models/block/pebbles_*.json,
models/block/fallen_sticks_*.json et les deux blockstates) : ce sont des
fichiers generes, ne les modifie pas a la main.

=== LES REGLAGES SONT JUSTE EN DESSOUS ===

Chaque caillou est une petite boite : cote tire entre MIN_SIZE et MAX_SIZE
pixels, hauteur entre MIN_HEIGHT et MAX_HEIGHT, texture tiree parmi celles de
TEXTURES, le tout pose a plat sur le sol et tourne d'un angle au hasard.

Comme un modele est un fichier fige, l'alea est "pre-tire" : on genere
VARIANTS modeles differents par quantite de cailloux, et le blockstate les
liste tous. Minecraft en choisit un selon les coordonnees du bloc, donc deux
tas voisins n'ont jamais la meme tete.

SEED rend le tirage reproductible : relancer le script sans rien changer
redonne exactement les memes fichiers. Change SEED pour repiocher un jeu de
modeles completement different.
"""

import json
import random
import re
from pathlib import Path

# ----------------------------------------------------------------------
# Reglages
# ----------------------------------------------------------------------

SEED = 20260912

# Nombre de modeles differents par quantite. Plus il y en a, plus les tas
# sont varies, mais plus il y a de fichiers (VARIANTS x MAX_COUNT par bloc).
VARIANTS = 6

# --- Cailloux ---
PEBBLE_MAX_COUNT = None       # lu dans ArcaBalance.PEBBLES_MAX
PEBBLE_MIN_SIZE = 2           # cote minimum, en pixels
PEBBLE_MAX_SIZE = 5           # cote maximum, en pixels
PEBBLE_MIN_HEIGHT = 1         # hauteur minimum, en pixels
PEBBLE_MAX_HEIGHT = 2         # hauteur maximum, en pixels
PEBBLE_TEXTURES = [
    "minecraft:block/tuff",
    "minecraft:block/diorite",
    "minecraft:block/granite",
    "minecraft:block/stone",
    "minecraft:block/andesite",
    "minecraft:block/gravel",
    "minecraft:block/mossy_cobblestone",
]

# --- Branches ---
STICK_MAX_COUNT = None        # lu dans ArcaBalance.STICKS_MAX
STICK_MIN_LENGTH = 7          # longueur minimum, en pixels
STICK_MAX_LENGTH = 13         # longueur maximum, en pixels
STICK_MIN_WIDTH = 1           # epaisseur, en pixels (1 = une brindille)
STICK_MAX_WIDTH = 1
STICK_MIN_HEIGHT = 1          # hauteur, en pixels
STICK_MAX_HEIGHT = 1
STICK_TEXTURES = [
    "minecraft:block/oak_log",
    "minecraft:block/spruce_log",
    "minecraft:block/birch_log",
    "minecraft:block/jungle_log",
    "minecraft:block/dark_oak_log",
    "minecraft:block/stripped_oak_log",
]

# Marge laissee libre sur les bords du bloc, en pixels : evite que les objets
# ne depassent sur le bloc d'a cote.
MARGIN = 1

# Angles autorises par le format de modele de Minecraft.
ANGLES = [-45, -22.5, 0, 22.5, 45]

# Distance minimale entre deux centres, en pixels : sans ca les objets se
# superposent et les faces clignotent.
MIN_SPACING = 3.0

# Decalage de hauteur ajoute a chaque objet supplementaire, en pixels.
#
# Deux objets qui se chevauchent a la MEME hauteur donnent deux faces
# exactement dans le meme plan : la carte graphique ne sait pas laquelle
# dessiner devant, et la texture papillote. Un ecart minuscule suffit a
# trancher, et reste invisible a l'oeil (0.01 px = 1/1600e de bloc).
Z_FIGHT_STEP = 0.01

# Les branches sont fines et longues : on les laisse se croiser, sinon il n'y
# a pas la place d'en poser cinq sur un bloc.
STICK_MIN_SPACING = 1.5

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / "src/main/resources/assets/arcamod"
BALANCE = ROOT / "src/main/java/dev/arca/arcamod/ArcaBalance.java"


def read_balance(constant):
    """Lit une constante entiere dans ArcaBalance.java.

    Le nombre de modeles doit toujours coller au nombre d'etats du bloc :
    plutot que de demander de tenir deux fichiers a jour, on va chercher la
    valeur a la source.
    """
    text = BALANCE.read_text()
    match = re.search(r"%s\s*=\s*(\d+)" % constant, text)

    if not match:
        raise SystemExit(f"constante {constant} introuvable dans {BALANCE}")

    return int(match.group(1))


def pick_spot(rng, width, depth, taken, spacing=MIN_SPACING):
    """Cherche une position libre pour une boite de width x depth pixels."""
    for _ in range(40):
        x = rng.randint(MARGIN, 16 - MARGIN - width)
        z = rng.randint(MARGIN, 16 - MARGIN - depth)
        cx, cz = x + width / 2, z + depth / 2

        if all((cx - ox) ** 2 + (cz - oz) ** 2 >= spacing ** 2 for ox, oz in taken):
            taken.append((cx, cz))
            return x, z

    return None


def make_element(rng, width, depth, height, textures, taken, spacing=MIN_SPACING, index=0):
    spot = pick_spot(rng, width, depth, taken, spacing)
    if spot is None:
        return None

    x, z = spot
    texture = rng.choice(textures)
    key = texture.split("/")[-1]

    # On decoupe un morceau au hasard dans la texture du bloc vanilla : deux
    # cailloux de tuff n'ont donc pas exactement les memes pixels.
    u = rng.randint(0, 16 - width)
    v = rng.randint(0, 16 - depth)

    # Chaque objet est pose un cheveu plus haut que le precedent.
    lift = round(index * Z_FIGHT_STEP, 4)

    element = {
        "from": [x, lift, z],
        "to": [x + width, round(height + lift, 4), z + depth],
        "faces": {
            "up":    {"uv": [u, v, u + width, v + depth], "texture": f"#{key}"},
            "down":  {"uv": [u, v + depth, u + width, v], "texture": f"#{key}"},
            "north": {"uv": [u, v, u + width, v + height], "texture": f"#{key}"},
            "south": {"uv": [u, v, u + width, v + height], "texture": f"#{key}"},
            "east":  {"uv": [v, u, v + depth, u + height], "texture": f"#{key}"},
            "west":  {"uv": [v, u, v + depth, u + height], "texture": f"#{key}"},
        },
    }

    angle = rng.choice(ANGLES)
    if angle:
        element["rotation"] = {"origin": [x + width / 2, lift, z + depth / 2], "axis": "y", "angle": angle}

    return element, texture, key


def build_variant(rng, count, kind):
    """Construit une serie cumulative : le modele a N objets contient les N-1
    precedents, exactement comme les bougies."""
    elements = []
    textures = {}
    taken = []

    while len(elements) < count:
        if kind == "pebble":
            width = rng.randint(PEBBLE_MIN_SIZE, PEBBLE_MAX_SIZE)
            depth = rng.randint(PEBBLE_MIN_SIZE, PEBBLE_MAX_SIZE)
            height = rng.randint(PEBBLE_MIN_HEIGHT, PEBBLE_MAX_HEIGHT)
            palette = PEBBLE_TEXTURES
        else:
            length = rng.randint(STICK_MIN_LENGTH, STICK_MAX_LENGTH)
            thickness = rng.randint(STICK_MIN_WIDTH, STICK_MAX_WIDTH)
            # une branche sur deux est posee dans l'autre sens
            width, depth = (length, thickness) if rng.random() < 0.5 else (thickness, length)
            height = rng.randint(STICK_MIN_HEIGHT, STICK_MAX_HEIGHT)
            palette = STICK_TEXTURES

        spacing = MIN_SPACING if kind == "pebble" else STICK_MIN_SPACING
        made = make_element(rng, width, depth, height, palette, taken, spacing, len(elements))

        if made is None:
            # Plus de place libre : on relache la contrainte d'espacement
            # plutot que de boucler indefiniment.
            taken.clear()
            continue

        element, texture, key = made
        elements.append(element)
        textures[key] = texture

    return elements, textures


def write_json(path, data):
    """Ecrit le JSON avec les listes de nombres sur une seule ligne : sinon un
    modele de cinq cailloux fait trois cents lignes et devient illisible."""
    text = json.dumps(data, indent="\t")
    text = re.sub(r"\[[\s\d.,\-\t\n]*?\]", lambda m: re.sub(r"\s+", " ", m.group(0)).replace("[ ", "[").replace(" ]", "]"), text)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text + "\n")


def generate(kind, name, max_count):
    rng = random.Random(f"{SEED}-{kind}")
    variants_elements = []

    # On tire d'abord la serie complete de chaque variante, puis on en garde
    # les N premiers objets : la variante 3 a N=2 est donc bien la variante 3
    # a N=1 avec un objet de plus.
    for _ in range(VARIANTS):
        variants_elements.append(build_variant(rng, max_count, kind))

    for count in range(1, max_count + 1):
        for index, (elements, _) in enumerate(variants_elements):
            kept = elements[:count]
            textures = {}

            for element in kept:
                key = element["faces"]["up"]["texture"].lstrip("#")
                palette = PEBBLE_TEXTURES if kind == "pebble" else STICK_TEXTURES
                textures[key] = next(t for t in palette if t.split("/")[-1] == key)

            model = {
                "ambientocclusion": False,
                "textures": dict(textures, particle=next(iter(textures.values()))),
                "elements": kept,
            }
            write_json(ASSETS / f"models/block/{name}_{count}_{index}.json", model)

    # Blockstate : pour chaque etat, la liste des variantes. Minecraft en
    # choisit une au hasard, mais toujours la meme pour un bloc donne.
    property_name = "pebbles" if kind == "pebble" else "sticks"
    # Les cailloux portent en plus "natural", qui ne change rien au modele
    # mais doit apparaitre dans toutes les cles du blockstate.
    extra_states = [",natural=true", ",natural=false"] if kind == "pebble" else [""]
    variants = {}

    for facing_index, facing in enumerate(["north", "east", "south", "west"]):
        for count in range(1, max_count + 1):
            entries = []
            for index in range(VARIANTS):
                entry = {"model": f"arcamod:block/{name}_{count}_{index}"}
                if facing_index:
                    entry["y"] = facing_index * 90
                entries.append(entry)
            for extra in extra_states:
                variants[f"facing={facing},{property_name}={count}{extra}"] = entries

    write_json(ASSETS / f"blockstates/{name}.json", {"variants": variants})

    # Modele d'inventaire : un tas moyen, vu en 3D.
    write_json(ASSETS / f"models/item/{name}.json", {"parent": f"arcamod:block/{name}_3_0"})

    print(f"{name} : {max_count * VARIANTS} modeles + blockstate")


def main():
    generate("pebble", "pebbles", PEBBLE_MAX_COUNT or read_balance("PEBBLES_MAX"))
    generate("stick", "fallen_sticks", STICK_MAX_COUNT or read_balance("STICKS_MAX"))


if __name__ == "__main__":
    main()
