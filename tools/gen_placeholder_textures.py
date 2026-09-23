#!/usr/bin/env python3
"""Genere des textures placeholder 16x16 pour le buisson d'XP et la berry.

Usage: python3 tools/gen_placeholder_textures.py

Ce script n'a AUCUN lien avec le build du mod : c'est juste un depannage pour
avoir des visuels corrects en attendant tes propres textures. Tu peux le
supprimer des que tu dessines les PNG toi-meme.

Le buisson utilise le modele "cross" : une texture 16x16 a fond transparent,
affichee sur deux plans croises. La ligne 0 est en HAUT de la texture, donc la
plante se dessine depuis la ligne 15 (le sol) vers le haut.
"""

import math
import struct
import sys
import zlib
from pathlib import Path

# Par defaut le script REFUSE d'ecraser un fichier existant : les textures que
# tu as dessinees toi-meme ne doivent jamais etre perdues par une simple
# re-execution. Utilise --force si tu veux vraiment tout regenerer.
FORCE = "--force" in sys.argv

# Ecraser une texture DEJA DESSINEE ne se fait que sur demande explicite :
# --force ne suffit pas. C'est la seule protection de ton travail, ces
# scripts ne savent pas distinguer un placeholder d'un dessin fini.
RESET_TEXTURES = "--reset-textures" in sys.argv

ROOT = Path(__file__).resolve().parent.parent
BLOCK_DIR = ROOT / "src/main/resources/assets/arcamod/textures/block"
ITEM_DIR = ROOT / "src/main/resources/assets/arcamod/textures/item"

SIZE = 16

# Palette (R, G, B, A)
TRANSPARENT = (0, 0, 0, 0)
LEAF_DARK = (36, 71, 26, 255)
LEAF_MID = (53, 107, 38, 255)
LEAF_LIGHT = (74, 143, 49, 255)
XP_DIM = (143, 209, 43, 255)
XP_BRIGHT = (196, 245, 66, 255)
XP_GLOW = (234, 255, 192, 255)


def blank():
    return [[TRANSPARENT for _ in range(SIZE)] for _ in range(SIZE)]


def put(px, x, y, color):
    if 0 <= x < SIZE and 0 <= y < SIZE:
        px[y][x] = color


def stem_x(height):
    """Abscisse de la tige a une hauteur donnee (0 = sol) : legere ondulation."""
    return 7 + int(round(1.4 * math.sin(height * 0.55)))


# Palette des baies pour chaque frame de l'animation : les baies pulsent.
BERRY_FRAMES = [
    (XP_DIM, XP_DIM),        # frame 0 : eteinte
    (XP_BRIGHT, XP_DIM),     # frame 1
    (XP_BRIGHT, XP_GLOW),    # frame 2
    (XP_GLOW, XP_GLOW),      # frame 3 : au maximum
]


