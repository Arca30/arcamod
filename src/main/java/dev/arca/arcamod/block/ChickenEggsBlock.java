package dev.arca.arcamod.block;

import com.mojang.serialization.MapCodec;

import dev.arca.arcamod.ArcaBalance;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import net.minecraft.world.entity.animal.chicken.ChickenVariants;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Des oeufs de poule poses au sol, facon oeufs de tortue.
 *
 * De ArcaBalance.EGGS_MIN a EGGS_MAX sur le meme bloc, dans les trois couleurs du jeu. Ils n'eclosent
 * que sur une botte de foin, au bout de ArcaBalance.EGG_HATCH_TICKS.
 *
 * L'eclosion passe par un tick programme (scheduleTick) et non par les ticks
 * aleatoires : le delai est ainsi exact et previsible, meme si le joueur
 * s'eloigne un instant.
 */
public class ChickenEggsBlock extends Block {

	public static final MapCodec<ChickenEggsBlock> CODEC = simpleCodec(ChickenEggsBlock::new);

	public static final IntegerProperty EGGS =
			IntegerProperty.create("eggs", ArcaBalance.EGGS_MIN, ArcaBalance.EGGS_MAX);

	public static final EnumProperty<EggColor> COLOR = EnumProperty.create("color", EggColor.class);

	/** Les trois couleurs d'oeufs du jeu, et la poule qui en sort. */
	public enum EggColor implements StringRepresentable {
		WHITE("white", ChickenVariants.TEMPERATE),
		BLUE("blue", ChickenVariants.COLD),
		BROWN("brown", ChickenVariants.WARM);

		private final String name;
		private final ResourceKey<ChickenVariant> variant;

		EggColor(String name, ResourceKey<ChickenVariant> variant) {
			this.name = name;
			this.variant = variant;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public ResourceKey<ChickenVariant> variant() {
			return this.variant;
		}

		/** La couleur correspondant a un item d'oeuf, ou null. */
		public static EggColor fromItem(Item item) {
			if (item == Items.EGG) {
				return WHITE;
			}

			if (item == Items.BLUE_EGG) {
				return BLUE;
			}

			if (item == Items.BROWN_EGG) {
				return BROWN;
			}

			return null;
		}

		public Item item() {
			return switch (this) {
				case WHITE -> Items.EGG;
				case BLUE -> Items.BLUE_EGG;
				case BROWN -> Items.BROWN_EGG;
			};
		}
	}

	private static final VoxelShape ONE_EGG = Block.box(3.0, 0.0, 3.0, 12.0, 7.0, 12.0);
	private static final VoxelShape MANY_EGGS = Block.box(1.0, 0.0, 1.0, 15.0, 7.0, 15.0);

	public ChickenEggsBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(EGGS, ArcaBalance.EGGS_MIN)
				.setValue(COLOR, EggColor.WHITE));
	}

	@Override
	protected MapCodec<ChickenEggsBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(EGGS, COLOR);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(EGGS) > 1 ? MANY_EGGS : ONE_EGG;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, net.minecraft.world.level.ScheduledTickAccess ticks,
			BlockPos pos, Direction direction, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		return !state.canSurvive(level, pos)
				? Blocks.AIR.defaultBlockState()
				: super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
	}

	/** Poser un oeuf de plus sur le tas, tant qu'on reste sous le maximum. */
	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		EggColor placed = EggColor.fromItem(context.getItemInHand().getItem());

		if (placed == state.getValue(COLOR) && state.getValue(EGGS) < ArcaBalance.EGGS_MAX) {
			return true;
		}

		return super.canBeReplaced(state, context);
	}

	@Override
	protected void onPlace(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, BlockState oldState,
			boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		scheduleHatching(level, pos);
	}

	/** Relance le compte a rebours d'eclosion. */
	public static void scheduleHatching(LevelAccessor level, BlockPos pos) {
		level.scheduleTick(pos, level.getBlockState(pos).getBlock(), ArcaBalance.EGG_HATCH_TICKS);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		// Pas de paille dessous : les oeufs restent des oeufs, on repasse
		// plus tard au cas ou le joueur en poserait.
		if (!level.getBlockState(pos.below()).is(Blocks.HAY_BLOCK)) {
			level.scheduleTick(pos, this, ArcaBalance.EGG_HATCH_TICKS);
			return;
		}

		this.hatch(state, level, pos);
	}

	private void hatch(BlockState state, ServerLevel level, BlockPos pos) {
		level.playSound(null, pos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.7F, 0.9F);
		level.removeBlock(pos, false);

		EggColor color = state.getValue(COLOR);

		for (int i = 0; i < state.getValue(EGGS); i++) {
			Chicken chick = EntityTypes.CHICKEN.create(level, EntitySpawnReason.BREEDING);

			if (chick == null) {
				continue;
			}

			chick.setBaby(true);
			chick.setVariant(level.registryAccess().lookupOrThrow(Registries.CHICKEN_VARIANT).getOrThrow(color.variant()));
			chick.snapTo(pos.getX() + 0.3 + i * 0.2, pos.getY(), pos.getZ() + 0.3, 0.0F, 0.0F);
			level.addFreshEntity(chick);
		}
	}
}
