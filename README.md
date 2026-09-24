# ArcaMod

Un mod de survie pour **Minecraft 26.3** (Fabric) qui etoffe le vanilla sans
le trahir : tout ce qu'il ajoute ressemble a quelque chose que le jeu aurait
pu contenir.

## Ce qu'il change

- **Un debut de partie a mains nues.** Le bois ne se casse plus a la main : il
  faut ramasser un caillou et un baton au sol, tailler une dague en silex,
  couper des herbes pour la ficelle, puis monter un outil en silex.
- **De l'equipement.** Carquois, ceinture a outils, lance-pierre, bombe
  fumigene, altimetre, fleches en pieces detachees (pointe, corps, empennage),
  garnitures sur les outils et les elytres, or rose.
- **Des blocs et des machines.** Desenchanteur, embouteilleur d'XP, cristal
  d'enchantement, chaudron a potion et chaudron de lessive, corde, baril de
  TNT, chaume, briques qui vieillissent, escaliers et dalles pour des blocs
  vanilla qui n'en avaient pas.
- **Des monstres qui montent en puissance** avec l'age du monde.

## Reglages

Tout se regle en jeu : **Options > ArcaMod**. Une centaine d'interrupteurs,
ranges par rubrique, avec une barre de recherche et une infobulle qui explique
chaque fonctionnalite.

| Quoi | Ou |
| --- | --- |
| Les interrupteurs ON/OFF | en jeu, ou `config/arcamod.json` |
| Les valeurs (degats, chances, durees...) | `src/main/java/dev/arca/arcamod/ArcaBalance.java` |
| Le reste (fichiers de donnees, generateurs) | [BALANCING.md](BALANCING.md) |

**En multijoueur, c'est le serveur qui decide.** Il envoie ses interrupteurs de
jeu a la connexion ; ils apparaissent grises dans le menu du joueur. Seuls les
reglages purement visuels (lumiere dynamique, fleches colorees en vol, vision
dans la lave) restent au choix de chacun.

## Developpement

Voir la [documentation Fabric](https://docs.fabricmc.net/develop/getting-started/creating-a-project)
pour la mise en place de l'IDE. Les scripts de `tools/` regenerent les
ressources repetitives (modeles, textures de remplacement, progres de
recette) ; aucun ne reecrit une texture existante sans `--reset-textures`.

## Licence

CC0 1.0 Universal : faites-en ce que vous voulez.
