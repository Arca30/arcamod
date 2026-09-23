package dev.arca.arcamod.mixin;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.config.ArcaFeature;
import dev.arca.arcamod.registry.ModTags;
import dev.arca.arcamod.util.MossSkirt;
import dev.arca.arcamod.util.TorchflowerWard;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
 *
 * Deuxieme detournement, le meme principe pour la LUMIERE : la torchflower
 * eclaire comme une torche et l'eyeblossom ouverte eclaire faiblement. Comme
 * "combien ce bloc eclaire" est fige a la construction du bloc, on intercepte
 * la lecture. Les blocs concernes sont dans deux tags, donc reglables en JSON
 * (les fleurs en pot y sont deja).
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

	@Shadow
	public abstract Holder<Block> typeHolder();

	@Inject(method = "requiresCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
	private void arcamod$requireToolForDrops(CallbackInfoReturnable<Boolean> cir) {
		if (ArcaFeature.LOGS_REQUIRE_TOOL.isEnabled() && this.typeHolder().is(ModTags.REQUIRES_TOOL_FOR_DROPS)) {
			cir.setReturnValue(true);
		}
	}

	/**
	 * Lumiere des fleurs (ArcaBalance section 53).
	 *
	 * On ne repond que pour les fleurs listees, et seulement si la valeur
	 * reglee est plus forte que ce que le bloc emet deja : mettre 0 dans
	 * ArcaBalance rend donc simplement sa nuit a la fleur.
	 */
	@Inject(method = "getLightEmission", at = @At("HEAD"), cancellable = true)
	private void arcamod$flowerLight(CallbackInfoReturnable<Integer> cir) {
		if (!ArcaFeature.FLOWER_LIGHT.isEnabled()) {
			return;
		}

		if (this.typeHolder().is(ModTags.TORCH_LIGHT_FLOWERS)) {
			cir.setReturnValue(ArcaBalance.TORCHFLOWER_LIGHT);
		} else if (this.typeHolder().is(ModTags.DIM_LIGHT_FLOWERS)) {
			cir.setReturnValue(ArcaBalance.OPEN_EYEBLOSSOM_LIGHT);
		}
	}

	/**
	 * Troisieme detournement : previent l'index des torchflowers
	 * (util/TorchflowerWard) des qu'une fleur protectrice apparait : plantee a
	 * la main, poussee depuis sa culture, posee par une commande.
	 *
	 * Ici et pas sur le bloc : cette methode-ci est appelee pour TOUS les
	 * blocs poses, meme ceux dont la classe redefinit onPlace sans appeler
	 * super. Le test doit donc rester ce qu'il est, une comparaison de tag.
	 */
	@Inject(method = "onPlace", at = @At("HEAD"))
	private void arcamod$rememberTorchflower(Level level, BlockPos pos, BlockState oldState, boolean movedByPiston,
			CallbackInfo ci) {
		BlockState self = (BlockState) (Object) this;
		TorchflowerWard.add(level, pos, self);

		// Mousse debordante : un tapis qu'on vient de poser regarde ce qu'il a
		// sous lui. Le setBlock repasse par ici, mais la deuxieme fois l'etat
		// est deja bon et plus rien ne bouge.
		BlockState mossy = MossSkirt.updated(level, pos, self);

		if (mossy != self) {
			level.setBlock(pos, mossy, Block.UPDATE_CLIENTS);
		}
	}

	/**
	 * Quatrieme detournement : le bloc SOUS un tapis de mousse a change (pose,
	 * casse, remplace). Le tapis refait son calcul et fait couler - ou
	 * retire - sa mousse.
	 *
	 * updateShape est LA methode que le jeu appelle sur les voisins d'un bloc
	 * qui change, et son resultat remplace l'etat : c'est l'endroit prevu
	 * pour ce genre de dependance.
	 */
	@Inject(method = "updateShape", at = @At("RETURN"), cancellable = true)
	private void arcamod$updateMossSkirt(LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
			Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random,
			CallbackInfoReturnable<BlockState> cir) {
		if (directionToNeighbour != Direction.DOWN) {
			return;
		}

		BlockState result = cir.getReturnValue();
		BlockState mossy = MossSkirt.updated(level, pos, result);

		if (mossy != result) {
			cir.setReturnValue(mossy);
		}
	}
}
