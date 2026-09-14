package dev.arca.arcamod.block;

import java.util.Optional;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.config.ArcaFeature;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Le vieillissement du chaume, partage par le bloc plein, ses escaliers et
 * ses dalles.
 *
 * Les trois classes ne peuvent pas heriter d'un ancetre commun (elles
 * descendent respectivement de RotatedPillarBlock, StairBlock et SlabBlock) :
 * on met donc le comportement dans une interface, avec des methodes statiques
 * que chacune appelle.
 */
public interface WeatheringThatch {

	/**
	 * Cire appliquee au rayon de miel : le bloc ne vieillit plus.
	 *
	 * C'est une propriete d'etat et non un bloc separe (contrairement au
	 * cuivre vanilla) : deux fois moins de blocs a enregistrer, et la texture
	 * ne change pas. Revers de la medaille : la cire est perdue quand on
	 * ramasse le bloc.
	 */
	BooleanProperty WAXED = BooleanProperty.create("waxed");

	/** L'etape suivante du vieillissement, vide si c'est la derniere. */
	Optional<Block> nextStage();

	/** L'etape precedente, celle que rend un coup de hache. */
	Optional<Block> previousStage();

	/** Appelee par le randomTick de chaque bloc de la famille. */
	static void fade(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!ArcaFeature.THATCH.isEnabled() || state.getValue(WAXED)
				|| random.nextFloat() >= ArcaBalance.THATCH_FADE_CHANCE) {
			return;
		}

		if (state.getBlock() instanceof WeatheringThatch thatch) {
			thatch.nextStage().ifPresent(next -> level.setBlockAndUpdate(pos, next.withPropertiesOf(state)));
		}
	}

	/** Le rayon de miel fige le bloc dans son etat actuel. */
	static boolean applyWax(Level level, BlockPos pos, BlockState state) {
		if (!(state.getBlock() instanceof WeatheringThatch) || state.getValue(WAXED)) {
			return false;
		}

		if (!level.isClientSide()) {
			level.setBlockAndUpdate(pos, state.setValue(WAXED, true));
			level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
			// 3003 = les etincelles jaunes du cirage du cuivre
			level.levelEvent(null, 3003, pos, 0);
		}

		return true;
	}

	/**
	 * La hache enleve d'abord la cire, puis rajeunit le bloc d'une etape.
	 * Renvoie l'etat a poser, ou null s'il n'y a rien a faire.
	 */
	static BlockState scrape(BlockState state) {
		if (!(state.getBlock() instanceof WeatheringThatch thatch)) {
			return null;
		}

		if (state.getValue(WAXED)) {
			return state.setValue(WAXED, false);
		}

		return thatch.previousStage().map(block -> block.withPropertiesOf(state)).orElse(null);
	}
}
