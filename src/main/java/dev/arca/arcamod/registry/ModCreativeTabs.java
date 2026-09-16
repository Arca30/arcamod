package dev.arca.arcamod.registry;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

/**
 * Ajoute les items du mod dans les onglets creatifs vanilla.
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
	private static final ResourceKey<CreativeModeTab> REDSTONE_BLOCKS = vanillaTab("redstone_blocks");

	private static ResourceKey<CreativeModeTab> vanillaTab(String name) {
		return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace(name));
	}

	public static void init() {
		// La baie sert a la fois de graine et de consommable : comme les glow
		// berries, elle apparait dans les deux onglets.
		CreativeModeTabEvents.modifyOutputEvent(NATURAL_BLOCKS)
				.register(output -> {
					output.accept(ModItems.XP_BERRY);
					output.accept(ModItems.PEBBLE);
					output.accept(ModItems.FALLEN_STICKS);
					output.accept(ModItems.PINK_GOLD_ORE);
					output.accept(ModItems.DEEPSLATE_PINK_GOLD_ORE);
					output.accept(ModItems.RAW_PINK_GOLD_BLOCK);
					output.accept(ModItems.BRUSHED_SULFUR);
					output.accept(ModItems.SOUL_PEPPER);
				});

		// La petite pierre est aussi une arme de jet, comme la boule de neige.
		CreativeModeTabEvents.modifyOutputEvent(COMBAT)
				.register(output -> {
					output.accept(ModItems.PEBBLE);
					output.accept(ModItems.FLINT_DAGGER);
					output.accept(ModItems.QUIVER);
					output.accept(ModItems.PATCHWORK_ELYTRA);
					output.accept(ModItems.SCARECROW);
					output.accept(ModItems.SLINGSHOT);
					output.accept(ModItems.PINK_GOLD_SWORD);
					output.accept(ModItems.PINK_GOLD_AXE);
					output.accept(ModItems.PINK_GOLD_SPEAR);
					output.accept(ModItems.PINK_GOLD_HELMET);
					output.accept(ModItems.PINK_GOLD_CHESTPLATE);
					output.accept(ModItems.PINK_GOLD_LEGGINGS);
					output.accept(ModItems.PINK_GOLD_BOOTS);
				});

		CreativeModeTabEvents.modifyOutputEvent(INGREDIENTS)
				.register(output -> {
					output.accept(ModItems.PLANT_FIBER);
					output.accept(ModItems.PLANT_CORD);
					output.accept(ModItems.BAT_WING);
					output.accept(ModItems.SULFUR_POWDER);
					output.accept(ModItems.RAW_PINK_GOLD);
					output.accept(ModItems.PINK_GOLD_NUGGET);
					output.accept(ModItems.PINK_GOLD_INGOT);
					output.accept(ModItems.PINK_GOLD_UPGRADE_SMITHING_TEMPLATE);
				});

		CreativeModeTabEvents.modifyOutputEvent(TOOLS_AND_UTILITIES)
				.register(output -> {
					output.accept(ModItems.FLINT_TOOL);
					output.accept(ModItems.PINK_GOLD_SHOVEL);
					output.accept(ModItems.PINK_GOLD_PICKAXE);
					output.accept(ModItems.PINK_GOLD_AXE);
					output.accept(ModItems.PINK_GOLD_HOE);
				});

		CreativeModeTabEvents.modifyOutputEvent(REDSTONE_BLOCKS)
				.register(output -> output.accept(ModItems.ENDERMAN_HEAD));

		CreativeModeTabEvents.modifyOutputEvent(FOOD_AND_DRINKS)
				.register(output -> {
					output.accept(ModItems.XP_BERRY);
					output.accept(ModItems.GOLDEN_BEETROOT);
					output.accept(ModItems.SOUL_PEPPER);
					output.accept(ModItems.ENCHANTED_GOLDEN_BEETROOT);
				});

		// Escaliers, dalles et chaume : tout ce que pose ModDecorBlocks.
		CreativeModeTabEvents.modifyOutputEvent(BUILDING_BLOCKS)
				.register(output -> {
					ModDecorBlocks.all().forEach(block -> output.accept(block.asItem()));
					output.accept(ModItems.PINK_GOLD_BLOCK);
				});

		CreativeModeTabEvents.modifyOutputEvent(FUNCTIONAL_BLOCKS)
				.register(output -> {
					output.accept(ModItems.XP_BOTTLER);
					output.accept(ModItems.DISENCHANTER);
					output.accept(ModItems.ENCHANTING_CRYSTAL);
					output.accept(ModItems.CHARGED_ENCHANTING_CRYSTAL);
				});
	}

	private ModCreativeTabs() {
	}
}
