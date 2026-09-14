package dev.arca.arcamod.client;

import dev.arca.arcamod.client.render.DisenchanterRenderer;
import dev.arca.arcamod.client.render.PotionCauldronTint;
import dev.arca.arcamod.client.render.ScarecrowDamageNumberRenderer;
import dev.arca.arcamod.client.render.ScarecrowRenderer;
import dev.arca.arcamod.client.render.ThrownDaggerRenderer;
import dev.arca.arcamod.client.screen.DisenchanterScreen;
import dev.arca.arcamod.client.screen.FletchingScreen;
import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.client.render.ArrowPartsModelProperty;
import dev.arca.arcamod.item.ArrowParts;
import dev.arca.arcamod.client.screen.QuiverScreen;
import dev.arca.arcamod.client.screen.XpBottlerScreen;
import dev.arca.arcamod.item.QuiverItem;
import dev.arca.arcamod.registry.ModBlockEntities;
import dev.arca.arcamod.registry.ModDataComponents;
import dev.arca.arcamod.registry.ModEntities;
import dev.arca.arcamod.registry.ModMenus;

import dev.arca.arcamod.registry.ModBlocks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class ArcaModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Associe le type de menu (commun) a son ecran (client). Sans cette
		// ligne, ouvrir le bloc affiche un ecran vide et un warning dans le log.
		MenuScreens.register(ModMenus.XP_BOTTLER, XpBottlerScreen::new);
		MenuScreens.register(ModMenus.DISENCHANTER, DisenchanterScreen::new);
		MenuScreens.register(ModMenus.QUIVER, QuiverScreen::new);
		MenuScreens.register(ModMenus.FLETCHING_TABLE, FletchingScreen::new);

		// Propriete de modele "arcamod:arrow_parts" : choisit le modele de la
		// fleche selon ses pieces (assets/minecraft/items/*arrow.json).
		SelectItemModelProperties.ID_MAPPER.put(ArcaMod.id("arrow_parts"), ArrowPartsModelProperty.TYPE);

		// Infobulles : garniture lumineuse, fleche choisie du carquois.
		ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
			if (stack.has(ModDataComponents.GLOWING_TRIM)) {
				lines.add(Component.translatable("tooltip.arcamod.glowing_trim").withStyle(ChatFormatting.AQUA));
			}

			ArrowParts parts = stack.get(ModDataComponents.ARROW_PARTS);

			// Le nom cite deja les pieces : l'infobulle decrit leurs effets.
			if (parts != null) {
				addArrowPartLine(lines, "tooltip.arcamod.arrow_tip", parts.tip());
				addArrowPartLine(lines, "tooltip.arcamod.arrow_shaft", parts.shaft());
				addArrowPartLine(lines, "tooltip.arcamod.arrow_fletching", parts.fletching());
			}

			if (stack.getItem() instanceof QuiverItem) {
				var items = QuiverItem.contents(stack);
				int selected = QuiverItem.selectedSlot(stack, items);

				if (selected >= 0) {
					lines.add(Component.translatable("tooltip.arcamod.quiver_selected", items.get(selected).getHoverName())
							.withStyle(ChatFormatting.GRAY));
				}
			}
		});

		// Le liquide du chaudron prend la couleur de la potion versee.
		BlockColorRegistry.register(java.util.List.of(new PotionCauldronTint()), ModBlocks.POTION_CAULDRON);

		// La pierre lancee s'affiche comme son item, exactement comme une
		// boule de neige ou un oeuf.
		EntityRenderers.register(ModEntities.THROWN_PEBBLE, ThrownItemRenderer::new);
		EntityRenderers.register(ModEntities.THROWN_DAGGER, ThrownDaggerRenderer::new);
		// Le siege du feu de camp est invisible : seul le joueur assis se voit.
		EntityRenderers.register(ModEntities.CAMPFIRE_SEAT, NoopRenderer::new);
		EntityRenderers.register(ModEntities.SCARECROW, ScarecrowRenderer::new);
		EntityRenderers.register(ModEntities.SCARECROW_DAMAGE_NUMBER, ScarecrowDamageNumberRenderer::new);

		// Le livre qui vole au-dessus du desenchanteur, comme sur une table
		// d'enchantement. (Registre vanilla : celui de Fabric,
		// BlockEntityRendererRegistry, est deprecie.)
		BlockEntityRenderers.register(ModBlockEntities.DISENCHANTER, DisenchanterRenderer::new);
	}

	/** Ligne "Pointe : effet" de l'infobulle, seulement pour une piece speciale. */
	private static void addArrowPartLine(java.util.List<Component> lines, String key, ArrowParts.Part part) {
		if (part == ArrowParts.DEFAULT.tip() || part == ArrowParts.DEFAULT.shaft() || part == ArrowParts.DEFAULT.fletching()) {
			return;
		}

		lines.add(Component.translatable(key, Component.translatable(part.translationKey() + ".desc"))
				.withStyle(ChatFormatting.BLUE));
	}
}
