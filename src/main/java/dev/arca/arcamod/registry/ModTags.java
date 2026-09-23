package dev.arca.arcamod.registry;

import dev.arca.arcamod.ArcaMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Les tags du mod. Le contenu de chaque tag se decrit en JSON dans
 * src/main/resources/data/arcamod/tags/.
 */
public final class ModTags {

	/**
	 * Tout ce que l'outil en silex mine efficacement.
	 *
	 * Le JSON se contente d'inclure les trois tags vanilla mineable/pickaxe,
	 * mineable/axe et mineable/shovel : l'outil suit donc automatiquement les
	 * blocs ajoutes par les autres mods a ces tags.
	 */
	public static final TagKey<Block> MINEABLE_WITH_FLINT_TOOL =
			TagKey.create(Registries.BLOCK, ArcaMod.id("mineable/flint_tool"));

	/**
	 * Les blocs qui ne lachent plus rien quand on les casse a mains nues.
	 *
	 * Par defaut le JSON n'y met que #minecraft:logs : plus moyen de ramasser
	 * une buche a la main, il faut au minimum un outil en silex. C'est le
	 * point de depart de toute la progression du debut de partie.
	 */
	public static final TagKey<Block> REQUIRES_TOOL_FOR_DROPS =
			TagKey.create(Registries.BLOCK, ArcaMod.id("requires_tool_for_drops"));

	/**
	 * Blocs devant lesquels un cadre garde un pixel de plus (ArcaBalance.
	 * ITEM_FRAME_SNAP_EXTRA_CLEARANCE) : ceux dont le modele depasse de leur
	 * hitbox, comme le loquet d'un coffre.
	 */
	public static final TagKey<Block> ITEM_FRAME_EXTRA_CLEARANCE =
			TagKey.create(Registries.BLOCK, ArcaMod.id("item_frame_extra_clearance"));

	/** Les lits qui exigent un toit au-dessus d'eux pour etre utilises. */
	public static final TagKey<Block> BEDS_REQUIRING_SHELTER =
			TagKey.create(Registries.BLOCK, ArcaMod.id("beds_requiring_shelter"));

	/**
	 * Blocs sans texture de craquelure au minage.
	 *
	 * Le jeu plaque la craquelure sur le cube entier : sur un bloc tres fin
	 * (la corde, la plaquette) on verrait un carre fissure flotter dans le
	 * vide. Purement visuel, le minage n'est pas touche.
	 */
	public static final TagKey<Block> NO_BREAK_OVERLAY =
			TagKey.create(Registries.BLOCK, ArcaMod.id("no_break_overlay"));

	/** Ce qui repare un outil en silex dans l'enclume. */
	public static final TagKey<Item> FLINT_TOOL_MATERIALS =
			TagKey.create(Registries.ITEM, ArcaMod.id("flint_tool_materials"));

	/** Blocs que traverse une fleche a corps en os (defaut : les feuilles). */
	public static final TagKey<Block> BONE_ARROW_PASSES_THROUGH =
			TagKey.create(Registries.BLOCK, ArcaMod.id("bone_arrow_passes_through"));

	/** Blocs que brise une fleche a corps en os (defaut : verre et vitres). */
	public static final TagKey<Block> BONE_ARROW_BREAKS =
			TagKey.create(Registries.BLOCK, ArcaMod.id("bone_arrow_breaks"));

	/** Outils qui acceptent les garnitures d'armure (recettes data/arcamod/recipe/tool_trim/). */
	/** Lames qui coupent des fibres dans les herbes (dague + toutes les epees). */
	public static final TagKey<Item> CUTS_PLANT_FIBER =
			TagKey.create(Registries.ITEM, ArcaMod.id("cuts_plant_fiber"));

	/** Herbes qui donnent des fibres (coupees a la lame, ou a la table de craft avec une lame). */
	public static final TagKey<Item> PLANT_FIBER_GRASSES =
			TagKey.create(Registries.ITEM, ArcaMod.id("plant_fiber_grasses"));

	public static final TagKey<Item> TRIMMABLE_TOOLS =
			TagKey.create(Registries.ITEM, ArcaMod.id("trimmable_tools"));

	/** Creatures qui prennent le bonus de la pointe en prismarine (defaut : aquatiques + noye). */
	public static final TagKey<EntityType<?>> ARROW_PRISMARINE_TARGETS =
			TagKey.create(Registries.ENTITY_TYPE, ArcaMod.id("arrow_prismarine_targets"));

	/** Creatures volantes, bonus de l'empennage en membrane de phantom. */
	public static final TagKey<EntityType<?>> ARROW_ANTI_AIR_TARGETS =
			TagKey.create(Registries.ENTITY_TYPE, ArcaMod.id("arrow_anti_air_targets"));

	/** Creatures que la pointe en chorus ne teleporte pas (boss). */
	public static final TagKey<EntityType<?>> ARROW_CHORUS_IMMUNE =
			TagKey.create(Registries.ENTITY_TYPE, ArcaMod.id("arrow_chorus_immune"));

	/** Ce qui repare les outils en or rose (defaut : le lingot d'or rose). */
	public static final TagKey<Item> PINK_GOLD_TOOL_MATERIALS =
			TagKey.create(Registries.ITEM, ArcaMod.id("pink_gold_tool_materials"));

	/** Ce qui repare l'armure en or rose (defaut : le lingot d'or rose). */
	public static final TagKey<Item> REPAIRS_PINK_GOLD_ARMOR =
			TagKey.create(Registries.ITEM, ArcaMod.id("repairs_pink_gold_armor"));

