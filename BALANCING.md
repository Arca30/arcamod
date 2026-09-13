# Equilibrage d'ArcaMod

Tout ce qui se regle cote Java est dans un seul fichier :
`src/main/java/dev/arca/arcamod/ArcaBalance.java`.

Ce document liste ce qui vit ailleurs : fichiers de donnees (JSON) et scripts
de generation.

---

## 1. La progression de debut de partie

Le mod impose un ordre : on ne peut plus rien obtenir du bois a mains nues.

1. ramasser des **branches** et des **cailloux** au sol (silex dans le butin
   bonus des cailloux, ou dans le gravier) ;
2. **dague en silex** = 1 silex + 1 baton ;
3. couper des herbes a la dague -> **fibres vegetales** -> **ficelle** ;
4. **outil en silex** = 2 silex + 1 ficelle + 2 batons ; il coupe enfin le
   bois, la pierre et le cuivre ;
5. outils en **cuivre** : seul palier capable de miner le **fer**.

Les deux verrous de cette progression :

| Verrou | Ou |
| --- | --- |
| Les buches ne tombent plus a mains nues | `data/arcamod/tags/block/requires_tool_for_drops.json` (par defaut `#minecraft:logs`) |
| Le fer echappe aux outils en pierre et en silex | `data/minecraft/tags/block/incorrect_for_stone_tool.json` |

Pour rendre un bloc a nouveau ramassable a mains nues, retire-le du premier
tag. Pour rendre le fer accessible a la pierre, vide le second fichier.

---

## 2. Outils en bois (nerf)

| Reglage | Ou | Defaut |
| --- | --- | --- |
| Durabilite | `ArcaBalance.WOOD_DURABILITY_MULTIPLIER` | `0.20` (-80% : 59 -> 12) |
| Vitesse de minage | `ArcaBalance.WOOD_MINING_SPEED_MULTIPLIER` | `0.70` (-30% : 2.0 -> 1.4) |

Au demarrage, le log affiche
`Outils en bois : durabilite 59 -> 12, vitesse 2.0 -> 1.4` : le moyen le plus
simple de verifier une nouvelle valeur.

---

## 3. Outil en silex et dague

Toutes les stats sont dans `ArcaBalance` (`FLINT_*` et `DAGGER_*`).

Les degats se saisissent comme ils s'affichent en jeu : `FLINT_TOTAL_ATTACK_DAMAGE`
vaut 6, et le code retire tout seul le point du joueur et le bonus du materiau.
Pour la dague, les valeurs sont en **coeurs** (`DAGGER_MELEE_DAMAGE_HEARTS`).

| Ce que tu veux changer | Ou |
| --- | --- |
| Blocs mines rapidement par l'outil | `data/arcamod/tags/block/mineable/flint_tool.json` |
| Item de reparation | `data/arcamod/tags/item/flint_tool_materials.json` |
| Palier de minage | `ModToolMaterials.FLINT`, premier parametre |
| Recettes | `data/arcamod/recipe/flint_tool.json`, `flint_dagger.json`, `plant_cord.json` |
| Herbes qui donnent des fibres | `ModLootTables.FIBER_SOURCES` |

Recette de l'outil (ficelle entre les deux eclats) :

```
F C F
  S
  S
```

Recette de la dague (lame au-dessus du manche) :

```
F
S
```

Pour la version en diagonale, remplacer le `pattern` de
`flint_dagger.json` par `["F ", " S"]`.

---

## 4. Cailloux et branches au sol

### Comportement

| Reglage | Ou |
| --- | --- |
| Nombre par bloc | `ArcaBalance.PEBBLES_MIN/MAX`, `STICKS_MIN/MAX` |
| Hauteur de la boite de selection | `ArcaBalance.CLUTTER_SHAPE_HEIGHT` |
| Butin des cailloux (quantite + bonus) | `data/arcamod/loot_table/blocks/pebbles.json` |
| Taille et forme des cailloux et brindilles | `tools/gen_ground_clutter_models.py` |
| Butin des branches | `data/arcamod/loot_table/blocks/fallen_sticks.json` |

La boite de selection couvre exprès tout le dessus du bloc : avec des objets
places au hasard, une boite collee a chaque caillou obligerait a viser au
pixel pres, et on minerait le bloc du dessous.

