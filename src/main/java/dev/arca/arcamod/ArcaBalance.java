package dev.arca.arcamod;

/**
 * TOUTES les valeurs d'equilibrage du mod.
 *
 * Deux fichiers pour tout regler :
 *  - CE fichier : les valeurs (degats, chances, durees, quantites...) ;
 *  - config/ArcaFeature.java : les interrupteurs ON/OFF de chaque fonctionnalite,
 *    modifiables aussi en jeu (Options > ArcaMod) ou dans
 *    config/arcamod.json.
 *
 * Ce qui vit forcement dans des fichiers de donnees (recettes, tags, tables de
 * butin des blocs du mod) est liste dans BALANCING.md a la racine du projet.
 *
 * Reperes : 20 ticks = 1 seconde ; 1 coeur = 2 points de vie ; une chance
 * s'ecrit entre 0 (jamais) et 1 (toujours).
 *
 * Sommaire
 *   1.  Joueur (bases de calcul)
 *   2.  Progression : outils en bois, fibres
 *   3.  Outil en silex
 *   4.  Dague en silex (combat, lancer, plantage, rendu)
 *   5.  Petite pierre lancee
 *   6.  Boule de feu lancee a la main
 *   7.  Carquois
 *   8.  Fleches a effet
 *   9.  Equipement : piles, garnitures lumineuses, pepites, elytres
 *   10. Baie et buisson d'XP
 *   11. Embouteilleur d'XP
 *   12. Desenchanteur
 *   13. Cristal d'enchantement
 *   14. Chaudrons (potions, lavage)
 *   15. Blocs poses au sol (cailloux, branches, oeufs)
 *   16. Chaume
 *   17. Balise
 *   18. Nourriture (betteraves dorees)
 *   19. Generation du monde
 *   20. Butin des creatures, blocs et coffres
 *   21. Table d'archerie et fleches composees
 *   22. Feu de camp (repos assis)
 *   23. Banniere de camp
 *   24. Diagnostic
 *   25. Epouvantail d'entrainement
 */
public final class ArcaBalance {

	// ==================================================================
	// 1. Joueur (bases de calcul)
	// ==================================================================

	/** Degats du joueur a mains nues. Sert de base a tous les calculs d'arme. */
	public static final float PLAYER_BASE_ATTACK_DAMAGE = 1.0F;

	/** Vitesse d'attaque du joueur a mains nues, en coups par seconde. */
	public static final float PLAYER_BASE_ATTACK_SPEED = 4.0F;

	/** Vitesse d'attaque d'une epee vanilla, sert de point de depart a la dague. */
	public static final float SWORD_ATTACK_SPEED = -2.4F;

	// ==================================================================
	// 2. Progression : outils en bois, fibres
	// ==================================================================

	/** 0.20 = -80% de durabilite. Vanilla : 59 utilisations -> 12. (Relancer le jeu.) */
	public static final float WOOD_DURABILITY_MULTIPLIER = 0.20F;

	/** 0.70 = -30% de vitesse de minage. Vanilla : 2.0 -> 1.4. (Relancer le jeu.) */
	public static final float WOOD_MINING_SPEED_MULTIPLIER = 0.70F;

	/** Fibres recoltees par touffe d'herbe coupee a la dague. */
	public static final int PLANT_FIBER_PER_GRASS = 1;

	/** Probabilite d'obtenir des fibres en coupant une herbe a la dague. */
	public static final float PLANT_FIBER_CHANCE = 0.2F;

	// ==================================================================
	// 3. Outil en silex (pioche + hache + pelle du debut de partie)
	// ==================================================================

	/** Durabilite. 131 = celle de la pierre. */
	public static final int FLINT_DURABILITY = 131;

	/** Vitesse de minage. 4.0 = celle de la pierre. */
	public static final float FLINT_MINING_SPEED = 4.0F;

	/** Bonus de degats du materiau. 1.0 = pierre et cuivre. */
	public static final float FLINT_ATTACK_DAMAGE_BONUS = 1.0F;

	/** Degats affiches en jeu, tout compris. */
	public static final float FLINT_TOTAL_ATTACK_DAMAGE = 6.0F;

	/** Vitesse d'attaque. Repere : pioche -2.8, pelle -3.0, hache -3.2. */
	public static final float FLINT_ATTACK_SPEED = -3.0F;

	/** Enchantabilite. 5 = celle de la pierre. */
	public static final int FLINT_ENCHANTMENT_VALUE = 5;

	// ==================================================================
	// 4. Dague en silex
	// ==================================================================

	/** Durabilite. Volontairement plus basse que l'outil : c'est une lame. */
	public static final int DAGGER_DURABILITY = 90;

	/** Enchantabilite. */
	public static final int DAGGER_ENCHANTMENT_VALUE = 5;

	/** Degats du coup au corps a corps, en coeurs. Repere : epee en fer = 3.5. */
	public static final float DAGGER_MELEE_DAMAGE_HEARTS = 2.0F;

	/**
	 * Cooldown d'attaque par rapport a une epee. 0.70 = 30% plus court, donc
	 * environ 2.29 coups par seconde contre 1.6 pour une epee.
	 */
	public static final float DAGGER_COOLDOWN_MULTIPLIER = 0.70F;

	// ---- Lancer ----

	/** Degats de la dague lancee, en coeurs. */
	public static final float DAGGER_THROWN_DAMAGE_HEARTS = 2.0F;

	/** Puissance du lancer (fleche a l'arc charge a fond = 3.0). */
	public static final float DAGGER_THROW_POWER = 1.2F;

	/** Dispersion du lancer. 0 = parfaitement droit. */
	public static final float DAGGER_THROW_INACCURACY = 0.0F;

	/** Cooldown entre deux lancers, en ticks. */
	public static final int DAGGER_THROW_COOLDOWN_TICKS = 20;

	/** Points de durabilite perdus a chaque lancer. */
	public static final int DAGGER_THROW_DURABILITY_COST = 3;

