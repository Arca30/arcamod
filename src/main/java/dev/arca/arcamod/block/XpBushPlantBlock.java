package dev.arca.arcamod.block;

import com.mojang.serialization.MapCodec;

import dev.arca.arcamod.registry.ModBlocks;
import dev.arca.arcamod.registry.ModItems;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Corps du buisson d'XP : tous les segments sous la tete.
 *
 * Il ne pousse pas (pas de randomTicks dans ses Properties) mais il peut
 * porter des baies, eclairer et etre recolte. Si la tete au-dessus disparait,
 * le segment le plus haut se transforme automatiquement en tete
 * (GrowingPlantBodyBlock.updateShape s'en charge).
 */
public class XpBushPlantBlock extends GrowingPlantBodyBlock implements XpVines {


	public static final MapCodec<XpBushPlantBlock> CODEC = simpleCodec(XpBushPlantBlock::new);

	/**
	 * Le corps ne grandit jamais (seule la tete monte) : ici on ne fait que
	 * faire repousser une baie si le segment est vide.
	 *
	 * ATTENTION : cette methode n'est appelee que si le bloc a .randomTicks()
	 * dans ses Properties (voir ModBlocks). Sans ca, elle ne tourne jamais.
	 */
	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		XpVines.tryRegrowBerries(state, level, pos, random);
	}

	public XpBushPlantBlock(BlockBehaviour.Properties properties) {
		super(properties, Direction.UP, SHAPE, false);
		registerDefaultState(stateDefinition.any().setValue(BERRIES, false));
	}

	@Override
	public MapCodec<XpBushPlantBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BERRIES);
	}

	@Override
	protected GrowingPlantHeadBlock getHeadBlock() {
		return ModBlocks.XP_BUSH;
	}

	/** Voir le commentaire dans {@link XpBushBlock#canAttachTo}. */
	@Override
	protected boolean canAttachTo(BlockState state) {
		return state.is(BlockTags.SUPPORTS_VEGETATION) || state.is(this) || state.is(getHeadBlock());
	}

	/** Etat de la tete quand ce segment devient le sommet de la plante. */
	@Override
	protected BlockState updateHeadAfterConvertedFromBody(BlockState bodyState, BlockState headState) {
		return headState.setValue(BERRIES, bodyState.getValue(BERRIES));
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return XpVines.harvest(player, state, level, pos);
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(ModItems.XP_BERRY);
	}

	// Poudre d'os sur un segment de corps :
	//   - si l'apparition de baies est activee et que le segment touche est vide,
	//     la baie pousse ICI (c'est le segment que le joueur a vise) ;
	//   - sinon on relaie a la tete, ce qui permet de faire monter la plante en
	//     visant la base.

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
		if (XpVines.BONEMEAL_SPAWNS_BERRIES && !state.getValue(BERRIES)) {
			return true;
		}

		BlockPos headPos = findHeadPos(level, pos);
		if (headPos == null) {
			return false;
		}
		return getHeadBlock().isValidBonemealTarget(level, headPos, level.getBlockState(headPos));
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
		if (XpVines.BONEMEAL_SPAWNS_BERRIES && !state.getValue(BERRIES)) {
			XpVines.growBerries(level, pos, state);
			return;
		}

		BlockPos headPos = findHeadPos(level, pos);
		if (headPos != null) {
			getHeadBlock().performBonemeal(level, random, headPos, level.getBlockState(headPos));
		}
	}

	/**
	 * Remonte la tige a travers les segments de corps jusqu'a la tete.
	 * Renvoie null si aucune tete valide n'est trouvee au sommet.
	 */
	private BlockPos findHeadPos(BlockGetter level, BlockPos pos) {
		BlockPos.MutableBlockPos cursor = pos.mutable();
		while (level.getBlockState(cursor.above()).is(this)) {
			cursor.move(Direction.UP);
		}
		BlockPos headPos = cursor.above();
		return level.getBlockState(headPos).is(getHeadBlock()) ? headPos : null;
	}
}
