package dev.arca.arcamod.client;

import dev.arca.arcamod.client.render.PotionCauldronTint;
import dev.arca.arcamod.client.render.ThrownDaggerRenderer;
import dev.arca.arcamod.client.screen.DisenchanterScreen;
import dev.arca.arcamod.client.screen.XpBottlerScreen;
import dev.arca.arcamod.registry.ModEntities;
import dev.arca.arcamod.registry.ModMenus;

import dev.arca.arcamod.registry.ModBlocks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class ArcaModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Associe le type de menu (commun) a son ecran (client). Sans cette
		// ligne, ouvrir le bloc affiche un ecran vide et un warning dans le log.
		MenuScreens.register(ModMenus.XP_BOTTLER, XpBottlerScreen::new);
		MenuScreens.register(ModMenus.DISENCHANTER, DisenchanterScreen::new);

		// Le liquide du chaudron prend la couleur de la potion versee.
		BlockColorRegistry.register(java.util.List.of(new PotionCauldronTint()), ModBlocks.POTION_CAULDRON);

		// La pierre lancee s'affiche comme son item, exactement comme une
		// boule de neige ou un oeuf.
		EntityRenderers.register(ModEntities.THROWN_PEBBLE, ThrownItemRenderer::new);
		EntityRenderers.register(ModEntities.THROWN_DAGGER, ThrownDaggerRenderer::new);
	}
}
