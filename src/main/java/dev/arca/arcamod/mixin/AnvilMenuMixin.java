package dev.arca.arcamod.mixin;

import dev.arca.arcamod.config.ArcaFeature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;

/**
 * Supprime le plafond "Trop cher !" de l'enclume.
 *
 * Le vanilla vide le slot de resultat des que le cout atteint 40 niveaux :
 *
 *     if (this.cost.get() >= 40 && !this.player.hasInfiniteMaterials()) {
 *         result = ItemStack.EMPTY;
 *     }
 *
 * On detourne l'appel a hasInfiniteMaterials() de cette ligne pour qu'il
 * reponde "oui", ce qui neutralise la condition sans toucher au calcul du
 * cout. Le joueur doit toujours POSSEDER les niveaux : ce controle est
 * ailleurs, dans mayPickup().
 *
 * ordinal = 1 car hasInfiniteMaterials() est appele deux fois dans
 * createResult() ; c'est le second appel qui nous interesse. Si Mojang
 * reorganise la methode, le mixin ne s'appliquera plus et le jeu refusera de
 * demarrer avec une erreur explicite, plutot que d'echouer en silence.
 */
@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

	private static final int INPUT_SLOT = 0;

	@WrapOperation(
			method = "createResult",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z",
					ordinal = 1))
	private boolean arcamod$removeCostLimit(Player player, Operation<Boolean> original) {
		if (!ArcaFeature.ANVIL_NO_COST_LIMIT.isEnabled()) {
			return original.call(player);
		}

		AnvilMenu self = (AnvilMenu) (Object) this;

		// Garde-fou : le mod rend les outils neufs empilables. Sans ce test, on
		// enchanterait 16 epees d'un coup pour le prix d'une (le vanilla s'en
		// protegeait justement via le plafond a 40 qu'on vient d'enlever).
		if (self.getSlot(INPUT_SLOT).getItem().getCount() > 1) {
			return original.call(player);
		}

		return true;
	}
}
