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
				});

		// La petite pierre est aussi une arme de jet, comme la boule de neige.
		CreativeModeTabEvents.modifyOutputEvent(COMBAT)
				.register(output -> {
					output.accept(ModItems.PEBBLE);
					output.accept(ModItems.FLINT_DAGGER);
					output.accept(ModItems.QUIVER);
					output.accept(ModItems.PATCHWORK_ELYTRA);
					output.accept(ModItems.SCARECROW);
				});

		CreativeModeTabEvents.modifyOutputEvent(INGREDIENTS)
				.register(output -> {
					output.accept(ModItems.PLANT_FIBER);
					output.accept(ModItems.PLANT_CORD);
					output.accept(ModItems.BAT_WING);
				});

		CreativeModeTabEvents.modifyOutputEvent(TOOLS_AND_UTILITIES)
				.register(output -> output.accept(ModItems.FLINT_TOOL));

		CreativeModeTabEvents.modifyOutputEvent(FOOD_AND_DRINKS)
				.register(output -> {
					output.accept(ModItems.XP_BERRY);
					output.accept(ModItems.GOLDEN_BEETROOT);
					output.accept(ModItems.ENCHANTED_GOLDEN_BEETROOT);
				});

		// Escaliers, dalles et chaume : tout ce que pose ModDecorBlocks.
		CreativeModeTabEvents.modifyOutputEvent(BUILDING_BLOCKS)
				.register(output -> ModDecorBlocks.all().forEach(block -> output.accept(block.asItem())));

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
