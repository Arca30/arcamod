package dev.arca.arcamod.registry;

import java.util.function.Predicate;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.ArcaMod;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** Ajout des pierres et des branches a la generation naturelle du monde. */
public final class ModWorldGen {

	/** Pointent sur data/arcamod/worldgen/placed_feature/*.json. */
	private static final ResourceKey<PlacedFeature> PEBBLE_PATCH = placedFeature("pebble_patch");
	private static final ResourceKey<PlacedFeature> STICK_PATCH = placedFeature("stick_patch");
	private static final ResourceKey<PlacedFeature> PINK_GOLD_CONTACT_ORE = placedFeature("pink_gold_contact_ore");
	private static final ResourceKey<PlacedFeature> SOUL_PEPPER_BUSH = placedFeature("patch_soul_pepper_bush");

	private static ResourceKey<PlacedFeature> placedFeature(String name) {
		return ResourceKey.create(Registries.PLACED_FEATURE, ArcaMod.id(name));
	}

	/**
	 * Assez chaud pour ne pas etre enneige, et sur la terre ferme.
	 *
	 * Le filtre par temperature plutot que par liste de biomes a deux
	 * avantages : il exclut automatiquement tout ce qui est enneige, et il
	 * marche aussi pour les biomes ajoutes par d'autres mods.
	 */
	private static Predicate<BiomeSelectionContext> warmLand() {
		return BiomeSelectors.foundInOverworld()
				.and(context -> context.getBiome().getBaseTemperature() >= ArcaBalance.BIOME_MIN_TEMPERATURE)
				.and(context -> !context.hasTag(BiomeTags.IS_OCEAN)
						&& !context.hasTag(BiomeTags.IS_RIVER)
						&& !context.hasTag(BiomeTags.IS_BEACH));
	}

	/**
	 * Les biomes ou il pousse des arbres, sans neige.
	 *
	 * Il n'existe pas de tag vanilla "il y a des arbres ici" : on prend les
	 * grandes familles boisees, plus les quelques biomes ouverts qui ont
	 * quand meme des arbres isoles. Le filtre de temperature de warmLand()
	 * ecarte ensuite les versions enneigees (taiga enneigee, bosquet...).
	 */
	private static Predicate<BiomeSelectionContext> treeCovered() {
		return warmLand().and(context -> context.hasTag(BiomeTags.IS_FOREST)
				|| context.hasTag(BiomeTags.IS_JUNGLE)
				|| context.hasTag(BiomeTags.IS_TAIGA)
				|| context.hasTag(BiomeTags.IS_SAVANNA)
				|| context.getBiomeKey() == Biomes.PLAINS
				|| context.getBiomeKey() == Biomes.SUNFLOWER_PLAINS
				|| context.getBiomeKey() == Biomes.SWAMP
				|| context.getBiomeKey() == Biomes.MANGROVE_SWAMP
				|| context.getBiomeKey() == Biomes.CHERRY_GROVE
				|| context.getBiomeKey() == Biomes.WINDSWEPT_FOREST);
	}

	public static void init() {
		// VEGETAL_DECORATION : la meme etape que les fleurs et l'herbe, donc
		// apres le terrain et les arbres.
		BiomeModifications.addFeature(warmLand(), GenerationStep.Decoration.VEGETAL_DECORATION, PEBBLE_PATCH);
		BiomeModifications.addFeature(treeCovered(), GenerationStep.Decoration.VEGETAL_DECORATION, STICK_PATCH);

		// Meme etape que les minerais, ajoute APRES eux : l'or et le cuivre du
		// chunk sont deja en place quand on cherche leurs points de contact.
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES,
				PINK_GOLD_CONTACT_ORE);

		// Buissons de piments des ames : uniquement dans la vallee des ames.
		// Frequence et taille des touffes : placed_feature/patch_soul_pepper_bush.json.
		BiomeModifications.addFeature(BiomeSelectors.includeByKey(Biomes.SOUL_SAND_VALLEY),
				GenerationStep.Decoration.VEGETAL_DECORATION, SOUL_PEPPER_BUSH);
	}

	private ModWorldGen() {
	}
}
