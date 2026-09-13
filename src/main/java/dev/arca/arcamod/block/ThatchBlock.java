package dev.arca.arcamod.block;

import java.util.Optional;
import java.util.function.Supplier;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Le chaume : de la paille battue, dont le lien rouge a saute.
 *
 * Il vieillit comme le cuivre, d'etape en etape jusqu'au gris. Un coup de
 * hache le rajeunit, un rayon de miel le fige (voir WeatheringThatch).
 *
 * Le bloc suivant est fourni par un Supplier : les quatre etapes se
 * referencent en chaine, et ModDecorBlocks n'est pas encore initialisee quand
 * la premiere est construite.
 */
public class ThatchBlock extends RotatedPillarBlock implements WeatheringThatch {

	public static final MapCodec<ThatchBlock> CODEC = simpleCodec(properties -> new ThatchBlock(properties, null, null));

	private final Supplier<Block> next;
	private final Supplier<Block> previous;

	public ThatchBlock(BlockBehaviour.Properties properties, Supplier<Block> next, Supplier<Block> previous) {
		super(properties);
		this.next = next;
		this.previous = previous;
		this.registerDefaultState(this.defaultBlockState().setValue(WAXED, false));
	}

	@Override
	public MapCodec<? extends RotatedPillarBlock> codec() {
		return CODEC;
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
		WeatheringThatch.fade(state, level, pos, random);
	}
}
