# Equilibrage d'ArcaMod

Deux endroits pour tout regler :

| Quoi | Ou |
| --- | --- |
| Les **valeurs** (degats, chances, durees, quantites...) | `src/main/java/dev/arca/arcamod/ArcaBalance.java` |
| Les **interrupteurs** ON/OFF de chaque fonctionnalite | en jeu : **Options > ArcaMod**, ou `config/arcamod.json` (dossier `run/config` en developpement) |

La liste des interrupteurs et leur valeur par defaut est dans
`src/main/java/dev/arca/arcamod/config/ArcaFeature.java`. Un interrupteur coupe
le comportement d'une fonctionnalite, mais ne retire jamais ses blocs et objets
du jeu (les mondes existants resteraient intacts). Dans le menu, l'infobulle
indique en jaune les changements qui demandent de recharger le monde (butin)
ou de relancer le jeu (outils en bois).

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
| Durabilite des elytres de fortune | `ArcaBalance.PATCHWORK_ELYTRA_DURABILITY` (4, reparable a la membrane de phantom) |
| Recette des elytres | `data/arcamod/recipe/patchwork_elytra.json` |

Le cadre rendu invisible a l'eclat d'amethyste redevient visible des qu'on lui
reprend son objet.

Cadres colles aux blocs (`ArcaFeature.ITEM_FRAME_SNAPPING`) : un cadre pose sur
une barriere, un muret, une vitre ou un poteau vient se plaquer contre la forme
reelle du bloc au lieu de flotter au bord de son cube.

