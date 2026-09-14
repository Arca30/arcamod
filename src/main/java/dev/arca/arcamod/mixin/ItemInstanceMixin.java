package dev.arca.arcamod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.arca.arcamod.item.StackingRules;

import net.minecraft.world.item.ItemInstance;

/**
 * Releve la taille de pile des potions, armures et outils.
 *
 * On vise l'interface ItemInstance et pas ItemStack : getMaxStackSize() y est
 * declaree en methode par defaut, ItemStack ne la redefinit pas. Un mixin sur
 * ItemStack ne trouverait donc rien a injecter.
 *
 * Pourquoi pas DefaultItemComponentEvents de Fabric, qui serait plus propre :
 * chaque item vanilla embarque un validateur qui refuse la combinaison
 * "DAMAGE + pile > 1" et ferait planter le jeu au demarrage. On change donc la
 * limite a la lecture, sans toucher aux components.
 *
 * Le vanilla fait deja le reste du travail :
 *     isStackable() = getMaxStackSize() > 1 && (!isDamageableItem() || !isDamaged())
 * donc seuls les objets INTACTS fusionnent, et uniquement entre exemplaires
 * strictement identiques (memes enchantements, meme nom, meme potion...).
 */
@Mixin(ItemInstance.class)
public interface ItemInstanceMixin {

	@Inject(method = "getMaxStackSize", at = @At("RETURN"), cancellable = true)
	private void arcamod$stackGear(CallbackInfoReturnable<Integer> cir) {
		if (cir.getReturnValue() != 1 || !StackingRules.isEnabled()) {
			return; // deja empilable, ou fonctionnalite desactivee
		}

		if (StackingRules.shouldStack((ItemInstance) this)) {
			cir.setReturnValue(StackingRules.stackSize());
		}
	}
}
