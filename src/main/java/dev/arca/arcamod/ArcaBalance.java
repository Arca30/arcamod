package dev.arca.arcamod;

/**
 * TOUTES les valeurs d'equilibrage du mod sont ici.
 *
 * C'est le seul fichier Java a toucher pour regler le jeu. Les reglages qui
 * vivent forcement dans des fichiers de donnees (butin, recettes, tags de
 * paliers d'outils) et la taille des cailloux (geometrie des modeles) sont
 * listes dans BALANCING.md a la racine du projet.
 */
public final class ArcaBalance {

	/** Degats du joueur a mains nues. Sert de base a tous les calculs d'arme. */
	public static final float PLAYER_BASE_ATTACK_DAMAGE = 1.0F;

	/** Vitesse d'attaque du joueur a mains nues, en coups par seconde. */
	public static final float PLAYER_BASE_ATTACK_SPEED = 4.0F;

	// ------------------------------------------------------------------
	// 1. Nerf des outils en bois (vanilla)
	// ------------------------------------------------------------------

	/** 0.20 = -80% de durabilite. Vanilla : 59 utilisations -> 12. */
	public static final float WOOD_DURABILITY_MULTIPLIER = 0.20F;

	/** 0.70 = -30% de vitesse de minage. Vanilla : 2.0 -> 1.4. */
	public static final float WOOD_MINING_SPEED_MULTIPLIER = 0.70F;

	// ------------------------------------------------------------------
	// 2. Outil en silex (pioche + hache + pelle du debut de partie)
	// ------------------------------------------------------------------

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

	// ------------------------------------------------------------------
	// 3. Dague en silex
	// ------------------------------------------------------------------

	/** Durabilite. Volontairement plus basse que l'outil : c'est une lame. */
	public static final int DAGGER_DURABILITY = 90;

	/**
	 * Degats du coup au corps a corps, en coeurs.
	 *
	 * ATTENTION a la valeur par defaut : 5 coeurs (10 points de degats) font
	 * de la dague l'arme la plus puissante du jeu, devant l'epee en netherite
	 * (8). C'est la valeur demandee ; 3 coeurs la mettraient au niveau d'une
	 * bonne epee de debut de partie.
	 */
	public static final float DAGGER_MELEE_DAMAGE_HEARTS = 2.0F;

	/**
	 * Cooldown d'attaque par rapport a une epee. 0.70 = 30% plus court, donc
	 * environ 2.29 coups par seconde contre 1.6 pour une epee.
	 */
	public static final float DAGGER_COOLDOWN_MULTIPLIER = 0.70F;

	/** Vitesse d'attaque d'une epee vanilla, sert de point de depart. */
	public static final float SWORD_ATTACK_SPEED = -2.4F;

	/** Degats de la dague lancee, en coeurs. */
	public static final float DAGGER_THROWN_DAMAGE_HEARTS = 2.0F;

	/** Puissance du lancer (fleche a l'arc charge a fond = 3.0). */
	public static final float DAGGER_THROW_POWER = 1.2F;

	/** Dispersion du lancer. 0 = parfaitement droit. */
	public static final float DAGGER_THROW_INACCURACY = 0.6F;

	/** Cooldown entre deux lancers, en ticks (20 ticks = 1 s). */
	public static final int DAGGER_THROW_COOLDOWN_TICKS = 20;

	/** Points de durabilite perdus a chaque lancer. */
	public static final int DAGGER_THROW_DURABILITY_COST = 3;

	/** Enchantabilite. */
	public static final int DAGGER_ENCHANTMENT_VALUE = 5;

	/**
	 * true  : la dague tombe en item des qu'elle touche une creature.
	 * false : elle reste plantee dedans jusqu'a la mort du mob.
	 */
	public static final boolean DAGGER_DROPS_AFTER_HIT = true;

	/**
	 * Profondeur, en blocs, dont la lame s'enfonce dans la surface touchee.
	 * 0 = elle s'arrete pile sur la face (et a l'air posee dessus).
	 */
	public static final double DAGGER_EMBED_DEPTH = 0.20;

	/** Fibres recoltees par touffe d'herbe coupee a la dague. */
	public static final int PLANT_FIBER_PER_GRASS = 1;