| Reglage | Ou |
| --- | --- |
| Profondeur max du collage | `ArcaBalance.ITEM_FRAME_SNAP_MAX_DEPTH` (0.75 ; au-dela = rien sur quoi s'appuyer) |
| Espace laisse contre le bloc | `ArcaBalance.ITEM_FRAME_SNAP_GAP` |
| Suivre les blocs sans collision | `ArcaBalance.ITEM_FRAME_SNAP_USE_MODEL_SHAPE` |
| Pose sur les blocs fins a collision (baton de l'End, chaine...) | `ArcaBalance.ITEM_FRAME_SNAP_ALLOW_MODEL_SUPPORTS` |
| Collage au sol et au plafond | `ArcaBalance.ITEM_FRAME_SNAP_VERTICAL` |
| Frequence de recalcul | `ArcaBalance.ITEM_FRAME_SNAP_REFRESH_TICKS` |
| Pose sur le dessus d'une barriere ou d'un muret | `ArcaBalance.ITEM_FRAME_SNAP_IGNORES_SUPPORT_COLLISION` |
| Trace de secours (pose refusee) | `ArcaBalance.ITEM_FRAME_SURVIVES_DEBUG` |
| Blocs a 1 pixel de rab (loquet du coffre) | `data/arcamod/tags/block/item_frame_extra_clearance.json` + `ArcaBalance.ITEM_FRAME_SNAP_EXTRA_CLEARANCE` |

Cadre invisible (`ArcaFeature.ITEM_FRAME_HIDING`) : membrane de phantom pour le
faire disparaitre, poudre d'os pour le faire revenir. Tant qu'il est invisible
les clics le traversent, donc on ouvre normalement le coffre derriere lui.

| Reglage | Ou |
| --- | --- |
| Avancer l'objet hors du mur (en pixels) | `ArcaBalance.ITEM_FRAME_INVISIBLE_ITEM_FORWARD_PIXELS` (0 = vanilla ; 4 sort un objet-bloc du mur) |
| Clics qui traversent | `ArcaBalance.ITEM_FRAME_INVISIBLE_CLICK_THROUGH` |
| Poudre d'os qui revele | `ArcaBalance.ITEM_FRAME_BONE_MEAL_REVEALS` |
| Particules a la reapparition | `ArcaBalance.ITEM_FRAME_REVEAL_PARTICLES` |

Panneaux : memes reglages de collage que les cadres
(`ArcaFeature.SIGN_SNAPPING`, panneaux muraux uniquement).

La membrane de phantom fait aussi disparaitre la planche d'un panneau ecrit en
laissant le texte (`ArcaFeature.SIGN_HIDING`, poudre d'os pour la faire
revenir).

| Reglage | Ou |
| --- | --- |
| Forme du panneau collee a son image | `ArcaBalance.SIGN_SNAP_MOVES_SHAPE` |
| Exiger du texte pour cacher un panneau | `ArcaBalance.SIGN_HIDING_REQUIRES_TEXT` |
| Trace temporaire dans le journal | `ArcaBalance.SIGN_SNAP_DEBUG` |

Un panneau colle occupe toujours son propre cube, comme en vanilla : on ne peut
donc pas poser de bloc dans l'espace qui parait vide devant la barriere.

---

## 9. Oeufs de poule au sol

| Reglage | Ou |
| --- | --- |
| Nombre d'oeufs par bloc | `ArcaBalance.EGGS_MIN` / `EGGS_MAX` |
| Duree d'eclosion | `ArcaBalance.EGG_HATCH_TICKS` (2400 = 2 min) |
| Blocs ou ils eclosent | `data/arcamod/tags/block/egg_hatching_blocks.json` |

Accroupi + clic droit pour les poser. Ils n'eclosent que sur de la paille ou
du chaume (bloc, escalier ou dalle, toutes les etapes du chaume comprises) ;
la couleur de l'oeuf decide de la variete de poussin.

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
| Fioles pour remplir | `ArcaBalance.POTION_CAULDRON_CAPACITY` (3, soit 1 par niveau) |
| Duree de l'effet donne | `ArcaBalance.POTION_CAULDRON_EFFECT_TICKS` (80 = 4 s) |
| Frequence de renouvellement | `ArcaBalance.POTION_CAULDRON_REFRESH_TICKS` |
| Delai avant retour a l'eau | `ArcaBalance.POTION_CAULDRON_DECAY_TICKS` (300 = 15 s) |

Seules les potions normales sont acceptees, et une seule sorte a la fois. Un
feu de camp allume dessous empeche la preparation de retomber en eau.

---

## 12. Escaliers, dalles, chaume et briques

44 familles (16 terres cuites, 16 poudres de beton, mousse, paille, les 4
etapes du chaume et les 4 etapes de la brique du mod). Tout se regenere avec :

```bash
python3 tools/gen_decor_assets.py
python3 tools/gen_brick_textures.py   # textures PLACEHOLDER des briques
```

La liste des matieres est en tete de ces scripts ET dans `ModDecorBlocks` :
les trois doivent rester d'accord.

Le chaume s'obtient en frappant une botte de foin a la hache, puis grise tout
seul en trois etapes (`ArcaBalance.THATCH_FADE_CHANCE`).

La brique VANILLA (bloc, escalier, dalle, muret) vieillit toute seule, comme
le cuivre (`block/VanillaBricks.java`, `ArcaBalance.BRICK_FADE_CHANCE`) :

brique vanilla -> `faded_brick_block` -> `worn_brick_block` ->
`weathered_brick_block` -> `cracked_brick_block`, soit 5 aspects. Chaque
etape a son bloc plein, son escalier, sa dalle et son muret. Aucune recette
de conversion : c'est le temps qui travaille, la hache qui rajeunit.

`waxed_brick_block` (et ses escalier, dalle, muret) : la brique CIREE. Elle
reprend la texture VANILLA et ne vieillit jamais. C'est ce que devient une
brique vanilla enduite de resine, puisque le bloc vanilla n'a pas d'etat
"cire". Un coup de hache la rend vanilla.

Les deux familles obeissent aux memes gestes (`block/ArcaWeathering.java`) :

| Geste | Effet |
| --- | --- |
| **touffe de resine** (clic droit) | fige l'etat courant (propriete `waxed`) |
| **hache** (clic droit) | enleve d'abord la resine, puis rajeunit d'une etape |

La cire est une propriete d'etat, pas un bloc separe : elle est **perdue quand
on ramasse le bloc**.

Textures **placeholder** des briques : `assets/arcamod/textures/block/
faded_brick_block.png`, `worn_brick_block.png`,
`weathered_brick_block.png`, `cracked_brick_block.png` (la brique ciree n'a
pas de texture : elle utilise celle du vanilla)
(brique vanilla patinee par `tools/gen_brick_textures.py` ; le script refuse
d'ecraser tes propres PNG sans `--force`).

---

## 13. Donnees vanilla remplacees

```bash
python3 tools/gen_vanilla_overrides.py
```

Depuis 26.3, le script ne recopie plus une version figee du fichier vanilla :
il LIT le fichier dans le jar du jeu (cache de Loom, version prise dans
`gradle.properties`) et n'y change que les reglages ci-dessous. Tout le reste
suit donc la version de Minecraft. **A relancer apres chaque montee de
version**, sinon les fichiers du mod restent sur l'ancienne version du jeu.

| Reglage | Constante du script |
| --- | --- |
| Fente (lunge) : propulsion verticale | `LUNGE_SCALE_Y` (1.0 = suit le regard) |
| Fente : poussee vers le haut ajoutee | `LUNGE_UPWARD_BIAS` |
| Fente : puissance | `LUNGE_MAGNITUDE_BASE` / `_PER_LEVEL` (None = valeur vanilla) |
| Fente : autorisee en vol / dans l'eau / monte | `LUNGE_ALLOW_*` (true/false) |
| Objets acceptes par Puissance et Recul | `ENCHANTMENT_SUPPORTED_ITEMS` |
| Diamants visibles dans les grottes | `DIAMOND_AIR_EXPOSURE_REDUCTION` (0.35 = -35%) |
| Quantite de diamant | `DIAMOND_FREQUENCY_MULTIPLIER` (1.0 = vanilla, aucun fichier de placement ecrit) |

---

## 14. Textures d'items

```bash
python3 tools/gen_pebble_textures.py --force
```

Genere les icones (caillou, outil, dague, fibres, ficelle). Sans `--force`, il
ne touche pas aux fichiers existants : tes propres dessins ne risquent rien.

---

## 15. Table d'archerie et flèches composées

Clic droit sur une table d'archerie vanilla : slots **pointe**, **corps**,
**empennage**. Chaque craft consomme une pièce de chaque et donne le plus petit
des trois `_YIELD`. Tout se règle dans `ArcaBalance`, section 21.

Chaque pièce a les mêmes réglages génériques (multipliés entre les 3 pièces) :
`_YIELD`, `_DAMAGE` (dégâts finaux), `_SPEED`, `_RANGE` (portée totale : la
gravité est calculée pour l'atteindre), `_INACCURACY`, `_KNOCKBACK`,
`_DRAW_SPEED` (bandage de l'arc), puis ses réglages d'effet.

| Pièce | Objet | Effet par défaut |
| --- | --- | --- |
| Pointe | Silex | normale |
| Pointe | Éclat d'améthyste | ignore 40% de l'armure (`_ARMOR_PIERCE`, `_TOUGHNESS_PIERCE`) |
| Pointe | Pépite de fer | transperce 1 créature, portée -15% (plus lourde) |
| Pointe | Éclat de prismarine | dégâts x2 contre les créatures aquatiques ou dans l'eau |
| Pointe | Boule de slime | 1 rebond, Lenteur IV 2,5 s, dégâts -50% |
| Pointe | Perle de l'Ender | téléporte le tireur (5 dégâts, 5% d'endermite) |
| Pointe | Poudre à canon | explosion 1.5, pas de dégâts directs, flèche détruite |
| Pointe* | Fruit de chorus | téléporte la cible (8 blocs), touche les endermen, dégâts x0.25, pas de recul |
| Pointe* | Débris de netherite | dégâts x1.5, bouclier désactivé 5 s, ne disparaît jamais, retombe en objet ; portée -20%, vitesse -10%, bandage -15% |
| Pointe* | Éclat d'écho | vibrations sans source (leurre), silencieuse, x3 contre le Warden qui oublie le tireur ; dégâts x0.8 |
| Pointe* | Pointe de soufre | Nausée 5 s, Poison I 3 s, +4 d'usure par pièce d'armure ; dégâts x0.8 |
| Pointe* | Stalactite pointue | jusqu'à x2 selon la vitesse de chute ; portée -20%, recul x0.5 |
| Pointe* | Pépite d'or | 50% de butin en double, seul le piglin touché s'énerve (pas d'alerte au groupe) ; dégâts x0.8 |
| Pointe* | Pépite de cuivre | x1.5 sur cible mouillée, foudre sous l'orage ; 8 flèches |
| Pointe* | Pépite d'or rose | +8% de dégâts par niveau d'enchantement de l'arc (max +40%), vitesse +20%, bandage +15% |
| Pointe* | Quartz | toujours critique, dégâts x1.25, x0.7 contre l'armure, se brise ; 8 flèches |
| Pointe* | Charge de vent | éclate comme une charge de vent (rafale, recul, 1 dégât fixe), rend 3 bulles d'air, arc bandé en 0,5 s (= recharge de la charge) |
| Corps | Bâton | normal |
| Corps | Bambou | portée +20%, recul x1.75, 65% de casse en se plantant |
| Corps | Bâton de blaze | enflamme créature (5 s) et bloc, disparaît dans l'eau |
| Corps | Bâton de breeze | sans gravité 3 s, recul x0.15 |
| Corps | Os | dégâts +30%, portée -30%, vitesse -20%, traverse les feuilles, brise 3 verres |
| Corps* | Canne à sucre | bandage +25%, dégâts -15%, recul x0.5 ; 8 flèches |
| Corps* | Bâton de l'End | lumière 12 en vol et plantée (lumières dynamiques) |
| Empennage | Plume | normal |
| Empennage | Membrane de phantom | portée +35%, dégâts x1.5 contre les volants et le vol plané |
| Empennage | Écaille de tatou | vitesse +20%, bandage +20%, dispersion x1.75 |
| Empennage* | Écaille de tortue | vol parfait sous l'eau, portée -10% |
| Empennage* | Algue | cible touchée dans l'eau : effet Entravé 4 s, coule sans pouvoir remonter ; 8 flèches |
| Empennage* | Aile de chauve-souris | dégâts x1.4 si la lumière à l'impact est ≤ 7 (nuit, grotte) |

\* Pièces de l'interrupteur `ARROW_NEW_PARTS` (la table d'archerie les refuse
quand il est coupé).

| Tag | Rôle |
| --- | --- |
| `data/arcamod/tags/block/bone_arrow_passes_through.json` | blocs traversés par l'os (feuilles) |
| `data/arcamod/tags/block/bone_arrow_breaks.json` | blocs brisés par l'os (verre, vitres) |
| `data/arcamod/tags/entity_type/arrow_prismarine_targets.json` | cibles du bonus prismarine (aquatiques + noyé) |
| `data/arcamod/tags/entity_type/arrow_anti_air_targets.json` | cibles volantes de la membrane de phantom |
| `data/arcamod/tags/entity_type/arrow_chorus_immune.json` | créatures que le chorus ne téléporte pas (boss) |

Le nom de la flèche cite ses pièces spéciales (« Flèche (Améthyste, Bambou) »),
format dans `lang/` : `item.arcamod.arrow_parts.name`. Les pièces sont gardées
en craftant des flèches spectrales et des flèches à effet (les 8 flèches
doivent alors être identiques).

### Textures

Une texture par pièce, assemblées automatiquement :

```
assets/arcamod/textures/item/arrow/tip/<id>.png
assets/arcamod/textures/item/arrow/shaft/<id>.png
assets/arcamod/textures/item/arrow/fletching/<id>.png
assets/arcamod/textures/item/arrow/spectral_overlay.png   (optionnel)
```

Puis `python3 tools/gen_arrow_models.py` et F3+T en jeu. Une combinaison
garde l'apparence vanilla tant qu'une de ses trois textures manque.

## 16. Flèches trempées

Clic droit sur un chaudron à potions avec des flèches (y compris celles de la
table d'archerie, qui gardent leurs pièces) :
`ARROW_DIP_ARROWS_PER_POTION` flèches par fiole, `ARROW_DIP_POTIONS_USED`
fioles retirées par trempage.

## 17. Cuir de chair putréfiée

Recette de feu de camp : `data/arcamod/recipe/rotten_flesh_to_leather_campfire.json`
(`cookingtime` 600 = 30 s). Seul le feu de camp l'accepte, pas le four.

## 18. Repos au feu de camp

Main vide, clic droit sur le dessus d'un bloc du tag
`data/arcamod/tags/block/campfire_seats.json` (bûches, dalles, escaliers,
tapis, botte de foin) avec un feu de camp allumé à portée. On se relève en
s'accroupissant.

| Réglage | Défaut |
| --- | --- |
| `CAMPFIRE_REST_RADIUS` | 3 blocs |
| `CAMPFIRE_REST_INTERVAL_TICKS` | 300 (15 s) |
| `CAMPFIRE_REST_HEAL` / `CAMPFIRE_REST_FOOD` | 1 / 1 (demi-cœur, demi-gigot) |
| `CAMPFIRE_SEAT_HEIGHT_OFFSET` | -0.15 (hauteur du joueur assis) |

## 19. Bannière de camp

N'importe quelle bannière (sol ou mur) à moins de `CAMP_BANNER_CAMPFIRE_RADIUS`
blocs d'un feu de camp allumé : aucun monstre n'apparaît naturellement dans un
rayon de `CAMP_BANNER_RADIUS` blocs. Les monstres déjà présents, les spawners,
les patrouilles, les phantoms et les raids ne sont pas concernés.

## 20. Lance-pierre et résine

Valeurs : `ArcaBalance` sections 26 (lance-pierre, munitions) et 27 (résine).

| Fichier de données | Rôle |
| --- | --- |
| `data/arcamod/recipe/slingshot.json` | bâton + ficelle de fibres (diagonale) + cuir sous la ficelle |
| `data/minecraft/enchantment/power.json`, `knockback.json` | copies vanilla, `supported_items` pointe vers `#arcamod:enchantable/power` / `knockback` |
| `data/arcamod/tags/item/enchantable/*.json` | ajoute le lance-pierre à Puissance et Recul |
| `assets/arcamod/items/slingshot.json` | animation de tension : `scale` = 1 / `SLINGSHOT_FULL_DRAW_TICKS` (0.0625 pour 16) |

La pointe de résine de la table d'archerie : `ARROW_RESIN_*` (section 21).

## 21. Tête d'Enderman, soufre

- Tête : `ArcaBalance` section 28. Lâchée via la table `minecraft:charged_creeper/root`
  (code : `ModLootTables`). Portée, elle déguise comme une citrouille
  (`data/minecraft/tags/item/gaze_disguise_equipment.json`).
- Soufre : section 29. Poudre à canon : `data/arcamod/recipe/gunpowder_from_sulfur.json`
  (charbon + poudre de soufre + 2 poudres d'os = 2 poudres à canon).

## 22. Or rose

Valeurs : `ArcaBalance` section 30 (outils, armure, génération).

| Fichier de données | Rôle |
| --- | --- |
| `data/arcamod/recipe/raw_pink_gold.json` | 4 cuivre brut + 4 or brut = 1 or rose brut (changer `count`) |
| `data/arcamod/recipe/pink_gold_*_from_golden_smithing.json` / `_from_copper_smithing.json` | amélioration à la table de forge, garde les enchantements |
| `data/arcamod/trim_material/pink_gold.json` | couleur de garniture |
| `data/minecraft/tags/block/needs_iron_tool.json` | minerais : pioche en fer minimum |

## 23. Modèle de forge, cuivre oxydé, piment des âmes, écho

Valeurs : `ArcaBalance` sections 30 (chance dans les épaves), 31 (cuivre), 32 (piment), 33 (écho).

| Fichier de données | Rôle |
| --- | --- |
| `data/arcamod/recipe/pink_gold_upgrade_smithing_template.json` | tuf autour, modèle au-dessus, lingot au centre : 2 modèles |
| `data/arcamod/recipe/pink_gold_*_smithing.json` | le champ `template` exige le modèle |
| `data/arcamod/worldgen/placed_feature/patch_soul_pepper_bush.json` | fréquence (`count`) et étalement des buissons dans la vallée des âmes |
| `data/arcamod/loot_table/blocks/soul_pepper_bush.json` | piments lâchés quand on CASSE le buisson (la récolte au clic droit est dans ArcaBalance) |
| `data/arcamod/tags/block/soul_pepper_plantable_on.json` | blocs où le buisson peut être planté |
| `data/arcamod/recipe/echo_trim.json` | armure garnie + éclat d'écho = garniture pulsante |
| `data/minecraft/tags/item/enchantable/sharp_weapon.json` | le trident accepte Tranchant (enclume) |

## 24. Deblocage des recettes et advancements

Une recette n'apparait dans le livre de recettes que si un advancement de
deblocage l'a donnee au joueur. Ces fichiers se generent :

```bash
python3 tools/gen_recipe_advancements.py          # ecrit ce qui manque
python3 tools/gen_recipe_advancements.py --check  # liste sans rien ecrire
python3 tools/gen_recipe_advancements.py --force  # reecrit TOUT
```

Le script lit `data/arcamod/recipe/` et ecrit `data/arcamod/advancement/recipes/`.
Il **ne touche jamais** a un fichier existant sans `--force` : les advancements
ecrits a la main restent intacts. A relancer apres avoir ajoute des recettes
(ou une seule fois en fin de developpement, `--check` sert de verification).

L'ingredient qui declenche le deblocage est choisi automatiquement (le premier
ingredient du mod, sinon le premier ingredient vanilla pas trop banal). Pour
l'imposer, ajouter une ligne dans `KEY_INGREDIENTS`, en tete du script. Les
recettes de type `arcamod:*` et le brassage sont ignores : ils ne passent pas
par le livre de recettes.

### Branche de progression

Cinq advancements visibles, dans `data/arcamod/advancement/progression/` :
caillou -> dague en silex -> fibres vegetales -> outil en silex -> premier fer.
Titres et descriptions : cles `advancements.arcamod.<nom>.*` dans `lang/`.

### Messages d'aide

`Hints.showOnce(joueur, "<nom>")` envoie un message une seule fois par joueur.
La memoire est un advancement cache (`data/arcamod/advancement/hints/<nom>.json`,
sans bloc `display`). Le seul en place previent le joueur qui frappe une buche
sans le bon outil. Pour retester : `/advancement revoke <joueur> only
arcamod:hints/<nom>`.

## 25. Seau a allay

Seau vide + clic droit (accroupi) sur un allay : il est range dans le seau,
avec l'objet qu'il tient. Clic droit sur un bloc : il ressort tel quel.

| Reglage | Defaut |
| --- | --- |
| `ALLAY_BUCKET_REQUIRES_SNEAK` | `true` (sinon on ne peut plus lui donner un seau a porter) |
| `ALLAY_BUCKET_RETURNS_EMPTY_BUCKET` | `true` |
| `ALLAY_BUCKET_PERSISTENT` | `true` (l'allay relache ne disparait plus jamais) |

Interrupteur : `ArcaFeature.ALLAY_BUCKET`. Code : `util/AllayBucket.java`
(rangement et sortie), `item/AllayBucketItem.java` (clic droit), et
`ModEvents.registerAllayBucketing` (capture).

L'allay est enregistre entier dans le composant vanilla `entity_data` : nom,
vie, souvenirs et inventaire. Position, vitesse et UUID sont effaces au
passage (`AllayBucket.CLEARED_TAGS`), sinon l'allay relache reviendrait a
l'endroit ou il a ete attrape. Le composant `arcamod:bucketed_item` ne sert
qu'a l'infobulle du seau.

Texture placeholder (copie du seau a axolotl) :
`assets/arcamod/textures/item/allay_bucket.png`.


## 26. Corde

Une corde qui s'accroche **sous** un bloc et qui ne pousse que vers le bas. On
y descend comme a l'echelle (tag `minecraft:climbable`).

L'accroche sur le **cote** d'un bloc est desactivee (`ROPE_WALL_ANCHOR =
false`) : un mur voisin retenait la corde quand son plafond se brisait, et
elle restait pendue dans le vide. La remettre a `true` reactive l'ancrage
mural.

### Toujours le long d'un cote

La corde n'est jamais au milieu du bloc : elle court **le long d'un des quatre
cotes**, paroi ou pas, comme une liane. C'est ce qui lui donne sa continuite -
une corde qui longe un mur puis debouche dans le vide garde le meme cote et
reste dans le meme plan, au lieu de sauter d'un coup au centre.

| Pose | Cote choisi |
| --- | --- |
| Clic sur le **cote** d'un bloc | le long de ce bloc |
| Clic **sous** un bloc | le cote le plus proche du point vise sur la face |

Les maillons du dessous **heritent** du cote du maillon du haut, sans jamais le
remettre en question. Une plaquette impose le sien a la corde qui pend dessous.

Le modele est une **croix a 45 degres** : deux plans de la largeur du cordage
qui se croisent sur son axe, a 2 px de la face, tournes d'un quart de quart de
tour comme les plantes et les chaines vanilla.

Ce quart de quart de tour n'est pas decoratif. Une croix "droite" met le plan
perpendiculaire pile sur l'axe de l'autre, et vu de face il passe sur la
jointure du cordage et en mange une colonne selon l'angle : la corde paraissait
faire 1 px au lieu de 2. A 45 degres, aucun des deux plans n'est jamais vu de
profil.

Les faces sont en `shade: false` pour que la corde garde la meme luminosite
quel que soit le cote qu'elle longe.

Les modeles lisent la texture la ou elle est vraiment dessinee : **colonnes 7
et 8**, dont le milieu tombe pile au centre du bloc - c'est ce qui fait que les
deux plans se croisent en leur milieu.

**Si tu redessines le cordage a une autre largeur**, trois endroits a reporter,
sinon les deux couches se croisent de travers :

| Ou | Quoi |
| --- | --- |
| `models/block/rope.json`, `rope_end.json`, `rope_hitch.json` | les `from`/`to` et les `uv` |
| `RopeBlock.CORD_INSET` | la distance du cordage a la paroi |
| `RopePlateRenderer.WIDTH`, `U_MIN`, `U_MAX` | la largeur du second brin |

### Cinq cordes tirees au sort

Le brin principal existe en **cinq textures** (`rope.png`, `rope_2.png` ...
`rope_5.png`). Le blockstate donne les cinq modeles dans la meme `apply` : le
jeu en choisit un **selon la position du bloc**, toujours le meme pour une
position donnee, si bien qu'une corde n'a jamais le meme motif sur toute sa
longueur.

Les quatre variantes sont des **placeholders** : c'est `rope.png` decalee
verticalement de 3, 6, 9 et 12 px, pour que les maillons ne tombent pas au
meme endroit. A remplacer par de vraies variantes quand tu voudras.

Le bout de corde (`rope_end`), le raccord (`rope_hitch`) et le second brin du
rendu gardent chacun leur texture : ils ne sont pas tires au sort.

La boite de selection suit : `ROPE_WALL_THICKNESS` d'epaisseur,
`ROPE_HITBOX_WIDTH` de large.

Accrocher et longer restent deux choses differentes : la corde pend toujours a
ce qu'elle a **au-dessus**, le cote longe n'est qu'une position.

### Amarrage : la plaquette

En speleologie une corde n'est sure qu'avec **deux points d'ancrage**, chacun
avec sa plaquette, les deux brins rejoignant la corde principale pour repartir
la charge. C'est ce que le mod reproduit, en trois etages :

| Amarrage | Ce qui tient la corde | Ce qui lache |
| --- | --- | --- |
| **Aucun** (corde nouee sur le bloc) | le bloc lui-meme | le **bloc**, et tout ce qui etait dessus tombe |
| **Une plaquette** au-dessus de la corde | la plaquette | la **plaquette** s'arrache, la corde tombe avec |
| **Deux plaquettes** | les deux, charge repartie | la **seconde** plaquette s'arrache : la corde tient, l'amarrage retombe a une plaquette |

Ce qui lache est **perdu** : le bloc d'accroche saute sans rien lacher, la
plaquette arrachee disparait, et **seule la corde** retombe en objets. Une
rupture coute donc son amarrage. Deux interrupteurs pour revenir en arriere :
`ROPE_ANCHOR_DROPS_WHEN_BROKEN` et `ROPE_PLATE_DROPS_WHEN_RIPPED`, tous deux a
`false` par defaut.

La plaquette (`arcamod:rope_plate`, 3 pepites de fer pour 2) se visse **sous un
bloc plein ou contre un mur** (propriete `support` : `up` ou une direction
horizontale) et, comme la corde, court le long d'un cote (propriete `side` :
celui du mur, ou celui qu'on visait au plafond). La corde pendue juste dessous
reprend ce cote, les deux restent donc alignees.

Son modele est un **maillon de 5 px** (`ROPE_PLATE_HEIGHT`) colle en haut du
bloc, en **deux variantes** qui ont chacune sa texture :

| Pose | Modele | Texture |
| --- | --- | --- |
| Sous un bloc (`support=up`) | `block/rope_plate` | `block/rope_plate.png` |
| Contre un mur (`support=<horizontale>`) | `block/rope_plate_wall` | `block/rope_plate_wall.png` |

Les deux textures sont pour l'instant des **placeholders identiques** (copie de
la chaine en fer vanilla). Leur decoupage est celui de la chaine : colonnes
**0 a 3** pour le plan de face, colonnes **3 a 6** pour le plan
perpendiculaire, et seules les **5 premieres lignes** servent. Attention, en
26.3 la chaine vanilla s'appelle `block/iron_chain`, plus `block/chain`.

### Miner une plaquette chargee recupere la CORDE

Des qu'une corde y pend, la boite de la plaquette descend sur **tout le bloc**
(`ROPE_PLATE_ROPED_HEIGHT`) pour couvrir le raccord dessine dessous : sans
elle, il n'y aurait rien a viser et les clics passeraient au travers.

Mais ce raccord ressemble a de la corde, et un joueur qui tape dedans veut
recuperer sa corde - pas devisser son amarrage, perdre la plaquette (usage
unique) et faire tomber toute la longueur. Alors **miner une plaquette chargee
recolte le maillon du bas de la corde**, exactement comme miner la corde :
meme temps de casse, meme objet rendu en main propre, la plaquette ne bouge
pas.

Pour devisser pour de bon : **s'accroupir** en minant, ou retirer toute la
corde d'abord. Interrupteur : `ROPE_PLATE_MINING_HARVESTS_ROPE`.

La seconde plaquette se pose **n'importe ou autour de la premiere** : les
faces, les aretes et les coins de son cube 3x3x3, quel que soit son propre
support. Deux cases seulement sont refusees, **pile au-dessus et pile en
dessous** : une plaquette dans la meme colonne ne repartit rien, elle reprend
la meme ligne de charge.

Le second brin ne passe PAS par un modele de bloc : il relie deux points
quelconques de l'espace (la base du maillon de la jumelle et le noeud sur la
corde mere), et la jumelle peut etre dans n'importe laquelle des 24 cases, avec
son propre cote longe. Ca fait des centaines de geometries : impossible a
ecrire une par une.

Il est donc **dessine a la volee** par `RopePlateRenderer` (client), un
BlockEntityRenderer accroche a la plaquette - d'ou le `RopePlateBlockEntity`,
qui ne stocke rien d'autre. Le rendu tend un cordage entre les deux points :
deux bandes croisees, decoupees en troncons d'environ un bloc pour que la
texture se repete au lieu de s'etirer. Chaque bande n'est posee qu'**une
fois** : deux faces au meme endroit (une par sens) et les pixels se mettent a
clignoter, chacune passant devant l'autre selon l'angle. C'est **la plaquette qui porte la
corde** qui le dessine, pas la jumelle, sinon le brin serait dessine deux fois.

Consequence : n'importe quelle case marche, dans n'importe quelle combinaison
de cotes, sans nouveau modele. Les reperes geometriques sont dans `RopeBlock` :
`CORD_INSET` (a quelle distance du cote court le cordage) et `KNOT_HEIGHT` (la
hauteur du noeud), partages par les modeles statiques et par le rendu.

**Ordre de pose** : la plaquette occupe la case ou la corde s'accrocherait. On
visse donc la (ou les) plaquette(s) D'ABORD, puis on pend la corde sous la
premiere. Pour securiser une corde deja en place, il faut casser son maillon
du haut, poser les plaquettes, et la re-pendre.

La plaquette fait partie de la **chaine de pose** : un clic droit dessus avec
de la corde rallonge la corde qui y pend, exactement comme un clic sur la
corde (accroupi, elle se deroule jusqu'au sol). L'inverse n'est pas vrai : la
plaquette ne rend PAS de corde au clic gauche.

**Usage unique** : une plaquette posee ne se recupere pas. Son butin
(`loot_table/blocks/rope_plate.json`) est sous `tool/can_silk_touch` : seule
la Toucher de soie la rend. Et la casser fait tomber toute la corde qui y
pendait, en objets.

Si le brin en biais pointe du mauvais cote une fois en jeu, deux valeurs a
inverser : le signe de `angle` dans `models/block/rope_strand*.json`, ou la
correspondance `facing` -> `y` dans `blockstates/rope.json`.

### Comment le modele est assemble

`blockstates/rope.json` est un **multipart** : chaque morceau s'ajoute selon
l'etat, ce qui evite d'ecrire un modele par combinaison.

| Morceau | Condition | Modeles |
| --- | --- | --- |
| Brin principal | `side` + `bottom` | `rope`, `rope_end` |
| Raccord a la plaquette | `hitched=true` | `rope_hitch` |

Chaque morceau existe en un seul exemplaire, tourne par le `y` du blockstate
selon `side` : 12 parties pour 3 modeles. Le second brin, lui, est dessine par
le rendu (voir plus haut).

Le raccord est dessine **dans le bloc de la corde**, pas dans celui de la
plaquette : une face est eclairee par la lumiere de son propre bloc, et celui
de la plaquette, colle au plafond, est plus sombre - le raccord paraissait
alors d'un ton en dessous du reste de la corde.

### Usure

Tant qu'au moins un grimpeur est **pendu** a la corde (les pieds au sol ne
comptent pas ; les creatures comptent si `ROPE_MOBS_COUNT_AS_LOAD`), un tirage
a lieu toutes les `ROPE_STRESS_CHECK_TICKS` (1 s par defaut) :

| Grimpeurs | Corde nue `ROPE_STRESS_CHANCES` | 1 plaquette `ROPE_PLATE_STRESS_CHANCES` | 2 plaquettes `ROPE_RIG_STRESS_CHANCES` |
| --- | --- | --- | --- |
| 1 | 2 % | 0,2 % | 0,05 % |
| 2 | 15 % | 1 % | 0,1 % |
| 3 | **90 %** | 5 % | 0,3 % |
| 4 | 90 % | 25 % | 1 % |
| 5 | 90 % | **90 %** | 3 % |
| 6 | 90 % | 90 % | 10 % |
| 7 | 90 % | 90 % | 35 % |
| 8 et + | 90 % | 90 % | **90 %** |

Les cases en gras sont le seuil ou l'amarrage **cede dans les deux secondes** :
3 grimpeurs a nu, 5 avec une plaquette, 8 avec deux. A `ROPE_BREAKING_CHANCE`
= 0,90, le premier tirage passe neuf fois sur dix, et la seconde de craquement
suit.

Il n'y a **plus aucune casse nette** : au-dela du tableau, la derniere case est
simplement reutilisee, donc meme une surcharge passe par le craquement. Et
aucune case n'est a zero : meme seul sur un amarrage reparti, on n'est jamais
completement a l'abri (une chance sur deux mille par seconde).

Les tirages ont lieu **par seconde passee pendu** : 2 % a un grimpeur, c'est
une corde nue qui tient plus d'une minute en moyenne.

### Craquement

Un tirage gagnant ne casse rien tout de suite : un **craquement** part d'abord
(son `CREAKING_HEART_HURT`), et la rupture arrive `ROPE_WARNING_TICKS` ticks
plus tard (1 s). Une fois le craquement parti, c'est certain : c'est un
avertissement trop tardif, pas une seconde chance. L'etat `failing=true` du
bloc porte cette attente, elle survit donc a une sauvegarde.

### Ce qui emporte une corde

| Cause | Effet |
| --- | --- |
| **Explosion** | durete et resistance nulles (`instabreak()`) : la corde part avec, et ce qui pendait dessous tombe |
| **Eau ou lave qui arrive dessus** | le liquide prend sa place et la corde tombe en objet : une cascade coupe une corde |
| **Lave / feu** | `ignitedByLava()` : elle brule |

La corde ne s'immerge pas (plus de waterlogging) et ne **remplace rien** a la
pose : ni l'eau, ni les herbes, ni la neige. Elle ne se pend que dans le vide,
et le deroulage s'arrete au premier obstacle, liquide compris.

La plaquette, elle, **resiste a l'eau** : c'est une piece de metal vissee dans
la roche, le liquide la contourne (`canBeReplaced(state, fluid)` a `false`).

### Montee

`ROPE_ALLOW_CLIMB_UP` (defaut `false`) : la corde ne sert qu'a descendre, la
poussee vers le haut est annulee. A `true`, elle se comporte comme une
echelle.

### Donnees

| Fichier | Contenu |
| --- | --- |
| `data/arcamod/recipe/rope.json` | 3 ficelles vanilla en diagonale + 4 ficelles vegetales (`arcamod:plant_cord`), 2 angles vides, donne 6 cordes |
| `data/arcamod/recipe/rope_plate.json` | 3 pepites de fer (`N N` / ` N `), donne 2 plaquettes |
| `data/arcamod/loot_table/blocks/rope_plate.json` | la plaquette ne se recupere qu'a la Toucher de soie |
| `assets/arcamod/models/block/rope_*.json` | les morceaux assembles par le multipart (voir plus haut) |
| `client/render/RopePlateRenderer.java` | le second brin, tendu a la volee entre la jumelle et la corde |
| `assets/arcamod/models/block/rope_plate.json` | plaquette (mur ou plafond, meme modele) |
| `assets/arcamod/textures/block/rope_plate.png` | **placeholder** de la plaquette de plafond |
| `assets/arcamod/textures/block/rope_plate_wall.png` | **placeholder** de la plaquette murale |
| `data/arcamod/tags/block/no_break_overlay.json` | blocs sans texture de craquelure |
| `data/arcamod/loot_table/blocks/rope.json` | une corde par maillon (chute, explosion) |
| `data/minecraft/tags/block/climbable.json` | ajoute la corde aux blocs ou l'on grimpe |
| `assets/arcamod/textures/block/rope.png` | corde |
| `assets/arcamod/textures/block/rope_2..5.png` | les quatre variantes tirees au sort (**placeholders** : `rope.png` decalee) |
| `assets/arcamod/textures/block/rope_end.png` | dernier maillon |
| `assets/arcamod/textures/item/rope.png` | **placeholder** de l'objet corde (laisse vanilla) |
| `assets/arcamod/textures/item/rope_plate.png` | **placeholder** de l'objet plaquette (chaine en fer vanilla) |

---

## Trident, tortues, chevres et bibliotheques

Ajouts recents, tous coupables depuis **Options > ArcaMod**.

### Trident (`TRIDENT_DAMAGE_BUFF`)

`ArcaBalance.TRIDENT_DAMAGE_MULTIPLIER` (defaut `1.30`) multiplie les degats
en main (`ModVanillaItemTweaks`) ET au lancer (`ThrownTridentDamageMixin`).
Le corps a corps est fige au demarrage : relancer le jeu apres un changement.

### Ecaille de tortue au pinceau (`TURTLE_BRUSHING`)

| Reglage | Constante |
| --- | --- |
| Chance d'obtenir une ecaille | `TURTLE_BRUSH_SCUTE_CHANCE` (0.35) |
| Nombre d'ecailles | `TURTLE_BRUSH_SCUTE_COUNT` |
| Recharge du pinceau | `TURTLE_BRUSH_COOLDOWN_TICKS` (67 = 3,35 s) |
| Usure du pinceau | `TURTLE_BRUSH_TOOL_DAMAGE` |
| Adultes seulement | `TURTLE_BRUSH_ADULTS_ONLY` |

### Butin de la chevre (`GOAT_EXTRA_DROPS`, recharger le monde)

Laine `GOAT_WOOL_DROP_CHANCE` (0.65), couleur `GOAT_WOOL_COLOR`, corne
`GOAT_HORN_DROP_CHANCE` (0.05) avec un instrument tire au hasard parmi le tag
`minecraft:goat_horns`.

### Casque de tortue (`TURTLE_HELMET_UNDERWATER_MINING`)

`TURTLE_HELMET_SUBMERGED_MINING_MULTIPLIER` (defaut `5.0`) annule la penalite
de minage sous l'eau, comme Affinite aquatique. Fige au demarrage.

### Bibliotheques eloignees (`BOOKSHELF_RANGE`)

| Reglage | Constante |
| --- | --- |
| Portee maximale | `BOOKSHELF_MAX_DISTANCE` (10 blocs) |
| Distance ou une bibliotheque vaut encore 1 | `BOOKSHELF_FULL_POWER_DISTANCE` (2) |
| Puissance du niveau 30 | `BOOKSHELF_POWER_FOR_MAX_LEVEL` (15) |
| Bibliotheques necessaires a la portee maximale | `BOOKSHELF_SHELVES_AT_MAX_DISTANCE` (60) |
| Hauteurs fouillees | `BOOKSHELF_MIN_HEIGHT` / `BOOKSHELF_MAX_HEIGHT` |
| Mur obligatoirement degage | `BOOKSHELF_NEEDS_CLEAR_PATH` |
| Particules des lointaines | `BOOKSHELF_FAR_PARTICLE_CHANCE` |

Le poids d'une bibliotheque descend en ligne droite entre les deux distances :
1.00 a 2 blocs, 0.25 a 10 blocs (donc 60 pour le niveau 30). Le calcul est
dans `util/EnchantingPower.java`.

---

## Support de canne a peche (`FISHING_ROD_STAND`)

Un bloc ou l'on pose une canne : elle peche toute seule, sans aucun timing a
respecter. La prise attend qu'on releve la canne.

| Geste | Effet |
| --- | --- |
| **clic droit avec une canne, support vide** | pose la canne, qui part a l'eau aussitot (`FISHING_STAND_AUTO_CAST_ON_PLACE`) |
| **clic droit** (main vide ou pleine) | lance ou releve la ligne ; c'est au releve que se joue la prise |
| **clic droit accroupi** | reprend la canne (dans la main si elle est libre, sinon elle tombe au pied du support), prise comprise |
| **impulsion de redstone** | pareil que le clic simple : une impulsion releve, la suivante relance |
| **comparateur** | 0 vide, 1 canne rangee, 5 en train de pecher, 15 ca mord |

Un comparateur sur "ca mord" (15) qui renvoie une impulsion au support suffit
donc a automatiser toute la boucle.

| Reglage | Constante |
| --- | --- |
| Attente avant que ca morde | `FISHING_STAND_MIN_WAIT_TICKS` / `_MAX_WAIT_TICKS` (1200-2400 = 1 a 2 min) |
| Chance d'avoir du butin en relevant | `FISHING_STAND_CATCH_CHANCE` (0.85) |
| Appat (Lure) | `FISHING_STAND_LURE_TICKS_PER_LEVEL` |
| Chance de la mer | `FISHING_STAND_LUCK_PER_LEVEL` |
| Pluie | `FISHING_STAND_RAIN_WAIT_MULTIPLIER` |
| Usure de la canne | `FISHING_STAND_ROD_DAMAGE_PER_CATCH` / `_ON_MISS` |
| Experience lachee | `FISHING_STAND_XP_MIN` / `_XP_MAX` |
| **Distance de lancer** | `FISHING_STAND_MIN_CAST_DISTANCE` / `_MAX_CAST_DISTANCE` (3 a 8 blocs, tiree a chaque lancer) |
| Lancer du cote du support | `FISHING_STAND_CAST_IN_FACING_DIRECTION` |
| Repli sur l'eau la plus proche si rien d'assez loin | `FISHING_STAND_FALLBACK_TO_NEAREST_WATER` (false : le lancer echoue) |
| Recherche d'eau | `FISHING_STAND_WATER_MIN_HEIGHT`, `_WATER_MAX_HEIGHT`, `_NEEDS_OPEN_WATER` |
| Duree pendant laquelle la prise attend | `FISHING_STAND_BITE_TIMEOUT_TICKS` (0 = indefiniment) |
| Particules du bouchon | `FISHING_STAND_PARTICLE_INTERVAL_TICKS` |
| Hauteur d'apparition du butin | `FISHING_STAND_LOOT_DROP_HEIGHT` |
| Signaux du comparateur | `FISHING_STAND_SIGNAL_*` |

Une canne qui casse **reste sur le support** : elle y prend l'apparence d'un
outil casse et ne peche plus tant qu'on ne l'a pas reprise et reparee (regle
commune du mod, `ArcaFeature.BROKEN_TOOLS_KEPT` ; interrupteur coupe, elle
disparait comme en vanilla).

Le butin est celui de la table vanilla `minecraft:gameplay/fishing`, tire
exactement comme pour un joueur. Il tombe juste au-dessus du support : un
entonnoir dessous le ramasse.

### Donnees

| Fichier | Contenu |
| --- | --- |
| `data/arcamod/recipe/fishing_rod_stand.json` | 2 batons sur 3 planches |
| `data/arcamod/loot_table/blocks/fishing_rod_stand.json` | le bloc se ramasse tel quel (la canne posee tombe a part) |
| `assets/arcamod/models/block/fishing_rod_stand.json` | pied + montant, en textures **vanilla** (chene et chene ecorce) |
| `client/render/FishingRodStandRenderer.java` | la canne posee (trois poses), le fil et le flotteur. Constantes en tete de la classe : `TEXTURE_ROLL` (redresse la canne, dessinee en diagonale dans sa texture), `SPRITE_YAW` (quart de tour autour du manche : de face on voit la tranche), `ROD_SHIFT` / `ROD_TIP_DISTANCE`, `TILT_IDLE` / `TILT_CAST` / `TILT_BITE` (0 = canne verticale), houle et plongeon du flotteur |
| flotteur | texture **vanilla** `textures/entity/fishing/fishing_hook.png`, tournee vers la camera comme le bouchon d'un joueur |
| `assets/arcamod/items/fishing_rod_cast.json` | pointe vers le modele **vanilla** de la canne lancee (sans le fil qui pend), affiche tant que la ligne est a l'eau |

## 46. Baril de TNT

`ArcaBalance` section 46, interrupteur `TNT_BARREL`. Recette : tonneau au
centre, poudre à canon dans les coins, poudre de blaze sur trois côtés et
obsidienne pleureuse en bas (`recipe/tnt_barrel.json`). Ou clic droit avec une
TNT sur un tonneau contenant 3 poudres de blaze et 1 obsidienne pleureuse.

L'explosion fige la lave : chaque source dans un rayon de
`TNT_BARREL_LAVA_RADIUS` devient du basalte, ou de l'obsidienne pleureuse
(`TNT_BARREL_LAVA_CRYING_OBSIDIAN_CHANCE`) ; la lave qui coule disparaît.

| Réglage | Défaut |
| --- | --- |
| `TNT_BARREL_EXPLOSION_POWER` | 8 (TNT : 4) |
| `TNT_BARREL_FUSE_TICKS` | 80 (4 s) |
| `TNT_BARREL_LIT_LIGHT` | 12 (lumière pendant la mèche ; relancer le jeu) |
| `TNT_BARREL_CHAIN_FUSE_MIN_TICKS` / `_MAX_TICKS` | 10 / 30 (allumé par une autre explosion) |
| `TNT_BARREL_NO_DROPS` | true : aucun objet lâché par les blocs détruits (le contenu des conteneurs et l'équipement d'un joueur tué sont épargnés) |
| `TNT_BARREL_PLAYER_DAMAGE_MULTIPLIER` | 0.5 (dégâts du souffle sur les joueurs) |
| `TNT_BARREL_CONVERT_BLAZE_POWDER` / `_CRYING_OBSIDIAN` / `_TNT` | 3 / 1 / 1 |
| `TNT_BARREL_LAVA_RADIUS` | 6 blocs |
| `TNT_BARREL_LAVA_CRYING_OBSIDIAN_CHANCE` | 0.1 (sinon basalte) |

Reste un bloc solide pendant la mèche. S'allume au briquet, à la boule de feu,
à la redstone, par un projectile enflammé ou une explosion ; pas par le feu seul.

## 47. Plume et Chute amortie

- `FEATHER_PUSH` : frapper une créature avec une plume la pousse
  (`FEATHER_PUSH_STRENGTH` 0.6, `FEATHER_PUSH_UPWARD` 0.1), sans dégâts ni colère.
- `FEATHER_FALLING_NO_TRAMPLE` : des bottes avec Chute amortie
  (`FEATHER_FALLING_NO_TRAMPLE_MIN_LEVEL` 1) ne piétinent plus la terre labourée.

## 48. Garnitures d'outils

Interrupteur `TOOL_TRIMS`. Les garnitures d'armure se posent à la table de
forge sur les épées, pioches, haches, pelles, houes (tous matériaux, or rose
compris) et l'arc. Un dessin par motif et par type d'outil :
`assets/arcamod/textures/trims/items/tools/<type>/<motif>.png` (gris de la
palette `trim_base`), puis `python3 tools/gen_tool_trims.py`. Détails dans
l'en-tête du script.


---

## 49. Élytres, lances, arc, feuilles, feu, archéologie

| Sujet | Où régler |
| --- | --- |
| Garnitures élytres / lances / arc bandé | `python3 tools/gen_tool_trims.py` (textures placeholder listées en tête du script) |
| Teinture des élytres | `recipe/elytra_dyed.json`, `recipe/patchwork_elytra_dyed.json` ; lavage au chaudron (eau ou lessive) |
| Textures teintables | `assets/arcamod/textures/item/*_dyed.png`, `assets/arcamod/textures/entity/equipment/wings/*_dyed.png` |
| Garnitures portées sur les ailes | `assets/minecraft/textures/trims/entity/wings/<motif>.png` (64x32, gris EXACTS de la palette clé) + `<motif>.png.mcmeta` (palette `trim_base`, écrit par le script). `python3 tools/fix_trim_palette.py` recale tes dessins sur la palette |
| Icônes élytres garnies | `assets/arcamod/textures/trims/items/elytra/<motif>.png` (une par motif, comme les outils). Seules les élytres vanilla se garnissent (`TRIMMABLE_ELYTRAS` dans le script) ; celles en membranes se teignent seulement |
| Fibres à l'épée | tags `arcamod:cuts_plant_fiber` (lames) et `arcamod:plant_fiber_grasses` (herbes) ; `recipe/blade_fiber.json`. Une lame **cassée** ne donne plus rien, et chaque herbe coupée coûte `BLADE_INSTANT_BREAK_DURABILITY_COST` |
| Coffre bonus | `ArcaBalance.STARTER_CHEST_*` |
| Aide silex | `ArcaBalance.FLINT_HINT_DELAY_TICKS`, `advancement/hints/no_flint_yet.json` |
| Branches dans les feuilles (main nue) | `ArcaBalance.LEAVES_HAND_STICK_CHANCE` ; avancement `progression/stick` |
| Cailloux des grottes | `ArcaBalance.CAVE_PEBBLE_*`, `worldgen/*/cave_pebbles.json` |
| Œuf de renifleur | `ArcaBalance.SNIFFER_EGG_ARCHAEOLOGY_CHANCE` (toutes les tables `archaeology/*`) |
| Feuilles / feu | `ArcaBalance` section 49 |

Le rendu d'item vanilla ne sait pas faire briller un calque : les recettes
d'encre lumineuse et d'éclat d'écho ne s'appliquent plus qu'à ce qui se porte
(armures, élytres).



---

## Les générateurs de `tools/` et tes dessins

**Aucun script ne remplace une texture déjà présente, même avec `--force`.**
Les placeholders sont là pour combler un trou, pas pour écraser ton travail.

| Option | Ce qu'elle refait |
| --- | --- |
| *(rien)* | seulement ce qui manque |
| `--force` | les modèles, blockstates et fichiers JSON |
| `--reset-textures` | **aussi les textures**, en écrasant tes dessins |

Deux exceptions, volontaires :

- les images **calculées** à partir d'une autre suivent `--force` : les 16
  crans de fondu de la lanterne (dérivés de `eyeblossom_lantern_off/on.png`)
  et les 756 flèches en vol (dérivées de tes 31 icônes de pièces). Les
  redessiner à la main n'aurait pas de sens, la moindre retouche de la source
  les refabrique ;
- `fix_trim_palette.py` recale volontairement tes dessins sur la palette des
  garnitures : c'est tout son travail.

---

## 50. Les trois fleurs (lanterne, gousse, pitcher plant)

### Lanterne d'eyeblossom (`EYEBLOSSOM_LANTERN`)

8 pépites de fer autour d'une eyeblossom, ouverte **ou** fermée. Posée, elle
dort ; un joueur à moins de `EYEBLOSSOM_LANTERN_RADIUS` blocs la réveille et
elle gagne **`EYEBLOSSOM_LANTERN_LEVELS_PER_STEP` niveaux de lumière tous les
`EYEBLOSSOM_LANTERN_TICKS_PER_STEP` ticks** jusqu'à
`EYEBLOSSOM_LANTERN_MAX_LIGHT`. Quand on s'éloigne, elle redescend au même
rythme.

**Deux réglages pour la vitesse, et c'est voulu** : un bloc ne peut pas jouer
plus d'une fois par tick, donc `TICKS_PER_STEP = 1` est le plancher. Pour
aller plus vite, il faut monter le nombre de niveaux gagnés par cran.

| Réglage | Durée de l'allumage complet |
| --- | --- |
| 1 niveau / 1 tick | 15 ticks — 0,75 s |
| 3 niveaux / 1 tick | 5 ticks — 0,25 s (défaut) |
| 5 niveaux / 1 tick | 3 ticks — 0,15 s |
| 15 niveaux / 1 tick | 1 tick — instantané |

C'est **aussi** le fondu de la texture : à 3 niveaux par cran, l'image saute
de trois crans à la fois (5 images sur 16). Lumière et apparence sont portées
par la même propriété, impossible de les séparer.

| Sujet | Où régler |
| --- | --- |
| Vitesse, rayon, lumière max, sons | `ArcaBalance` section 50 |
| Recettes | `recipe/eyeblossom_lantern_from_open.json`, `..._from_closed.json` |
| Textures | `python3 tools/gen_flower_assets.py` |

Le fondu entre l'éteint et l'allumé est **calculé dans les textures** : un
modèle de bloc ne sait pas mélanger deux images en opacité. Le script écrit
donc les seize crans (`block/eyeblossom_lantern_0..15.png`) à partir de deux
images de référence, `eyeblossom_lantern_off.png` et
`eyeblossom_lantern_on.png` (animée, comme les lanternes vanilla). Redessine
ces deux-là, relance le script avec `--force`, et le fondu se refait tout
seul.

### Gousse à pichet mangeable (`PITCHER_POD_FOOD`, redémarrage)

1 gigot (`PITCHER_POD_NUTRITION = 2`, le jeu compte en demi-gigots), avalée en
`PITCHER_POD_CONSUME_SECONDS` (0,8 s = deux fois plus vite qu'un aliment
normal), et chaque gousse retire `PITCHER_POD_EFFECT_REDUCTION_SECONDS` à
**tous** les effets en cours. Mettre `PITCHER_POD_ONLY_SHORTENS_HARMFUL` à
`true` pour épargner les bons effets. Les effets infinis ne sont jamais
touchés.

L'effet de consommation est réutilisable ailleurs (aliment du mod ou
datapack) sous le nom `arcamod:shorten_effects` :
`{"type": "arcamod:shorten_effects", "seconds": 30, "only_harmful": false}`.

### Pitcher plant carnivore (`PITCHER_PLANT_FEEDING`)

Clic droit avec de la viande sur une pitcher plant. Il faut
`PITCHER_FEED_POINTS_REQUIRED` points : chair putréfiée =
`PITCHER_FEED_WEAK_POINTS`, viande crue = `PITCHER_FEED_STRONG_POINTS`
(10 chairs, ou 5 viandes, ou un mélange). Pleine, elle change de texture,
crache des spores et double la vitesse de pousse dans
`PITCHER_BOOST_RADIUS` blocs pendant `PITCHER_BOOST_DURATION_TICKS`, puis
redevient une pitcher plant vanilla.

Tant qu'elle digère, **elle refuse la nourriture** : le clic échoue, la viande
reste dans la main du joueur (et n'est pas mangée par mégarde en cliquant
vite) et le compte à rebours ne se recharge pas. Il faut attendre la fin de la
digestion pour la renourrir.

La **moitié haute** d'une pitcher plant a une boîte de sélection raccourcie de
8 px (`PITCHER_PLANT_TOP_SHAPE_HEIGHT`, 16 = le cube plein de vanilla) : on
vise le bloc derrière la plante au lieu de s'accrocher dans le vide au-dessus
de ses feuilles. La plante vanilla et celle qui digère ont la même boîte —
sinon on sentirait la différence au moment où l'une remplace l'autre.

| Sujet | Où régler |
| --- | --- |
| Coût, durée, rayon, particules, boîte | `ArcaBalance` section 52 |
| Ce qu'elle mange | tags `arcamod:pitcher_feed_weak` et `arcamod:pitcher_feed_strong` |
| Ce qui pousse plus vite | tag `arcamod:pitcher_boost_grows` |

`PITCHER_BOOST_TICKS_PER_TICK = 3` correspond exactement à « deux fois plus
vite » : le jeu envoie 3 ticks aléatoires par tick et par cube de 4096 blocs,
et une sphère de rayon 10 en fait ~4190. Mettre 6 pour tripler.

Le bloc `arcamod:fed_pitcher_plant` remplace la plante vanilla le temps de la
digestion (un bloc vanilla ne peut porter ni compteur ni texture à lui) et lui
rend sa place à la fin. Tant qu'elle se remplit, elle utilise les modèles
vanilla : rien ne se voit.

### Fleurs lumineuses (`FLOWER_LIGHT`, rechargement du monde)

`TORCHFLOWER_LIGHT = 14` (une torche) et `OPEN_EYEBLOSSOM_LIGHT = 2`. Quelles
fleurs sont concernées se décide dans les tags
`arcamod:torch_light_flowers` et `arcamod:dim_light_flowers` (les versions en
pot y sont déjà). Mettre 0 dans `ArcaBalance` rend la nuit à la fleur, mais
les chunks déjà éclairés ne se recalculent qu'au rechargement du monde.


---

## 51. Torchflower protectrice et allay porte-lanterne

### Torchflower protectrice (`TORCHFLOWER_WARD`)

Une torchflower plantée protège `TORCHFLOWER_WARD_RADIUS` blocs à la ronde
(20 par défaut) :

- **le feu ne s'y propage plus** — rien ne peut s'y enflammer, même depuis un
  feu situé juste en dehors ;
- **les flammes déjà là s'éteignent lentement** — `TORCHFLOWER_WARD_EXTINGUISH_CHANCE`
  par tour de feu (un feu joue environ toutes les 1,5 s). Elles ne vieillissent
  plus, ne brûlent plus leurs voisins et ne sautent plus pendant ce temps ;
- **l'eau ne gèle plus** (`TORCHFLOWER_WARD_STOPS_FREEZING`) ;
- **la neige ne se dépose plus** (`TORCHFLOWER_WARD_STOPS_SNOW`).

Le briquet marche toujours dans la zone : allumer un feu est un geste
volontaire, c'est sa propagation qu'on arrête. La glace et la neige **déjà
posées** ne fondent pas — la fleur empêche, elle ne nettoie pas.

| Sujet | Où régler |
| --- | --- |
| Rayon, vitesse d'extinction, ce qui est bloqué | `ArcaBalance` section 54 |
| Quelles fleurs protègent | tag `arcamod:torchflower_ward` (fleur + version en pot) |

**Coût en performance** : chercher une fleur à 20 blocs, ce serait fouiller
33 000 blocs à chaque flamme et à chaque flocon. Le mod tient donc à jour la
liste des torchflowers des chunks chargés
([TorchflowerWard.java](src/main/java/dev/arca/arcamod/util/TorchflowerWard.java)),
remplie au chargement d'un chunk (seules les tranches dont la palette annonce
la fleur sont lues) et à chaque pose. **Le rayon ne coûte donc rien ; c'est le
nombre de fleurs plantées qui compte** — quelques dizaines ne se sentent pas.

### Allay porte-lanterne (`ALLAY_LANTERN_FOLLOW`)

Mettre une lanterne (tag `arcamod:allay_lanterns`, qui reprend
`#minecraft:lanterns` — la lanterne d'eyeblossom en fait partie) dans la main
d'un allay : il ne quitte plus son joueur. Au-delà de
`ALLAY_LANTERN_FOLLOW_DISTANCE` il revient à `ALLAY_LANTERN_FOLLOW_SPEED`, et
au-delà de `ALLAY_LANTERN_TELEPORT_DISTANCE` il se téléporte, comme un loup.

Le joueur suivi est celui qui lui a donné quelque chose (mémoire
`LIKED_PLAYER`) ; s'il n'en a pas, le plus proche dans
`ALLAY_LANTERN_ADOPT_RADIUS` l'adopte. En dessous de la distance de retour, il
reprend sa vie normale (ramasser des objets, voleter, danser).

Avec `DYNAMIC_LIGHTS` actif, il éclaire vraiment : la lumière dynamique du mod
prend déjà en compte ce que porte n'importe quelle créature.

### Lanternes retirées de la ceinture à outils

`#minecraft:lanterns` ne fait plus partie de `arcamod:tool_belt_allowed`.


---

## 52. Feu de camp gratté et mousse débordante

### Feu de camp gratté (`CAMPFIRE_LOGS`, `CAMPFIRE_PLACED_UNLIT`)

Un feu de camp **posé est éteint** (`CAMPFIRE_PLACED_UNLIT`) ; ceux des
villages et des structures ne sont pas concernés, ils n'arrivent pas par la
pose. Une **pelle sur un feu de camp éteint** en gratte la cendre : le bloc
devient `arcamod:campfire_logs` (ou `soul_campfire_logs`), il ne reste que les
quatre bûches, et `CAMPFIRE_ASH_SCRAPED` cendre tombe au sol. Le tas de bûches
se casse, se ramasse et se repose, et **quatre choses le rallument** : un
briquet, une boule de feu (tag `arcamod:campfire_igniters`), des braises en
bouteille, et **tout projectile en feu** — une flèche à hampe en bâton de
blaze, une flèche enflammée, une boule de feu lancée. Mettre
`CAMPFIRE_LOGS_LIT_BY_ANY_FIRE_ARROW` à `false` réserve ce dernier privilège
aux seules flèches en bâton de blaze.

Un feu **allumé** demande donc deux coups de pelle : le premier l'éteint
(vanilla), le second en gratte la cendre.

Le tas de bûches se casse **à la hache** (tag `minecraft:mineable/axe`), et
ce qu'il lâche dépend de l'outil : **Toucher de soie** rend le bloc lui-même,
sinon il tombe en **3 bâtons**.

| Sujet | Où régler |
| --- | --- |
| Cendre rendue, usure de la pelle et du briquet | `ArcaBalance` section 56 |
| Ce qui rallume les bûches | tag `arcamod:campfire_igniters` |
| Bâtons rendus sans Toucher de soie | `loot_table/blocks/campfire_logs.json` (et sa version des âmes) |
| Textures et modèles | `python3 tools/gen_campfire_moss_assets.py` |

Deux blocs et non un seul : gratter un feu **des âmes** donne des bûches qui
se rallument en feu des âmes. Leurs quatre textures (`block/campfire_logs.png`
et `block/soul_campfire_logs.png` pour le bloc, `item/…` pour l'icône) sont
dans le dossier du mod, prêtes à être redessinées. L'icône d'inventaire est
l'icône vanilla du feu de camp **privée de ses flammes**, les trous rebouchés
avec le bois d'à côté. Le modèle est le modèle vanilla du
feu éteint **relu dans le jar** puis amputé de sa planchette de braises : il
suivra une retouche de Mojang sans rien faire.

### Mousse débordante (`MOSS_CARPET_SKIRT`)

Un tapis de mousse posé sur un bloc **naturel** fait couler sa mousse sur les
quatre côtés du bloc qui le porte — l'inverse du tapis du jardin pâle, qui
grimpe sur ses voisins.

| Sujet | Où régler |
| --- | --- |
| Quels tapis débordent | tag `arcamod:moss_carpets` |
| Sur quels blocs | tag `arcamod:moss_skirt_blocks` |
| Textures | `assets/arcamod/textures/block/moss_carpet_overlay.png` et `pale_moss_carpet_overlay.png` (la mousse se lit du haut de l'image vers le bas) |
| Écart anti-clignotement | `SKIRT_OFFSET` dans `tools/gen_campfire_moss_assets.py` |

**Comment ça marche.** Le bloc du dessous n'est jamais touché : c'est le tapis
qui dessine les quatre pans, avec un modèle qui déborde de son propre cube
(un modèle a le droit d'aller de -16 à 32). Une propriété `moss_skirt` est
ajoutée aux tapis vanilla ([MossSkirt.java](src/main/java/dev/arca/arcamod/util/MossSkirt.java))
et tenue à jour à la pose et quand le bloc du dessous change. C'est une
propriété **énumérée** (`none` / `moss`) et non un booléen : l'état par défaut
d'un bloc prend la première valeur de chaque propriété, et le jeu compte
`true` avant `false` — un tapis posé par la génération du monde serait donc
moussu d'office. Avec un enum, c'est notre ordre qui décide.

Elle atterrit aussi sur les tapis de laine — la liste des propriétés d'un bloc
est figée à sa construction, impossible d'y reconnaître le tapis de mousse —
mais elle y reste à `none` pour toujours.


---

## 53. Flèches colorées en vol (`ARROW_PART_TEXTURES`)

Une flèche tirée prend l'apparence de ses pièces : elle garde ses vraies
couleurs en vol, plantée dans un bloc et plantée dans un mob.

**Pourquoi ce n'est pas le même chemin que l'inventaire.** Une flèche dans la
main utilise son modèle d'objet, qui empile trois calques (`gen_arrow_models.py`).
Une flèche tirée est une **entité** : le jeu lui plaque une seule image de
32×32 sur un modèle fixe. On prépare donc une image par combinaison.

| Sujet | Où régler |
| --- | --- |
| Les images | `python3 tools/gen_arrow_entity_textures.py --force` |
| Le relief (quel pixel, quelle nuance) | `TONE_MASK` en tête du script |
| Les couleurs | tes textures d'objet `textures/item/arrow/<sorte>/<id>.png` — rien d'autre à dessiner |

**Comment les images sont faites.** Rien n'est redessiné : la flèche vanilla
est repeinte pixel par pixel, avec tes couleurs telles que tu les as peintes.

**Qui est quoi** : tout est dans `TONE_MASK`, une carte de 32×32 où chaque
lettre dit deux choses — la pièce à laquelle le pixel appartient et son rang
d'ombrage :

| Pièce | Nuances, du sombre au clair |
| --- | --- |
| Empennage | `F` → `B` → `A` |
| Pointe | `J` → `C` → `D` |
| Corps | `G` → `E` → `H` → `I` |

Il fallait cette carte : la pointe et l'empennage **partagent** une couleur
vanilla (le gris 226), donc trier les pixels par teinte mélangeait les deux et
repeignait le haut de la pointe en couleur d'empennage. L'empennage y gagne
au passage une **troisième** nuance que la texture d'origine n'avait pas.

**Quelle couleur pour quel rang :**

- **silex, bâton et plume** sont les pièces de la flèche vanilla : elles
  gardent **exactement** les couleurs de la texture d'origine. Une flèche
  silex + bâton + plume est identique à celle du jeu, au pixel près, et un
  bâton reste un bâton quelle que soit la pointe ;
- pour les autres pièces, le script prend dans ton icône les **N couleurs les
  plus présentes** (N = ce que la carte demande à cette pièce), rangées du
  sombre au clair. Un reflet posé sur un seul pixel ne vole donc pas la place
  d'une teinte de fond, et le contour de ton icône sert de nuance sombre.

Le script s'arrête tout seul si la carte et la texture ne se recouvrent plus
(Mojang qui redessine la flèche) ou si une lettre recouvre deux couleurs
vanilla — c'est ce qui garantit la fidélité des pièces d'origine.

Quand une icône a moins de nuances que la carte n'en demande, ce sont les
rangs **du haut** qui se partagent la teinte la plus claire (`shaft/bamboo` et
`shaft/sugar_cane` sont dans ce cas, 3 teintes sur 4). Ajoute une quatrième
teinte à ces icônes pour récupérer le dégradé complet.

**Un motif propre à une pièce** : `PART_MASKS` laisse une pièce redécouper
**ses** pixels à elle, sans toucher aux autres. C'est ce qui raye le bambou
comme une vraie tige — anneau clair, anneau sombre, puis deux pixels de
tige — au lieu de suivre l'ombrage d'un bâton lisse. Rien d'automatique
là-dedans, c'est un dessin : recopie le modèle pour rayer une autre pièce.

756 images (18 × 7 × 6), ~280 octets chacune, soit **210 Ko**. Les textures
d'entité ne passent pas par un atlas : chacune est chargée à sa première
utilisation, donc rien ne coûte tant qu'une combinaison n'est pas tirée.

La flèche **spectrale** garde son apparence dorée (son éclat compte plus que
ses pièces) ; la flèche **à effet** est couverte, les deux images vanilla
normale et à effet étant le même fichier.

---

## 54. Montée en puissance des monstres (`MOB_SCALING`)

Tout est en Java (`ArcaBalance` section 57, `util/MobScaling`), **aucun fichier
de données** : cette section est là pour la commande de test et pour le rappel
de ce que fait vanilla tout seul.

### Ce que vanilla fait déjà

`DifficultyInstance` calcule une **difficulté locale** qui monte avec l'âge du
monde (du jour 3 au jour 63, pour un quart du total seulement) et surtout avec
le **temps passé dans le chunk** (150 jours de présence pour le maximum). C'est
elle qui décide des armures, de leur matériau, des enchantements, des renforts
de zombie, du trident des noyés. Son « multiplicateur spécial » vaut 0 tant
qu'elle est sous 2.0 : en **Facile**, aucun monstre n'a jamais d'armure.

### Ce que le mod ajoute

| Couche | Interrupteur | Où |
| --- | --- | --- |
| A. L'âge du monde compte comme du temps d'occupation, partout | `MOB_SCALING_DIFFICULTY_CLOCK` | `ServerLevelDifficultyMixin` |
| B. Armure qui monte l'échelle vanilla + enchantements | `MOB_SCALING_EQUIPMENT` | `MobScalingMixin` |
| C. Bonus de vie, dégâts, vitesse (en % , plafonnés) | `MOB_SCALING_ATTRIBUTES` | `MobScalingMixin` |
| D. Effet rare (Rapidité, Force, Résistance, Résistance au feu) | `MOB_SCALING_EFFECTS` | `MobScalingMixin` |

La couche A ne dépasse **jamais** le maximum que vanilla sait produire : c'est
la même formule, atteinte plus tôt et loin de chez toi. Les paliers arrivent
par défaut au jour 10 puis tous les 15 jours, jusqu'au palier 8 (jour 115).
La difficulté du monde multiplie le tout : rien en Paisible, moitié en Facile.

Seuls les **monstres** sont concernés (pas les animaux, pas les villageois,
pas les boss sauf `MOB_SCALING_AFFECTS_BOSSES`), et seulement à leur
apparition : un monstre déjà né ne change plus.

### Tester sans attendre cent nuits

```
/arcamod menace                  état ici et maintenant
/arcamod menace avance 60        ajoute 60 jours artificiels
/arcamod menace decalage 300     fixe le décalage total
/arcamod menace reset            retour au monde réel
```

Les jours artificiels ne bougent **que** la montée en puissance : ni l'heure,
ni la météo, ni les cultures. Le décalage est sauvegardé avec le monde
(`data/arcamod/mob_scaling_clock.dat`).

Il faut de **nouveaux** monstres pour voir l'effet : avance les jours, puis
tue les monstres existants et attends une nuit.
