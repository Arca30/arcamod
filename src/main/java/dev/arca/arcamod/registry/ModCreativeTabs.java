package dev.arca.arcamod.registry;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/**
 * Range les items du mod dans les onglets creatifs vanilla.
 *
 * Chaque item est insere A COTE de son equivalent vanilla (insertAfter /
 * insertBefore) et pas a la fin de l'onglet : l'or rose suit l'or, le chaume
 * suit le foin, les escaliers suivent leur bloc plein... Si l'objet repere est
 * absent (un autre mod l'a retire), l'item atterrit en fin d'onglet.
 *
 * En 26.2 les onglets sont des entrees de registre : on les cible par leur
 * ResourceKey (il n'y a plus de constantes publiques dans CreativeModeTabs).
 */
public final class ModCreativeTabs {

	private static final ResourceKey<CreativeModeTab> NATURAL_BLOCKS = vanillaTab("natural_blocks");
	private static final ResourceKey<CreativeModeTab> FOOD_AND_DRINKS = vanillaTab("food_and_drinks");
	private static final ResourceKey<CreativeModeTab> FUNCTIONAL_BLOCKS = vanillaTab("functional_blocks");
	private static final ResourceKey<CreativeModeTab> TOOLS_AND_UTILITIES = vanillaTab("tools_and_utilities");
	private static final ResourceKey<CreativeModeTab> COMBAT = vanillaTab("combat");
	private static final ResourceKey<CreativeModeTab> INGREDIENTS = vanillaTab("ingredients");
	private static final ResourceKey<CreativeModeTab> BUILDING_BLOCKS = vanillaTab("building_blocks");
	private static final ResourceKey<CreativeModeTab> COLORED_BLOCKS = vanillaTab("colored_blocks");

