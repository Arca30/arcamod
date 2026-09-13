package dev.arca.arcamod.block;

import com.mojang.serialization.MapCodec;

import dev.arca.arcamod.registry.ModBlocks;
import dev.arca.arcamod.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Tete du buisson d'XP : le segment du sommet, le seul qui pousse.
 *
 * Meme mecanique que les lianes des cavernes (glow berries) mais retournee :
 * la plante part du sol et grandit vers le haut. Quand la tete pousse, elle se
 * deplace d'un bloc vers le haut et laisse derriere elle un
 * {@link XpBushPlantBlock} (le corps).
 */
public class XpBushBlock extends GrowingPlantHeadBlock implements XpVines {

	// ---------------------------------------------------------------------
	// Reglages
	// ---------------------------------------------------------------------

	/** Taille finale de la plante, tiree au hasard entre ces deux valeurs. */
	public static final int MIN_HEIGHT = 4;
	public static final int MAX_HEIGHT = 7;

	/** Probabilite de pousser d'un bloc a chaque random tick (0.1 = vanilla). */
	private static final double GROW_PER_TICK_PROBABILITY = 0.1;

	/** Probabilite qu'un nouveau segment naisse avec des baies (0.11 = vanilla). */
	private static final float CHANCE_OF_BERRIES_ON_GROWTH = 0.05F;

	// Les reglages communs a la tete et au corps (repousse des baies, effets de
	// la poudre d'os) sont dans XpVines : un seul endroit a modifier.

	// ---------------------------------------------------------------------

	/**
	 * Taille max tiree a la plantation et conservee dans l'etat du bloc : c'est
	 * la seule facon pour un bloc de "se souvenir" de quelque chose sans
	 * BlockEntity. Cette propriete n'existe que sur la tete.
	 */
	public static final IntegerProperty HEIGHT_LIMIT = IntegerProperty.create("height_limit", MIN_HEIGHT, MAX_HEIGHT);

	public static final MapCodec<XpBushBlock> CODEC = simpleCodec(XpBushBlock::new);

	public XpBushBlock(BlockBehaviour.Properties properties) {
		// Direction.UP = sens de croissance. false = pas de tick de fluide.
		super(properties, Direction.UP, SHAPE, false, GROW_PER_TICK_PROBABILITY);
		registerDefaultState(stateDefinition.any()
				.setValue(AGE, 0)
				.setValue(BERRIES, false)
				.setValue(HEIGHT_LIMIT, MIN_HEIGHT));
	}

