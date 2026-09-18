package dev.arca.arcamod.registry;

import dev.arca.arcamod.ArcaMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
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

	/** Les lits qui exigent un toit au-dessus d'eux pour etre utilises. */
	public static final TagKey<Block> BEDS_REQUIRING_SHELTER =
			TagKey.create(Registries.BLOCK, ArcaMod.id("beds_requiring_shelter"));

	/** Ce qui repare un outil en silex dans l'enclume. */
	public static final TagKey<Item> FLINT_TOOL_MATERIALS =
			TagKey.create(Registries.ITEM, ArcaMod.id("flint_tool_materials"));

	/**
	 * Les blocs sur lesquels on peut s'asseoir pres d'un feu de camp (main
	 * vide, clic droit sur le dessus). Uniquement des blocs sans interaction
	 * propre : un clic droit sur un coffre doit toujours l'ouvrir.
	 */
	public static final TagKey<Block> CAMPFIRE_SEATS =
			TagKey.create(Registries.BLOCK, ArcaMod.id("campfire_seats"));

	/** Blocs que traverse une fleche a corps en os (defaut : les feuilles). */
	public static final TagKey<Block> BONE_ARROW_PASSES_THROUGH =
			TagKey.create(Registries.BLOCK, ArcaMod.id("bone_arrow_passes_through"));

	/** Blocs que brise une fleche a corps en os (defaut : verre et vitres). */
	public static final TagKey<Block> BONE_ARROW_BREAKS =
			TagKey.create(Registries.BLOCK, ArcaMod.id("bone_arrow_breaks"));

	/** Ce qui repare les outils en or rose (defaut : le lingot d'or rose). */
	public static final TagKey<Item> PINK_GOLD_TOOL_MATERIALS =
			TagKey.create(Registries.ITEM, ArcaMod.id("pink_gold_tool_materials"));

	/** Ce qui repare l'armure en or rose (defaut : le lingot d'or rose). */
	public static final TagKey<Item> REPAIRS_PINK_GOLD_ARMOR =
			TagKey.create(Registries.ITEM, ArcaMod.id("repairs_pink_gold_armor"));

	/** Blocs sur lesquels pousse le buisson de piments des ames (defaut : sable et terre des ames). */
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

	private ModTags() {
	}
}