	/** Blocs sur lesquels pousse le buisson de piments des ames (defaut : sable et terre des ames). */
	/** Blocs sur lesquels les oeufs de poule eclosent (paille et chaume). */
	public static final TagKey<Block> EGG_HATCHING_BLOCKS =
			TagKey.create(Registries.BLOCK, ArcaMod.id("egg_hatching_blocks"));

	public static final TagKey<Block> SOUL_PEPPER_PLANTABLE_ON =
			TagKey.create(Registries.BLOCK, ArcaMod.id("soul_pepper_plantable_on"));

	/** Ce qui se range dans la ceinture a outils (outils, briquet, cisailles, lanternes...). */
	public static final TagKey<Item> TOOL_BELT_ALLOWED =
			TagKey.create(Registries.ITEM, ArcaMod.id("tool_belt_allowed"));

	/** Le sol sous lequel une buche peut bruler en buche brulee (defaut : #minecraft:dirt). */
	public static final TagKey<Block> BURNT_LOG_GROUND =
			TagKey.create(Registries.BLOCK, ArcaMod.id("burnt_log_ground"));

	/** Blocs que les braises en bouteille peuvent enflammer (defaut : laine, feuilles, tapis, foin). */
	public static final TagKey<Block> HOT_COAL_IGNITABLE =
			TagKey.create(Registries.BLOCK, ArcaMod.id("hot_coal_ignitable"));

	/**
	 * Les blocs de bois travailles (planches, escaliers, barrieres, portes...)
	 * qui laissent aussi un peu de cendre en brulant, moins que les buches.
	 */
	public static final TagKey<Block> ASH_FROM_WOOD =
			TagKey.create(Registries.BLOCK, ArcaMod.id("ash_from_wood"));

	/**
	 * Couvertures au sol qui brulent d'un coup : le feu prend leur place au
	 * lieu de se poser dessus (feuilles mortes, herbe sechee...).
	 */
	public static final TagKey<Block> BURNS_INSTANTLY =
			TagKey.create(Registries.BLOCK, ArcaMod.id("burns_instantly"));

	// ---- Pitcher plant carnivore ---------------------------------------------

	/**
	 * Ce qui vaut PITCHER_FEED_WEAK_POINTS a une pitcher plant (par defaut :
	 * la chair putrefiee).
	 */
	public static final TagKey<Item> PITCHER_FEED_WEAK =
			TagKey.create(Registries.ITEM, ArcaMod.id("pitcher_feed_weak"));

	/**
	 * Ce qui vaut PITCHER_FEED_STRONG_POINTS (par defaut : toutes les viandes
	 * crues, poissons compris).
	 */
	public static final TagKey<Item> PITCHER_FEED_STRONG =
			TagKey.create(Registries.ITEM, ArcaMod.id("pitcher_feed_strong"));

	/**
	 * Ce que la plante pleine fait pousser plus vite autour d'elle.
	 *
	 * Un filtre est indispensable : beaucoup de blocs "tickent" au hasard
	 * sans etre des plantes (le feu, la neige qui fond, le cuivre). Ajouter
	 * une plante d'un autre mod = une ligne dans le JSON, rien a recompiler.
	 */
	public static final TagKey<Block> PITCHER_BOOST_GROWS =
			TagKey.create(Registries.BLOCK, ArcaMod.id("pitcher_boost_grows"));

	// ---- Lumiere des fleurs ---------------------------------------------------

	/** Fleurs qui eclairent comme une torche (ArcaBalance.TORCHFLOWER_LIGHT). */
	public static final TagKey<Block> TORCH_LIGHT_FLOWERS =
			TagKey.create(Registries.BLOCK, ArcaMod.id("torch_light_flowers"));

	/** Fleurs qui eclairent faiblement (ArcaBalance.OPEN_EYEBLOSSOM_LIGHT). */
	public static final TagKey<Block> DIM_LIGHT_FLOWERS =
			TagKey.create(Registries.BLOCK, ArcaMod.id("dim_light_flowers"));

	// ---- Torchflower protectrice et allay porte-lanterne ----------------------

	/**
	 * Les fleurs qui arretent le feu et le gel autour d'elles
	 * (ArcaBalance section 54). Par defaut la torchflower et sa version en pot.
	 */
	public static final TagKey<Block> TORCHFLOWER_WARD =
			TagKey.create(Registries.BLOCK, ArcaMod.id("torchflower_ward"));

	/** Ce qu'un allay doit tenir pour devenir une lanterne volante. */
	public static final TagKey<Item> ALLAY_LANTERNS =
			TagKey.create(Registries.ITEM, ArcaMod.id("allay_lanterns"));

	// ---- Feu de camp gratte ---------------------------------------------------

	/** Ce qui rallume un tas de buches (briquet, boule de feu). */
	public static final TagKey<Item> CAMPFIRE_IGNITERS =
			TagKey.create(Registries.ITEM, ArcaMod.id("campfire_igniters"));

	// ---- Mousse qui deborde ---------------------------------------------------

	/** Les tapis dont la mousse deborde sur le bloc du dessous. */
	public static final TagKey<Block> MOSS_CARPETS =
			TagKey.create(Registries.BLOCK, ArcaMod.id("moss_carpets"));

	/**
	 * Les blocs NATURELS sur lesquels la mousse deborde. Poser un tapis sur
	 * une planche ou une table de craft ne salit donc rien.
	 */
	public static final TagKey<Block> MOSS_SKIRT_BLOCKS =
			TagKey.create(Registries.BLOCK, ArcaMod.id("moss_skirt_blocks"));

	private ModTags() {
	}
}