	@Override
	public MapCodec<XpBushBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder); // ajoute AGE
		builder.add(BERRIES, HEIGHT_LIMIT);
	}

	// ---------------------------------------------------------------------
	// Plantation
	// ---------------------------------------------------------------------

	/**
	 * Etat pose par la baie. Le vanilla tire ici un age aleatoire (pour les
	 * lianes generees dans les grottes) ; nous on veut une jeune pousse, donc
	 * age 0, et on tire la taille max de cette plante-la.
	 */
	@Override
	public BlockState getStateForPlacement(RandomSource random) {
		return defaultBlockState()
				.setValue(AGE, 0)
				.setValue(HEIGHT_LIMIT, random.nextIntBetweenInclusive(MIN_HEIGHT, MAX_HEIGHT));
	}

	/**
	 * Sur quoi la plante peut s'accrocher. GrowingPlantBlock.canSurvive()
	 * appelle cette methode sur le bloc du dessous : il faut donc accepter la
	 * terre & co (pour le bloc du bas) ET nos propres blocs (pour les segments
	 * du dessus, sinon la plante s'auto-detruit).
	 */
	@Override
	protected boolean canAttachTo(BlockState state) {
		return state.is(BlockTags.SUPPORTS_VEGETATION) || state.is(this) || state.is(getBodyBlock());
	}

	// ---------------------------------------------------------------------
	// Croissance
	// ---------------------------------------------------------------------

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		// 1) Regeneration : si le segment est vide, une baie peut repousser toute seule.
		if (XpVines.tryRegrowBerries(state, level, pos, random)) {
			// On s'arrete ici : l'etat local est desormais perime, on ne fait pas
			// grandir dans le meme tick pour eviter de propager un etat incoherent.
			return;
		}

		// 2) Croissance : la tete monte d'un bloc tant que la hauteur max n'est pas atteinte.
		if (hasRoomToGrow(level, pos, state.getValue(HEIGHT_LIMIT))) {
			// super s'occupe du tirage de probabilite, de canGrowInto() et de la pose.
			super.randomTick(state, level, pos, random);
		}
	}

	/**
	 * Compte les segments deja presents sous la tete. On mesure dans le monde
	 * plutot que de se fier a AGE : ainsi, meme si on casse le sommet et que la
	 * plante se reconstruit une tete, la hauteur totale reste bornee.
	 */
	private boolean hasRoomToGrow(BlockGetter level, BlockPos headPos, int heightLimit) {
		BlockPos cursor = headPos.below();
		for (int height = 1; height < heightLimit; height++) {
			if (!level.getBlockState(cursor).is(getBodyBlock())) {
				return true; // moins de heightLimit blocs : il reste de la place
			}
			cursor = cursor.below();
		}
		return false;
	}

	/**
	 * Etat de la NOUVELLE tete, un bloc plus haut. cycle(AGE) incremente l'age
	 * et conserve les autres proprietes, donc HEIGHT_LIMIT se transmet tout
	 * seul a la nouvelle tete.
	 */
	@Override
	protected BlockState getGrowIntoState(BlockState growFromState, RandomSource random) {
		return super.getGrowIntoState(growFromState, random)
				.setValue(BERRIES, random.nextFloat() < CHANCE_OF_BERRIES_ON_GROWTH);
	}

	/** Etat du segment laisse derriere quand la tete monte d'un bloc. */
	@Override
	protected BlockState updateBodyAfterConvertedFromHead(BlockState headState, BlockState bodyState) {
		return bodyState.setValue(BERRIES, headState.getValue(BERRIES));
	}

	@Override
	protected Block getBodyBlock() {
		return ModBlocks.XP_BUSH_PLANT;
	}

	/** Dans quoi la plante peut pousser : uniquement de l'air. */
	@Override
	protected boolean canGrowInto(BlockState state) {
		return state.isAir();
	}

	@Override
	protected int getBlocksToGrowWhenBonemealed(RandomSource random) {
		return 1;
	}

	// ---------------------------------------------------------------------
	// Interactions
	// ---------------------------------------------------------------------

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return XpVines.harvest(player, state, level, pos);
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(ModItems.XP_BERRY);
	}

	// ---------------------------------------------------------------------
	// Poudre d'os : pilotee par les deux interrupteurs de XpVines.
	// ---------------------------------------------------------------------

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
		boolean canSpawnBerry = XpVines.BONEMEAL_SPAWNS_BERRIES && !state.getValue(BERRIES);
		boolean canGrow = XpVines.BONEMEAL_GROWS_PLANT && hasRoomToGrow(level, pos, state.getValue(HEIGHT_LIMIT));
		return canSpawnBerry || canGrow;
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
		// Priorite a la baie : si le segment est vide et que c'est autorise, on pose une baie.
		if (XpVines.BONEMEAL_SPAWNS_BERRIES && !state.getValue(BERRIES)) {
			XpVines.growBerries(level, pos, state);
			return;
		}

		if (!XpVines.BONEMEAL_GROWS_PLANT || !hasRoomToGrow(level, pos, state.getValue(HEIGHT_LIMIT))) {
			return;
		}

		BlockPos growthPos = pos.above();
		if (canGrowInto(level.getBlockState(growthPos)) && level.isInsideBuildHeight(growthPos)) {
			// On n'appelle PAS super.performBonemeal() : le vanilla recopie l'etat
			// de la tete actuelle telle quelle, donc une tete qui porte une baie
			// donnerait une nouvelle tete AVEC baie + un segment de corps AVEC
			// baie -> la poudre d'os duplique la baie. getGrowIntoState() retire
			// le tirage de baie, comme pour la croissance naturelle.
			level.setBlockAndUpdate(growthPos, getGrowIntoState(state, random));
		}
	}
}
