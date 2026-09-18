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
 *   26. Lance-pierre
 *   27. Resine collante
 *   28. Tete d'Enderman (detecteur de regard)
 *   29. Soufre (pinceau, poudre de soufre)
 *   30. Or rose
 *   31. Cuivre : oxydation et orages
 *   32. Piment des ames
 *   33. Garniture pulsante (echo)
 *   34. Lumiere dynamique
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

	/**
	 * Vitesse de minage : celle des outils en bois du mod (vanilla 2.0 x
	 * WOOD_MINING_SPEED_MULTIPLIER = 1.4). Pierre : 4.0.
	 */
	public static final float FLINT_MINING_SPEED = 2.0F * WOOD_MINING_SPEED_MULTIPLIER;

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
	 * Texture actuelle : pointe au pixel (12, 3), soit 3 pixels du coin.
	 */
	public static final float DAGGER_RENDER_TIP_PIXEL_INSET = 3.0F;

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

	/**
	 * true : les fleches ramassees (au sol ou plantees) vont directement dans
	 * un carquois de l'inventaire qui contient deja ce type de fleche.
	 */
	public static final boolean QUIVER_AUTO_PICKUP = true;

	/**
	 * Ramassage auto : true = une fois les piles identiques pleines, le
	 * surplus occupe aussi les emplacements vides du carquois ; false = il va
	 * dans l'inventaire.
	 */
	public static final boolean QUIVER_PICKUP_FILLS_EMPTY_SLOTS = true;

	/**
	 * Clics facon bundle dans l'inventaire : clic gauche avec des fleches sur
	 * le carquois (ou avec le carquois sur des fleches) pour les ranger, clic
	 * droit pour sortir la fleche choisie.
	 */
	public static final boolean QUIVER_BUNDLE_CLICKS = true;

	/** Molette sur le carquois dans l'inventaire : choisit la fleche a tirer. */
	public static final boolean QUIVER_SCROLL_SELECT = true;

	/** Apercu du contenu (grille + jauge) dans l'infobulle, comme le bundle. */
	public static final boolean QUIVER_TOOLTIP_PREVIEW = true;

	/** Nombre de colonnes de la grille dans l'infobulle. */
	public static final int QUIVER_TOOLTIP_COLUMNS = 3;

	/**
	 * Accroupi + clic droit maintenu : vide le carquois pile par pile, comme
	 * le bundle (le clic droit simple ouvre toujours son inventaire).
	 */
	public static final boolean QUIVER_SNEAK_USE_DROPS = true;

	/** Vidage : delai avant la 2e pile, puis entre chaque pile, en ticks. */
	public static final int QUIVER_DROP_FIRST_DELAY_TICKS = 10;
	public static final int QUIVER_DROP_INTERVAL_TICKS = 2;

	/** Taille d'une pile pleine pour la jauge (capacite = 9 x cette valeur). */
	public static final int QUIVER_STACK_CAPACITY = 64;

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

	/**
	 * Taille de pile des livres enchantes (seuls les livres aux enchantements
	 * strictement identiques s'empilent). 1 = comme en vanilla. Les
	 * bibliotheques sculptees n'en acceptent toujours qu'un par emplacement.
	 */
	public static final int ENCHANTED_BOOK_STACK_SIZE = 16;

	/** Luminosite des garnitures lumineuses, de 0 (normale) a 15 (pleine lumiere). */
	public static final int GLOWING_TRIM_LIGHT = 15;

	/** true : une poche d'encre normale retire l'effet lumineux. */
	public static final boolean INK_SAC_REMOVES_TRIM_GLOW = true;

	// ---- Fonte de l'equipement en pepites (four, haut fourneau) ----
	//
	// Pepites = lingots de la recette x 9 x SMELTING_RETURN_NEW x durabilite
	// restante (1.0 = neuf, 0.0 = casse). Arrondi a l'inferieur.
	// Exemple : bottes en or neuves = 4 lingots = 36 pepites x 0.8 = 28.
	// Au-dela d'une pile (64), le resultat est plafonne a 64.

	/** Part des pepites rendue pour un objet neuf (0.8 = 80 %). */
	public static final float SMELTING_RETURN_NEW = 0.8F;

	/**
	 * Minimum de pepites rendues, meme a durabilite 0. Doit rester >= 1 :
	 * avec 0, le four refuse la recette et l'objet reste bloque dans l'entree.
	 */
	public static final int SMELTING_MIN_NUGGETS = 1;

	/** Lingots de chaque recette (valeurs vanilla). */
	public static final int SMELTING_INGOTS_HELMET = 5;
	public static final int SMELTING_INGOTS_CHESTPLATE = 8;
	public static final int SMELTING_INGOTS_LEGGINGS = 7;
	public static final int SMELTING_INGOTS_BOOTS = 4;
	public static final int SMELTING_INGOTS_SWORD = 2;
	public static final int SMELTING_INGOTS_PICKAXE = 3;
	public static final int SMELTING_INGOTS_AXE = 3;
	public static final int SMELTING_INGOTS_SHOVEL = 1;
	public static final int SMELTING_INGOTS_HOE = 2;
	public static final int SMELTING_INGOTS_SPEAR = 1;

	/** Armures de cheval et de nautile (pas de recette vanilla : valeur libre). */
	public static final int SMELTING_INGOTS_ANIMAL_ARMOR = 5;

	/** Objet reconnu par aucune categorie ci-dessus. */
	public static final int SMELTING_INGOTS_OTHER = 1;

	/**
	 * Cotte de mailles : introuvable en craft, on la compte comme une armure
	 * en fer multipliee par ce facteur (0.5 = moitie).
	 */
	public static final float SMELTING_CHAINMAIL_FACTOR = 0.5F;

	/** Durabilite des elytres en membranes. Vanilla : 432. (Relancer le jeu.) */
	public static final int PATCHWORK_ELYTRA_DURABILITY = 4;

	/**
	 * Frein supplementaire en vol, applique a chaque tick (1.0 = vitesse
	 * vanilla). Petit ecart = gros effet, car il se cumule : 0.997 donne
	 * environ -20 % de vitesse de croisiere, 0.99 environ -50 %.
	 */
	public static final double PATCHWORK_ELYTRA_DRAG = 0.998;

	/**
	 * Vitesse maximale en vol, en blocs par seconde, fusees comprises. 0 = pas
	 * de limite. Reperes vanilla : plane ~ 30, avec fusees ~ 33 a 40.
	 */
	public static final double PATCHWORK_ELYTRA_MAX_SPEED = 20.0;

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

	/** Lumiere emise par le desenchanteur (table d'enchantement vanilla : 7). */
	public static final int DISENCHANTER_LIGHT = 7;

	/** Chance (0 a 1) qu'un desenchantement fasse pousser du sculk sous le desenchanteur. */
	public static final float DISENCHANT_SCULK_CHANCE = 0.20F;

	/** Nombre de blocs de sculk crees quand la chance tombe (tire entre MIN et MAX inclus). */
	public static final int DISENCHANT_SCULK_MIN_BLOCKS = 1;
	public static final int DISENCHANT_SCULK_MAX_BLOCKS = 3;

	/** Delai entre l'apparition de deux blocs de sculk, en ticks (propagation progressive). */
	public static final int DISENCHANT_SCULK_INTERVAL_TICKS = 10;

	/** Distance max (en blocs) sous/autour du desenchanteur ou le sculk peut apparaitre. */
	public static final int DISENCHANT_SCULK_RADIUS = 4;

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

	/**
	 * Generation du monde : poids de chaque taille de tas de cailloux.
	 * Case 0 = 1 caillou, case 1 = 2 cailloux... jusqu'a PEBBLES_MAX.
	 * Chance d'une taille = son poids / somme des poids (ici 40+26+17+11+6 =
	 * 100, donc 40 % de tas a 1 caillou... 6 % de tas a 5). Une case manquante
	 * compte comme 0 (taille jamais generee).
	 */
	public static final int[] PEBBLE_COUNT_WEIGHTS = { 27, 30, 20, 15, 8 };

	/** Nombre de branches empilables sur un meme bloc. (Relancer le jeu.) */
	/**
	 * Inflammabilite des branches au sol. Propagation : chance qu'un feu voisin
	 * les allume (herbe haute : 60, feuilles : 30). Combustion : chance que le
	 * feu les consume (herbe haute : 100, feuilles : 60). 0 = ne brulent pas.
	 */
	public static final int FALLEN_STICKS_IGNITE_ODDS = 60;
	public static final int FALLEN_STICKS_BURN_ODDS = 100;

	public static final int STICKS_MIN = 1;
	public static final int STICKS_MAX = 3;

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
	public static final float DROWNED_TRIDENT_DROP_CHANCE = 0.1F;

	/** Fouille du renifleur : baie d'XP. */
	public static final float SNIFFER_XP_BERRY_CHANCE = 0.20F;

	/** Zombie tue par un joueur : petite pierre. */
	public static final float ZOMBIE_PEBBLE_CHANCE = 0.0F;

	/** Gravier casse a la pelle en fer : fibre vegetale. */
	public static final float GRAVEL_FIBER_CHANCE = 0.0F;

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
	public static final float ARROW_SLIME_KNOCKBACK = 5.5F;
	public static final float ARROW_SLIME_DRAW_SPEED = 1.0F;
	/** Nombre de rebonds sur les blocs avant de se planter. */
	public static final int ARROW_SLIME_BOUNCES = 2;
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

	/**
	 * Boule de resine : englue la cible (voir section 27, RESIN_*). Les
	 * degats sont FIXES (ARROW_RESIN_FIXED_DAMAGE), quels que soient l'arc et
	 * les autres pieces.
	 */
	public static final int ARROW_RESIN_YIELD = 4;
	public static final double ARROW_RESIN_DAMAGE = 1.0;
	public static final float ARROW_RESIN_SPEED = 1.0F;
	public static final float ARROW_RESIN_RANGE = 1.0F;
	public static final float ARROW_RESIN_INACCURACY = 1.0F;
	public static final float ARROW_RESIN_KNOCKBACK = 0.0F;
	public static final float ARROW_RESIN_DRAW_SPEED = 1.0F;
	/** Degats fixes, en points (1 = un demi-coeur). Mettre -1 pour garder le calcul normal. */
	public static final float ARROW_RESIN_FIXED_DAMAGE = 1.0F;

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
	public static final int CAMP_BANNER_RADIUS = 32;

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

	/**
	 * true : ecrit dans la console chaque minerai d'or rose genere (lignes
	 * "[PinkGold]", avec la position). Voir aussi /arcamod pinkgold scan.
	 */
	public static final boolean DEBUG_PINK_GOLD_ORE = false;

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

	/**
	 * Fabrication : clic droit avec une botte de foin sur un porte-armure
	 * portant un plastron ET des jambieres en cuir (toute couleur, toute
	 * durabilite). true : le cuir est consomme ; false : l'epouvantail le garde
	 * sur lui.
	 */
	public static final boolean SCARECROW_CONVERSION_CONSUMES_LEATHER = true;

	/** Nombre de bottes de foin consommees par la transformation. */
	public static final int SCARECROW_CONVERSION_HAY_COST = 1;

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

	/** Item en 3D (inventaire, main) : taille du modele (0.6 = tient dans un bloc). */
	public static final float SCARECROW_ITEM_SCALE = 0.6F;

	/** Item en 3D : rotation du modele autour de l'axe vertical, en degres. */
	public static final float SCARECROW_ITEM_YAW = 0.0F;

	/** true : les bras se balancent doucement comme ceux d'un joueur immobile. */
	public static final boolean SCARECROW_ARMS_BOB = false;

	/**
	 * Grossissement des manches d'armure autour de l'epaule (0.05 = +5 %).
	 * Evite que les epaules et le corps du plastron, parfaitement alignes
	 * quand les bras sont immobiles, se melangent (z-fighting). 0 = desactive.
	 */
	public static final float SCARECROW_ARMOR_ARM_INFLATE = 0.05F;

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
	// 26. Lance-pierre
	// ==================================================================
	//
	// Clic droit maintenu pour tendre, relacher pour tirer (comme l'arc).
	// Munitions cherchees dans la main secondaire, puis dans l'inventaire,
	// dans l'ordre de l'enum SlingshotAmmo.
	//
	// Degats finaux = (degats de la munition + bonus Puissance) x tension.
	// La tension va de 0 a 1 (courbe de l'arc vanilla).

	/** Durabilite (arc vanilla : 384). */
	public static final int SLINGSHOT_DURABILITY = 250;

	/** Enchantabilite (arc vanilla : 1, or : 22). */
	public static final int SLINGSHOT_ENCHANTMENT_VALUE = 10;

	/** Ticks pour une tension complete (arc vanilla : 20). */
	public static final int SLINGSHOT_FULL_DRAW_TICKS = 16;

	/** Tension minimale pour que le tir parte (arc vanilla : 0.1). */
	public static final float SLINGSHOT_MIN_DRAW = 0.15F;

	/** Vitesse de depart a pleine tension (arc vanilla : 3.0, boule de neige : 1.5). */
	public static final float SLINGSHOT_BASE_VELOCITY = 2.2F;

	/** Dispersion du tir (arc vanilla : 1.0). */
	public static final float SLINGSHOT_INACCURACY = 1.0F;

	/** Durabilite perdue par tir. */
	public static final int SLINGSHOT_DURABILITY_PER_SHOT = 1;

	/** Temps de recharge apres un tir, en ticks (0 = aucun). */
	public static final int SLINGSHOT_COOLDOWN_TICKS = 4;

	/** Pleine tension : le projectile laisse une trainee de particules critiques. */
	public static final boolean SLINGSHOT_CRIT_PARTICLES_AT_FULL_DRAW = true;

	/** Puissance : degats AJOUTES par niveau, en points (1 = un demi-coeur). */
	public static final float SLINGSHOT_POWER_DAMAGE_PER_LEVEL = 0.5F;

	/** Recul de base inflige (0 = aucun ; 0.5 = recul d'un coup de poing). */
	public static final float SLINGSHOT_BASE_KNOCKBACK = 0.1F;

	/** Recul : force AJOUTEE par niveau d'enchantement Recul. */
	public static final float SLINGSHOT_KNOCKBACK_PER_LEVEL = 0.5F;

	/** Mode creatif : tire sans consommer de munition. */
	public static final boolean SLINGSHOT_CREATIVE_INFINITE_AMMO = true;

	// ---- Munitions ----
	// _DAMAGE      degats a pleine tension, en points (1 = un demi-coeur) ;
	// _VELOCITY    multiplicateur de vitesse de depart ;
	// _GRAVITY     gravite par tick (boule de neige vanilla : 0.03) ;
	// _RECOVERY    chance que la munition retombe au sol en item quand elle touche
	//              un BLOC (sur une creature, elle disparait toujours).

	/** Petite pierre : la munition de base. */
	public static final float SLINGSHOT_PEBBLE_DAMAGE = 3.0F;
	public static final float SLINGSHOT_PEBBLE_VELOCITY = 1.0F;
	public static final double SLINGSHOT_PEBBLE_GRAVITY = 0.03;
	public static final float SLINGSHOT_PEBBLE_RECOVERY = 0.5F;

	/** Eclat d'amethyste : leger, rapide, se brise toujours. */
	public static final float SLINGSHOT_AMETHYST_DAMAGE = 4.0F;
	public static final float SLINGSHOT_AMETHYST_VELOCITY = 1.15F;
	public static final double SLINGSHOT_AMETHYST_GRAVITY = 0.025;
	public static final float SLINGSHOT_AMETHYST_RECOVERY = 0.0F;

	/** Pepite de fer : lourde, fait mal, retombe vite. */
	public static final float SLINGSHOT_IRON_NUGGET_DAMAGE = 5.0F;
	public static final float SLINGSHOT_IRON_NUGGET_VELOCITY = 0.9F;
	public static final double SLINGSHOT_IRON_NUGGET_GRAVITY = 0.04;
	public static final float SLINGSHOT_IRON_NUGGET_RECOVERY = 0.25F;

	/**
	 * Autres pepites (or, cuivre, or rose) : comme le fer, avec 0.5 point de
	 * degats en moins.
	 */
	public static final float SLINGSHOT_NUGGET_DAMAGE = SLINGSHOT_IRON_NUGGET_DAMAGE - 0.5F;
	public static final float SLINGSHOT_NUGGET_VELOCITY = SLINGSHOT_IRON_NUGGET_VELOCITY;
	public static final double SLINGSHOT_NUGGET_GRAVITY = SLINGSHOT_IRON_NUGGET_GRAVITY;
	public static final float SLINGSHOT_NUGGET_RECOVERY = SLINGSHOT_IRON_NUGGET_RECOVERY;

	// ---- Rebond des pepites (fer, or, cuivre, or rose) ----

	/** Nombre de rebonds sur le sol avant de retomber en item ou de se briser. */
	public static final int SLINGSHOT_NUGGET_BOUNCES = 1;

	/** true : rebondit aussi sur les murs et plafonds (false = uniquement le dessus des blocs). */
	public static final boolean SLINGSHOT_NUGGET_BOUNCE_ON_WALLS = false;

	/** Vitesse verticale gardee au rebond (0.4 = 40 %). */
	public static final double SLINGSHOT_NUGGET_BOUNCE_RESTITUTION = 0.4;

	/** Vitesse horizontale gardee au rebond (frottement du sol). */
	public static final double SLINGSHOT_NUGGET_BOUNCE_FRICTION = 0.6;

	/** En dessous de cette vitesse d'impact (blocs/tick), la pepite ne rebondit pas : elle se pose. */
	public static final double SLINGSHOT_NUGGET_BOUNCE_MIN_SPEED = 0.15;

	/** Degats d'une pepite qui touche une creature APRES avoir rebondi (0.5 = moitie). */
	public static final float SLINGSHOT_NUGGET_BOUNCE_DAMAGE_MULTIPLIER = 0.5F;

	/**
	 * Boule de resine : degats FIXES (la tension et Puissance ne changent
	 * rien), mais englue la cible (section 27).
	 */
	public static final float SLINGSHOT_RESIN_CLUMP_DAMAGE = 1.0F;
	public static final float SLINGSHOT_RESIN_CLUMP_VELOCITY = 0.85F;
	public static final double SLINGSHOT_RESIN_CLUMP_GRAVITY = 0.035;
	public static final float SLINGSHOT_RESIN_CLUMP_RECOVERY = 0.0F;
	/** true : Puissance et tension n'augmentent pas les degats de la resine. */
	public static final boolean SLINGSHOT_RESIN_CLUMP_FIXED_DAMAGE = true;

	// ==================================================================
	// 27. Resine collante (lance-pierre, fleches a pointe de resine)
	// ==================================================================

	/** Sauts necessaires pour se liberer : tire au hasard entre MIN et MAX (inclus). */
	public static final int RESIN_MIN_JUMPS = 3;
	public static final int RESIN_MAX_JUMPS = 5;

	/**
	 * Duree maximale d'un joueur englue, en ticks (securite si le joueur ne
	 * saute jamais). 600 = 30 s.
	 */
	public static final int RESIN_PLAYER_MAX_TICKS = 600;

	/** Duree de l'engluement d'un mob (il ne sait pas sauter pour se liberer). 60 = 3 s. */
	public static final int RESIN_MOB_TICKS = 45;

	/**
	 * Duree de l'engluement des boss, en ticks (10 = 0.5 s). La liste est le
	 * tag data/arcamod/tags/entity_type/resin_resistant.json (wither, dragon,
	 * gardiens...), modifiable sans recompiler.
	 */
	public static final int RESIN_BOSS_TICKS = 10;

	/** true : un mob englue ne peut lancer aucun projectile (fleches, boules de feu, potions...). */
	public static final boolean RESIN_BLOCKS_MOB_PROJECTILES = true;

	/** true : un mob englue relache l'arme qu'il bandait / chargeait (arc, arbalete, trident). */
	public static final boolean RESIN_CANCELS_MOB_WEAPON_CHARGE = true;

	/** true : un creeper englue fige sa meche (ni progression, ni desamorcage). */
	public static final boolean RESIN_FREEZES_CREEPER_FUSE = true;

	/** true : fleches a pointe de resine et resine du lance-pierre ne repoussent jamais la cible. */
	public static final boolean RESIN_NO_KNOCKBACK = true;

	/**
	 * true : un creeper englue EN PLEINE meche finit d'exploser des qu'il est
	 * libere, meme si le joueur s'est eloigne. false : il reprend son
	 * comportement normal (et se desamorce si le joueur est trop loin).
	 */
	public static final boolean RESIN_CREEPER_EXPLODES_AFTER_RELEASE = true;

	/** Particules de resine autour d'une creature engluee : tous les N ticks (0 = aucune). */
	public static final int RESIN_PARTICLE_INTERVAL_TICKS = 4;

	/** Nombre de particules a chaque salve. */
	public static final int RESIN_PARTICLE_COUNT = 2;

	/** Delai minimum entre deux sauts comptes, en ticks (anti-spam de la touche). */
	public static final int RESIN_JUMP_MIN_INTERVAL_TICKS = 3;

	/** true : bloque aussi la camera du joueur englue. */
	public static final boolean RESIN_FREEZES_VIEW = true;

	/** true : affiche "Sautez pour vous liberer (x restants)" au-dessus de la barre. */
	public static final boolean RESIN_SHOW_JUMPS_LEFT = true;

	/** Couleur de l'effet (icone, particules). */
	public static final int RESIN_EFFECT_COLOR = 0xE36E14;

	// ==================================================================
	// 28. Tete d'Enderman (detecteur de regard)
	// ==================================================================

	/** Distance maximale, en blocs, a laquelle un regard est detecte. */
	public static final double ENDERMAN_HEAD_MAX_RANGE = 16.0;

	/**
	 * Tolerance du regard. Meme formule que l'Enderman vanilla (0.025) : le
	 * cone se resserre avec la distance. Plus grand = plus facile a declencher.
	 */
	public static final double ENDERMAN_HEAD_LOOK_TOLERANCE = 0.06;

	/** Signal au plus pres (15 = maximum redstone). */
	public static final int ENDERMAN_HEAD_MAX_SIGNAL = 15;

	/** Signal a la distance maximale (1 = le plus faible encore detecte). */
	public static final int ENDERMAN_HEAD_MIN_SIGNAL = 1;

	/** Frequence de la detection, en ticks (plus grand = moins de calculs). */
	public static final int ENDERMAN_HEAD_CHECK_INTERVAL_TICKS = 2;

	/** true : un joueur portant une citrouille sculptee (ou une tete d'Enderman) n'est pas detecte. */
	public static final boolean ENDERMAN_HEAD_RESPECTS_DISGUISE = true;

	/** true : les blocs transparents (verre...) ne bloquent pas le regard. */
	public static final boolean ENDERMAN_HEAD_SEES_THROUGH_GLASS = true;

	/** Tremblement de la tete quand elle est regardee (Enderman vanilla : 0.02). */
	public static final float ENDERMAN_HEAD_SHAKE = 0.02F;

	/** Ouverture de la bouche quand elle est regardee, en pixels (Enderman vanilla : 5). */
	public static final float ENDERMAN_HEAD_JAW_OPEN_PIXELS = 5.0F;

	/** Ticks pour ouvrir/fermer completement la bouche (0 = instantane, comme vanilla). */
	public static final float ENDERMAN_HEAD_JAW_ANIMATION_TICKS = 3.0F;

	/** Chance de lacher la tete quand un creeper charge tue un Enderman (vanilla : 1.0). */
	public static final float ENDERMAN_HEAD_CHARGED_CREEPER_CHANCE = 1.0F;

	// ==================================================================
	// 29. Soufre (pinceau, poudre de soufre)
	// ==================================================================

	/**
	 * Chaque bloc de soufre brosse tire une reserve cachee de poudre entre
	 * MIN et MAX. Le bloc reste en place ; une fois la reserve videe OU le
	 * temps de brossage ecoule, il devient "soufre epuise" et ne peut plus
	 * etre brosse.
	 */
	public static final int SULFUR_POWDER_MIN = 0;
	public static final int SULFUR_POWDER_MAX = 3;

	/** Coups de pinceau (un toutes les 10 ticks = 0.5 s) entre deux poudres. */
	public static final int SULFUR_STROKES_PER_POWDER = 2;

	/** Coups de pinceau maximum sur un meme bloc ("temps max"). 8 coups = 4 s. */
	public static final int SULFUR_MAX_STROKES = 8;

	/** Durabilite perdue par le pinceau a chaque poudre obtenue. */
	public static final int SULFUR_BRUSH_DURABILITY_PER_POWDER = 1;

	/**
	 * Un brossage interrompu est oublie apres ce delai, en ticks (1200 = 1 min).
	 * La reserve deja tiree est perdue et sera retiree au prochain brossage.
	 */
	public static final int SULFUR_PROGRESS_FORGET_TICKS = 1200;

	// ==================================================================
	// 30. Or rose (alliage cuivre + or)
	// ==================================================================
	//
	// "Solide comme le fer, rapide et enchantable comme l'or."

	// ---- Outils ----
	/** Durabilite des outils (fer 250, or 32). */
	public static final int PINK_GOLD_TOOL_DURABILITY = 250;
	/** Vitesse de minage (fer 6, or 12). */
	public static final float PINK_GOLD_MINING_SPEED = 12.0F;
	/** Bonus de degats du materiau (fer 2, or 0). */
	public static final float PINK_GOLD_ATTACK_DAMAGE_BONUS = 2.0F;
	/** Enchantabilite des outils (fer 14, or 22). */
	public static final int PINK_GOLD_TOOL_ENCHANTMENT_VALUE = 22;

	// Degats et vitesse d'attaque de chaque outil, AVANT le bonus du materiau
	// (memes conventions que Items.java vanilla ; valeurs du fer par defaut).
	public static final float PINK_GOLD_SWORD_DAMAGE = 3.0F;
	public static final float PINK_GOLD_SWORD_SPEED = -2.4F;
	public static final float PINK_GOLD_AXE_DAMAGE = 6.0F;
	public static final float PINK_GOLD_AXE_SPEED = -3.1F;
	public static final float PINK_GOLD_PICKAXE_DAMAGE = 1.0F;
	public static final float PINK_GOLD_PICKAXE_SPEED = -2.8F;
	public static final float PINK_GOLD_SHOVEL_DAMAGE = 1.5F;
	public static final float PINK_GOLD_SHOVEL_SPEED = -3.0F;
	public static final float PINK_GOLD_HOE_DAMAGE = -2.0F;
	public static final float PINK_GOLD_HOE_SPEED = -1.0F;

	/** Lance : duree d'attaque et multiplicateur de degats (fer : 0.95 / 0.95 ; or : 0.95 / 0.7). */
	public static final float PINK_GOLD_SPEAR_ATTACK_DURATION = 0.95F;
	public static final float PINK_GOLD_SPEAR_DAMAGE_MULTIPLIER = 0.95F;

	// ---- Armure ----
	/** Multiplicateur de durabilite de l'armure (fer 15, or 7). */
	public static final int PINK_GOLD_ARMOR_DURABILITY = 15;
	/** Protection par piece (fer : 2 / 6 / 5 / 2). */
	public static final int PINK_GOLD_HELMET_DEFENSE = 2;
	public static final int PINK_GOLD_CHESTPLATE_DEFENSE = 6;
	public static final int PINK_GOLD_LEGGINGS_DEFENSE = 5;
	public static final int PINK_GOLD_BOOTS_DEFENSE = 2;
	/** Enchantabilite de l'armure (fer 9, or 25). */
	public static final int PINK_GOLD_ARMOR_ENCHANTMENT_VALUE = 25;
	/** Robustesse et resistance au recul (fer : 0 et 0). */
	public static final float PINK_GOLD_ARMOR_TOUGHNESS = 0.0F;
	public static final float PINK_GOLD_ARMOR_KNOCKBACK_RESISTANCE = 0.0F;

	// ---- Minerai ----
	/**
	 * Le minerai n'apparait QUE la ou un filon d'or touche un filon de cuivre.
	 * A chaque contact trouve, on remplace entre MIN et MAX blocs de chaque
	 * filon (le minerai d'or rose garde la variante pierre/ardoise du bloc
	 * remplace).
	 */
	public static final int PINK_GOLD_ORE_REPLACED_GOLD_MIN = 1;
	public static final int PINK_GOLD_ORE_REPLACED_GOLD_MAX = 2;
	public static final int PINK_GOLD_ORE_REPLACED_COPPER_MIN = 1;
	public static final int PINK_GOLD_ORE_REPLACED_COPPER_MAX = 2;

	/** Chance qu'un contact or/cuivre donne vraiment du minerai d'or rose. */
	public static final float PINK_GOLD_ORE_CONTACT_CHANCE = 1.0F;

	/** Hauteurs scannees (or : -64 a 32, +256 en badlands ; cuivre : -16 a 112). */
	public static final int PINK_GOLD_ORE_MIN_Y = -64;
	public static final int PINK_GOLD_ORE_MAX_Y = 128;

	/** Taille maximale d'un filon explore (securite de performance). */
	public static final int PINK_GOLD_ORE_MAX_VEIN_SIZE = 48;

	// ---- Modele de forge ----
	/** Chance de trouver le modele "Amelioration en or rose" dans un coffre au tresor d'epave. */
	public static final float PINK_GOLD_TEMPLATE_SHIPWRECK_CHANCE = 0.65F;

	// ==================================================================
	// 31. Cuivre : oxydation de l'equipement et orages
	// ==================================================================
	//
	// Les outils et armures en cuivre s'oxydent avec le temps, en 4 etats :
	// 0 neuf, 1 expose, 2 altere, 3 oxyde. Plus ils sont oxydes, plus ils
	// s'usent vite. Etre frappe par la foudre les remet a neuf.

	/** Frequence du test d'oxydation, en ticks (1200 = 1 minute). */
	public static final int COPPER_OXIDATION_INTERVAL_TICKS = 1200;

	/** Chance qu'une piece passe a l'etat suivant a chaque test (0.05 = ~20 min par etat). */
	public static final float COPPER_OXIDATION_CHANCE = 0.05F;

	/** Multiplicateur de cette chance sous la pluie ou dans l'eau. */
	public static final float COPPER_OXIDATION_WET_MULTIPLIER = 3.0F;

	/** true : seule la piece portee ou tenue s'oxyde (pas celles rangees dans l'inventaire). */
	public static final boolean COPPER_OXIDATION_ONLY_EQUIPPED = false;

	/** true : l'armure posee sur un porte-armure ou un epouvantail s'oxyde aussi. */
	public static final boolean COPPER_OXIDATION_ON_ARMOR_STANDS = true;

	/**
	 * Oxydation par l'usure : sous cette part de durabilite restante (0.5 =
	 * 50 %), l'objet est au moins "expose", puis "altere", puis "oxyde", les
	 * trois etats se partageant le reste a parts egales (50-33 %, 33-17 %,
	 * 17-0 %). Si le temps l'a deja plus oxyde, c'est l'etat le plus avance
	 * qui compte. Reparer l'objet fait redescendre cette part-la.
	 * 0 = desactive.
	 */
	public static final float COPPER_OXIDATION_DURABILITY_START = 0.5F;

	/**
	 * Usure par etat d'oxydation : multiplicateur de la durabilite perdue
	 * (index = etat 0, 1, 2, 3). 2.0 = s'use deux fois plus vite.
	 */
	public static final float[] COPPER_OXIDATION_WEAR_MULTIPLIER = {1.0F, 1.25F, 1.5F, 2.0F};

	/**
	 * Durabilite perdue par la hache qui retire la cire d'un equipement en
	 * cuivre (table de craft). 0 = la hache ressort intacte.
	 */
	public static final int COPPER_UNWAX_AXE_DAMAGE = 0;

	/** true : un eclair qui frappe le joueur desoxyde toute son armure en cuivre portee. */
	public static final boolean COPPER_LIGHTNING_CLEANS_OXIDATION = true;

	// ---- Orages ----
	/** Frequence du test de foudre pendant un orage, en ticks (100 = 5 s). */
	public static final int COPPER_LIGHTNING_CHECK_TICKS = 100;

	/** Chance d'etre frappe a chaque test, PAR piece d'armure en cuivre portee (x4 en armure complete). */
	public static final float COPPER_LIGHTNING_CHANCE_PER_PIECE = 0.005F;

	/** Armure complete en cuivre frappee : Celerite (Haste) et Vitesse. */
	public static final int COPPER_STORM_BUFF_SECONDS = 480;
	/** Niveaux : 0 = I, 1 = II. */
	public static final int COPPER_STORM_HASTE_AMPLIFIER = 0;
	public static final int COPPER_STORM_SPEED_AMPLIFIER = 1;

	// ==================================================================
	// 32. Piment des ames (Nether)
	// ==================================================================

	/** Duree de "Frappe ardente" (Aura de feu + riposte enflammee), en ticks. 600 = 30 s. */
	public static final int SOUL_PEPPER_FIRE_ASPECT_TICKS = 600;

	/** Secondes de feu infligees a chaque coup, comme Aura de feu I (4 s). */
	public static final int SOUL_PEPPER_IGNITE_SECONDS = 4;

	/**
	 * Riposte : toute creature qui frappe au corps a corps quelqu'un sous
	 * "Frappe ardente" prend feu pendant ces secondes. 0 = desactive.
	 */
	public static final int SOUL_PEPPER_RETALIATION_IGNITE_SECONDS = 4;

	/** Duree de la Resistance au feu (50 ticks = 2.5 s). */
	public static final int SOUL_PEPPER_FIRE_RESISTANCE_TICKS = 50;

	/** Nourriture rendue (baies sucrees : 2 et 0.1). */
	public static final int SOUL_PEPPER_NUTRITION = 2;
	public static final float SOUL_PEPPER_SATURATION = 0.1F;

	/** Piments recoltes sur un buisson mur (age 3), entre MIN et MAX. */
	public static final int SOUL_PEPPER_HARVEST_MIN = 1;
	public static final int SOUL_PEPPER_HARVEST_MAX = 3;

	/** Piments recoltes sur un buisson presque mur (age 2). 0 = pas de recolte a cet age. */
	public static final int SOUL_PEPPER_HARVEST_AGE_2 = 1;

	/** Chance de pousser a chaque tick aleatoire (baies sucrees : 0.2). Pas besoin de lumiere. */
	public static final float SOUL_PEPPER_GROW_CHANCE = 0.2F;

	/** true : le buisson pique comme un buisson de baies sucrees. */
	public static final boolean SOUL_PEPPER_BUSH_HURTS = true;

	/**
	 * Lumiere emise par le buisson selon son stade (0 a 15, torche des ames :
	 * 10). Les stades plus jeunes n'eclairent pas. (Relancer le jeu.)
	 */
	public static final int SOUL_PEPPER_BUSH_LIGHT_BEFORE_LAST_STAGE = 5;
	public static final int SOUL_PEPPER_BUSH_LIGHT_LAST_STAGE = 8;

	// ==================================================================
	// 33. Garniture pulsante (eclat d'echo)
	// ==================================================================

	/** Duree d'une pulsation complete, en millisecondes. */
	public static final int ECHO_TRIM_PULSE_PERIOD_MS = 3000;

	/** Lumiere de la garniture au creux et au sommet de la pulsation (0 a 15). */
	public static final int ECHO_TRIM_LIGHT_MIN = 0;
	public static final int ECHO_TRIM_LIGHT_MAX = 5;

	/**
	 * Opacite de la garniture au creux de la pulsation (1.0 = toujours
	 * opaque, 0.0 = disparait completement). La lumiere seule ne se voit pas
	 * quand l'endroit est deja eclaire (plein jour) : c'est cette variation
	 * d'opacite qui garde la pulsation visible partout.
	 */
	public static final float ECHO_TRIM_MIN_OPACITY = 0.30F;

	// ==================================================================
	// 33 bis. Plastron a elytres (table de craft)
	// ==================================================================
	//
	// Recette : un plastron du tag arcamod:elytra_harness_chestplates (par
	// defaut : plastron en netherite uniquement) + des elytres + les
	// ingredients ci-dessous. Le plastron garde tout (enchantements,
	// garniture, nom) ; les enchantements des elytres sont perdus.
	// Le vol use la durabilite du PLASTRON.

	/** Etoiles du Nether consommees par la recette. 0 = aucune. */
	public static final int ELYTRA_HARNESS_NETHER_STARS = 1;

	/** Membranes de phantom consommees par la recette. 0 = aucune. */
	public static final int ELYTRA_HARNESS_PHANTOM_MEMBRANES = 4;

	/** true : les elytres doivent etre intactes (aucune durabilite perdue). */
	public static final boolean ELYTRA_HARNESS_REQUIRES_INTACT_ELYTRA = true;

	/**
	 * Usure en vol, multipliee par rapport a des elytres (vanilla : 1 point
	 * toutes les 20 ticks de vol). 3 = un plastron en netherite (592) tient
	 * environ 197 points de vol, contre 432 pour de vraies elytres.
	 */
	public static final int ELYTRA_HARNESS_GLIDE_WEAR = 3;

	// ==================================================================
	// 33 ter. Bibliotheque sculptee : nom du livre vise
	// ==================================================================

	/**
	 * Duree d'affichage du nom (au-dessus de la barre d'objets), en ticks,
	 * une fois que le regard quitte le livre. Tant qu'on le regarde, il reste.
	 */
	public static final int CHISELED_BOOKSHELF_NAME_TICKS = 20;

	/** true : un livre enchante affiche ses enchantements plutot que "Livre enchante". */
	public static final boolean CHISELED_BOOKSHELF_SHOW_ENCHANTMENTS = true;

	/**
	 * true : le nom s'affiche plus bas que les messages de la barre d'action
	 * (voir CHISELED_BOOKSHELF_NAME_LOWER_OFFSET).
	 */
	public static final boolean CHISELED_BOOKSHELF_NAME_AT_ITEM_NAME_POSITION = true;

	/**
	 * De combien de pixels le nom descend sous la position des messages de la
	 * barre d'action (0 = vanilla, 13 = pile a la place du nom de l'objet tenu).
	 * Tant que le nom de l'objet tenu s'affiche, le livre revient a 0.
	 */
	public static final int CHISELED_BOOKSHELF_NAME_LOWER_OFFSET = 13;

	// ==================================================================
	// 34. Lumiere dynamique (client uniquement)
	// ==================================================================
	// La lumiere de CHAQUE objet / creature est dans les tables en haut de
	// client/light/DynamicLightSources.java. Ici : portee, fluidite, cout.

	/**
	 * Perte de lumiere par bloc de distance. 1.0 = comme une torche posee
	 * (vanilla). Plus haut = halo plus serre.
	 */
	public static final float DYNAMIC_LIGHT_FALLOFF_PER_BLOCK = 2.0F;

	/**
	 * Rayon maximal d'une source, en blocs. Une source plus forte que ce rayon
	 * ne le permet decroit plus vite pour s'eteindre pile a ce rayon.
	 * C'est LE reglage de cout : chaque bloc de rayon en plus = plus de
	 * sections de chunk a reconstruire quand la source bouge.
	 */
	public static final float DYNAMIC_LIGHT_MAX_RADIUS = 10.0F;

	/** Distance (blocs, depuis la camera) au-dela de laquelle les sources sont ignorees. */
	public static final double DYNAMIC_LIGHT_SOURCE_RANGE = 64.0;

	/** Nombre maximal de sources actives (les plus proches de la camera gagnent). */
	public static final int DYNAMIC_LIGHT_MAX_SOURCES = 48;

	/** Mise a jour des sources tous les N ticks. 1 = 20 fois par seconde (le plus fluide). */
	public static final int DYNAMIC_LIGHT_UPDATE_INTERVAL_TICKS = 1;

	/**
	 * Deplacement minimal (blocs) avant de re-eclairer le decor. Plus petit =
	 * plus fluide mais plus de reconstructions de chunks.
	 */
	public static final double DYNAMIC_LIGHT_MOVE_THRESHOLD = 0.25;

	/** Variation minimale de luminosite (0 a 15) avant de re-eclairer le decor. */
	public static final float DYNAMIC_LIGHT_CHANGE_THRESHOLD = 0.5F;

	/**
	 * Hauteur de la source sur une creature, en fraction de sa taille
	 * (0 = pieds, 1 = sommet). 0.7 = a peu pres la main.
	 */
	public static final float DYNAMIC_LIGHT_SOURCE_HEIGHT = 0.7F;

	/** Lumiere d'une entite en feu (creature, fleche enflammee...). 0 = aucune. */
	public static final float DYNAMIC_LIGHT_ON_FIRE = 12.0F;

	/** Lumiere d'un creeper au moment d'exploser (monte progressivement). */
	public static final float DYNAMIC_LIGHT_CREEPER_MAX = 10.0F;

	/** Lumiere d'un calmar luisant (s'eteint quand il est blesse, comme son rendu). */
	public static final float DYNAMIC_LIGHT_GLOW_SQUID = 8.0F;

	/** Lumiere d'une piece d'equipement a garniture lumineuse / pulsante. */
	public static final float DYNAMIC_LIGHT_GLOWING_TRIM = 5.0F;
	public static final float DYNAMIC_LIGHT_ECHO_TRIM = 4.0F;

	/** true : torches, feux de camp... tenus ou laches s'eteignent sous l'eau. */
	public static final boolean DYNAMIC_LIGHT_WATER_SENSITIVE = true;

	/** true : les creatures et objets sont eux-memes eclaires (pas seulement le decor). */
	public static final boolean DYNAMIC_LIGHT_ON_ENTITIES = true;

	// ==================================================================
	// Conversions (pour ne pas dupliquer ces calculs dans le reste du code)
	// ==================================================================

	/** Coeurs -> points de degats (1 coeur = 2 points). */
	public static float hearts(float hearts) {
		return hearts * 2.0F;
	}

	// ==================================================================
	// 35. Interface du bundle
	// ==================================================================

	/**
	 * Nombre maximal d'emplacements de l'interface. Il s'affiche toujours
	 * (nombre d'emplacements utilises + 1 vide), jusqu'a ce maximum. Un bundle
	 * ne peut de toute facon pas contenir plus de 64 piles (poids max = 1).
	 */
	public static final int BUNDLE_INTERFACE_MAX_SLOTS = 64;

	/** Emplacements par ligne dans l'interface (9 = largeur d'un coffre). */
	public static final int BUNDLE_INTERFACE_COLUMNS = 9;

	/** Secondes -> ticks. */
	public static int seconds(int seconds) {
		return seconds * 20;
	}

	// ---- Altimetre --------------------------------------------------------------
	// Shift + clic droit : memorise l'altitude (Y du bloc sous les pieds).
	// Re-shift + clic droit : l'efface.

	/** Ecart (en blocs) toleré pour considerer le joueur "a la bonne hauteur" (0 = Y exact). */
	public static final int ALTIMETER_TOLERANCE_BLOCKS = 0;

	/** Taille de pile de l'altimetre. */
	public static final int ALTIMETER_MAX_STACK_SIZE = 64;

	/** Affiche aussi l'altitude memorisee a cote de l'altitude actuelle. */
	public static final boolean ALTIMETER_HUD_SHOW_TARGET = true;

	/** L'altitude s'affiche aussi quand l'altimetre est dans la main secondaire. */
	public static final boolean ALTIMETER_HUD_IN_OFFHAND = true;

	/** Hauteur du texte depuis le bas de l'ecran (vanilla, nom de l'objet : 59). */
	public static final int ALTIMETER_HUD_Y_OFFSET = 59;

	/** Couleurs du texte (RGB) : sans altitude memorisee, a la bonne hauteur, au-dessus, en dessous. */
	public static final int ALTIMETER_HUD_COLOR_DEFAULT = 0xFFFFFF;
	public static final int ALTIMETER_HUD_COLOR_LEVEL = 0x55FF55;
	public static final int ALTIMETER_HUD_COLOR_UP = 0xFFAA55;
	public static final int ALTIMETER_HUD_COLOR_DOWN = 0x55AAFF;

	/** Volume et hauteurs du son quand on memorise / efface l'altitude. */
	public static final float ALTIMETER_SOUND_VOLUME = 1.0F;
	public static final float ALTIMETER_SOUND_PITCH_SET = 1.4F;
	public static final float ALTIMETER_SOUND_PITCH_CLEAR = 0.8F;

	// ---- Ceinture a outils ----------------------------------------------------
	// Se porte dans la case au-dessus du bouclier (inventaire).
	// Touche "Ceinture a outils" (R par defaut) :
	//  - appui court : echange l'objet en main avec l'outil choisi de la ceinture ;
	//  - maintenue + molette : choisit l'outil (la barre d'objets ne bouge pas).
	// Objets acceptes : tag data/arcamod/tags/item/tool_belt_allowed.json.

	/** Nombre d'emplacements de la ceinture (1 a 5, gabarit de l'ecran de l'entonnoir). */
	public static final int TOOL_BELT_SLOTS = 5;

	/** Les lanternes rangees dans la ceinture portee eclairent (lumiere dynamique). */
	public static final boolean TOOL_BELT_LANTERN_LIGHT = true;

	/** Lumiere des lanternes de la ceinture, en fraction de leur lumiere en main (1.0 = identique). */
	public static final float TOOL_BELT_LANTERN_LIGHT_FACTOR = 1.0F;

	/**
	 * Main pleine d'un objet qui ne va pas dans la ceinture : il est range
	 * ailleurs dans l'inventaire pour laisser la place a l'outil. false = echange refuse.
	 */
	public static final boolean TOOL_BELT_STASH_OTHER_ITEMS = true;

	/**
	 * Accroupi (Shift) + molette, un objet rangeable en main : fait tourner
	 * l'objet en main avec les outils de la ceinture. Main vide ou autre
	 * objet : la molette garde son comportement normal.
	 */
	public static final boolean TOOL_BELT_SNEAK_SCROLL_ROLL = true;

	/** Affiche la colonne de la ceinture a droite de la barre d'objets tant que la touche est maintenue. */
	public static final boolean TOOL_BELT_HUD = true;

	/** Decalage (pixels) de la colonne de la ceinture a droite de la barre d'objets. */
	public static final int TOOL_BELT_HUD_X_OFFSET = 8;

	/** Duree d'affichage (ticks) du nom de l'outil choisi a la molette. */
	public static final int TOOL_BELT_NAME_DISPLAY_TICKS = 30;

	// ---- Buches brulees ------------------------------------------------------
	// Quand le feu consume une buche au pied d'un arbre, elle peut devenir une
	// buche brulee (6 charbons de bois a l'etabli) au lieu de disparaitre.
	// Chaque colonne de tronc est traitee a part : un arbre 2x2 a 4 chances.

	/**
	 * Chance (0 a 1) qu'un ARBRE qui brule laisse une buche brulee a son pied.
	 *
	 * La chance est ensuite repartie entre les colonnes du tronc : un chene
	 * (1 colonne) la joue d'un coup, un chene noir ou un sapin geant (4
	 * colonnes) a la meme chance globale, repartie sur ses quatre pieds.
	 */
	public static final float BURNT_LOG_TREE_CHANCE = 0.6F;

	/**
	 * Un arbre se vide du haut vers le bas : quand le feu consume une buche,
	 * c'est celle du sommet de la colonne qui disparait, meme si les flammes
	 * sont au pied. Plus de troncs qui flottent, et l'incendie ne dure pas plus
	 * longtemps : une buche consumee reste une buche consumee.
	 */
	public static final boolean BURNT_LOG_BURN_TOP_DOWN = true;

	/** Profondeur de recherche du pied d'un tronc (hauteur maximale d'un arbre). */
	public static final int BURNT_LOG_TRUNK_SCAN = 32;

	/** Nombre maximum de buches brulees par colonne, en partant du sol (1 = seulement celle du sol). */
	public static final int BURNT_LOG_MAX_PER_COLUMN = 2;

	/**
	 * Chance que la buche du dessus brule aussi, si celle du dessous a deja
	 * reussi (chaque buche supplementaire refait ce tirage).
	 */
	public static final float BURNT_LOG_NEXT_CHANCE = 0.5F;

	/**
	 * Duree (ticks) pendant laquelle le tirage d'une colonne reste le meme.
	 * Les buches d'un tronc ne brulent pas dans l'ordre : le resultat est
	 * tire une fois par colonne (et par periode) pour que "celle du dessus"
	 * suive toujours "celle du sol". 6000 = 5 minutes.
	 */
	public static final int BURNT_LOG_ROLL_PERIOD_TICKS = 6000;

	/**
	 * Chance qu'une buche soit consumee quand un feu la touche (vanilla : 5,
	 * feuilles : 60). Le jeu tire sur 300 a chaque tick du feu : a 5, le feu
	 * s'eteint souvent avant d'avoir brule le tronc. Touche TOUTES les buches
	 * inflammables (maisons en bois comprises).
	 */
	public static final int FIRE_LOG_BURN_ODDS = 5;

	/** Chance qu'un feu voisin se propage a une buche (vanilla : 5). */
	public static final int FIRE_LOG_IGNITE_ODDS = 5;

	// ---- Buche brulee incandescente ------------------------------------------

	/** Duree (ticks) avant qu'une buche incandescente s'eteigne (1200 = 1 min). */
	public static final int IGNITED_BURNT_LOG_DURATION_TICKS = 1200;

	/** Intervalle (ticks) des verifications : feu proche, propagation, extinction. */
	public static final int IGNITED_BURNT_LOG_CHECK_TICKS = 20;

	/** Nombre maximum de faces enflammees par verification (1 = la buche n'allume qu'un voisin a la fois). */
	public static final int IGNITED_BURNT_LOG_IGNITE_MAX_FACES = 1;

	/** Chance, a chaque verification et pour chaque bloc inflammable qui la touche, d'y mettre le feu. */
	public static final float IGNITED_BURNT_LOG_IGNITE_CHANCE = 0.15F;

	/** Une buche voisine sans aucune face a l'air (coeur d'un tronc large) est consumee directement. */
	public static final boolean IGNITED_BURNT_LOG_CONSUMES_ENCLOSED_LOGS = true;

	/** Lumiere emise (0 a 15). */
	public static final int IGNITED_BURNT_LOG_LIGHT = 9;

	/** Degats en marchant dessus, comme le bloc de magma (1.0 = un demi-coeur, 0 = aucun). */
	public static final float IGNITED_BURNT_LOG_STEP_DAMAGE = 1.0F;

	/** Particules : une chance sur N, a chaque image d'ambiance (plus grand = plus rare). */
	public static final int IGNITED_BURNT_LOG_SMOKE_CHANCE = 12;
	public static final int IGNITED_BURNT_LOG_ASH_CHANCE = 8;

	/**
	 * Hauteur de depart des cendres, en partant du bas du bloc. Leur vitesse
	 * n'est pas reglable (elles descendent toujours un peu) : monte cette
	 * valeur pour qu'elles tombent le long du bloc au lieu de dessous.
	 */
	public static final double IGNITED_BURNT_LOG_ASH_HEIGHT = 1.0;

	/** Duree de cuisson (ticks) d'un aliment pose sur la buche, quel qu'il soit (600 = 30 s). */
	public static final int IGNITED_BURNT_LOG_COOK_TICKS = 600;

	/** Particules du "pschitt" quand la buche s'eteint : vapeur, puis fumee. */
	public static final int IGNITED_BURNT_LOG_DOUSE_STEAM = 5;
	public static final int IGNITED_BURNT_LOG_DOUSE_SMOKE = 3;

	/** La pluie eteint la buche (si le ciel est visible au-dessus). L'eau qui la touche l'eteint toujours. */
	public static final boolean IGNITED_BURNT_LOG_RAIN_DOUSES = true;

	/**
	 * Inflammabilite minimale d'un bloc pour que les braises en bouteille
	 * puissent l'allumer (herbe et feuilles mortes : 100, laine et feuilles :
	 * 60, planches : 20, buche : 5). Voir aussi le tag arcamod:hot_coal_ignitable.
	 */
	public static final int HOT_COAL_MIN_BURN_ODDS = 60;

	/** Recuperer des braises avec une bouteille eteint la buche. */
	public static final boolean HOT_COAL_EXTINGUISHES_LOG = true;

	/** Combustible au four : buche brulee et incandescente = autant de charbons de bois. */
	public static final int BURNT_LOG_FUEL_CHARCOAL = 6;

	/** Lumiere des braises en bouteille tenues en main (lumiere dynamique, 0 a 15). */
	public static final float HOT_COAL_IN_A_BOTTLE_LIGHT = 6.0F;

	// ---- Cendre ---------------------------------------------------------------
	// Le feu depose de la cendre sous ce qui brule. Un nouvel endroit ne recoit
	// qu'une couche ; en revanche un depot epaissit en priorite un tas voisin
	// (ASH_STACK_CHANCE) et lui ajoute alors plusieurs couches d'un coup. Avec
	// des positions qui penchent vers le centre, on obtient quelques monticules
	// au pied de l'arbre, entoures de fines couches, au lieu d'un tapis.

	/** Chance qu'une buche consumee par le feu laisse une couche de cendre. */
	public static final float ASH_FROM_LOG_CHANCE = 0.6F;

	/**
	 * Chance qu'un bloc de bois travaille consume (planches, escalier,
	 * barriere... tag arcamod:ash_from_wood) laisse de la cendre. Volontairement
	 * plus faible que pour une buche.
	 */
	public static final float ASH_FROM_WOOD_CHANCE = 0.12F;

	/** Chance qu'un bloc de feuilles consume laisse une couche de cendre (il y en a beaucoup). */
	public static final float ASH_FROM_LEAVES_CHANCE = 0.05F;

	/** Chance d'epaissir un tas voisin plutot que d'en commencer un nouveau. */
	public static final float ASH_STACK_CHANCE = 0.92F;

	/**
	 * Poids du nombre de couches d'un tas tout neuf : 1 couche, 2 couches,
	 * 3 couches. Les valeurs peuvent avoir des decimales ; seul leur rapport
	 * compte. Ici 7.5/2/0.5, soit 75 %, 20 % et 5 %.
	 */
	public static final float[] ASH_NEW_PILE_WEIGHTS = { 7.5F, 2.0F, 0.5F };

	/** Couches ajoutees d'un coup quand un depot epaissit un tas existant. */
	public static final int ASH_BURST_MIN = 2;
	public static final int ASH_BURST_MAX = 4;

	/**
	 * Rayon (blocs) autour du bloc brule ou une NOUVELLE couche peut se poser.
	 * Le tirage est triangulaire : le centre sort bien plus souvent que le bord.
	 */
	public static final int ASH_SPREAD_RADIUS = 1;

	/**
	 * Rayon (blocs) ou l'on cherche un tas a epaissir. Plus large que le rayon
	 * de pose : une cendre qui tombe un peu a cote rejoint quand meme le tas
	 * voisin au lieu d'en commencer un nouveau.
	 */
	public static final int ASH_PILE_SEARCH_RADIUS = 3;

	/** Hauteur de chute maximale : la cendre cherche le sol sous elle sur cette distance. */
	public static final int ASH_FALL_DISTANCE = 12;

	/** Nombre d'endroits essayes avant d'abandonner (un endroit plein ou sans sol ne compte pas). */
	public static final int ASH_PLACE_ATTEMPTS = 2;

	/** Couches maximum deposees par le feu (1 couche = 1 pixel ; a la main : 16, le bloc plein). */
	public static final int ASH_NATURAL_MAX_LAYERS = 6;

	/** Rayon (blocs) dans lequel un feu ou un feu de camp allume rallume / entretient une buche brulee. */
	public static final int BURNT_LOG_REIGNITE_RADIUS = 1;

	/** Rayon (blocs) pour la lave (1 = il faut qu'elle touche la buche). */
	public static final int BURNT_LOG_LAVA_RADIUS = 1;

	/** Intervalle (ticks) auquel une buche brulee eteinte cherche un feu proche. */
	public static final int BURNT_LOG_REIGNITE_CHECK_TICKS = 40;

	/** Durete de la cendre (neige fine : 0.1). */
	public static final float ASH_HARDNESS = 0.1F;

	/** Durete de la buche brulee (buche vanilla : 2.0). Le charbon obtenu a l'etabli est dans les recettes JSON. */
	public static final float BURNT_LOG_HARDNESS = 1.5F;

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
