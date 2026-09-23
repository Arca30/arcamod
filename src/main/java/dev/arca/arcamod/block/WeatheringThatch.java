package dev.arca.arcamod.block;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.config.ArcaFeature;

/**
 * Le vieillissement du chaume, partage par le bloc plein, ses escaliers et
 * ses dalles.
 *
 * Toute la mecanique est dans ArcaWeathering ; il ne reste ici que ce qui est
 * propre au chaume : son interrupteur et sa vitesse de vieillissement.
 */
public interface WeatheringThatch extends ArcaWeathering {

	@Override
	default boolean weatheringEnabled() {
		return ArcaFeature.THATCH.isEnabled();
	}

	@Override
	default float fadeChance() {
		return ArcaBalance.THATCH_FADE_CHANCE;
	}
}
