package dev.arca.arcamod.mixin;

import dev.arca.arcamod.registry.ModTags;

import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Force certains blocs a exiger un outil pour lacher leur butin.
 *
 * "Exige le bon outil" est une propriete figee a la construction du bloc :
 * impossible de la changer par un fichier de donnees. On intercepte donc la
 * lecture de cette propriete et on repond oui pour les blocs listes dans le
 * tag arcamod:requires_tool_for_drops (les buches, par defaut).
 *
 * Effet de bord voulu : le jeu applique aussi sa penalite de vitesse de
 * minage quand on s'acharne a mains nues sur un bloc qui demande un outil.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

	@Shadow
	public abstract Holder<Block> typeHolder();

	@Inject(method = "requiresCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
	private void arcamod$requireToolForDrops(CallbackInfoReturnable<Boolean> cir) {
		if (this.typeHolder().is(ModTags.REQUIRES_TOOL_FOR_DROPS)) {
			cir.setReturnValue(true);
		}
	}
}