def draw_vine(top_row, tip, ripe, berry_core=XP_BRIGHT, berry_glow=XP_GLOW):
    """top_row = ligne la plus haute occupee ; tip = bourgeon au sommet."""
    px = blank()
    bottom_row = SIZE - 1

    for y in range(top_row, bottom_row + 1):
        height = bottom_row - y
        x = stem_x(height)

        # tige : 2 px de large, cote droit plus sombre (fausse ombre)
        put(px, x, y, LEAF_MID)
        put(px, x + 1, y, LEAF_DARK)

        # feuilles tous les 3 px, alternees gauche / droite
        if height % 3 == 1:
            side = -1 if (height // 3) % 2 == 0 else 1
            for i in (1, 2, 3):
                put(px, x + (i * side if side < 0 else i + 1), y, LEAF_LIGHT if i == 2 else LEAF_MID)
            put(px, x + (4 * side if side < 0 else 5), y, LEAF_DARK)

    if tip:
        x = stem_x(bottom_row - top_row)
        put(px, x, top_row - 1, LEAF_LIGHT)
        put(px, x + 1, top_row - 1, LEAF_MID)
        put(px, x, top_row - 2, LEAF_MID)

    if ripe:
        # baies suspendues a la tige, sur les lignes sans feuille
        for height in range(2, bottom_row - top_row, 3):
            y = bottom_row - height
            x = stem_x(height)
            side = 1 if (height // 3) % 2 == 0 else -1
            bx = x + (2 if side > 0 else -1)
            put(px, bx, y, berry_core)
            put(px, bx, y + 1, XP_DIM)
            put(px, bx + (1 if side > 0 else -1), y, berry_glow)

    return px


def draw_vine_animated(top_row, tip):
    """Version animee : les 4 frames empilees verticalement (16 x 64).

    C'est la convention Minecraft pour les textures animees ; le .mcmeta a
    cote du PNG dit dans quel ordre et a quelle vitesse les jouer.
    """
    frames = []
    for (core, glow) in BERRY_FRAMES:
        frames.extend(draw_vine(top_row, tip, True, core, glow))
    return frames


def draw_berry():
    px = blank()
    cx, cy, r = 7.5, 8.0, 4.6
    for y in range(SIZE):
        for x in range(SIZE):
            d = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
            if d > r:
                continue
            if d > r - 1.1:
                px[y][x] = LEAF_DARK              # contour
            elif d > r - 2.4:
                px[y][x] = XP_DIM                 # corps
            else:
                px[y][x] = XP_BRIGHT              # coeur
    # reflet en haut a gauche
    for (x, y) in ((6, 5), (7, 5), (5, 6), (6, 6)):
        px[y][x] = XP_GLOW
    return px


# --------------------------------------------------------------------------
# Embouteilleur d'XP : texture du bloc + texture du GUI
# --------------------------------------------------------------------------

# Couleurs des panneaux de GUI vanilla
GUI_BG = (198, 198, 198, 255)
GUI_LIGHT = (255, 255, 255, 255)
GUI_DARK = (85, 85, 85, 255)
SLOT_BG = (139, 139, 139, 255)
SLOT_SHADOW = (55, 55, 55, 255)

METAL_DARK = (92, 84, 76, 255)
METAL_MID = (140, 128, 116, 255)
METAL_LIGHT = (176, 164, 150, 255)

GUI_SIZE = 256
PANEL_W, PANEL_H = 176, 166
# (x, y) du coin haut-gauche des cases, = coordonnees des Slot dans le menu -1
SLOTS = [(44, 35), (116, 35)]
PLAYER_INV_ORIGIN = (8, 84)


def blank_sized(w, h):
    return [[TRANSPARENT for _ in range(w)] for _ in range(h)]


def draw_rect(px, x0, y0, w, h, color):
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            if 0 <= y < len(px) and 0 <= x < len(px[0]):
                px[y][x] = color


def draw_slot(px, x, y):
    """Une case d'inventaire : 18x18, creusee."""
    draw_rect(px, x - 1, y - 1, 18, 18, SLOT_BG)
    draw_rect(px, x - 1, y - 1, 18, 1, SLOT_SHADOW)      # haut
    draw_rect(px, x - 1, y - 1, 1, 18, SLOT_SHADOW)      # gauche
    draw_rect(px, x - 1, y + 16, 18, 1, GUI_LIGHT)       # bas
    draw_rect(px, x + 16, y - 1, 1, 18, GUI_LIGHT)       # droite


def draw_arrow(px, ox, oy, color):
    """Fleche de progression facon vanilla, dans une boite de 24x16.

    On passe par draw_rect et pas put() : put() borne a 16 px (les textures de
    bloc), alors que le GUI fait 256 px de cote.
    """
    draw_rect(px, ox + 1, oy + 6, 14, 4, color)          # la hampe
    for i in range(6):                                   # la pointe
        draw_rect(px, ox + 15 + i, oy + 2 + i, 1, 12 - 2 * i, color)


def draw_gui():
    px = blank_sized(GUI_SIZE, GUI_SIZE)
    draw_rect(px, 0, 0, PANEL_W, PANEL_H, GUI_BG)
    draw_rect(px, 0, 0, PANEL_W, 1, GUI_LIGHT)
    draw_rect(px, 0, 0, 1, PANEL_H, GUI_LIGHT)
    draw_rect(px, 0, PANEL_H - 1, PANEL_W, 1, GUI_DARK)
    draw_rect(px, PANEL_W - 1, 0, 1, PANEL_H, GUI_DARK)

    for (sx, sy) in SLOTS:
        draw_slot(px, sx, sy)

    # fleche vide, dans le panneau
    draw_arrow(px, 72, 35, SLOT_BG)
    # fleche pleine, rangee hors du panneau : l'ecran en blit une portion
    # croissante par dessus la fleche vide (coordonnees 176,0 dans le fichier)
    draw_arrow(px, 176, 0, XP_BRIGHT)

    # inventaire du joueur : 3 rangees + barre d'action
    ox, oy = PLAYER_INV_ORIGIN
    for row in range(3):
        for col in range(9):
            draw_slot(px, ox + col * 18, oy + row * 18)
    for col in range(9):
        draw_slot(px, ox + col * 18, oy + 58)

    return px


def metal_plate():
    """Fond metallique commun, avec un motif fixe (donc reproductible)."""
    px = blank_sized(SIZE, SIZE)
    for y in range(SIZE):
        for x in range(SIZE):
            px[y][x] = METAL_LIGHT if (x * 5 + y * 3) % 7 == 0 else METAL_MID
    draw_rect(px, 0, 0, SIZE, 1, METAL_LIGHT)
    draw_rect(px, 0, 0, 1, SIZE, METAL_LIGHT)
    draw_rect(px, 0, SIZE - 1, SIZE, 1, METAL_DARK)
    draw_rect(px, SIZE - 1, 0, 1, SIZE, METAL_DARK)
    return px


def draw_bottler_side():
    """Cote : un hublot qui laisse voir l'XP. Le bloc ne fait que 8 px de
    haut, donc seule la MOITIE BASSE de la texture est visible en jeu."""
    px = metal_plate()
    draw_rect(px, 4, 9, 8, 6, METAL_DARK)
    draw_rect(px, 5, 10, 6, 4, XP_DIM)
    draw_rect(px, 6, 11, 4, 2, XP_BRIGHT)
    draw_rect(px, 6, 11, 1, 1, XP_GLOW)
    return px


def draw_bottler_top():
    """Dessus : l'ouverture ou l'XP est aspiree."""
    px = metal_plate()
    draw_rect(px, 4, 4, 8, 8, METAL_DARK)
    draw_rect(px, 5, 5, 6, 6, XP_DIM)
    draw_rect(px, 6, 6, 4, 4, XP_BRIGHT)
    draw_rect(px, 7, 7, 2, 2, XP_GLOW)
    return px


GOLD_DARK = (150, 108, 20, 255)
GOLD_MID = (222, 173, 44, 255)
GOLD_LIGHT = (255, 224, 120, 255)
BEET_DARK = (94, 20, 40, 255)
BEET_MID = (150, 32, 62, 255)
LEAF = (63, 118, 40, 255)


def draw_golden_beetroot(enchanted):
    """Racine de betterave sertie d'or, avec des feuilles."""
    px = blank()
    cx, cy, r = 7.5, 9.5, 4.4
    for y in range(SIZE):
        for x in range(SIZE):
            d = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
            if d > r:
                continue
            if d > r - 1.1:
                px[y][x] = GOLD_MID if not enchanted else GOLD_LIGHT
            elif d > r - 2.3:
                px[y][x] = BEET_MID
            else:
                px[y][x] = BEET_DARK
    # pepites d'or sur la racine
    for (x, y) in ((6, 8), (9, 10), (7, 11)):
        px[y][x] = GOLD_LIGHT if enchanted else GOLD_MID
    # fanes
    for (x, y) in ((7, 4), (8, 3), (6, 3), (8, 4), (9, 2), (5, 2)):
        put(px, x, y, LEAF)
    for (x, y) in ((7, 5), (8, 5)):
        put(px, x, y, GOLD_DARK)
    return px


def write_png(path, pixels):
    height = len(pixels)
    width = len(pixels[0])
    raw = b""
    for row in pixels:
        raw += b"\x00" + b"".join(struct.pack("BBBB", *p) for p in row)

    def chunk(tag, data):
        body = tag + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body))

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")

    path.parent.mkdir(parents=True, exist_ok=True)
    if path.exists() and not RESET_TEXTURES:
        print(f"conserve (existe deja) {path.relative_to(ROOT)}")
        return
    path.write_bytes(png)
    print(f"ecrit {path.relative_to(ROOT)}")


