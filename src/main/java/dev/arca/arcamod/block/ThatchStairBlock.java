package dev.arca.arcamod.block;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/** Escalier en chaume : il vieillit exactement comme le bloc plein. */
public class ThatchStairBlock extends StairBlock implements WeatheringThatch {

	private final Supplier<Block> next;
	private final Supplier<Block> previous;

	public ThatchStairBlock(BlockState baseState, BlockBehaviour.Properties properties,
			Supplier<Block> next, Supplier<Block> previous) {
		super(baseState, properties);
		this.next = next;
		this.previous = previous;
		this.registerDefaultState(this.defaultBlockState().setValue(WAXED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(WAXED);
	}

	@Override
	public Optional<Block> nextStage() {
		return this.next == null ? Optional.empty() : Optional.ofNullable(this.next.get());
	}

	@Override
	public Optional<Block> previousStage() {
		return this.previous == null ? Optional.empty() : Optional.ofNullable(this.previous.get());
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return this.next != null && !state.getValue(WAXED);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		ArcaWeathering.fade(state, level, pos, random);
	}
}