### Taille, forme et texture des objets

Elles sont dans les **modeles**, donc dans le script qui les genere :
`tools/gen_ground_clutter_models.py`. Les reglages sont en tete de fichier :

| Reglage | Effet |
| --- | --- |
| `PEBBLE_MIN_SIZE` / `PEBBLE_MAX_SIZE` | cote d'un caillou, en pixels (2 a 5) |
| `PEBBLE_MIN_HEIGHT` / `PEBBLE_MAX_HEIGHT` | hauteur, en pixels (1 a 2) |
| `PEBBLE_TEXTURES` | tuff, diorite, granite, pierre, andesite, gravier, pave moussu |
| `STICK_MIN_LENGTH` / `STICK_MAX_LENGTH` | longueur d'une branche |
| `STICK_TEXTURES` | les buches utilisees |
| `VARIANTS` | nombre de modeles differents par quantite |
| `SEED` | change le tirage : meme reglages, autre resultat |

Puis :

```bash
python3 tools/gen_ground_clutter_models.py
```

Le script reecrit les modeles **et** les blockstates. Si tu changes
`PEBBLE_MAX_COUNT` ou `STICK_MAX_COUNT`, pense a mettre `ArcaBalance.PEBBLES_MAX`
/ `STICKS_MAX` et les paliers des loot tables en accord.

### Butin bonus des cailloux

Dans `loot_table/blocks/pebbles.json`, la deuxieme `pool` :

- `conditions.chance` : probabilite d'un bonus a la casse (`0.06` = 6%) ;
- `entries[].weight` : le poids de chaque item une fois le bonus tire.

Par defaut : silex dans 20% des tas, et un cadeau plus rare dans 6% (charbon
5, argile 4, cuivre brut 3, fer brut 1).

Les deux pools ne se declenchent que sur un tas d'origine **naturelle** : la
propriete `natural` passe a faux des qu'un joueur pose ou agrandit un tas, ce
qui empeche de farmer le bonus en cassant et reposant les memes cailloux.

---

## 5. Taux d'apparition dans le monde

```java
ArcaBalance.PEBBLE_PATCH_CHANCE = 0.136F;  // ~1 chunk sur 7
ArcaBalance.STICK_PATCH_CHANCE = 0.11F;    // ~1 chunk sur 9
```

La taille des amas se regle separement pour chaque type
(`PEBBLE_PATCH_MIN_BLOCKS` / `MAX_BLOCKS` / `SPREAD`, et les equivalents
`STICK_*`).

C'est une probabilite par chunk eligible, entre 0 et 1. `0` coupe la
generation. La forme des amas (nombre de blocs, dispersion, pente toleree) est
juste au-dessus dans le meme fichier (`PATCH_*`).

Le choix des biomes est dans `ModWorldGen` :

- cailloux : Overworld, hors ocean / riviere / plage, temperature >=
  `ArcaBalance.BIOME_MIN_TEMPERATURE` (0.15, ce qui exclut tout ce qui est
  enneige) ;
- branches : les memes conditions, plus un biome boise (forets, jungles,
  taigas, savanes, plaines, marais, cerisaie).

Reperes de temperature : desert 2.0, savane 2.0, plaines 0.8, foret 0.7,
taiga 0.25, montagnes venteuses 0.2, plaines enneigees 0.0, bosquet -0.2,
pics geles -0.7.

---

## 6. Petite pierre lancee

Tout est dans `ArcaBalance` (`PEBBLE_*`) : degats (en coeurs), puissance,
cooldown, rayon de dispersion autour d'un mob, profondeur de recherche du sol.

