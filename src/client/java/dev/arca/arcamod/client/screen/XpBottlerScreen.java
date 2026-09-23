package dev.arca.arcamod.client.screen;

import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.menu.CompactMachineLayout;
import dev.arca.arcamod.menu.XpBottlerMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/**
 * Ecran de l'embouteilleur : slot d'entree, fleche de progression, slot de
 * sortie. Aucun bouton : la machine est pilotee par la redstone.
 */
public class XpBottlerScreen extends AbstractContainerScreen<XpBottlerMenu> {

	private static final Identifier TEXTURE = ArcaMod.id("textures/gui/container/xp_bottler.png");

	/**
	 * Y de la rangee de slots DANS la texture (gabarit 166 px). L'ecran est
	 * affiche en version compacte : voir CompactMachinePanel.
	 */
	private static final int TEXTURE_SLOT_ROW_Y = 35;

	/** Position de la fleche dans le panneau (alignee sur les slots). */
	private static final int ARROW_X = 72;
	private static final int ARROW_Y = CompactMachineLayout.SLOT_ROW_Y;
	private static final int ARROW_WIDTH = 24;
	private static final int ARROW_HEIGHT = 16;

	/** Position de la fleche "pleine" dans le fichier de texture. */
	private static final int ARROW_SPRITE_U = 176;
	private static final int ARROW_SPRITE_V = 0;

	public XpBottlerScreen(XpBottlerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, CompactMachineLayout.IMAGE_HEIGHT);
		this.inventoryLabelY = CompactMachineLayout.INVENTORY_LABEL_Y;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);

		// Ici les coordonnees sont absolues : le GUI est centre a l'ecran.
		CompactMachinePanel.draw(graphics, TEXTURE, this.leftPos, this.topPos, this.imageWidth,
				TEXTURE_SLOT_ROW_Y);

		// La fleche se remplit de gauche a droite : on ne dessine que les N
		// premiers pixels de la version pleine, par dessus la fleche vide.
		int filled = Mth.ceil(this.menu.getProgress() * ARROW_WIDTH);
		if (filled > 0) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
					this.leftPos + ARROW_X, this.topPos + ARROW_Y,
					ARROW_SPRITE_U, ARROW_SPRITE_V,
					filled, ARROW_HEIGHT, 256, 256);
		}
	}
}
