#!/usr/bin/env python3
"""Decoupe une texture animee (bande verticale de N frames) en N fichiers.

Usage: python3 tools/split_animated_texture.py <texture.png> <nb_frames>
   ex: python3 tools/split_animated_texture.py \
           src/main/resources/assets/arcamod/textures/block/xp_bottler_top.png 8

Pourquoi : le systeme d'animation de Minecraft (le .mcmeta) joue les frames
tout seul, en boucle, sans qu'on puisse choisir laquelle afficher. Pour piloter
la frame depuis l'etat du bloc, il faut une texture par frame. Ce script
fabrique ces fichiers a partir de TA bande : il ne la modifie jamais, tu peux
continuer a dessiner dedans et relancer le script.

Sortie : <nom>_0.png ... <nom>_(N-1).png, dans le meme dossier.
"""

import struct
import sys
import zlib
from pathlib import Path


def read_png(path):
    """Decodeur PNG minimal : 8 bits par canal, non entrelace."""
    data = path.read_bytes()
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise SystemExit(f"{path} n'est pas un PNG")

    idat, plte, trns = b"", None, None
    width = height = depth = color = interlace = None
    i = 8
    while i < len(data):
        length = int.from_bytes(data[i:i + 4], "big")
        tag = data[i + 4:i + 8]
        body = data[i + 8:i + 8 + length]
        if tag == b"IHDR":
            width, height, depth, color, _comp, _filt, interlace = struct.unpack(">IIBBBBB", body)
        elif tag == b"IDAT":
            idat += body
        elif tag == b"PLTE":
            plte = body
        elif tag == b"tRNS":
            trns = body
        i += 12 + length

    if depth != 8 or interlace != 0:
        raise SystemExit(f"{path}: seul le PNG 8 bits non entrelace est gere "
                         f"(depth={depth}, interlace={interlace})")

    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[color]
    bpp = channels
    stride = width * channels
    raw = zlib.decompress(idat)

    rows, prev, pos = [], bytearray(stride), 0
    for _ in range(height):
        ftype = raw[pos]
        pos += 1
        line = bytearray(raw[pos:pos + stride])
        pos += stride
        # "defiltrage" : chaque ligne PNG est encodee par rapport a ses voisines
        for x in range(stride):
            a = line[x - bpp] if x >= bpp else 0
            b = prev[x]
            c = prev[x - bpp] if x >= bpp else 0
            if ftype == 1:
                line[x] = (line[x] + a) & 0xFF
            elif ftype == 2:
                line[x] = (line[x] + b) & 0xFF
            elif ftype == 3:
                line[x] = (line[x] + (a + b) // 2) & 0xFF
            elif ftype == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                pred = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                line[x] = (line[x] + pred) & 0xFF
        rows.append(bytes(line))
        prev = line

    # conversion en RGBA quel que soit le type de couleur d'origine
    pixels = []
    for line in rows:
        row = []
        for x in range(width):
            if color == 6:
                row.append(tuple(line[x * 4:x * 4 + 4]))
            elif color == 2:
                r, g, b = line[x * 3:x * 3 + 3]
                row.append((r, g, b, 255))
            elif color == 3:
                idx = line[x]
                r, g, b = plte[idx * 3:idx * 3 + 3]
                alpha = trns[idx] if trns and idx < len(trns) else 255
                row.append((r, g, b, alpha))
            elif color == 0:
                v = line[x]
                row.append((v, v, v, 255))
            else:  # gris + alpha
                v, alpha = line[x * 2:x * 2 + 2]
                row.append((v, v, v, alpha))
        pixels.append(row)
    return pixels


def write_png(path, pixels):
    height, width = len(pixels), len(pixels[0])
    raw = b"".join(b"\x00" + b"".join(struct.pack("BBBB", *p) for p in row) for row in pixels)

    def chunk(tag, body):
        payload = tag + body
        return struct.pack(">I", len(body)) + payload + struct.pack(">I", zlib.crc32(payload))

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")
    path.write_bytes(png)


def main():
    if len(sys.argv) != 3:
        raise SystemExit(__doc__)

    source = Path(sys.argv[1])
    frames = int(sys.argv[2])
    pixels = read_png(source)
    height, width = len(pixels), len(pixels[0])

    if height % frames:
        raise SystemExit(f"{source}: hauteur {height} non divisible par {frames} frames")
    frame_height = height // frames

    for index in range(frames):
        start = index * frame_height
        out = source.with_name(f"{source.stem}_{index}.png")
        write_png(out, pixels[start:start + frame_height])
        print(f"ecrit {out.name}  ({width}x{frame_height})")

    print(f"\n{source.name} : inchange")


if __name__ == "__main__":
    main()