Pour un comportement de pure boule de neige (la pierre disparait a l'impact) :
`PEBBLE_LANDS_AS_BLOCK = false` et `PEBBLE_DROP_ITEM_IF_NO_ROOM = false`.

---

## 7. Outils casses

Un outil qui tombe a zero ne disparait plus : il reste dans l'inventaire avec
la texture `arcamod:item/broken_tool`, ne mine plus rien et ne donne plus
aucun bonus d'attaque, jusqu'a reparation (enclume, etabli, Raccommodage).

Une pile de plusieurs outils garde l'ancien comportement : un exemplaire est
consomme et la barre repart a neuf pour les suivants. Tout est dans
`mixin/ItemStackMixin.java`.

---

## 8. Lits, cadres, elytres

| Reglage | Ou |
| --- | --- |
| Lits qui exigent un toit | `data/arcamod/tags/block/beds_requiring_shelter.json` |
| Durabilite des elytres de fortune | `ArcaBalance.PATCHWORK_ELYTRA_DURABILITY` (15) |
| Recette des elytres | `data/arcamod/recipe/patchwork_elytra.json` |

Le cadre rendu invisible a l'eclat d'amethyste redevient visible des qu'on lui
reprend son objet.

---

## 9. Oeufs de poule au sol

| Reglage | Ou |
| --- | --- |
| Nombre d'oeufs par bloc | `ArcaBalance.EGGS_MIN` / `EGGS_MAX` |
| Duree d'eclosion | `ArcaBalance.EGG_HATCH_TICKS` (2400 = 2 min) |

Accroupi + clic droit pour les poser. Ils n'eclosent que sur une botte de
foin ; la couleur de l'oeuf decide de la variete de poussin.

---

## 10. Desenchanteur et cristal

| Reglage | Ou |
| --- | --- |
| Cout en niveaux | `ArcaBalance.DISENCHANT_XP_BASE` + `DISENCHANT_XP_PER_LEVEL` x niveau |
| Rayon de charge du cristal | `ArcaBalance.CRYSTAL_CHARGE_RADIUS` |
| Rayon de la table d'enchantement | `ArcaBalance.CRYSTAL_TABLE_RADIUS` |
| Recettes (provisoires) | `data/arcamod/recipe/disenchanter.json`, `enchanting_crystal.json` |

Le desenchanteur sort les enchantements un par un, dans l'ordre alphabetique
de leur identifiant. Un livre enchante vide de tout enchantement redevient un
livre ordinaire.

---

## 11. Chaudron a potions

| Reglage | Ou |
| --- | --- |
| Fioles pour remplir | `ArcaBalance.POTION_CAULDRON_CAPACITY` (6, soit 2 par niveau) |
| Duree de l'effet donne | `ArcaBalance.POTION_CAULDRON_EFFECT_TICKS` (40 = 2 s) |
| Frequence de renouvellement | `ArcaBalance.POTION_CAULDRON_REFRESH_TICKS` |
| Delai avant retour a l'eau | `ArcaBalance.POTION_CAULDRON_DECAY_TICKS` (6000 = 5 min) |

Seules les potions normales sont acceptees, et une seule sorte a la fois. Un
feu de camp allume dessous empeche la preparation de retomber en eau.

---

## 12. Escaliers, dalles et chaume

38 familles (16 terres cuites, 16 poudres de beton, mousse, paille, et les 4
etapes du chaume), soit 80 blocs. Tout se regenere avec :

```bash
python3 tools/gen_decor_assets.py
```

La liste des matieres est en tete de ce script ET dans `ModDecorBlocks` : les
deux doivent rester d'accord.

Le chaume s'obtient en frappant une botte de foin a la hache, puis grise tout
seul en trois etapes (`ArcaBalance.THATCH_FADE_CHANCE`). Un coup de hache le
decape d'une etape, comme le cuivre.

---

## 13. Donnees vanilla remplacees

```bash
python3 tools/gen_vanilla_overrides.py
```

| Reglage | Constante du script |
| --- | --- |
| Fente (lunge) : propulsion verticale | `LUNGE_SCALE_Y` (1.0 = suit le regard) |
| Fente : poussee vers le haut ajoutee | `LUNGE_UPWARD_BIAS` |
| Fente : puissance | `LUNGE_MAGNITUDE_BASE` / `_PER_LEVEL` |
| Fente : autorisee en vol / dans l'eau / monte | `LUNGE_ALLOW_*` (true/false) |
| Diamants visibles dans les grottes | `DIAMOND_AIR_EXPOSURE_REDUCTION` (0.35 = -35%) |

---

## 14. Textures d'items

```bash
python3 tools/gen_pebble_textures.py --force
```

Genere les icones (caillou, outil, dague, fibres, ficelle). Sans `--force`, il
ne touche pas aux fichiers existants : tes propres dessins ne risquent rien.