def main():
    # tete (le sommet de la plante) : tige plus courte + bourgeon
    write_png(BLOCK_DIR / "xp_bush.png", draw_vine(top_row=4, tip=True, ripe=False))
    write_png(BLOCK_DIR / "xp_bush_lit.png", draw_vine_animated(top_row=4, tip=True))
    # corps (les segments du milieu) : tige sur toute la hauteur
    write_png(BLOCK_DIR / "xp_bush_plant.png", draw_vine(top_row=0, tip=False, ripe=False))
    write_png(BLOCK_DIR / "xp_bush_plant_lit.png", draw_vine_animated(top_row=0, tip=False))

    write_png(ITEM_DIR / "xp_berry.png", draw_berry())
    write_png(ITEM_DIR / "golden_beetroot.png", draw_golden_beetroot(False))
    write_png(ITEM_DIR / "enchanted_golden_beetroot.png", draw_golden_beetroot(True))

    # embouteilleur d'XP
    write_png(BLOCK_DIR / "xp_bottler_side.png", draw_bottler_side())
    write_png(BLOCK_DIR / "xp_bottler_top.png", draw_bottler_top())
    write_png(ROOT / "src/main/resources/assets/arcamod/textures/gui/container/xp_bottler.png", draw_gui())


if __name__ == "__main__":
    main()
