package dev.arca.arcamod.block;

import com.mojang.serialization.MapCodec;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.block.entity.PotionCauldronBlockEntity;
import dev.arca.arcamod.config.ArcaFeature;
import dev.arca.arcamod.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jspecify.annotations.Nullable;

/**
 * Un chaudron rempli de potion.
 *
 * On y verse des potions normales (ni jet ni persistante) : chaque fiole fait
 * monter le niveau, et une fois plein, toute creature qui se tient dedans
 * recoit l'effet en continu.
 *
 * Sans feu de camp allume dessous, la preparation retombe en eau au bout de
 * ArcaBalance.POTION_CAULDRON_DECAY_TICKS.
 */
public class PotionCauldronBlock extends Block implements net.minecraft.world.level.block.EntityBlock {

	public static final MapCodec<PotionCauldronBlock> CODEC = simpleCodec(PotionCauldronBlock::new);

	/** Meme propriete que le chaudron vanilla : 1, 2 ou 3. */
	public static final IntegerProperty LEVEL = LayeredCauldronBlock.LEVEL;

	/** Les parois du chaudron, comme en vanilla. */
	private static final VoxelShape SHAPE = Shapes.join(
			Shapes.block(),
			Shapes.or(Block.box(0.0, 0.0, 4.0, 16.0, 3.0, 12.0),
					Block.box(4.0, 0.0, 0.0, 12.0, 3.0, 16.0),
					Block.box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0),
					Block.box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0)),
			net.minecraft.world.phys.shapes.BooleanOp.ONLY_FIRST);

	public PotionCauldronBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 1));
	}

	@Override
	protected MapCodec<PotionCauldronBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEVEL);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return Shapes.block();
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PotionCauldronBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		if (level.isClientSide()) {
			// Cote client : surveille que la couleur affichee est a jour.
			return createTickerHelper(type, ModBlockEntities.POTION_CAULDRON, PotionCauldronBlockEntity::clientTick);
		}

		return createTickerHelper(type, ModBlockEntities.POTION_CAULDRON, PotionCauldronBlockEntity::serverTick);
	}

	@SuppressWarnings("unchecked")
	private static <T extends BlockEntity, U extends BlockEntity> @Nullable BlockEntityTicker<T> createTickerHelper(
			BlockEntityType<T> type, BlockEntityType<U> expected, BlockEntityTicker<? super U> ticker) {
		return expected == type ? (BlockEntityTicker<T>) ticker : null;
	}

	/**
	 * Une creature dans un chaudron plein recoit l'effet en boucle : la duree
	 * est courte, mais elle est renouvelee a chaque passage.
	 */
	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
			InsideBlockEffectApplier applier, boolean movedByPiston) {
		if (level.isClientSide() || !ArcaFeature.POTION_CAULDRON.isEnabled()
				|| !(entity instanceof LivingEntity living)) {
			return;
		}

		if (level.getGameTime() % ArcaBalance.POTION_CAULDRON_REFRESH_TICKS != 0) {
			return;
		}

		if (!(level.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron) || !cauldron.isFull()) {
			return;
		}

		cauldron.contents().forEachEffect(effect -> living.addEffect(new MobEffectInstance(
				effect.getEffect(), ArcaBalance.POTION_CAULDRON_EFFECT_TICKS, effect.getAmplifier(),
				true, false, true)), 1.0F);
	}
}
