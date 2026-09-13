package dev.arca.arcamod.block;

import dev.arca.arcamod.ArcaBalance;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Base commune aux petits objets poses au sol : cailloux, branches...
 *
 * Meme principe que les feuilles mortes ou les bougies : plusieurs exemplaires
 * tiennent sur le meme bloc, et le modele change a chaque ajout. FACING donne
 * quatre orientations, et le blockstate liste plusieurs modeles par etat : le
 * jeu en choisit un au hasard selon les coordonnees du bloc, ce qui evite
 * l'effet de grille sans avoir a decaler le bloc.
 *
 * La boite de selection couvre tout le dessus du bloc (voir
 * ArcaBalance.CLUTTER_SHAPE_HEIGHT) : comme les objets sont places au hasard
 * sur la surface, une boite collee a chaque caillou obligerait a viser au
 * pixel pres et on minerait le bloc du dessous.
 */
public abstract class GroundClutterBlock extends VegetationBlock {

	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

	/**
	 * Vrai uniquement pour les tas poses par la generation du monde.
	 *
	 * Tout ce qu'un joueur pose repasse a faux : sans ca, il suffirait de
	 * casser puis reposer le meme tas en boucle pour refaire tourner le
	 * tirage du butin bonus.
	 */
	public static final BooleanProperty NATURAL = BooleanProperty.create("natural");

	private final VoxelShape shape;

	protected GroundClutterBlock(BlockBehaviour.Properties properties) {
		super(properties);
		BlockState defaultState = this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(this.getCountProperty(), this.getMinCount());

		if (this.tracksNaturalOrigin()) {
			defaultState = defaultState.setValue(NATURAL, false);
		}

		this.registerDefaultState(defaultState);
		this.shape = Block.box(0.0, 0.0, 0.0, 16.0, ArcaBalance.CLUTTER_SHAPE_HEIGHT, 16.0);
	}

	/**
	 * La propriete qui compte les objets poses. Elle DOIT etre un champ
	 * statique de la sous-classe : le jeu l'appelle depuis le constructeur de
	 * Block, avant que les champs d'instance ne soient initialises.
	 */
	public abstract IntegerProperty getCountProperty();

	public abstract int getMinCount();

	public abstract int getMaxCount();

	/**
	 * Ce bloc distingue-t-il les tas naturels de ceux poses par le joueur ?
	 * Seuls les cailloux en ont besoin (butin bonus).
	 *
	 * Attention : appelee depuis le constructeur de Block, elle doit donc
	 * renvoyer une constante.
	 */
	protected boolean tracksNaturalOrigin() {
		return false;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, this.getCountProperty());

		if (this.tracksNaturalOrigin()) {
			builder.add(NATURAL);
		}
	}

	/** Marque un etat comme naturel ou non, si ce bloc suit l'information. */
	private BlockState withNatural(BlockState state, boolean natural) {
		return this.tracksNaturalOrigin() ? state.setValue(NATURAL, natural) : state;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return this.shape;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	/** N'importe quelle face superieure solide fait l'affaire. */
	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
	}

	/**
	 * Cliquer sur un tas avec le meme item en main en ajoute un, tant qu'on
	 * n'a pas atteint le maximum.
	 */
	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		if (context.getItemInHand().is(this.asItem()) && state.getValue(this.getCountProperty()) < this.getMaxCount()) {
			return true;
		}

		return super.canBeReplaced(state, context);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState existing = context.getLevel().getBlockState(context.getClickedPos());

		if (existing.is(this)) {
			// Le tas agrandi a la main n'est plus considere comme naturel.
			return this.withNatural(existing.setValue(this.getCountProperty(),
					Math.min(this.getMaxCount(), existing.getValue(this.getCountProperty()) + 1)), false);
		}

		return this.withNatural(
				this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()), false);
	}

	// ------------------------------------------------------------------
	// Aide au placement, utilisee par les projectiles et par la generation
	// ------------------------------------------------------------------

	/** Y a-t-il la place de poser (ou d'agrandir) un tas a cette position ? */
	public boolean canPlaceAt(LevelReader level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);

		if (state.is(this)) {
			return state.getValue(this.getCountProperty()) < this.getMaxCount();
		}

		return (state.isAir() || state.canBeReplaced()) && this.defaultBlockState().canSurvive(level, pos);
	}

	/**
	 * Pose un exemplaire : agrandit le tas s'il y en a deja un, sinon en cree
	 * un oriente au hasard. Renvoie false si la place n'est pas libre.
	 */
	public boolean placeOne(LevelAccessor level, BlockPos pos, RandomSource random) {
		if (!this.canPlaceAt(level, pos)) {
			return false;
		}

		BlockState existing = level.getBlockState(pos);
		BlockState placed = existing.is(this)
				? existing.setValue(this.getCountProperty(), existing.getValue(this.getCountProperty()) + 1)
				: this.defaultBlockState().setValue(FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));

		// Pose par un joueur (ou par un projectile) : ce n'est pas naturel.
		return level.setBlock(pos, this.withNatural(placed, false), 3);
	}

	/** Pose un tas complet d'un coup (utilise par la generation du monde). */
	public boolean placeCluster(LevelAccessor level, BlockPos pos, RandomSource random, int min, int max) {
		if (!level.getBlockState(pos).isAir() || !this.canPlaceAt(level, pos)) {
			return false;
		}

		int count = Math.clamp(
				min + random.nextInt(Math.max(1, max - min + 1)),
				this.getMinCount(),
				this.getMaxCount());

		return level.setBlock(pos, this.withNatural(this.defaultBlockState()
				.setValue(FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random))
				.setValue(this.getCountProperty(), count), true), 3);
	}
}
