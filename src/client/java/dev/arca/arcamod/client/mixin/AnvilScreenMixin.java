package dev.arca.arcamod.client.mixin;

import dev.arca.arcamod.config.ArcaFeature;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.player.LocalPlayer;

/**
 * Pendant client de AnvilMenuMixin : sans ca l'ecran continuerait d'afficher
 * "Trop cher !" en rouge alors que l'objet est parfaitement recuperable.
 * On affiche le cout reel a la place.
 *
 * Attention au type dans le target : l'ecran appelle this.minecraft.player,
 * qui est declare LocalPlayer. C'est donc LocalPlayer, et pas Player, qui
 * figure comme proprietaire de l'appel dans le bytecode -- viser Player ne
 * trouve aucun point d'injection et fait echouer le chargement de la classe.
 */
@Mixin(AnvilScreen.class)
public class AnvilScreenMixin {

	@Redirect(
			method = "extractLabels",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;hasInfiniteMaterials()Z"))
	private boolean arcamod$hideTooExpensiveLabel(LocalPlayer player) {
		return ArcaFeature.ANVIL_NO_COST_LIMIT.isEnabled() || player.hasInfiniteMaterials();
	}
}