	/** Probabilite d'obtenir des fibres en coupant une herbe a la dague. */
	public static final float PLANT_FIBER_CHANCE = 0.2F;

	// ------------------------------------------------------------------
	// 3 bis. Elytres de fortune
	// ------------------------------------------------------------------

	/**
	 * Durabilite des elytres en membranes. Vanilla : 432.
	 *
	 * Elles ne sont pas enchantables (donc ni Solidite ni Raccommodage), mais
	 * restent reparables a la membrane de phantom.
	 */
	public static final int PATCHWORK_ELYTRA_DURABILITY = 4;

	// ------------------------------------------------------------------
	// 4. Petites pierres et branches au sol (blocs de decor ramassables)
	// ------------------------------------------------------------------

	/** Nombre de cailloux empilables sur un meme bloc. */
	public static final int PEBBLES_MIN = 1;
	public static final int PEBBLES_MAX = 5;

	/** Nombre de branches empilables sur un meme bloc. */
	public static final int STICKS_MIN = 1;
	public static final int STICKS_MAX = 4;

	/**
	 * Hauteur de la boite de selection de ces blocs, en pixels.
	 *
	 * Elle couvre tout le dessus du bloc : sans ca, avec des cailloux places
	 * au hasard sur la surface, viser le caillou reviendrait souvent a miner
	 * le bloc du dessous.
	 */
	public static final double CLUTTER_SHAPE_HEIGHT = 1.0;

	// ------------------------------------------------------------------
	// 4 bis. Oeufs de poule poses au sol
	// ------------------------------------------------------------------

	/** Nombre d'oeufs sur un meme bloc. */
	public static final int EGGS_MIN = 1;
	public static final int EGGS_MAX = 3;

	/** Duree d'eclosion, en ticks. 2400 = 2 minutes (20 ticks = 1 s). */
	public static final int EGG_HATCH_TICKS = 2000;

	// ------------------------------------------------------------------
	// 4 ter. Chaume (la paille battue qui grise avec le temps)
	// ------------------------------------------------------------------

	/**
	 * Probabilite qu'un tick aleatoire fasse vieillir un bloc de chaume d'une
	 * etape. Un bloc recoit en moyenne trois ticks aleatoires par minute :
	 * 0.05 donne environ une etape toutes les sept minutes.
	 */
	public static final float THATCH_FADE_CHANCE = 0.05F;

	// ------------------------------------------------------------------
	// 4 quater. Cristal d'enchantement
	// ------------------------------------------------------------------

	/** Rayon, en blocs, dans lequel la mort d'une creature charge le cristal. */
	public static final int CRYSTAL_CHARGE_RADIUS = 5;

	/** Rayon, en blocs, dans lequel il faut une table d'enchantement. */
	public static final int CRYSTAL_TABLE_RADIUS = 5;

	// ------------------------------------------------------------------
	// 4 quinquies. Desenchanteur
	// ------------------------------------------------------------------

	/** Cout fixe, en niveaux d'experience, pour extraire un enchantement. */
	public static final int DISENCHANT_XP_BASE = 1;

	/** Cout supplementaire par niveau de l'enchantement extrait. */
	public static final int DISENCHANT_XP_PER_LEVEL = 1;

	// ------------------------------------------------------------------
	// 4 sexies. Chaudron a potions
	// ------------------------------------------------------------------

	/**
	 * Nombre de fioles pour remplir le chaudron. Le niveau visible monte par
	 * paliers : avec 6, il faut deux fioles par niveau.
	 */
	public static final int POTION_CAULDRON_CAPACITY = 3;

	/** Duree de l'effet donne a une creature dans le chaudron, en ticks. */
	public static final int POTION_CAULDRON_EFFECT_TICKS = 80;

	/** Intervalle entre deux applications de l'effet, en ticks. */
	public static final int POTION_CAULDRON_REFRESH_TICKS = 20;

	/**
	 * Temps au bout duquel une preparation sans feu de camp allume dessous
	 * retombe en eau. 6000 ticks = 5 minutes.
	 */
	public static final int POTION_CAULDRON_DECAY_TICKS = 300;