	/**
	 * true  : la dague tombe en item des qu'elle touche une creature.
	 * false : elle reste plantee dedans jusqu'a la mort du mob.
	 */
	public static final boolean DAGGER_DROPS_AFTER_HIT = false;

	// ---- Plantage ----

	/**
	 * Longueur de lame, en blocs, qui s'enfonce dans le bloc touche, mesuree
	 * le long de la trajectoire. 0 = la pointe s'arrete pile sur la face.
	 */
	public static final double DAGGER_EMBED_DEPTH = 0.20;

	/**
	 * Profondeur minimale, en blocs, sous la surface touchee. Utile pour les
	 * tirs rasants, qui sinon restent quasiment en surface.
	 */
	public static final double DAGGER_EMBED_MIN_SURFACE_DEPTH = 0.06;

	/**
	 * Profondeur, en blocs, dont la dague s'enfonce dans la boite de collision
	 * d'une creature. La boite est souvent plus large que le vrai modele.
	 */
	public static final double DAGGER_MOB_EMBED_DEPTH = 0.25;

	// ---- Rendu de la dague lancee ----

	/** Taille, en blocs (1.0 = la texture fait 1 bloc de cote). */
	public static final float DAGGER_RENDER_SCALE = 0.55F;

	/**
	 * Pixel de la pointe dans la texture 16x16, compte depuis le coin
	 * haut-droit (0 = pixel tout au coin). A ajuster avec la texture finale.
	 */
	public static final float DAGGER_RENDER_TIP_PIXEL_INSET = 1.0F;

	/** Decalage de la pointe le long de la lame, en blocs. Positif = s'enfonce plus. */
	public static final float DAGGER_RENDER_TIP_EXTRA_OFFSET = 0.0F;

	// ==================================================================
	// 5. Petite pierre lancee
	// ==================================================================

	/** Degats infliges a une entite touchee, en coeurs. */
	public static final float PEBBLE_DAMAGE_HEARTS = 0.5F;

	/** Puissance du lancer. 1.5 = celle de la boule de neige. */
	public static final float PEBBLE_THROW_POWER = 1.0F;

	/** Dispersion du lancer. Plus c'est haut, moins c'est precis. */
	public static final float PEBBLE_THROW_INACCURACY = 1.5F;

	/** Cooldown entre deux lancers, en ticks. 0 = aucun. */
	public static final int PEBBLE_COOLDOWN_TICKS = 10;

	/** Si true, la pierre lancee se repose au sol la ou elle atterrit. */
	public static final boolean PEBBLE_LANDS_AS_BLOCK = true;

	/** Rayon de recherche d'une place libre autour d'un mob touche. 0 = aucune recherche. */
	public static final int PEBBLE_MOB_SCATTER_RADIUS = 0;

	/** Nombre d'essais de placement autour d'un mob touche. */
	public static final int PEBBLE_MOB_SCATTER_ATTEMPTS = 24;

	/** Profondeur maximale de chute quand la pierre tape un mur. */
	public static final int PEBBLE_MAX_FALL_SEARCH = 4;

	/** Si aucune place n'est libre : true = tombe en item, false = disparait. */
	public static final boolean PEBBLE_DROP_ITEM_IF_NO_ROOM = false;

	// ==================================================================
	// 6. Boule de feu lancee a la main
	// ==================================================================

	/** Puissance d'explosion. 1 = celle d'un ghast. */
	public static final int FIRE_CHARGE_EXPLOSION_POWER = 1;

	/** Cooldown entre deux lancers, en ticks. */
	public static final int FIRE_CHARGE_THROW_COOLDOWN_TICKS = 40;

	// ==================================================================
	// 7. Carquois
	// ==================================================================

	/**
	 * Duree d'affichage du nom de la fleche choisie au-dessus de la barre
	 * d'objets, en ticks. La derniere seconde est un fondu.
	 */
	public static final int QUIVER_NAME_DISPLAY_TICKS = 40;

	/**
	 * true : le carquois passe AVANT les fleches en vrac dans l'inventaire.
	 * Une fleche tenue en main reste toujours prioritaire, comme en vanilla.
	 */
	public static final boolean QUIVER_BEFORE_INVENTORY_ARROWS = true;

	// ==================================================================
	// 8. Fleches a effet (flaque a l'impact)
	// ==================================================================

	/** Rayon de la flaque au moment de l'impact, en blocs. */
	public static final float ARROW_CLOUD_RADIUS = 3.0F;

	/** Duree de vie de la flaque, en ticks. */
	public static final int ARROW_CLOUD_DURATION_TICKS = 150;

	/** Multiplicateur de duree des effets. Repere : potion persistante = 0.25. */
	public static final float ARROW_CLOUD_EFFECT_SCALE = 0.25F;

	/** Delai avant que la flaque ne commence a agir, en ticks. */
	public static final int ARROW_CLOUD_WAIT_TICKS = 5;

	// ==================================================================
	// 9. Equipement
	// ==================================================================

	/**
	 * Taille de pile des outils, armures, potions... intacts. 1 = comme en
	 * vanilla. La liste des objets en plus est le tag arcamod:stackable.
	 */
	public static final int GEAR_STACK_SIZE = 16;

	/** Luminosite des garnitures lumineuses, de 0 (normale) a 15 (pleine lumiere). */
	public static final int GLOWING_TRIM_LIGHT = 15;

	/** true : une poche d'encre normale retire l'effet lumineux. */
	public static final boolean INK_SAC_REMOVES_TRIM_GLOW = true;

	/** Pepites obtenues en fondant une piece d'equipement. Vanilla : 1. */
	public static final int IRON_GEAR_SMELTING_NUGGETS = 3;
	public static final int GOLD_GEAR_SMELTING_NUGGETS = 3;
	public static final int COPPER_GEAR_SMELTING_NUGGETS = 3;

