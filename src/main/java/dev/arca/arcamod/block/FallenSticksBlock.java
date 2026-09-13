package dev.arca.arcamod.block;

import com.mojang.serialization.MapCodec;

import dev.arca.arcamod.ArcaBalance;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Les branches mortes posees au sol, sous les arbres. */
public class FallenSticksBlock extends GroundClutterBlock {

	public static final MapCodec<FallenSticksBlock> CODEC = simpleCodec(FallenSticksBlock::new);

	/** Nombre de branches sur ce bloc. */
	public static final IntegerProperty STICKS =
			IntegerProperty.create("sticks", ArcaBalance.STICKS_MIN, ArcaBalance.STICKS_MAX);

	public FallenSticksBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<FallenSticksBlock> codec() {
		return CODEC;
	}

	@Override
	public IntegerProperty getCountProperty() {
		return STICKS;
	}

	@Override
	public int getMinCount() {
		return ArcaBalance.STICKS_MIN;
	}

	@Override
	public int getMaxCount() {
		return ArcaBalance.STICKS_MAX;
	}
}
