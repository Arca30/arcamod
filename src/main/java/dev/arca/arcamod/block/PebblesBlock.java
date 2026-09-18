package dev.arca.arcamod.block;

import com.mojang.serialization.MapCodec;

import dev.arca.arcamod.ArcaBalance;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Les petits cailloux poses au sol. */
public class PebblesBlock extends GroundClutterBlock {

	public static final MapCodec<PebblesBlock> CODEC = simpleCodec(PebblesBlock::new);

	/** Nombre de cailloux sur ce bloc. */
	public static final IntegerProperty PEBBLES =
			IntegerProperty.create("pebbles", ArcaBalance.PEBBLES_MIN, ArcaBalance.PEBBLES_MAX);

	public PebblesBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<PebblesBlock> codec() {
		return CODEC;
	}

	@Override
	protected boolean tracksNaturalOrigin() {
		return true;
	}

	/**
	 * Plus un tas a de cailloux, plus il est rare : tirage pondere par
	 * ArcaBalance.PEBBLE_COUNT_WEIGHTS.
	 */
	@Override
	protected int pickClusterCount(RandomSource random, int min, int max) {
		int[] weights = ArcaBalance.PEBBLE_COUNT_WEIGHTS;
		int total = 0;

		for (int count = min; count <= max; count++) {
			total += weightOf(weights, count);
		}

		if (total <= 0) {
			return super.pickClusterCount(random, min, max);
		}

		int roll = random.nextInt(total);

		for (int count = min; count <= max; count++) {
			roll -= weightOf(weights, count);

			if (roll < 0) {
				return count;
			}
		}

		return min;
	}

	private static int weightOf(int[] weights, int count) {
		int index = count - 1;
		return index >= 0 && index < weights.length ? Math.max(0, weights[index]) : 0;
	}

	@Override
	public IntegerProperty getCountProperty() {
		return PEBBLES;
	}

	@Override
	public int getMinCount() {
		return ArcaBalance.PEBBLES_MIN;
	}

	@Override
	public int getMaxCount() {
		return ArcaBalance.PEBBLES_MAX;
	}
}