	/** Durabilite des elytres en membranes. Vanilla : 432. (Relancer le jeu.) */
	public static final int PATCHWORK_ELYTRA_DURABILITY = 4;

	// ==================================================================
	// 10. Baie et buisson d'XP
	// ==================================================================

	/** Points d'XP donnes par baie mangee (un zombie en donne 5). */
	public static final int XP_BERRY_XP = 8;

	/** Delai entre deux baies mangees, en ticks. 0 = aucun. */
	public static final int XP_BERRY_COOLDOWN_TICKS = 1;

	/** Taille finale du buisson, tiree au hasard entre ces bornes. (Relancer le jeu.) */
	public static final int XP_BUSH_MIN_HEIGHT = 4;
	public static final int XP_BUSH_MAX_HEIGHT = 7;

	/** Probabilite de pousser d'un bloc a chaque tick aleatoire. Vanilla : 0.1. */
	public static final double XP_BUSH_GROW_CHANCE = 0.1;

	/** Probabilite qu'un nouveau segment naisse avec des baies. Vanilla : 0.11. */
	public static final float XP_BUSH_BERRIES_ON_GROWTH_CHANCE = 0.05F;

	/** Probabilite, par tick aleatoire, qu'une baie repousse sur un segment vide. */
	public static final float XP_BUSH_BERRY_REGROW_CHANCE = 0.02F;

	/** Nombre de baies recoltees sur un segment, bornes incluses. */
	public static final int XP_BUSH_HARVEST_MIN = 1;
	public static final int XP_BUSH_HARVEST_MAX = 2;

	/** La poudre d'os fait-elle monter la plante d'un segment ? */
	public static final boolean XP_BUSH_BONEMEAL_GROWS_PLANT = true;

	/** La poudre d'os fait-elle apparaitre une baie sur un segment vide ? */
	public static final boolean XP_BUSH_BONEMEAL_SPAWNS_BERRIES = false;

	/** Lumiere emise par un segment qui porte des baies (0-15). (Relancer le jeu.) */
	public static final int XP_BUSH_LIGHT_WITH_BERRIES = 10;

	// ==================================================================
	// 11. Embouteilleur d'XP
	// ==================================================================

	/** Rayon d'aspiration autour du bloc, en blocs. */
	public static final double XP_BOTTLER_RADIUS = 3.0;

	/** XP a prelever pour fabriquer une fiole (une fiole vanilla en rend ~7). */
	public static final int XP_BOTTLER_XP_PER_BOTTLE = 8;

	/** Delai entre deux prelevements, en ticks. */
	public static final int XP_BOTTLER_SIPHON_INTERVAL_TICKS = 2;

	/** XP prelevee a chaque prelevement. */
	public static final int XP_BOTTLER_XP_PER_SIPHON = 3;

	/** Fatigue par fiole produite. 4.0 = 1 point de saturation. */
	public static final float XP_BOTTLER_EXHAUSTION_PER_BOTTLE = 4.0F;

	/** Duree d'une image de l'animation du dessus, en ticks. */
	public static final int XP_BOTTLER_TICKS_PER_FRAME = 2;

	/** true : un filet de particules relie le joueur a la machine. */
	public static final boolean XP_BOTTLER_PARTICLES = false;

	// ==================================================================
	// 12. Desenchanteur
	// ==================================================================

	/** Cout fixe, en niveaux d'experience, pour extraire un enchantement. */
	public static final int DISENCHANT_XP_BASE = 1;

	/** Cout supplementaire par niveau de l'enchantement extrait. */
	public static final int DISENCHANT_XP_PER_LEVEL = 1;

	// ==================================================================
	// 13. Cristal d'enchantement
	// ==================================================================

	/** Rayon, en blocs, dans lequel la mort d'une creature charge le cristal. */
	public static final int CRYSTAL_CHARGE_RADIUS = 5;

	/** Rayon, en blocs, dans lequel il faut une table d'enchantement. */
	public static final int CRYSTAL_TABLE_RADIUS = 5;

	/** Lumiere emise par un cristal charge (0-15). (Relancer le jeu.) */
	public static final int CRYSTAL_CHARGED_LIGHT = 7;

	// ==================================================================
	// 14. Chaudrons
	// ==================================================================

	/** Nombre de fioles pour remplir le chaudron a potions. */
	public static final int POTION_CAULDRON_CAPACITY = 3;

	/** Duree de l'effet donne a une creature dans le chaudron, en ticks. */
	public static final int POTION_CAULDRON_EFFECT_TICKS = 80;

	/** Intervalle entre deux applications de l'effet, en ticks. */
	public static final int POTION_CAULDRON_REFRESH_TICKS = 20;

	/** Temps avant qu'une preparation sans feu de camp dessous retombe en eau, en ticks. */
	public static final int POTION_CAULDRON_DECAY_TICKS = 300;

	/**
	 * Delai avant que le serveur renvoie le contenu du chaudron au client
	 * apres chaque fiole (corrige la couleur parfois absente). 0 = desactive.
	 */
	public static final int POTION_CAULDRON_RESYNC_DELAY_TICKS = 1;

	/** Nombre d'objets decolores par niveau d'eau consomme (chaudron d'eau). */
	public static final int CAULDRON_WASH_ITEMS_PER_LEVEL = 8;

	/** Fleches trempees (transformees en fleches a effet) par fiole consommee. */
	public static final int ARROW_DIP_ARROWS_PER_POTION = 8;

	/**
	 * Fioles retirees du chaudron a potions a chaque trempage. Le chaudron en
	 * contient POTION_CAULDRON_CAPACITY au maximum ; a zero, il se vide.
	 */
	public static final int ARROW_DIP_POTIONS_USED = 1;

	// ==================================================================
	// 15. Blocs poses au sol
	// ==================================================================

	/** Nombre de cailloux empilables sur un meme bloc. (Relancer le jeu.) */
	public static final int PEBBLES_MIN = 1;
	public static final int PEBBLES_MAX = 5;

