package dev.arca.arcamod.mixin;

import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Un cadre rendu invisible (membrane de phantom) redevient visible des qu'on lui
 * retire son objet : sans ca il resterait introuvable.
 */
@Mixin(ItemFrame.class)
public abstract class ItemFrameMixin {

	@Inject(method = "setItem(Lnet/minecraft/world/item/ItemStack;Z)V", at = @At("TAIL"))
	private void arcamod$revealWhenEmptied(ItemStack stack, boolean playSound, CallbackInfo ci) {
		ItemFrame self = (ItemFrame) (Object) this;

		if (stack.isEmpty() && self.isInvisible()) {
			self.setInvisible(false);
		}
	}
}
