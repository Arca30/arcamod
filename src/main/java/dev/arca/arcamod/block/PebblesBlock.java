package dev.arca.arcamod.block;

import com.mojang.serialization.MapCodec;

import dev.arca.arcamod.ArcaBalance;

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