	/** Nombre de branches empilables sur un meme bloc. (Relancer le jeu.) */
	public static final int STICKS_MIN = 1;
	public static final int STICKS_MAX = 4;

	/** Hauteur de la boite de selection des cailloux et branches, en pixels. */
	public static final double CLUTTER_SHAPE_HEIGHT = 1.0;

	/** Nombre d'oeufs sur un meme bloc. (Relancer le jeu.) */
	public static final int EGGS_MIN = 1;
	public static final int EGGS_MAX = 3;

	/** Duree d'eclosion des oeufs poses sur une botte de foin, en ticks. */
	public static final int EGG_HATCH_TICKS = 2000;

	// ==================================================================
	// 16. Chaume
	// ==================================================================

	/**
	 * Probabilite qu'un tick aleatoire fasse vieillir le chaume d'une etape.
	 * 0.05 = environ une etape toutes les sept minutes.
	 */
	public static final float THATCH_FADE_CHANCE = 0.05F;

	// ==================================================================
	// 17. Balise
	// ==================================================================

	/** Portee ajoutee par un bloc de netherite pose sous la balise, en blocs. */
	public static final double BEACON_NETHERITE_BONUS_RANGE = 1000.0;

	// ==================================================================
	// 18. Nourriture (Relancer le jeu.)
	// ==================================================================

	public static final int GOLDEN_BEETROOT_NUTRITION = 4;
	public static final float GOLDEN_BEETROOT_SATURATION = 0.6F;
	public static final int GOLDEN_BEETROOT_HASTE_SECONDS = 180;
	/** 0 = Celerite I, 1 = Celerite II... */
	public static final int GOLDEN_BEETROOT_HASTE_LEVEL = 0;

	public static final int ENCHANTED_GOLDEN_BEETROOT_NUTRITION = 4;
	public static final float ENCHANTED_GOLDEN_BEETROOT_SATURATION = 1.2F;
	public static final int ENCHANTED_GOLDEN_BEETROOT_HASTE_SECONDS = 480;
	public static final int ENCHANTED_GOLDEN_BEETROOT_HASTE_LEVEL = 1;

	// ==================================================================
	// 19. Generation du monde (nouveaux chunks uniquement)
	// ==================================================================

	/** Probabilite qu'un chunk eligible recoive un amas. 0 = plus rien. */
	public static final float PEBBLE_PATCH_CHANCE = 0.13F;
	public static final float STICK_PATCH_CHANCE = 0.13F;

	/** Nombre de blocs par amas de cailloux, et rayon de dispersion. */
	public static final int PEBBLE_PATCH_MIN_BLOCKS = 1;
	public static final int PEBBLE_PATCH_MAX_BLOCKS = 5;
	public static final int PEBBLE_PATCH_SPREAD = 5;

	/** Nombre de blocs par amas de branches, et rayon de dispersion. */
	public static final int STICK_PATCH_MIN_BLOCKS = 1;
	public static final int STICK_PATCH_MAX_BLOCKS = 3;
	public static final int STICK_PATCH_SPREAD = 5;

	/** Ecart vertical tolere entre le centre de l'amas et chaque bloc. */
	public static final int PATCH_VERTICAL_SPREAD = 3;

	/** Nombre d'essais de placement par amas. */
	public static final int PATCH_TRIES = 16;

	/**
	 * Temperature minimale d'un biome pour qu'il recoive pierres et branches.
	 * Reperes : desert 2.0, plaines 0.8, foret 0.7, taiga 0.25, plaines
	 * enneigees 0.0. (Relancer le jeu.)
	 */
	public static final float BIOME_MIN_TEMPERATURE = 0.15F;

	// ==================================================================
	// 20. Butin (Recharger le monde)
	// ==================================================================

	/** Chauve-souris tuee par un joueur : aile de chauve-souris. */
	public static final float BAT_WING_DROP_CHANCE = 0.70F;

	/** Nautile : coquille, en plus de la chance vanilla (5 a 6%). */
	public static final float NAUTILUS_SHELL_DROP_CHANCE = 0.50F;

	/** Noye equipe d'un trident : chance de le lacher. Vanilla : 0.085. (Immediat.) */
	public static final float DROWNED_TRIDENT_DROP_CHANCE = 0.085F;

	/** Fouille du renifleur : baie d'XP. */
	public static final float SNIFFER_XP_BERRY_CHANCE = 0.20F;

	/** Zombie tue par un joueur : petite pierre. */
	public static final float ZOMBIE_PEBBLE_CHANCE = 0.25F;

	/** Gravier casse a la pelle en fer : fibre vegetale. */
	public static final float GRAVEL_FIBER_CHANCE = 0.10F;

	/** Cailloux ajoutes dans les coffres des maisons de village (plaines). */
	public static final float VILLAGE_CHEST_PEBBLES_MIN = 2.0F;
	public static final float VILLAGE_CHEST_PEBBLES_MAX = 5.0F;

	// ==================================================================
	// 21. Table d'archerie et fleches composees
	// ==================================================================
	//
	// Une fleche = une POINTE + un CORPS + un EMPENNAGE. Un passage a la table
	// consomme une piece de chaque et rend le plus petit des trois _YIELD.
	// Silex + baton + plume = fleche normale, sans aucun composant.
	//
	// Chaque piece a les MEMES reglages generiques, qui se multiplient entre
	// les trois pieces de la fleche (1.0 = neutre) :
	//   _YIELD       fleches produites par piece consommee ;
	//   _DAMAGE      degats FINAUX infliges a la creature touchee ;
	//   _SPEED       vitesse de depart (plus rapide = moins de temps de vol) ;
	//   _RANGE       portee totale. La gravite est ajustee pour l'atteindre :
	//                gravite = SPEED x SPEED / RANGE. Donc RANGE = SPEED x SPEED
	//                laisse la gravite vanilla (voir l'ecaille de tatou) ;
	//   _INACCURACY  dispersion du tir (0 = parfaitement droit) ;
	//   _KNOCKBACK   recul inflige a la creature touchee ;
	//   _DRAW_SPEED  vitesse de bandage de l'arc (1.2 = se charge 20% plus vite).
	// Puis les reglages propres a l'effet de la piece.

