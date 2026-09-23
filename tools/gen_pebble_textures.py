#!/usr/bin/env python3
"""Genere les textures d'items du mod (et l'ancienne planche des cailloux).

Usage : python3 tools/gen_pebble_textures.py [--force] [--reset-textures]
ATTENTION AUX DESSINS. Par defaut, et MEME avec --force, ce script ne touche
jamais a une texture deja presente. Seul --reset-textures, demande
explicitement, remplace tes dessins par les versions derivees du vanilla.


Comme gen_placeholder_textures.py, ce script ne fait pas partie du build : il
sert juste a avoir des visuels corrects tant que tu n'as pas dessine les tiens.
Sans --force, il refuse d'ecraser un fichier deja present.

Note : les cailloux et les branches poses au sol n'utilisent plus de texture
du mod, ils reprennent directement celles de la pierre, du tuff, du gravier,
des buches... (voir tools/gen_ground_clutter_models.py). Le PNG pebbles.png
n'est garde que pour les modeles de test presents dans le dossier.

Le PNG des pierres au sol n'est pas une texture "classique" : il contient une
grille de 5 petites pierres de 4x4 pixels (les faces du dessus) plus une bande
de 1 pixel qui sert aux cotes des boites. Les modeles block/pebbles_N.json
piochent dedans avec des UV precis, donc si tu redessines cette texture, garde
la meme grille :

    (0,0)  (4,0)  (8,0)  (12,0)  -> pierres 1 a 4, 4x4 px chacune
    (0,4)                        -> pierre 5
    (0,8) sur 4 px de large      -> tranche (cote des pierres)
"""

import struct
import sys
import zlib
from pathlib import Path

FORCE = "--force" in sys.argv

# Ecraser une texture DEJA DESSINEE ne se fait que sur demande explicite :
# --force ne suffit pas. C'est la seule protection de ton travail, ces
# scripts ne savent pas distinguer un placeholder d'un dessin fini.
RESET_TEXTURES = "--reset-textures" in sys.argv

ROOT = Path(__file__).resolve().parent.parent
BLOCK_DIR = ROOT / "src/main/resources/assets/arcamod/textures/block"
ITEM_DIR = ROOT / "src/main/resources/assets/arcamod/textures/item"

SIZE = 16
TRANSPARENT = (0, 0, 0, 0)

# Palette pierre (du plus clair au plus sombre)
STONE_LIGHT = (166, 166, 166, 255)
STONE_MID = (135, 135, 135, 255)
STONE_DARK = (106, 106, 106, 255)
STONE_EDGE = (82, 82, 82, 255)

# Palette silex
FLINT_LIGHT = (94, 98, 106, 255)
FLINT_MID = (66, 69, 77, 255)
FLINT_DARK = (44, 46, 53, 255)
STICK_LIGHT = (150, 110, 66, 255)
STICK_DARK = (110, 78, 46, 255)

# Palette vegetale (fibres et ficelle)
FIBER_LIGHT = (166, 178, 104, 255)
FIBER_MID = (131, 146, 78, 255)
FIBER_DARK = (94, 108, 55, 255)
CORD_LIGHT = (190, 178, 126, 255)
CORD_MID = (154, 140, 93, 255)
CORD_DARK = (112, 100, 64, 255)


def blank(width=SIZE, height=SIZE):
    return [[TRANSPARENT for _ in range(width)] for _ in range(height)]


def put(px, x, y, color):
    if 0 <= y < len(px) and 0 <= x < len(px[0]):
        px[y][x] = color


# Les 5 cailloux vus du dessus, en 4x4. "." = transparent, les chiffres sont
# des niveaux de gris (1 = clair ... 4 = bord sombre). Chacun a une silhouette
# differente pour que le tas n'ait jamais l'air d'une grille reguliere.
PEBBLE_TOPS = [
    [".22.",
     "2113",
     "2234",
     ".34."],
    [".33.",
     "3123",
     "2113",
     ".34."],
    ["..3.",
     ".213",
     "3124",
     ".44."],
    [".33.",
     "2124",
     ".234",
     "..4."],
    ["..2.",
     ".113",
     "2234",
     ".4.."],
]

LEVELS = {"1": STONE_LIGHT, "2": STONE_MID, "3": STONE_DARK, "4": STONE_EDGE}


