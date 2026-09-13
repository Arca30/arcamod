package dev.arca.arcamod.client.screen;

import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.menu.DisenchanterMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Ecran du desenchanteur : objet enchante a gauche, livres vierges au milieu,
 * livre produit a droite, et le cout en niveaux affiche sous le resultat.
 */
public class DisenchanterScreen extends AbstractContainerScreen<DisenchanterMenu> {

	private static final Identifier TEXTURE = ArcaMod.id("textures/gui/container/disenchanter.png");

	/**
	 * Vert quand le joueur peut payer, rouge sinon.
	 *
	 * Ces couleurs sont en ARGB : sans le FF de tete (l'opacite), le texte
	 * est parfaitement transparent et ne s'affiche pas.
	 */
	private static final int COST_AFFORDABLE = 0xFF80FF20;
	private static final int COST_TOO_EXPENSIVE = 0xFFFF6060;

	/** Fond sombre derriere le texte, comme sur l'enclume. */
	private static final int COST_BACKGROUND = 0x4F000000;

	public DisenchanterScreen(DisenchanterMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
				this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		super.extractLabels(graphics, mouseX, mouseY);

		int cost = this.menu.getCost();

		if (cost <= 0 || this.minecraft == null) {
			return;
		}

		Component label = Component.translatable("container.arcamod.disenchanter.cost", cost);
		int colour = this.menu.canAfford(this.minecraft.player) ? COST_AFFORDABLE : COST_TOO_EXPENSIVE;

		// Coordonnees relatives au panneau : extractLabels dessine deja dans
		// le repere du GUI (meme convention que l'enclume vanilla).
		int textX = this.imageWidth - 8 - this.font.width(label) - 2;
		graphics.fill(textX - 2, 67, this.imageWidth - 8, 79, COST_BACKGROUND);
		graphics.text(this.font, label, textX, 69, colour);
	}
}