	/**
	 * Vanilla : les degats d'une fleche augmentent avec sa vitesse. false = les
	 * _SPEED ne changent que la trajectoire, pas les degats (plus lisible pour
	 * equilibrer). true = comportement vanilla.
	 */
	public static final boolean ARROW_SPEED_AFFECTS_DAMAGE = false;

	/** Bornes du multiplicateur de gravite calcule (evite les trajectoires absurdes). */
	public static final float ARROW_GRAVITY_FACTOR_MIN = 0.1F;
	public static final float ARROW_GRAVITY_FACTOR_MAX = 4.0F;

	// ---- Pointes ------------------------------------------------------------

	/** Silex : fleche normale. */
	public static final int ARROW_FLINT_YIELD = 8;
	public static final double ARROW_FLINT_DAMAGE = 1.0;
	public static final float ARROW_FLINT_SPEED = 1.0F;
	public static final float ARROW_FLINT_RANGE = 1.0F;
	public static final float ARROW_FLINT_INACCURACY = 1.0F;
	public static final float ARROW_FLINT_KNOCKBACK = 1.0F;
	public static final float ARROW_FLINT_DRAW_SPEED = 1.0F;

	/** Eclat d'amethyste : perce l'armure. */
	public static final int ARROW_AMETHYST_YIELD = 4;
	public static final double ARROW_AMETHYST_DAMAGE = 1.0;
	public static final float ARROW_AMETHYST_SPEED = 1.0F;
	public static final float ARROW_AMETHYST_RANGE = 1.0F;
	public static final float ARROW_AMETHYST_INACCURACY = 1.0F;
	public static final float ARROW_AMETHYST_KNOCKBACK = 1.0F;
	public static final float ARROW_AMETHYST_DRAW_SPEED = 1.0F;
	/** Part de l'armure ignoree. 0.4 = 40%. */
	public static final float ARROW_AMETHYST_ARMOR_PIERCE = 0.4F;
	/** Part de la robustesse d'armure (diamant, netherite) ignoree. */
	public static final float ARROW_AMETHYST_TOUGHNESS_PIERCE = 0.4F;

	/** Pepite de fer : transperce les creatures, plus lourde. */
	public static final int ARROW_IRON_YIELD = 4;
	public static final double ARROW_IRON_DAMAGE = 1.0;
	public static final float ARROW_IRON_SPEED = 1.0F;
	/** Portee reduite = la fleche retombe plus vite (plus lourde). */
	public static final float ARROW_IRON_RANGE = 0.85F;
	public static final float ARROW_IRON_INACCURACY = 1.0F;
	public static final float ARROW_IRON_KNOCKBACK = 1.0F;
	public static final float ARROW_IRON_DRAW_SPEED = 1.0F;
	/** Creatures traversees en plus. 1 = comme Perforation I. */
	public static final int ARROW_IRON_PIERCE_LEVEL = 1;

	/** Eclat de prismarine : ignore la friction de l'eau. */
	public static final int ARROW_PRISMARINE_YIELD = 4;
	public static final double ARROW_PRISMARINE_DAMAGE = 1.0;
	public static final float ARROW_PRISMARINE_SPEED = 1.0F;
	public static final float ARROW_PRISMARINE_RANGE = 1.0F;
	public static final float ARROW_PRISMARINE_INACCURACY = 1.0F;
	public static final float ARROW_PRISMARINE_KNOCKBACK = 1.0F;
	public static final float ARROW_PRISMARINE_DRAW_SPEED = 1.0F;
	/** Vitesse conservee par tick dans l'eau. Vanilla 0.6 ; 0.99 = comme dans l'air. */
	public static final float ARROW_PRISMARINE_WATER_INERTIA = 0.99F;

	/** Boule de slime : rebondit, ralentit la cible. */
	public static final int ARROW_SLIME_YIELD = 4;
	public static final double ARROW_SLIME_DAMAGE = 0.5;
	public static final float ARROW_SLIME_SPEED = 1.0F;
	public static final float ARROW_SLIME_RANGE = 1.0F;
	public static final float ARROW_SLIME_INACCURACY = 1.0F;
	public static final float ARROW_SLIME_KNOCKBACK = 1.0F;
	public static final float ARROW_SLIME_DRAW_SPEED = 1.0F;
	/** Nombre de rebonds sur les blocs avant de se planter. */
	public static final int ARROW_SLIME_BOUNCES = 1;
	/** Vitesse conservee a chaque rebond (0.6 = 60%). */
	public static final float ARROW_SLIME_BOUNCE_RESTITUTION = 0.6F;
	/** Lenteur appliquee : duree en ticks (50 = 2.5 s) et niveau (3 = Lenteur IV). */
	public static final int ARROW_SLIME_SLOWNESS_TICKS = 50;
	public static final int ARROW_SLIME_SLOWNESS_AMPLIFIER = 3;

	/** Perle de l'Ender : teleporte le tireur a l'impact. */
	public static final int ARROW_ENDER_PEARL_YIELD = 2;
	public static final double ARROW_ENDER_PEARL_DAMAGE = 1.0;
	public static final float ARROW_ENDER_PEARL_SPEED = 1.0F;
	public static final float ARROW_ENDER_PEARL_RANGE = 1.0F;
	public static final float ARROW_ENDER_PEARL_INACCURACY = 1.0F;
	public static final float ARROW_ENDER_PEARL_KNOCKBACK = 1.0F;
	public static final float ARROW_ENDER_PEARL_DRAW_SPEED = 1.0F;
	/** Degats subis par le joueur teleporte. Perle vanilla : 5 (2.5 coeurs). */
	public static final float ARROW_ENDER_PEARL_SELF_DAMAGE = 5.0F;
	/** Chance de faire apparaitre un endermite. Perle vanilla : 0.05. */
	public static final float ARROW_ENDER_PEARL_ENDERMITE_CHANCE = 0.05F;
	/** true : teleporte aussi quand la fleche touche une creature. */
	public static final boolean ARROW_ENDER_PEARL_ON_ENTITY_HIT = true;
	/** Distance, en blocs, a laquelle on reapparait devant le bloc touche. */
	public static final double ARROW_ENDER_PEARL_WALL_OFFSET = 0.4;

