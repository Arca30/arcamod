package dev.arca.arcamod.registry;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.config.ArcaFeature;
import dev.arca.arcamod.worldgen.ClutterPatchFeature;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Les types de features du mod.
 *
 * Un "feature" est le code qui pose les blocs ; les fichiers JSON
 * worldgen/configured_feature et worldgen/placed_feature disent ou l'appeler.
 * Le taux d'apparition, lui, est dans ArcaBalance.
 */
public final class ModFeatures {

	public static final Feature<NoneFeatureConfiguration> PEBBLE_PATCH = Registry.register(
			BuiltInRegistries.FEATURE,
			ArcaMod.id("pebble_patch"),
			new ClutterPatchFeature(NoneFeatureConfiguration.CODEC,
					() -> ModBlocks.PEBBLES, ArcaFeature.WORLDGEN_PEBBLES, ArcaBalance.PEBBLE_PATCH_CHANCE,
					ArcaBalance.PEBBLE_PATCH_MIN_BLOCKS, ArcaBalance.PEBBLE_PATCH_MAX_BLOCKS,
					ArcaBalance.PEBBLE_PATCH_SPREAD));

	public static final Feature<NoneFeatureConfiguration> STICK_PATCH = Registry.register(
			BuiltInRegistries.FEATURE,
			ArcaMod.id("stick_patch"),
			new ClutterPatchFeature(NoneFeatureConfiguration.CODEC,
					() -> ModBlocks.FALLEN_STICKS, ArcaFeature.WORLDGEN_STICKS, ArcaBalance.STICK_PATCH_CHANCE,
					ArcaBalance.STICK_PATCH_MIN_BLOCKS, ArcaBalance.STICK_PATCH_MAX_BLOCKS,
					ArcaBalance.STICK_PATCH_SPREAD));

	public static void init() {
	}

	private ModFeatures() {
	}
}
