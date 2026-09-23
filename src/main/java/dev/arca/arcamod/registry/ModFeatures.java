package dev.arca.arcamod.registry;

import com.mojang.serialization.MapCodec;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.config.ArcaFeature;
import dev.arca.arcamod.worldgen.CavePebbleFeature;
import dev.arca.arcamod.worldgen.ClutterPatchFeature;
import dev.arca.arcamod.worldgen.PinkGoldContactFeature;
import dev.arca.arcamod.worldgen.SimpleArcaFeature;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Les types de features du mod.
 *
 * Depuis 26.3 : plus de ConfiguredFeature. On enregistre un CODEC par type
 * dans BuiltInRegistries.FEATURE_TYPE, et les fichiers JSON
 * worldgen/feature/*.json (qui remplacent worldgen/configured_feature) ne
 * contiennent plus qu'une ligne "type". Le reglage reste en Java, dans
 * ArcaBalance : nos codecs sont des "unit" qui rendent toujours l'instance
 * creee ici.
 */
public final class ModFeatures {

	public static final ClutterPatchFeature PEBBLE_PATCH = register("pebble_patch",
			new ClutterPatchFeature(() -> ModBlocks.PEBBLES, ArcaFeature.WORLDGEN_PEBBLES,
					ArcaBalance.PEBBLE_PATCH_CHANCE, ArcaBalance.PEBBLE_PATCH_MIN_BLOCKS,
					ArcaBalance.PEBBLE_PATCH_MAX_BLOCKS, ArcaBalance.PEBBLE_PATCH_SPREAD));

	public static final ClutterPatchFeature STICK_PATCH = register("stick_patch",
			new ClutterPatchFeature(() -> ModBlocks.FALLEN_STICKS, ArcaFeature.WORLDGEN_STICKS,
					ArcaBalance.STICK_PATCH_CHANCE, ArcaBalance.STICK_PATCH_MIN_BLOCKS,
					ArcaBalance.STICK_PATCH_MAX_BLOCKS, ArcaBalance.STICK_PATCH_SPREAD));

	/** Cailloux au sol des grottes (voir CavePebbleFeature). */
	public static final CavePebbleFeature CAVE_PEBBLES = register("cave_pebbles", new CavePebbleFeature());

	/** Or rose : remplace des blocs la ou l'or touche le cuivre (voir PinkGoldContactFeature). */
	public static final PinkGoldContactFeature PINK_GOLD_CONTACT_ORE = register("pink_gold_contact_ore",
			new PinkGoldContactFeature());

	/**
	 * Enregistre le codec du type et le relie a l'instance unique : le JSON ne
	 * porte aucun reglage, il se contente de designer le type.
	 */
	private static <F extends SimpleArcaFeature> F register(String name, F feature) {
		MapCodec<F> codec = MapCodec.unit(feature);
		Registry.register(BuiltInRegistries.FEATURE_TYPE, ArcaMod.id(name), codec);
		feature.bindCodec(codec);
		return feature;
	}

	public static void init() {
	}

	private ModFeatures() {
	}
}