	/** Poudre a canon : explosion a l'impact, degats de zone uniquement. */
	public static final int ARROW_GUNPOWDER_YIELD = 2;
	/** Sans effet : la fleche ne blesse pas directement, seule l'explosion compte. */
	public static final double ARROW_GUNPOWDER_DAMAGE = 1.0;
	public static final float ARROW_GUNPOWDER_SPEED = 1.0F;
	public static final float ARROW_GUNPOWDER_RANGE = 1.0F;
	public static final float ARROW_GUNPOWDER_INACCURACY = 1.0F;
	public static final float ARROW_GUNPOWDER_KNOCKBACK = 1.0F;
	public static final float ARROW_GUNPOWDER_DRAW_SPEED = 1.0F;
	/** Puissance d'explosion. Reperes : charge de feu 1, creeper 3, TNT 4. */
	public static final float ARROW_GUNPOWDER_EXPLOSION_POWER = 1.5F;
	/** true : l'explosion casse les blocs (suit la regle de jeu tntExplodes). */
	public static final boolean ARROW_GUNPOWDER_BREAKS_BLOCKS = false;
	/** true : l'explosion allume des feux. */
	public static final boolean ARROW_GUNPOWDER_CAUSES_FIRE = false;

	// ---- Corps --------------------------------------------------------------

	/** Baton : fleche normale. */
	public static final int ARROW_STICK_YIELD = 8;
	public static final double ARROW_STICK_DAMAGE = 1.0;
	public static final float ARROW_STICK_SPEED = 1.0F;
	public static final float ARROW_STICK_RANGE = 1.0F;
	public static final float ARROW_STICK_INACCURACY = 1.0F;
	public static final float ARROW_STICK_KNOCKBACK = 1.0F;
	public static final float ARROW_STICK_DRAW_SPEED = 1.0F;

	/** Bambou : legere, plus de portee et de recul, fragile. */
	public static final int ARROW_BAMBOO_YIELD = 8;
	public static final double ARROW_BAMBOO_DAMAGE = 1.0;
	public static final float ARROW_BAMBOO_SPEED = 1.0F;
	public static final float ARROW_BAMBOO_RANGE = 1.2F;
	public static final float ARROW_BAMBOO_INACCURACY = 1.0F;
	public static final float ARROW_BAMBOO_KNOCKBACK = 1.75F;
	public static final float ARROW_BAMBOO_DRAW_SPEED = 1.0F;
	/** Chance que la fleche se brise en se plantant dans un bloc (non recuperable). */
	public static final float ARROW_BAMBOO_BREAK_CHANCE = 0.65F;

	/** Baton de blaze : enflamme ce qu'il touche, s'eteint dans l'eau. */
	public static final int ARROW_BLAZE_ROD_YIELD = 4;
	public static final double ARROW_BLAZE_ROD_DAMAGE = 1.0;
	public static final float ARROW_BLAZE_ROD_SPEED = 1.0F;
	public static final float ARROW_BLAZE_ROD_RANGE = 1.0F;
	public static final float ARROW_BLAZE_ROD_INACCURACY = 1.0F;
	public static final float ARROW_BLAZE_ROD_KNOCKBACK = 1.0F;
	public static final float ARROW_BLAZE_ROD_DRAW_SPEED = 1.0F;
	/** Duree du feu sur la creature touchee, en secondes. Fleche Flamme vanilla : 5. */
	public static final float ARROW_BLAZE_ROD_ENTITY_FIRE_SECONDS = 5.0F;
	/** true : pose du feu sur le bloc touche et allume feux de camp et bougies. */
	public static final boolean ARROW_BLAZE_ROD_IGNITES_BLOCKS = true;
	/** true : la fleche disparait des qu'elle entre dans l'eau. */
	public static final boolean ARROW_BLAZE_ROD_DESTROYED_IN_WATER = true;

	/** Baton de breeze : sans gravite pendant un temps, presque sans recul. */
	public static final int ARROW_BREEZE_ROD_YIELD = 4;
	public static final double ARROW_BREEZE_ROD_DAMAGE = 1.0;
	public static final float ARROW_BREEZE_ROD_SPEED = 1.0F;
	/** Sans effet pendant le vol sans gravite ; s'applique ensuite. */
	public static final float ARROW_BREEZE_ROD_RANGE = 1.0F;
	public static final float ARROW_BREEZE_ROD_INACCURACY = 1.0F;
	public static final float ARROW_BREEZE_ROD_KNOCKBACK = 0.15F;
	public static final float ARROW_BREEZE_ROD_DRAW_SPEED = 1.0F;
	/** Duree du vol sans gravite, en ticks (60 = 3 s). */
	public static final int ARROW_BREEZE_ROD_NO_GRAVITY_TICKS = 60;

	/** Os : tres lourd, traverse les feuilles, brise le verre. */
	public static final int ARROW_BONE_YIELD = 4;
	public static final double ARROW_BONE_DAMAGE = 1.3;
	public static final float ARROW_BONE_SPEED = 0.8F;
	public static final float ARROW_BONE_RANGE = 0.7F;
	public static final float ARROW_BONE_INACCURACY = 1.0F;
	public static final float ARROW_BONE_KNOCKBACK = 1.25F;
	public static final float ARROW_BONE_DRAW_SPEED = 1.0F;
	/** Nombre maximal de blocs de verre brises par une fleche. */
	public static final int ARROW_BONE_MAX_GLASS_BROKEN = 3;
	/** Vitesse conservee apres chaque verre brise. */
	public static final float ARROW_BONE_GLASS_SPEED_KEPT = 0.8F;
	/** true : le verre brise lache son bloc (vanilla : le verre ne lache rien). */
	public static final boolean ARROW_BONE_GLASS_DROPS = false;
	// Blocs traverses : tag data/arcamod/tags/block/bone_arrow_passes_through.json
	// Blocs brises   : tag data/arcamod/tags/block/bone_arrow_breaks.json

