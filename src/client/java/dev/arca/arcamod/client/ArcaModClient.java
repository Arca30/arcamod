package dev.arca.arcamod.client;

import dev.arca.arcamod.client.config.ArcaConfigSync;
import dev.arca.arcamod.client.light.DynamicLights;
import dev.arca.arcamod.client.render.AltimeterModelProperty;
import dev.arca.arcamod.client.render.CopperOxidationModelProperty;
import dev.arca.arcamod.client.render.AshCauldronTint;
import dev.arca.arcamod.client.render.DisenchanterRenderer;
import dev.arca.arcamod.client.render.RopePlateRenderer;
import dev.arca.arcamod.client.render.FishingRodStandRenderer;
import dev.arca.arcamod.client.render.IgnitedBurntLogRenderer;
import dev.arca.arcamod.client.render.EndermanHeadModel;
import dev.arca.arcamod.client.render.EndermanHeadRenderer;
import dev.arca.arcamod.client.render.EndermanHeadSpecialRenderer;
import dev.arca.arcamod.client.render.PotionCauldronTint;
import dev.arca.arcamod.client.render.ScarecrowDamageNumberRenderer;
import dev.arca.arcamod.client.render.ScarecrowRenderer;
import dev.arca.arcamod.client.render.ScarecrowSpecialRenderer;
import dev.arca.arcamod.client.render.ThrownDaggerRenderer;
import dev.arca.arcamod.client.screen.DisenchanterScreen;
import dev.arca.arcamod.client.screen.FletchingScreen;
import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.client.render.ArrowPartsModelProperty;
import dev.arca.arcamod.client.render.ToolTrimModelProperty;
import dev.arca.arcamod.item.ArrowParts;
import dev.arca.arcamod.client.screen.BundleScreen;
import dev.arca.arcamod.client.screen.ClientQuiverTooltip;
import dev.arca.arcamod.client.screen.QuiverScreen;
import dev.arca.arcamod.client.screen.ToolBeltScreen;
import dev.arca.arcamod.client.screen.XpBottlerScreen;
import dev.arca.arcamod.item.QuiverItem;
import dev.arca.arcamod.registry.ModBlockEntities;
import dev.arca.arcamod.registry.ModDataComponents;
import dev.arca.arcamod.registry.ModEntities;
import dev.arca.arcamod.registry.ModMenus;
import dev.arca.arcamod.util.CopperOxidation;

import dev.arca.arcamod.registry.ModBlocks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;