	private static ResourceKey<CreativeModeTab> vanillaTab(String name) {
		return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace(name));
	}

	/** Les matieres colorees vivent dans l'onglet "blocs colores", pas "construction". */
	private static boolean isColoured(String base) {
		return base.endsWith("terracotta") || base.endsWith("concrete_powder");
	}

	private static Block vanilla(String name) {
		return BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(name));
	}

	public static void init() {
		// ---- Blocs colores : terre cuite et beton ---------------------------
		CreativeModeTabEvents.modifyOutputEvent(COLORED_BLOCKS)
				.register(output -> {
					for (String base : ModDecorBlocks.VANILLA_BASES) {
						if (isColoured(base)) {
							output.insertAfter(vanilla(base),
									ModDecorBlocks.byName(base + "_stairs"),
									ModDecorBlocks.byName(base + "_slab"));
						}
					}
				});

		// ---- Blocs de construction ------------------------------------------
		CreativeModeTabEvents.modifyOutputEvent(BUILDING_BLOCKS)
				.register(output -> {
					// Les deux mousses, avec leurs escaliers et dalles, juste
					// avant la pierre (elles n'etaient pas dans cet onglet).
					output.insertBefore(Items.STONE,
							Items.MOSS_BLOCK,
							ModDecorBlocks.byName("moss_block_stairs"),
							ModDecorBlocks.byName("moss_block_slab"),
							Items.PALE_MOSS_BLOCK,
							ModDecorBlocks.byName("pale_moss_block_stairs"),
							ModDecorBlocks.byName("pale_moss_block_slab"));

					// L'or rose apres tout ce qui est en or.
					output.insertAfter(Items.LIGHT_WEIGHTED_PRESSURE_PLATE, ModItems.PINK_GOLD_BLOCK);

					// Le foin (absent de cet onglet) ouvre la famille du chaume.
					output.accept(Items.HAY_BLOCK);
					output.accept(ModDecorBlocks.byName("hay_block_stairs"));
					output.accept(ModDecorBlocks.byName("hay_block_slab"));

					for (String stage : ModDecorBlocks.THATCH_STAGES) {
						output.accept(ModDecorBlocks.byName(stage));
						output.accept(ModDecorBlocks.byName(stage + "_stairs"));
						output.accept(ModDecorBlocks.byName(stage + "_slab"));
					}
				});

		// ---- Blocs naturels ---------------------------------------------------
		// (ni les petites pierres ni les branches : ce sont des objets a
		// ramasser, ranges avec les munitions et les ingredients.)
		CreativeModeTabEvents.modifyOutputEvent(NATURAL_BLOCKS)
				.register(output -> {
					output.insertAfter(Items.DEEPSLATE_GOLD_ORE,
							ModItems.PINK_GOLD_ORE, ModItems.DEEPSLATE_PINK_GOLD_ORE);
					output.insertAfter(Items.RAW_GOLD_BLOCK, ModItems.RAW_PINK_GOLD_BLOCK);
					output.insertAfter(Items.GLOW_BERRIES, ModItems.XP_BERRY);
					output.insertAfter(Items.SULFUR, ModItems.BRUSHED_SULFUR);
					output.insertAfter(Items.CHERRY_LOG, ModItems.BURNT_LOG, ModItems.IGNITED_BURNT_LOG, ModItems.BURNT_WOOD,
							ModItems.ASH);
				});

		// ---- Blocs fonctionnels ------------------------------------------------
		CreativeModeTabEvents.modifyOutputEvent(FUNCTIONAL_BLOCKS)
				.register(output -> {
					output.insertAfter(Items.ENCHANTING_TABLE,
							ModItems.DISENCHANTER, ModItems.ENCHANTING_CRYSTAL, ModItems.CHARGED_ENCHANTING_CRYSTAL,
							ModItems.XP_BOTTLER);
					// L'epouvantail se pose et porte une armure : a cote du
					// porte-armure.
					output.insertAfter(Items.ARMOR_STAND, ModItems.SCARECROW);
					output.insertAfter(Items.CREEPER_HEAD, ModItems.ENDERMAN_HEAD);
				});

		// ---- Outils et utilitaires ---------------------------------------------
		CreativeModeTabEvents.modifyOutputEvent(TOOLS_AND_UTILITIES)
				.register(output -> {
					// L'outil en silex vient avant les outils en bois : c'est
					// le tout debut de la progression.
					output.insertBefore(Items.WOODEN_SHOVEL, ModItems.FLINT_TOOL);
					output.insertAfter(Items.RECOVERY_COMPASS, ModItems.ALTIMETER);
					output.insertAfter(Items.BUNDLE, ModItems.TOOL_BELT);
					output.insertAfter(Items.FLINT_AND_STEEL, ModItems.HOT_COAL_IN_A_BOTTLE);
					// L'onglet range les outils par matiere : la panoplie en or
					// rose reste groupee, juste apres celle en or.
					output.insertAfter(Items.GOLDEN_HOE,
							ModItems.PINK_GOLD_SHOVEL, ModItems.PINK_GOLD_PICKAXE,
							ModItems.PINK_GOLD_AXE, ModItems.PINK_GOLD_HOE);
					// Le carquois range des fleches : juste avant les sacs.
					output.insertBefore(Items.BUNDLE, ModItems.QUIVER);
					// Les ailes de fortune : la version pauvre des elytres.
					output.insertBefore(Items.ELYTRA, ModItems.PATCHWORK_ELYTRA);
				});

		// ---- Combat --------------------------------------------------------------
		CreativeModeTabEvents.modifyOutputEvent(COMBAT)
				.register(output -> {
					output.insertBefore(Items.WOODEN_SWORD, ModItems.FLINT_DAGGER);
					output.insertAfter(Items.GOLDEN_SWORD, ModItems.PINK_GOLD_SWORD);
					output.insertAfter(Items.GOLDEN_AXE, ModItems.PINK_GOLD_AXE);
					output.insertAfter(Items.GOLDEN_SPEAR, ModItems.PINK_GOLD_SPEAR);
					// La parure complete, juste apres celle en or.
					output.insertAfter(Items.GOLDEN_BOOTS,
							ModItems.PINK_GOLD_HELMET, ModItems.PINK_GOLD_CHESTPLATE,
							ModItems.PINK_GOLD_LEGGINGS, ModItems.PINK_GOLD_BOOTS);
					// Armes de jet et munitions.
					output.insertAfter(Items.CROSSBOW, ModItems.SLINGSHOT);
					output.insertAfter(Items.SNOWBALL, ModItems.PEBBLE);
				});

		// ---- Ingredients ----------------------------------------------------------
		CreativeModeTabEvents.modifyOutputEvent(INGREDIENTS)
				.register(output -> {
					// Fibres et ficelle vegetale : ce qui remplace la ficelle,
					// donc juste avant elle.
					output.insertBefore(Items.STRING, ModItems.PLANT_FIBER, ModItems.PLANT_CORD);
					output.insertAfter(Items.PHANTOM_MEMBRANE, ModItems.BAT_WING);
					output.insertAfter(Items.GUNPOWDER, ModItems.SULFUR_POWDER);
					output.insertAfter(Items.SNOWBALL, ModItems.PEBBLE);
					output.insertAfter(Items.RAW_GOLD, ModItems.RAW_PINK_GOLD);
					output.insertAfter(Items.GOLD_INGOT, ModItems.PINK_GOLD_INGOT);
					output.insertAfter(Items.GOLD_NUGGET, ModItems.PINK_GOLD_NUGGET);
					output.insertAfter(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
							ModItems.PINK_GOLD_UPGRADE_SMITHING_TEMPLATE);
				});

		// ---- Nourriture -------------------------------------------------------------
		CreativeModeTabEvents.modifyOutputEvent(FOOD_AND_DRINKS)
				.register(output -> {
					output.insertAfter(Items.BEETROOT,
							ModItems.GOLDEN_BEETROOT, ModItems.ENCHANTED_GOLDEN_BEETROOT);
					output.insertAfter(Items.GLOW_BERRIES, ModItems.XP_BERRY);
					output.insertAfter(Items.CHORUS_FRUIT, ModItems.SOUL_PEPPER);
				});
	}

	private ModCreativeTabs() {
	}
}