	// ---- Empennages ---------------------------------------------------------

	/** Plume : fleche normale. */
	public static final int ARROW_FEATHER_YIELD = 8;
	public static final double ARROW_FEATHER_DAMAGE = 1.0;
	public static final float ARROW_FEATHER_SPEED = 1.0F;
	public static final float ARROW_FEATHER_RANGE = 1.0F;
	public static final float ARROW_FEATHER_INACCURACY = 1.0F;
	public static final float ARROW_FEATHER_KNOCKBACK = 1.0F;
	public static final float ARROW_FEATHER_DRAW_SPEED = 1.0F;

	/** Membrane de phantom : +35% de portee, -10% de degats. */
	public static final int ARROW_PHANTOM_MEMBRANE_YIELD = 4;
	public static final double ARROW_PHANTOM_MEMBRANE_DAMAGE = 0.9;
	public static final float ARROW_PHANTOM_MEMBRANE_SPEED = 1.0F;
	public static final float ARROW_PHANTOM_MEMBRANE_RANGE = 1.35F;
	public static final float ARROW_PHANTOM_MEMBRANE_INACCURACY = 1.0F;
	public static final float ARROW_PHANTOM_MEMBRANE_KNOCKBACK = 1.0F;
	public static final float ARROW_PHANTOM_MEMBRANE_DRAW_SPEED = 1.0F;

	/** Ecaille de tatou : +20% de vitesse, arc charge 20% plus vite, legere dispersion. */
	public static final int ARROW_ARMADILLO_SCUTE_YIELD = 4;
	public static final double ARROW_ARMADILLO_SCUTE_DAMAGE = 1.0;
	public static final float ARROW_ARMADILLO_SCUTE_SPEED = 1.2F;
	/** 1.44 = 1.2 x 1.2 : la vitesse seule allonge la portee, gravite vanilla. */
	public static final float ARROW_ARMADILLO_SCUTE_RANGE = 1.44F;
	/** Dispersion vanilla d'un arc : 1.0. */
	public static final float ARROW_ARMADILLO_SCUTE_INACCURACY = 1.75F;
	public static final float ARROW_ARMADILLO_SCUTE_KNOCKBACK = 1.0F;
	public static final float ARROW_ARMADILLO_SCUTE_DRAW_SPEED = 1.2F;

	// ==================================================================
	// 22. Feu de camp (repos assis)
	// ==================================================================

	/**
	 * Distance maximale, en blocs, entre le siege et un feu de camp allume.
	 * Mesuree sur chaque axe (un cube), pas en ligne droite.
	 */
	public static final int CAMPFIRE_REST_RADIUS = 3;

	/** Intervalle entre deux soins, en ticks. 300 = 15 secondes. */
	public static final int CAMPFIRE_REST_INTERVAL_TICKS = 300;

	/** Points de vie rendus a chaque intervalle. 1 = un demi-coeur. */
	public static final float CAMPFIRE_REST_HEAL = 1.0F;

	/** Points de faim rendus a chaque intervalle. 1 = un demi-gigot. */
	public static final int CAMPFIRE_REST_FOOD = 1;

	/** Saturation rendue (multiplicateur vanilla : 0.1 pain sec, 0.8 steak). */
	public static final float CAMPFIRE_REST_SATURATION_MODIFIER = 0.0F;

	/**
	 * Hauteur du joueur assis, en blocs, par rapport au dessus du siege.
	 * A ajuster a l'oeil : plus bas = le joueur s'enfonce dans le siege.
	 */
	public static final double CAMPFIRE_SEAT_HEIGHT_OFFSET = -0.15;

	// ==================================================================
	// 23. Banniere de camp
	// ==================================================================

	/**
	 * Rayon, en blocs, autour d'une banniere de camp ou aucun monstre
	 * n'apparait naturellement. Les monstres deja la ne sont pas chasses.
	 */
	public static final int CAMP_BANNER_RADIUS = 16;

	/** Distance maximale entre la banniere et un feu de camp allume (cube). */
	public static final int CAMP_BANNER_CAMPFIRE_RADIUS = 4;

	// ==================================================================
	// 24. Diagnostic
	// ==================================================================

	/**
	 * true : ecrit dans la console chaque etape de l'affichage de la couleur
	 * du chaudron a potions (lignes "[PotionCauldron]").
	 */
	public static final boolean DEBUG_POTION_CAULDRON = false;

	// ==================================================================
	// 25. Epouvantail d'entrainement
	// ==================================================================

	// ---- Comportement ----

	/**
	 * true : meme temps d'invulnerabilite qu'un mob (1/2 seconde apres un
	 * coup, seul un coup plus fort passe). Garder true pour des chiffres
	 * fideles au combat reel ; false pour voir chaque coup, meme spamme.
	 */
	public static final boolean SCARECROW_USE_INVULNERABILITY_FRAMES = true;

	/** Pour le casser : il faut etre accroupi... */
	public static final boolean SCARECROW_BREAK_REQUIRES_SNEAK = true;

	/** ...et avoir la main principale vide. */
	public static final boolean SCARECROW_BREAK_REQUIRES_EMPTY_HAND = true;

	/** true : rend aussi l'item de l'epouvantail en mode creatif. */
	public static final boolean SCARECROW_DROP_IN_CREATIVE = false;

