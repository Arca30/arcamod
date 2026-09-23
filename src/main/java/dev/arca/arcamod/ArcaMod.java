package dev.arca.arcamod;

import dev.arca.arcamod.util.ElytraSlot;
import dev.arca.arcamod.util.BurntLogs;
import dev.arca.arcamod.util.ToolBelt;
import dev.arca.arcamod.command.PinkGoldDebugCommand;
import dev.arca.arcamod.registry.ModBlockEntities;
import dev.arca.arcamod.registry.ModBlocks;
import dev.arca.arcamod.registry.ModCreativeTabs;
import dev.arca.arcamod.registry.ModDataComponents;
import dev.arca.arcamod.registry.ModDecorBlocks;
import dev.arca.arcamod.registry.ModEffects;
import dev.arca.arcamod.registry.ModEntities;
import dev.arca.arcamod.registry.ModEvents;
import dev.arca.arcamod.registry.ModExampleLoot;
import dev.arca.arcamod.registry.ModExamples;
import dev.arca.arcamod.registry.ModFeatures;
import dev.arca.arcamod.registry.ModItems;
import dev.arca.arcamod.registry.ModLootTables;
import dev.arca.arcamod.registry.ModMenus;
import dev.arca.arcamod.registry.ModNetworking;
import dev.arca.arcamod.registry.ModRecipes;
import dev.arca.arcamod.registry.ModVanillaItemTweaks;
import dev.arca.arcamod.registry.ModWorldGen;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArcaMod implements ModInitializer {
	public static final String MOD_ID = "arcamod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// L'ordre compte : les items du bloc ont besoin des blocs, et le
		// BlockEntity a besoin de connaitre son bloc.
		// Les composants d'objet d'abord : des items les utilisent.
		ModDataComponents.init();
		// Piece jointe "elytres rangees" du joueur (slot d'elytres).
		ElytraSlot.init();
		// Piece jointe "ceinture portee" du joueur.
		ToolBelt.init();
		ModEffects.init();
		ModBlocks.init();
		// Le feu consume mieux les buches (ArcaBalance.FIRE_LOG_BURN_ODDS).
		BurntLogs.init();
		ModDecorBlocks.init();
		ModItems.init();
		// Retouches des objets vanilla (trident, casque de tortue).
		ModVanillaItemTweaks.init();
		ModEntities.init();
		ModBlockEntities.init();
		ModMenus.init();
		ModRecipes.init();
		ModNetworking.init();
		ModCreativeTabs.init();
		ModLootTables.init();

		// Exemples a copier pour tes propres ajouts (voir EXEMPLES.md).
		ModExamples.init();
		ModExampleLoot.init();
		ModEvents.init();

		// Generation du monde : le type de feature d'abord, la modification
		// des biomes ensuite.
		ModFeatures.init();
		ModWorldGen.init();

		// Commandes de test (operateurs) : /arcamod pinkgold scan [rayon].
		CommandRegistrationCallback.EVENT.register(
				(dispatcher, buildContext, selection) -> PinkGoldDebugCommand.register(dispatcher));

		LOGGER.info("ArcaMod loaded");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