def draw_pebbles_atlas():
    """La planche utilisee par les modeles de bloc."""
    px = blank()

    for index, shape in enumerate(PEBBLE_TOPS):
        ox = (index % 4) * 4
        oy = (index // 4) * 4
        for y, row in enumerate(shape):
            for x, cell in enumerate(row):
                if cell != ".":
                    put(px, ox + x, oy + y, LEVELS[cell])

    # La tranche : 4 px de large, sur la ligne 8. C'est ce qu'on voit sur les
    # cotes des boites de 1 pixel de haut.
    for x in range(4):
        put(px, x, 8, STONE_DARK if x % 2 else STONE_EDGE)

    return px


def draw_pebble_item():
    """L'icone d'inventaire : un seul caillou, bien lisible."""
    px = blank()
    shape = [
        "....####....",
        "..##1111##..",
        ".#11122211#.",
        "#1112222231#",
        "#1122223331#",
        "#1222233331#",
        ".#2223333#..",
        "..##333##...",
        "....###.....",
    ]
    palette = {"#": STONE_EDGE, "1": STONE_LIGHT, "2": STONE_MID, "3": STONE_DARK}

    for y, row in enumerate(shape):
        for x, cell in enumerate(row):
            if cell != ".":
                put(px, x + 2, y + 4, palette[cell])

    return px


def draw_flint_tool():
    """Manche en baton, tete en silex taillee : entre la hache et la pioche."""
    px = blank()

    # Le manche, en diagonale du coin bas-gauche vers le centre.
    for i in range(9):
        x = 3 + i
        y = 12 - i
        put(px, x, y, STICK_LIGHT)
        put(px, x, y + 1, STICK_DARK)

    # La tete : un eclat de silex pose en biais sur le haut du manche.
    head = [
        "...###..",
        "..#111#.",
        ".#11223#",
        "#1122333",
        "#122333#",
        ".#2333#.",
        "..###...",
    ]
    palette = {"#": FLINT_DARK, "1": FLINT_LIGHT, "2": FLINT_MID, "3": FLINT_DARK}

    for y, row in enumerate(head):
        for x, cell in enumerate(row):
            if cell != ".":
                put(px, x + 8, y + 1, palette[cell])

    # Le lien qui tient la tete au manche.
    put(px, 8, 8, STICK_DARK)
    put(px, 9, 8, STICK_DARK)
    put(px, 9, 7, STICK_LIGHT)

    return px


def draw_from_grid(grid, palette, offset_x=0, offset_y=0):
    """Petit utilitaire : une grille de caracteres -> des pixels."""
    px = blank()
    for y, row in enumerate(grid):
        for x, cell in enumerate(row):
            if cell != ".":
                put(px, x + offset_x, y + offset_y, palette[cell])
    return px


# La dague : lame de silex en diagonale, garde en ficelle, court manche.
DAGGER = [
    "............##..",
    "...........#L#..",
    "..........#LM#..",
    ".........#LLM#..",
    "........#LLMD#..",
    ".......#LLMD#...",
    "......#LLMD#....",
    ".....#LLMD#.....",
    "....#LMD#.......",
    "...#CCC#........",
    "..#CCC#.........",
    "..#HH#..........",
    ".#HH#...........",
    ".#H#............",
    ".##.............",
    "................",
]


def draw_dagger():
    return draw_from_grid(DAGGER, {
        "#": FLINT_DARK,
        "L": FLINT_LIGHT,
        "M": FLINT_MID,
        "D": FLINT_DARK,
        "C": CORD_MID,
        "H": STICK_DARK,
    })


# Les fibres : une poignee de brins secs en vrac.
FIBER = [
    "................",
    "......11........",
    ".....1..2.......",
    "....1...2.33....",
    "...1...2..3.3...",
    "..1...2..3...3..",
    "..1..2..3....3..",
    "...12..3....3...",
    "....1.3....3....",
    "....2.3...3.....",
    ".....23..3......",
    "......2.3.......",
    ".......23.......",
    "................",
]


def draw_plant_fiber():
    return draw_from_grid(FIBER, {"1": FIBER_LIGHT, "2": FIBER_MID, "3": FIBER_DARK}, offset_y=1)


# La ficelle : les brins tordus ensemble, en echeveau.
CORD = [
    "................",
    "....########....",
    "...#LLMLLMLL#...",
    "..#MLLMLLMLLM#..",
    "..#D########D#..",
    "..#LLMLLMLLML#..",
    "..#MLLMLLMLLM#..",
    "..#D########D#..",
    "..#LLMLLMLLML#..",
    "...#MLLMLLML#...",
    "....########....",
    "................",
]


def draw_plant_cord():
    return draw_from_grid(CORD, {"#": CORD_DARK, "L": CORD_LIGHT, "M": CORD_MID, "D": CORD_DARK}, offset_y=2)


# Les elytres de fortune : deux ailes en membranes rapiecees.
PATCHWORK_ELYTRA = [
    "................",
    "..###......###..",
    ".#MLM#....#MLM#.",
    ".#LMML#..#LMML#.",
    ".#MLLM#..#MLLM#.",
    "..#MLM#..#MLM#..",
    "..#LMML##LMML#..",
    "...#MLM##MLM#...",
    "...#LMMMMMML#...",
    "....#MLMMLM#....",
    "....#LM##ML#....",
    ".....##..##.....",
    "................",
]


def draw_patchwork_elytra():
    return draw_from_grid(PATCHWORK_ELYTRA, {
        "#": (58, 54, 66, 255),
        "M": (120, 114, 132, 255),
        "L": (164, 158, 178, 255),
    }, offset_y=2)


# L'outil casse : un manche, une tete fendue, et des fissures.
BROKEN_TOOL = [
    "................",
    "..........##....",
    ".........#GG#...",
    "........#G##G#..",
    "........#G..G#..",
    ".........##.#...",
    "........#..##...",
    ".......#GG#.....",
    "......#GG#......",
    ".....#HH#.......",
    "....#HH#........",
    "...#HH#.........",
    "..#HH#..........",
    "..##............",
    "................",
]


def draw_broken_tool():
    return draw_from_grid(BROKEN_TOOL, {
        "#": (48, 48, 52, 255),
        "G": (122, 122, 128, 255),
        "H": (92, 66, 40, 255),
    }, offset_y=1)


# Coquilles d'oeufs : vue de dessus (0,0) et vue de cote (0,4), utilisees par
# les modeles de block/chicken_eggs_*.json.
def draw_egg_shell(base, shade, speck):
    px = blank()

    # dessus : 4x4
    top = ["..", "..", "..", ".."]
    for y in range(4):
        for x in range(4):
            put(px, x, y, base if (x + y) % 3 else shade)
    put(px, 1, 1, speck)
    put(px, 3, 2, speck)

    # cote : 4 de large, 5 de haut, plus clair en haut
    for y in range(5):
        for x in range(4):
            colour = base if y < 3 else shade
            put(px, x, 4 + y, colour)
    put(px, 0, 4, shade)
    put(px, 3, 4, shade)
    put(px, 2, 6, speck)
    put(px, 1, 8, speck)

    return px


EGG_COLOURS = {
    "white": ((233, 226, 208, 255), (198, 190, 172, 255), (168, 158, 140, 255)),
    "blue": ((176, 206, 214, 255), (140, 172, 184, 255), (110, 140, 154, 255)),
    "brown": ((196, 154, 112, 255), (162, 122, 84, 255), (126, 92, 60, 255)),
}


# ----------------------------------------------------------------------
# Le chaume : de la paille battue, qui grise en quatre etapes.
# ----------------------------------------------------------------------

# Teinte de depart (jaune paille) et teinte d'arrivee (gris).
THATCH_FRESH = (206, 174, 84)
THATCH_GREY = (138, 136, 128)

# Nombre d'etapes (doit valoir len(THATCH_STAGES) dans gen_decor_assets.py).
THATCH_STAGE_COUNT = 4


def mix(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


def shade(colour, factor):
    return tuple(min(255, max(0, int(colour[i] * factor))) for i in range(3)) + (255,)


def draw_thatch_side(colour, seed):
    """Des brins couches a l'horizontale, d'epaisseur irreguliere."""
    px = blank()
    value = seed

    for y in range(SIZE):
        # bruit deterministe : pas besoin d'un vrai generateur aleatoire
        value = (value * 1103515245 + 12345) & 0x7FFFFFFF
        row = 0.78 + ((value >> 16) % 45) / 100.0

        for x in range(SIZE):
            value = (value * 1103515245 + 12345) & 0x7FFFFFFF
            jitter = ((value >> 18) % 17) / 100.0
            put(px, x, y, shade(colour, row + jitter))

        # un brin plus clair traverse de temps en temps
        if y % 4 == 1:
            for x in range(SIZE):
                if (x + y) % 5:
                    put(px, x, y, shade(colour, 1.12))

    return px


def draw_thatch_top(colour, seed):
    """Les bouts de brins vus en coupe : une trame de petits points."""
    px = blank()
    value = seed

    for y in range(SIZE):
        for x in range(SIZE):
            value = (value * 1103515245 + 12345) & 0x7FFFFFFF
            jitter = ((value >> 17) % 25) / 100.0
            put(px, x, y, shade(colour, 0.82 + jitter))

    # les brins forment des petits paquets de 2x2
    for y in range(0, SIZE, 4):
        for x in range(0, SIZE, 4):
            put(px, x, y, shade(colour, 1.15))
            put(px, x + 1, y + 1, shade(colour, 0.72))

    return px


# Le cristal d'enchantement : une gemme violette, terne ou lumineuse.
CRYSTAL = [
    "......##........",
    ".....#LL#.......",
    "....#LLMM#......",
    "...#LMMMMD#.....",
    "...#LMMMMD#.....",
    "...#MMMMDD#.....",
    "....#MMDD#......",
    ".....#DD#.......",
    "......##........",
]


def draw_crystal(light, mid, dark):
    return draw_from_grid(CRYSTAL, {"#": dark, "L": light, "M": mid, "D": mid}, offset_x=3, offset_y=4)


# ----------------------------------------------------------------------
# Le panneau de GUI du desenchanteur (176 x 166 dans une image de 256)
# ----------------------------------------------------------------------

PANEL = (198, 198, 198, 255)
PANEL_LIGHT = (255, 255, 255, 255)
PANEL_DARK = (85, 85, 85, 255)
SLOT_BG = (139, 139, 139, 255)
SLOT_DARK = (55, 55, 55, 255)

GUI_WIDTH = 176
GUI_HEIGHT = 166

# Les slots du menu, en pixels depuis le coin du panneau. Ils doivent
# correspondre aux coordonnees de DisenchanterMenu.
DISENCHANTER_SLOTS = [(27, 47), (76, 47), (134, 47)]


def draw_container_gui(slots):
    px = [[TRANSPARENT for _ in range(256)] for _ in range(256)]

    def rect(x0, y0, w, h, colour):
        for y in range(y0, y0 + h):
            for x in range(x0, x0 + w):
                if 0 <= x < 256 and 0 <= y < 256:
                    px[y][x] = colour

    rect(0, 0, GUI_WIDTH, GUI_HEIGHT, PANEL)
    rect(0, 0, GUI_WIDTH, 1, PANEL_LIGHT)
    rect(0, 0, 1, GUI_HEIGHT, PANEL_LIGHT)
    rect(0, GUI_HEIGHT - 1, GUI_WIDTH, 1, PANEL_DARK)
    rect(GUI_WIDTH - 1, 0, 1, GUI_HEIGHT, PANEL_DARK)

    def slot(x, y):
        # bord sombre en haut a gauche, clair en bas a droite
        rect(x - 1, y - 1, 18, 18, SLOT_DARK)
        rect(x - 1 + 1, y - 1 + 1, 17, 17, PANEL_LIGHT)
        rect(x, y, 16, 16, SLOT_BG)

    for (x, y) in slots:
        slot(x, y)

    # inventaire du joueur : trois rangees puis la barre d'action
    for row in range(3):
        for column in range(9):
            slot(8 + column * 18, 84 + row * 18)

    for column in range(9):
        slot(8 + column * 18, 142)

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
    write_png(BLOCK_DIR / "pebbles.png", draw_pebbles_atlas())
    write_png(ITEM_DIR / "pebble.png", draw_pebble_item())
    write_png(ITEM_DIR / "flint_tool.png", draw_flint_tool())
    write_png(ITEM_DIR / "flint_dagger.png", draw_dagger())
    write_png(ITEM_DIR / "plant_fiber.png", draw_plant_fiber())
    write_png(ITEM_DIR / "plant_cord.png", draw_plant_cord())
    write_png(ITEM_DIR / "patchwork_elytra.png", draw_patchwork_elytra())
    write_png(ITEM_DIR / "broken_tool.png", draw_broken_tool())

    for name, (base, shade_colour, speck) in EGG_COLOURS.items():
        write_png(BLOCK_DIR / f"chicken_egg_{name}.png", draw_egg_shell(base, shade_colour, speck))

    write_png(ROOT / "src/main/resources/assets/arcamod/textures/gui/container/disenchanter.png",
              draw_container_gui(DISENCHANTER_SLOTS))

    write_png(BLOCK_DIR / "enchanting_crystal.png",
              draw_crystal((150, 118, 186, 255), (110, 84, 148, 255), (70, 52, 100, 255)))
    write_png(BLOCK_DIR / "enchanting_crystal_charged.png",
              draw_crystal((226, 198, 255, 255), (186, 146, 240, 255), (128, 92, 190, 255)))

    stages = ["thatch_block", "weathered_thatch_block", "aged_thatch_block", "gray_thatch_block"]
    for index, stage in enumerate(stages):
        ratio = index / (THATCH_STAGE_COUNT - 1)
        colour = mix(THATCH_FRESH, THATCH_GREY, ratio)
        write_png(BLOCK_DIR / f"{stage}_side.png", draw_thatch_side(colour, 7 + index))
        write_png(BLOCK_DIR / f"{stage}_top.png", draw_thatch_top(colour, 31 + index))


if __name__ == "__main__":
    main()