	// ------------------------------------------------------------------
	// 4 septies. Fleches a effet
	// ------------------------------------------------------------------

	/** true = une fleche a effet laisse une flaque la ou elle se plante. */
	public static final boolean ARROW_LEAVES_CLOUD = true;

	/** Rayon de la flaque au moment de l'impact, en blocs. */
	public static final float ARROW_CLOUD_RADIUS = 3.0F;

	/** Duree de vie de la flaque, en ticks. 100 = 5 secondes. */
	public static final int ARROW_CLOUD_DURATION_TICKS = 150;

	/**
	 * Multiplicateur applique a la duree des effets donnes par la flaque.
	 * Repere : une potion persistante vanilla utilise 0.25.
	 */
	public static final float ARROW_CLOUD_EFFECT_SCALE = 0.25F;

	/** Delai avant que la flaque ne commence a agir, en ticks. */
	public static final int ARROW_CLOUD_WAIT_TICKS = 5;

	// ------------------------------------------------------------------
	// 5. Petite pierre lancee
	// ------------------------------------------------------------------

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

	/** Rayon de recherche d'une place libre autour d'un mob touche. */
	public static final int PEBBLE_MOB_SCATTER_RADIUS = 0;

	/** Profondeur maximale de chute quand la pierre tape un mur. */
	public static final int PEBBLE_MAX_FALL_SEARCH = 4;

	/** Si aucune place n'est libre : true = tombe en item, false = disparait. */
	public static final boolean PEBBLE_DROP_ITEM_IF_NO_ROOM = false;

	// ------------------------------------------------------------------
	// 6. Generation dans le monde
	// ------------------------------------------------------------------

	/**
	 * TAUX D'APPARITION, entre 0 et 1 : probabilite qu'un chunk eligible
	 * recoive un amas. 0 = plus rien du tout.
	 */
	public static final float PEBBLE_PATCH_CHANCE = 0.13F;
	public static final float STICK_PATCH_CHANCE = 0.13F;

	/** Nombre de blocs par amas de cailloux (tirage entre les deux bornes). */
	public static final int PEBBLE_PATCH_MIN_BLOCKS = 1;
	public static final int PEBBLE_PATCH_MAX_BLOCKS = 5;

	/** Rayon horizontal de dispersion d'un amas de cailloux, en blocs. */
	public static final int PEBBLE_PATCH_SPREAD = 5;

	/** Les amas de branches sont plus petits et plus serres. */
	public static final int STICK_PATCH_MIN_BLOCKS = 1;
	public static final int STICK_PATCH_MAX_BLOCKS = 3;
	public static final int STICK_PATCH_SPREAD = 5;

	/** Ecart vertical tolere entre le centre de l'amas et chaque bloc. */
	public static final int PATCH_VERTICAL_SPREAD = 3;

	/** Nombre d'essais de placement par amas. */
	public static final int PATCH_TRIES = 16;

	/**
	 * Temperature minimale d'un biome pour qu'il recoive pierres et branches.
	 *
	 * Reperes vanilla : desert 2.0, savane 2.0, plaines 0.8, foret 0.7,
	 * taiga 0.25, montagnes venteuses 0.2, plaines enneigees 0.0,
	 * bosquet -0.2, pics geles -0.7.
	 *
	 * 0.15 laisse passer les biomes temperes et chauds et coupe tout ce qui
	 * est enneige.
	 */
	public static final float BIOME_MIN_TEMPERATURE = 0.15F;

	// ------------------------------------------------------------------
	// Conversions (pour ne pas dupliquer ces calculs dans le reste du code)
	// ------------------------------------------------------------------

	/** Coeurs -> points de degats (1 coeur = 2 points). */
	public static float hearts(float hearts) {
		return hearts * 2.0F;
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

	/**
	 * Modificateur de vitesse d'attaque de la dague, deduit du cooldown
	 * souhaite par rapport a une epee.
	 */
	public static float daggerAttackSpeed() {
		float swordRate = PLAYER_BASE_ATTACK_SPEED + SWORD_ATTACK_SPEED;
		return swordRate / DAGGER_COOLDOWN_MULTIPLIER - PLAYER_BASE_ATTACK_SPEED;
	}

	private ArcaBalance() {
	}
}
