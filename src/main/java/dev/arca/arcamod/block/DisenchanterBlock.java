package dev.arca.arcamod.block;

import com.mojang.serialization.MapCodec;

import dev.arca.arcamod.menu.DisenchanterMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Le desenchanteur : il rend a des livres vierges les enchantements d'un
 * objet, un par un, contre de l'experience.
 *
 * Comme l'enclume, il n'a pas de BlockEntity : ce qui est pose dedans revient
 * au joueur quand il referme l'ecran.
 */
public class DisenchanterBlock extends Block {

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
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (!level.isClientSide()) {
			player.openMenu(new SimpleMenuProvider(
					(containerId, inventory, opener) -> new DisenchanterMenu(containerId, inventory,
							ContainerLevelAccess.create(level, pos)),
					TITLE));
		}

		return InteractionResult.SUCCESS;
	}
}