	/** true : les explosions affichent leurs degats (il n'est jamais casse). */
	public static final boolean SCARECROW_SHOW_EXPLOSION_DAMAGE = true;

	/** true : ni recul, ni souffle d'explosion, ni courant ne le deplacent. */
	public static final boolean SCARECROW_IMMOVABLE = true;

	/** true : l'armure portee perd de la durabilite comme sur un mob. */
	public static final boolean SCARECROW_WEARS_ARMOR = false;

	// ---- Apparence ----

	/** La plaque de pierre du porte-armure sous ses pieds. */
	public static final boolean SCARECROW_SHOW_BASE_PLATE = true;

	/**
	 * true : mains vides, les bras restent ecartes facon epouvantail. Des
	 * qu'une main tient quelque chose, les deux bras se baissent (pose joueur).
	 */
	public static final boolean SCARECROW_SPREAD_EMPTY_ARMS = true;

	/** Angle d'un bras vide ecarte, en degres (90 = a l'horizontale). */
	public static final float SCARECROW_ARM_ANGLE = 90.0F;

	/** true : les bras se balancent doucement comme ceux d'un joueur immobile. */
	public static final boolean SCARECROW_ARMS_BOB = false;

	// ---- Resine (figer) ----

	/**
	 * Une fois fige par de la resine : false = seules les mains sont
	 * bloquees (l'armure reste modifiable) ; true = tout l'equipement.
	 */
	public static final boolean SCARECROW_FREEZE_LOCKS_ARMOR = true;

	/** Durabilite perdue par la hache qui retire la resine. */
	public static final int SCARECROW_UNFREEZE_AXE_DAMAGE = 1;

	// ---- Chiffres de degats ----

	/** Duree de vie d'un chiffre, en ticks (20 = 1 seconde). */
	public static final int SCARECROW_NUMBER_LIFETIME_TICKS = 25;

	/** Hauteur au-dessus de la tete ou apparait le chiffre, en blocs. */
	public static final double SCARECROW_NUMBER_HEIGHT = 0.3;

	/** De combien de blocs le chiffre monte pendant sa vie. */
	public static final float SCARECROW_NUMBER_RISE = 0.8F;

	/** Decalage aleatoire max, en blocs, pour que les chiffres ne se superposent pas. */
	public static final double SCARECROW_NUMBER_SPREAD = 0.3;

	/** Taille du texte (1 = taille d'un text display vanilla). */
	public static final float SCARECROW_NUMBER_SCALE = 0.8F;

	/** Format des coeurs : "0.#" = 2,5 ; "0.##" = 2,25 ; "0" = arrondi. */
	public static final String SCARECROW_NUMBER_FORMAT = "0.##";

	/** true : ajoute ❤ apres le chiffre. */
	public static final boolean SCARECROW_SHOW_HEART_SYMBOL = false;

	/** true : 2e ligne "degats d'origine barres -X%" quand l'armure a absorbe. */
	public static final boolean SCARECROW_SHOW_REDUCTION = true;

	/** Fond du texte en ARGB. 0 = transparent ; 0x40000000 = voile noir leger. */
	public static final int SCARECROW_NUMBER_BACKGROUND = 0;

	public static final boolean SCARECROW_NUMBER_SHADOW = true;
	public static final boolean SCARECROW_CRITICAL_BOLD = true;

	// ---- Couleurs des chiffres (0xRRGGBB) ----

	public static final int SCARECROW_COLOR_NORMAL = 0xD3D3D3;    // gris clair
	public static final int SCARECROW_COLOR_CRITICAL = 0xCC8A2E;  // ocre
	public static final int SCARECROW_COLOR_FIRE = 0xFF6A1A;      // feu, lave, Aura de feu, boule de feu
	public static final int SCARECROW_COLOR_POISON = 0x6FBF2A;    // effet Poison
	public static final int SCARECROW_COLOR_WITHER = 0x5E4B5E;    // effet Wither, crane de wither
	public static final int SCARECROW_COLOR_MAGIC = 0xB05CFF;     // potion de degats, souffle du dragon
	public static final int SCARECROW_COLOR_FREEZE = 0x8FE3FF;    // neige poudreuse
	public static final int SCARECROW_COLOR_EXPLOSION = 0xE83B3B; // TNT, creeper
	public static final int SCARECROW_COLOR_LIGHTNING = 0xFFF36B; // foudre
	public static final int SCARECROW_COLOR_REDUCTION = 0x8A8A8A; // ligne "avant armure"

	// ==================================================================
	// Conversions (pour ne pas dupliquer ces calculs dans le reste du code)
	// ==================================================================

	/** Coeurs -> points de degats (1 coeur = 2 points). */
	public static float hearts(float hearts) {
		return hearts * 2.0F;
	}

	/** Secondes -> ticks. */
	public static int seconds(int seconds) {
		return seconds * 20;
	}

	/**
	 * Convertit des degats "affiches en jeu" en valeur a passer au jeu.
	 *
	 * Minecraft additionne le point de degats du joueur et le bonus du
	 * materiau par-dessus la valeur de l'item : on les retire donc ici.
	 */
	public static float attackDamageFromTotal(float totalDamage, float materialBonus) {
		return totalDamage - PLAYER_BASE_ATTACK_DAMAGE - materialBonus;
	}

	/** Degats a passer au jeu pour la dague. */
	public static float daggerAttackDamage() {
		return attackDamageFromTotal(hearts(DAGGER_MELEE_DAMAGE_HEARTS), FLINT_ATTACK_DAMAGE_BONUS);
	}

	/** Modificateur de vitesse d'attaque de la dague, deduit du cooldown voulu. */
	public static float daggerAttackSpeed() {
		float swordRate = PLAYER_BASE_ATTACK_SPEED + SWORD_ATTACK_SPEED;
		return swordRate / DAGGER_COOLDOWN_MULTIPLIER - PLAYER_BASE_ATTACK_SPEED;
	}

	private ArcaBalance() {
	}
}