public class ArcaModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Associe le type de menu (commun) a son ecran (client). Sans cette
		// ligne, ouvrir le bloc affiche un ecran vide et un warning dans le log.
		MenuScreens.register(ModMenus.XP_BOTTLER, XpBottlerScreen::new);

		// Interrupteurs imposes par le serveur : recus a la connexion.
		ArcaConfigSync.init();

		// Slot d'elytres du plastron : etat recu du serveur, bouton de l'inventaire.
		ElytraSlotClient.init();
		// Ceinture a outils : touche, molette, colonne du HUD, ecran.
		ToolBeltClient.init();
		MenuScreens.register(ModMenus.TOOL_BELT, ToolBeltScreen::new);
		// Nom du livre vise dans une bibliotheque sculptee.
		ChiseledBookshelfHud.register();
		MenuScreens.register(ModMenus.DISENCHANTER, DisenchanterScreen::new);
		MenuScreens.register(ModMenus.QUIVER, QuiverScreen::new);
		MenuScreens.register(ModMenus.BUNDLE, BundleScreen::new);
		// Apercu du contenu du carquois dans son infobulle (facon bundle).
		ClientTooltipComponentCallback.EVENT.register(component ->
				component instanceof QuiverItem.QuiverTooltip data ? new ClientQuiverTooltip(data) : null);
		MenuScreens.register(ModMenus.FLETCHING_TABLE, FletchingScreen::new);

		// Propriete de modele "arcamod:arrow_parts" : choisit le modele de la
		// fleche selon ses pieces (assets/minecraft/items/*arrow.json).
		SelectItemModelProperties.ID_MAPPER.put(ArcaMod.id("arrow_parts"), ArrowPartsModelProperty.TYPE);
		// Propriete "arcamod:copper_oxidation" : apparence de l'equipement en
		// cuivre oxyde (assets/minecraft/items/copper_*.json).
		SelectItemModelProperties.ID_MAPPER.put(ArcaMod.id("copper_oxidation"), CopperOxidationModelProperty.TYPE);
		// Propriete "arcamod:tool_trim" : garniture posee sur un outil ou un arc
		// (definitions generees par tools/gen_tool_trims.py).
		SelectItemModelProperties.ID_MAPPER.put(ArcaMod.id("tool_trim"), ToolTrimModelProperty.TYPE);

		// Propriete numerique "arcamod:altitude" : position de l'aiguille de
		// l'altimetre (assets/arcamod/items/altimeter.json), comme "minecraft:compass".
		RangeSelectItemModelProperties.ID_MAPPER.put(ArcaMod.id("altitude"), AltimeterModelProperty.MAP_CODEC);

		// Infobulles : garniture lumineuse, fleche choisie du carquois.
		ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
			if (stack.has(ModDataComponents.GLOWING_TRIM)) {
				lines.add(Component.translatable("tooltip.arcamod.glowing_trim").withStyle(ChatFormatting.AQUA));
			}

			if (stack.has(ModDataComponents.PULSING_TRIM)) {
				lines.add(Component.translatable("tooltip.arcamod.pulsing_trim").withStyle(ChatFormatting.DARK_AQUA));
			}

			if (stack.has(ModDataComponents.ELYTRA_HARNESS)) {
				lines.add(Component.translatable("tooltip.arcamod.elytra_harness").withStyle(ChatFormatting.LIGHT_PURPLE));
			}

			int oxidation = CopperOxidation.stage(stack);

			if (oxidation > 0) {
				lines.add(Component.translatable("tooltip.arcamod.copper_oxidation." + CopperOxidation.STAGE_NAMES[oxidation])
						.withStyle(ChatFormatting.DARK_GREEN));
			}

			if (CopperOxidation.isWaxed(stack)) {
				lines.add(Component.translatable("tooltip.arcamod.copper_waxed").withStyle(ChatFormatting.GOLD));
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
		// La lessive est grise, quel que soit le niveau du chaudron.
		BlockColorRegistry.register(java.util.List.of(new AshCauldronTint()), ModBlocks.ASH_CAULDRON);

		// La pierre lancee s'affiche comme son item, exactement comme une
		// boule de neige ou un oeuf.
		EntityRenderers.register(ModEntities.THROWN_PEBBLE, ThrownItemRenderer::new);
		// Bombe fumigene : l'objet qui vole, puis un nuage invisible qui souffle
		// ses particules depuis le serveur.
		EntityRenderers.register(ModEntities.THROWN_SMOKE_BOMB, ThrownItemRenderer::new);
		EntityRenderers.register(ModEntities.SMOKE_CLOUD, NoopRenderer::new);
		EntityRenderers.register(ModEntities.THROWN_DAGGER, ThrownDaggerRenderer::new);
		// Le projectile du lance-pierre s'affiche comme sa munition.
		EntityRenderers.register(ModEntities.SLINGSHOT_SHOT, ThrownItemRenderer::new);

		// Tete d'Enderman : modele, rendu pose (bloc) et rendu en item
		// ("type": "arcamod:enderman_head" dans assets/arcamod/items/).
		ModelLayerRegistry.registerModelLayer(EndermanHeadModel.LAYER, EndermanHeadModel::createLayer);
		BlockEntityRenderers.register(ModBlockEntities.ENDERMAN_HEAD, EndermanHeadRenderer::new);
		// Le second brin d'un amarrage double : voir RopePlateRenderer.
		BlockEntityRenderers.register(ModBlockEntities.ROPE_PLATE, RopePlateRenderer::new);
		SpecialModelRenderers.ID_MAPPER.put(ArcaMod.id("enderman_head"), EndermanHeadSpecialRenderer.Unbaked.MAP_CODEC);
		// Item de l'epouvantail : son modele 3D ("type": "arcamod:scarecrow").
		SpecialModelRenderers.ID_MAPPER.put(ArcaMod.id("scarecrow"), ScarecrowSpecialRenderer.Unbaked.MAP_CODEC);

		// Resine : chaque appui sur saut d'un joueur englue est envoye au serveur.
		ResinJumpClient.register();
		EntityRenderers.register(ModEntities.SCARECROW, ScarecrowRenderer::new);
		EntityRenderers.register(ModEntities.SCARECROW_DAMAGE_NUMBER, ScarecrowDamageNumberRenderer::new);

		// Le livre qui vole au-dessus du desenchanteur, comme sur une table
		// d'enchantement. (Registre vanilla : celui de Fabric,
		// BlockEntityRendererRegistry, est deprecie.)
		BlockEntityRenderers.register(ModBlockEntities.DISENCHANTER, DisenchanterRenderer::new);
		// Aliments qui cuisent sur une buche incandescente.
		BlockEntityRenderers.register(ModBlockEntities.IGNITED_BURNT_LOG, IgnitedBurntLogRenderer::new);
		// La canne posee sur son support (et sa pose : rangee, lancee, ca mord).
		BlockEntityRenderers.register(ModBlockEntities.FISHING_ROD_STAND, FishingRodStandRenderer::new);

		// Lumiere dynamique : torches tenues, fleches enflammees, objets
		// lumineux au sol... (client/light, ArcaBalance section 34).
		ClientTickEvents.END_CLIENT_TICK.register(DynamicLights::tick);
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
