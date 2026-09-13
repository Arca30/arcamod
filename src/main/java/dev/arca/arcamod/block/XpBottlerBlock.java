package dev.arca.arcamod.block;

import com.mojang.serialization.MapCodec;

import dev.arca.arcamod.block.entity.XpBottlerBlockEntity;
import dev.arca.arcamod.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jspecify.annotations.Nullable;

/**
 * Embouteilleur d'XP : un bloc a BlockEntity + interface.
 *
 * Le bloc lui-meme ne fait presque rien : il cree le BlockEntity, branche son
 * ticker cote serveur, et ouvre le menu au clic droit. Toute la logique est
 * dans {@link XpBottlerBlockEntity}.
 */
public class XpBottlerBlock extends BaseEntityBlock {

	public static final MapCodec<XpBottlerBlock> CODEC = simpleCodec(XpBottlerBlock::new);

	/**
	 * 12 px de cote, 8 px de haut, centre sur le bloc : de (2,0,2) a (14,8,14).
	 * Doit rester coherent avec le modele JSON, sinon la boite de selection ne
	 * correspond pas a ce qu'on voit.
	 */
	private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 9.0, 14.0);

	/**
	 * Image de l'animation du dessus : 0 = eteint, 7 = a plein regime.
	 *
	 * L'animation .mcmeta de Minecraft tourne toute seule en boucle : pour
	 * choisir l'image affichee, il faut passer par l'etat du bloc. Chaque
	 * valeur pointe vers un modele qui utilise une texture differente.
	 */
	public static final IntegerProperty FRAME =
			IntegerProperty.create("frame", 0, XpBottlerBlockEntity.MAX_FRAME);

	public XpBottlerBlock(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FRAME, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FRAME);
	}

	@Override
	public MapCodec<XpBottlerBlock> codec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new XpBottlerBlockEntity(pos, state);
	}

	/** Le ticker ne tourne que cote serveur : la machine n'a pas d'animation. */
	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return level.isClientSide() ? null
				: createTickerHelper(type, ModBlockEntities.XP_BOTTLER, XpBottlerBlockEntity::serverTick);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof XpBottlerBlockEntity bottler) {
			// openMenu declenche createMenu() cote serveur et l'ouverture du
			// Screen cote client, via les paquets vanilla.
			player.openMenu(bottler);
		}

		return InteractionResult.SUCCESS;
	}

	/** Met a jour les comparateurs / observateurs voisins quand le bloc casse. */
	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		Containers.updateNeighboursAfterDestroy(state, level, pos);
	}
}
