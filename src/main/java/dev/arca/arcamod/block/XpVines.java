package dev.arca.arcamod.block;

import java.util.function.ToIntFunction;

import dev.arca.arcamod.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Code commun a la tete ({@link XpBushBlock}) et au corps
 * ({@link XpBushPlantBlock}) du buisson d'XP.
 *
 * Meme decoupage que le vanilla : Java n'a pas d'heritage multiple, et les deux
 * blocs heritent deja de GrowingPlantHeadBlock / GrowingPlantBodyBlock. On met
 * donc le code partage dans une interface a methodes statiques, exactement
 * comme l'interface CaveVines de Minecraft.
 */
public interface XpVines {

	/** Nombre de baies recoltees sur UN segment, bornes incluses. */
	int HARVEST_MIN = 1;
	int HARVEST_MAX = 2;

	/**
	 * Probabilite, par random tick, qu'une baie repousse sur un segment vide.
	 * Vaut pour la tete ET le corps : un seul reglage pour toute la plante.
	 */
	float BERRY_REGROW_CHANCE = 0.02F;

	/** La poudre d'os fait-elle monter la plante d'un segment ? */
	boolean BONEMEAL_GROWS_PLANT = true;

	/** La poudre d'os peut-elle faire apparaitre une baie sur un segment vide ? */
	boolean BONEMEAL_SPAWNS_BERRIES = false;

	/** Niveau de lumiere emis par un segment qui porte des baies (0-15). */
	int LIGHT_WITH_BERRIES = 10;

	/** Boite de selection d'un segment : colonne de 14x16x14 px. */
	VoxelShape SHAPE = Block.column(12.0, 0.0, 16.0);

	/** Propriete "berries" : ce segment porte-t-il des baies ? */
	BooleanProperty BERRIES = BlockStateProperties.BERRIES;

	/**
	 * Fonction de luminosite passee aux Properties du bloc : la lumiere depend
	 * de l'etat, donc elle s'eteint quand on recolte.
	 */
	static ToIntFunction<BlockState> emission(int lightEmission) {
		return state -> state.getValue(BERRIES) ? lightEmission : 0;
	}

	static boolean hasBerries(BlockState state) {
		return state.hasProperty(BERRIES) && state.getValue(BERRIES);
	}

	/** Fait apparaitre une baie sur ce segment. */
	static void growBerries(ServerLevel level, BlockPos pos, BlockState state) {
		level.setBlock(pos, state.setValue(BERRIES, true), Block.UPDATE_CLIENTS);
	}

	/**
	 * Tirage de repousse d'une baie sur un segment vide, appele depuis le
	 * randomTick de la tete et du corps.
	 *
	 * @return true si une baie est apparue : dans ce cas l'appelant doit
	 *         s'arreter, son BlockState local est perime.
	 */
	static boolean tryRegrowBerries(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.getValue(BERRIES) || random.nextFloat() >= BERRY_REGROW_CHANCE) {
			return false;
		}
		growBerries(level, pos, state);
		return true;
	}

	/**
	 * Recolte d'un segment : les baies vont directement dans l'inventaire, le
	 * bloc n'est pas casse et sa lumiere s'eteint.
	 */
	static InteractionResult harvest(Player player, BlockState state, Level level, BlockPos pos) {
		if (!state.getValue(BERRIES)) {
			// Pas de baies ici : on laisse passer (pose de bloc, autre item...).
			return InteractionResult.PASS;
		}

		if (level instanceof ServerLevel serverLevel) {
			int count = serverLevel.getRandom().nextIntBetweenInclusive(HARVEST_MIN, HARVEST_MAX);
			ItemStack berries = new ItemStack(ModItems.XP_BERRY, count);

// On fait toujours tomber les baies au sol, quelle que soit la place en inventaire.
			Block.popResource(serverLevel, pos, berries);

			float pitch = Mth.randomBetween(serverLevel.getRandom(), 0.8F, 1.2F);
			serverLevel.playSound(null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, pitch);

			BlockState newState = state.setValue(BERRIES, false);
			serverLevel.setBlock(pos, newState, Block.UPDATE_CLIENTS);
			serverLevel.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));
		}

		return InteractionResult.SUCCESS;
	}
}
