package dev.arca.arcamod.block;

import com.mojang.serialization.MapCodec;

import dev.arca.arcamod.block.entity.DisenchanterBlockEntity;
import dev.arca.arcamod.config.ArcaFeature;
import dev.arca.arcamod.menu.DisenchanterMenu;
import dev.arca.arcamod.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jspecify.annotations.Nullable;

/**
 * Le desenchanteur : il rend a des livres vierges les enchantements d'un
 * objet, un par un, contre de l'experience.
 *
 * Il n'a pas d'inventaire persistant : ce qui est pose dedans revient au
 * joueur quand il referme l'ecran (voir DisenchanterMenu). Sa seule
 * BlockEntity (DisenchanterBlockEntity) ne sert qu'a faire voler le livre
 * au-dessus, exactement comme sur une table d'enchantement.
 */
public class DisenchanterBlock extends Block implements EntityBlock {

	public static final MapCodec<DisenchanterBlock> CODEC = simpleCodec(DisenchanterBlock::new);

	private static final Component TITLE = Component.translatable("container.arcamod.disenchanter");

	public DisenchanterBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);

	@Override
	protected MapCodec<DisenchanterBlock> codec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DisenchanterBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		// Serveur : propagation du sculk apres un desenchantement.
		if (!level.isClientSide()) {
			return createTickerHelper(type, ModBlockEntities.DISENCHANTER, DisenchanterBlockEntity::serverTick);
		}

		return createTickerHelper(type, ModBlockEntities.DISENCHANTER, DisenchanterBlockEntity::bookAnimationTick);
	}

	@SuppressWarnings("unchecked")
	private static <T extends BlockEntity, U extends BlockEntity> @Nullable BlockEntityTicker<T> createTickerHelper(
			BlockEntityType<T> type, BlockEntityType<U> expected, BlockEntityTicker<? super U> ticker) {
		return expected == type ? (BlockEntityTicker<T>) ticker : null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (!ArcaFeature.DISENCHANTER.isEnabled()) {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			player.openMenu(new SimpleMenuProvider(
					(containerId, inventory, opener) -> new DisenchanterMenu(containerId, inventory,
							ContainerLevelAccess.create(level, pos)),
					TITLE));
		}

		return InteractionResult.SUCCESS;
	}
}