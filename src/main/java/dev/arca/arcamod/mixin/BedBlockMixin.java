package dev.arca.arcamod.mixin;

import dev.arca.arcamod.config.ArcaFeature;
import dev.arca.arcamod.registry.ModTags;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Un lit en laine ne s'utilise qu'a l'abri.
 *
 * Dormir a la belle etoile n'est plus possible : il faut un toit, c'est-a-dire
 * n'importe quel bloc au-dessus du lit (canSeeSky renvoie faux). La liste des
 * lits concernes est le tag arcamod:beds_requiring_shelter, pour pouvoir en
 * exclure plus tard un eventuel lit de paille.
 */
@Mixin(BedBlock.class)
public class BedBlockMixin {

	@Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
	private void arcamod$requireShelter(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
		if (!ArcaFeature.BED_NEEDS_SHELTER.isEnabled() || !state.is(ModTags.BEDS_REQUIRING_SHELTER) || !level.canSeeSky(pos.above())) {
			return;
		}

		if (!level.isClientSide()) {
			player.sendSystemMessage(
					Component.translatable("message.arcamod.bed_needs_shelter").withStyle(ChatFormatting.RED));
		}

		cir.setReturnValue(InteractionResult.CONSUME);
	}
}
