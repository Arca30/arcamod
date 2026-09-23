package dev.arca.arcamod.example;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * ============================================================
 *  BLOC D'EXEMPLE - a copier pour creer tes propres blocs
 * ============================================================
 *
 * Ce bloc ne sert a rien en jeu : il existe pour montrer, dans un seul
 * fichier, tout ce qu'on fait habituellement dans un bloc. Copie-le, renomme
 * la classe, change les constantes, jette ce qui ne te sert pas.
 *
 * CE QU'IL FAIT
 *   - il a un etat allume/eteint (la propriete ACTIVE) ;
 *   - un clic droit l'allume ou l'eteint, avec un son ;
 *   - allume, il crache des particules et donne un effet au joueur qui passe
 *     au-dessus... non : au joueur qui clique (voir useWithoutItem) ;
 *   - allume, il s'eteint tout seul au bout d'un moment (randomTick).
 *
 * LES SEPT FICHIERS D'UN BLOC
 * Pour qu'un bloc existe vraiment en jeu, il faut :
 *   1. cette classe Java ;
 *   2. son enregistrement dans ModExamples (bloc + item) ;
 *   3. assets/arcamod/blockstates/<nom>.json      -> quel modele pour quel etat
 *   4. assets/arcamod/models/block/<nom>.json     -> la forme et les textures
 *   5. assets/arcamod/models/item/<nom>.json      -> son apparence en main
 *      + assets/arcamod/items/<nom>.json          -> la "definition d'item"
 *   6. data/arcamod/loot_table/blocks/<nom>.json  -> ce qu'il lache quand on le casse
 *   7. assets/arcamod/lang/*.json                 -> son nom affiche
 * Et, selon les cas : une recette, un tag mineable/*, une texture.
 */
public class ExampleBlock extends Block {

	// =====================================================================
	// REGLAGES - c'est ici qu'on touche
	// =====================================================================

	/** Duree de vie de l'etat allume, en ticks moyens (20 ticks = 1 s). */
	public static final int TICKS_BEFORE_SHUTDOWN = 200;

	/** Effet donne au joueur qui allume le bloc, et sa duree en ticks. */
	public static final int EFFECT_DURATION_TICKS = 100;
	public static final int EFFECT_AMPLIFIER = 0; // 0 = niveau I, 1 = niveau II...

	/** Nombre de particules crachees a chaque tick d'animation. */
	public static final int PARTICLE_COUNT = 3;

	/** Mettre false pour un bloc purement decoratif, sans clic droit. */
	public static final boolean REACTS_TO_RIGHT_CLICK = true;

	// =====================================================================
	// ETAT DU BLOC
	// =====================================================================

	/**
	 * Une propriete d'etat : une info stockee dans le bloc lui-meme, visible
	 * dans la commande /setblock et utilisable par le blockstate JSON pour
	 * changer de modele.
	 *
	 * Trois familles existent :
	 *   BooleanProperty.create("nom")            -> true / false
	 *   IntegerProperty.create("nom", min, max)  -> un nombre
	 *   EnumProperty.create("nom", Enum.class)   -> une liste de valeurs
	 *
	 * Chaque propriete multiplie le nombre d'etats possibles : un bloc avec
	 * un booleen et un entier de 1 a 5 a 10 etats, et le blockstate JSON doit
	 * TOUS les decrire.
	 */
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

	/**
	 * Le codec sert a Minecraft pour les blocs definis par datapack. Une
	 * ligne obligatoire, toujours la meme : simpleCodec(TaClasse::new).
	 */
	public ExampleBlock(BlockBehaviour.Properties properties) {
		super(properties);
		// L'etat par defaut = celui du bloc quand on le pose sans rien
		// preciser. A definir pour CHAQUE propriete ajoutee.
		this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
	}

	/** Declare les proprietes au jeu. Oublier une propriete ici = crash. */
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ACTIVE);
	}

	// =====================================================================
	// INTERACTION
	// =====================================================================

	/**
	 * Clic droit MAINS VIDES (ou avec un item qui ne fait rien de special).
	 *
	 * Pour reagir a un item precis (une hache, un rayon de miel...), il y a
	 * deux ecoles :
	 *   - useItemOn(...) dans le bloc, si c'est TON bloc ;
	 *   - UseBlockCallback dans ModEvents, si c'est un bloc vanilla.
	 *
	 * Retours possibles :
	 *   InteractionResult.SUCCESS  -> ca a marche, le bras s'anime
	 *   InteractionResult.CONSUME  -> ca a marche, pas d'animation
	 *   InteractionResult.PASS     -> je ne fais rien, au suivant
	 *   InteractionResult.FAIL     -> bloque tout
	 */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (!REACTS_TO_RIGHT_CLICK) {
			return InteractionResult.PASS;
		}

		boolean nowActive = !state.getValue(ACTIVE);

		// REGLE D'OR : tout ce qui change le monde se fait cote SERVEUR.
		// level.isClientSide() vaut true sur la machine du joueur, false sur
		// le serveur. Modifier un bloc cote client donne un "fantome" qui
		// disparait a la prochaine mise a jour.
		if (!level.isClientSide()) {
			// Le 3 est un jeu d'options : 1 = prevenir les voisins,
			// 2 = prevenir les clients. 3 = les deux, le choix habituel.
			level.setBlock(pos, state.setValue(ACTIVE, nowActive), 3);

			if (nowActive) {
				player.addEffect(new MobEffectInstance(MobEffects.GLOWING, EFFECT_DURATION_TICKS, EFFECT_AMPLIFIER));

				// scheduleTick programme un reveil : le bloc s'eteindra tout
				// seul. C'est plus precis que randomTick quand on veut un
				// delai exact.
				level.scheduleTick(pos, this, TICKS_BEFORE_SHUTDOWN);
			}
		}

		// Le son, lui, se joue des deux cotes (le serveur previent les
		// joueurs autour). null en premier argument = personne n'est exclu.
		level.playSound(null, pos, nowActive ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE,
				SoundSource.BLOCKS, 0.6F, 1.4F);

		return InteractionResult.SUCCESS;
	}

	/** Reveil programme par scheduleTick : on eteint. */
	@Override
	protected void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos,
			RandomSource random) {
		if (state.getValue(ACTIVE)) {
			level.setBlock(pos, state.setValue(ACTIVE, false), 3);
		}
	}

	// =====================================================================
	// ANIMATION (client uniquement)
	// =====================================================================

	/**
	 * Appelee plusieurs fois par seconde sur la machine du joueur, pour les
	 * blocs visibles. Uniquement pour du decor : particules, sons d'ambiance.
	 * Ne jamais y modifier le monde.
	 */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(ACTIVE)) {
			return;
		}

		for (int i = 0; i < PARTICLE_COUNT; i++) {
			level.addParticle(ParticleTypes.END_ROD,
					pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6,
					pos.getY() + 1.0,
					pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6,
					0.0, 0.02, 0.0);
		}
	}
}
