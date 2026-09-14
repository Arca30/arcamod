package dev.arca.arcamod.worldgen;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.block.GroundClutterBlock;
import dev.arca.arcamod.config.ArcaFeature;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Seme un petit amas d'objets au sol (cailloux, branches).
 *
 * Le taux d'apparition est gere ici et pas dans le JSON du placed_feature :
 * comme ca il se regle depuis ArcaBalance, avec le reste.
 */
public class ClutterPatchFeature extends Feature<NoneFeatureConfiguration> {

	private final Supplier<? extends GroundClutterBlock> block;
	private final ArcaFeature feature;
	private final float chancePerChunk;
	private final int minBlocks;
	private final int maxBlocks;
	private final int spread;

	/**
	 * @param block          le bloc a poser (en Supplier : le registre des
	 *                       blocs n'est pas encore rempli a la construction)
	 * @param feature        l'interrupteur qui active cette generation
	 * @param chancePerChunk probabilite, entre 0 et 1, qu'un chunk eligible
	 *                       recoive un amas
	 * @param minBlocks      nombre de blocs minimum dans l'amas
	 * @param maxBlocks      nombre de blocs maximum dans l'amas
	 * @param spread         rayon horizontal de dispersion, en blocs
	 */
	public ClutterPatchFeature(Codec<NoneFeatureConfiguration> codec,
			Supplier<? extends GroundClutterBlock> block, ArcaFeature feature, float chancePerChunk,
			int minBlocks, int maxBlocks, int spread) {
		super(codec);
		this.block = block;
		this.feature = feature;
		this.chancePerChunk = chancePerChunk;
		this.minBlocks = minBlocks;
		this.maxBlocks = maxBlocks;
		this.spread = spread;
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		BlockPos origin = context.origin();

		if (!this.feature.isEnabled() || this.chancePerChunk <= 0.0F || random.nextFloat() >= this.chancePerChunk) {
			return false;
		}

		GroundClutterBlock clutter = this.block.get();
		int wanted = this.minBlocks + random.nextInt(this.maxBlocks - this.minBlocks + 1);
		int placed = 0;

		for (int attempt = 0; attempt < ArcaBalance.PATCH_TRIES && placed < wanted; attempt++) {
			int dx = random.nextInt(this.spread * 2 + 1) - this.spread;
			int dz = random.nextInt(this.spread * 2 + 1) - this.spread;

			// Chaque colonne a sa propre altitude de surface : sans ca un amas
			// a flanc de colline serait moitie en l'air, moitie enterre.
			BlockPos candidate = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, origin.offset(dx, 0, dz));

			// On ne suit pas le terrain trop loin en hauteur, sinon l'amas
			// s'etale le long des falaises.
			if (Math.abs(candidate.getY() - origin.getY()) > ArcaBalance.PATCH_VERTICAL_SPREAD) {
				continue;
			}

			if (clutter.placeCluster(level, candidate, random, clutter.getMinCount(), clutter.getMaxCount())) {
				placed++;
			}
		}

		return placed > 0;
	}
}
