package dev.arca.arcamod.block.entity;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * La memoire du chaudron a potions : ce qu'il contient, combien de fioles y
 * ont ete versees, et depuis combien de temps il refroidit.
 */
public class PotionCauldronBlockEntity extends BlockEntity {

	private PotionContents contents = PotionContents.EMPTY;

	/** Nombre de fioles versees, de 1 a ArcaBalance.POTION_CAULDRON_CAPACITY. */
	private int filled;

	/** Ticks ecoules sans feu de camp allume dessous. */
	private int coolingTicks;

	/** Couleur du liquide tant que le contenu n'est pas connu (eau vanilla). */
	public static final int DEFAULT_COLOUR = 0xFF3F76E4;

	/** Valeur de renderedColour avant tout rendu. */
	private static final int NOT_RENDERED = 0;

	/**
	 * Cote client : derniere couleur utilisee pour dessiner le liquide.
	 *
	 * Ecrite par PotionCauldronTint depuis le fil de construction des chunks
	 * (d'ou volatile), lue a chaque tick client : si elle ne correspond plus
	 * au contenu, on redemande un rendu. Ca rattrape TOUS les cas ou le chunk
	 * a ete dessine avant de connaitre la potion, quel que soit l'ordre
	 * d'arrivee des paquets.
	 */
	private volatile int renderedColour = NOT_RENDERED;

	/** Client : un rendu a deja ete force pour un chaudron jamais dessine. */
	private boolean forcedFirstRender;

	/**
	 * Serveur : ticks restants avant de renvoyer le contenu au client.
	 *
	 * Pourquoi : quand le joueur interagit avec un bloc, le client "predit"
	 * les changements et peut METTRE DE COTE la mise a jour du bloc envoyee
	 * par le serveur jusqu'a l'accuse de reception de son clic. Si le paquet
	 * du contenu arrive pendant ce temps, le client voit encore un chaudron
	 * vide vanilla, sans BlockEntity : le paquet est ignore. Le bloc apparait
	 * ensuite avec un contenu vide -> couleur de l'eau, jusqu'au prochain
	 * envoi. L'ordre d'arrivee de l'accuse et des paquets de chunk varie d'un
	 * tick a l'autre, d'ou l'aspect aleatoire. Un second envoi, quelques
	 * ticks plus tard, arrive forcement apres.
	 */
	private int resyncTicks;

	public PotionCauldronBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.POTION_CAULDRON, pos, state);
	}

	public PotionContents contents() {
		return this.contents;
	}

	/** La couleur (ARGB, toujours opaque) que doit avoir le liquide. */
	public int tintColour() {
		if (this.filled <= 0) {
			return DEFAULT_COLOUR;
		}

		return ARGB.opaque(this.contents.getColorOr(DEFAULT_COLOUR));
	}

	/** Appele par le rendu : memorise la couleur reellement dessinee. */
	public void markRendered(int colour) {
		this.renderedColour = colour;
		debug("rendered {} with colour {}", this.worldPosition, Integer.toHexString(colour));
	}

	/** Trace de diagnostic, active par ArcaBalance.DEBUG_POTION_CAULDRON. */
	public static void debug(String message, Object... args) {
		if (ArcaBalance.DEBUG_POTION_CAULDRON) {
			ArcaMod.LOGGER.info("[PotionCauldron] " + message, args);
		}
	}

	/**
	 * Tick client : redessine le bloc si la couleur affichee est perimee.
	 *
	 * Tant que le bloc n'a jamais ete dessine (hors champ, chunk pas encore
	 * construit), on ne fait rien : la construction utilisera de toute facon
	 * la bonne couleur.
	 */
	public static void clientTick(Level level, BlockPos pos, BlockState state, PotionCauldronBlockEntity cauldron) {
		int rendered = cauldron.renderedColour;
		int expected = cauldron.tintColour();

		// Jamais dessine par PotionCauldronTint alors qu'il a un contenu : le
		// chunk a pu etre construit avant que le BlockEntity existe cote
		// client. On force UN rendu (sans effet s'il est hors champ).
		if (rendered == NOT_RENDERED) {
			if (!cauldron.forcedFirstRender && cauldron.filled > 0) {
				cauldron.forcedFirstRender = true;
				debug("force first render at {} (colour {})", pos, Integer.toHexString(expected));
				level.sendBlockUpdated(pos, state, state, Block.UPDATE_IMMEDIATE);
			}

			return;
		}

		if (rendered == expected) {
			return;
		}

		debug("stale colour at {}: rendered {} expected {}", pos, Integer.toHexString(rendered), Integer.toHexString(expected));

		// Optimiste : on considere la bonne couleur comme affichee, pour ne
		// pas redemander un rendu a chaque tick en attendant la
		// reconstruction. Si celle-ci utilise encore l'ancienne couleur, elle
		// ecrasera cette valeur et on recommencera au tick suivant.
		cauldron.renderedColour = expected;
		level.sendBlockUpdated(pos, state, state, Block.UPDATE_IMMEDIATE);
	}

	public int filled() {
		return this.filled;
	}

	public boolean isFull() {
		return this.filled >= ArcaBalance.POTION_CAULDRON_CAPACITY;
	}

	/** Verse une fiole. Renvoie false si le chaudron est plein ou incompatible. */
	public boolean pour(PotionContents poured) {
		if (this.filled > 0 && !this.contents.equals(poured)) {
			return false;
		}

		if (this.isFull()) {
			return false;
		}

		this.contents = poured;
		this.filled++;
		this.coolingTicks = 0;
		this.resyncTicks = ArcaBalance.POTION_CAULDRON_RESYNC_DELAY_TICKS;
		this.setChanged();
		return true;
	}

	/**
	 * Retire des fioles (trempage de fleches). Renvoie le nombre reellement
	 * retire ; le chaudron est vide quand filled() retombe a 0.
	 */
	public int drain(int bottles) {
		int drained = Math.clamp(bottles, 0, this.filled);
		this.filled -= drained;
		this.resyncTicks = ArcaBalance.POTION_CAULDRON_RESYNC_DELAY_TICKS;
		this.setChanged();
		return drained;
	}

	/**
	 * Le niveau visible (1 a 3), deduit du nombre de fioles versees.
	 *
	 * Division vers le bas, avec un minimum de 1 : la premiere fiole fait
	 * apparaitre du liquide, et surtout la DERNIERE est celle qui fait monter
	 * le niveau le plus haut. Avec la capacite par defaut (6) : niveau 1 de 1
	 * a 3 fioles, niveau 2 a 4 et 5, niveau 3 a la sixieme.
	 */
	public int visualLevel() {
		int levels = LayeredCauldronBlock.MAX_FILL_LEVEL;
		return Math.clamp(this.filled * levels / Math.max(1, ArcaBalance.POTION_CAULDRON_CAPACITY), 1, levels);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, PotionCauldronBlockEntity cauldron) {
		if (cauldron.resyncTicks > 0 && --cauldron.resyncTicks == 0) {
			level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
		}

		BlockState below = level.getBlockState(pos.below());
		boolean heated = below.getBlock() instanceof CampfireBlock && below.getValue(CampfireBlock.LIT);

		if (heated) {
			// Tant que ca mijote, la potion ne se degrade pas.
			cauldron.coolingTicks = 0;
			return;
		}

		cauldron.coolingTicks++;

		if (cauldron.coolingTicks >= ArcaBalance.POTION_CAULDRON_DECAY_TICKS) {
			// Refroidie trop longtemps : il ne reste que de l'eau.
			level.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.defaultBlockState()
					.setValue(LayeredCauldronBlock.LEVEL, state.getValue(LayeredCauldronBlock.LEVEL)));
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.contents = input.read("contents", PotionContents.CODEC).orElse(PotionContents.EMPTY);
		this.filled = input.getIntOr("filled", 0);
		this.coolingTicks = input.getIntOr("cooling_ticks", 0);

		// Cote client : la couleur du liquide est "cuite" dans le maillage du
		// chunk au moment ou il est reconstruit. Or a la premiere fiole, le
		// bloc arrive AVANT le contenu du chaudron : le maillage est construit
		// avec un chaudron encore vide, et rien ne le redemande ensuite (le
		// niveau 1 ne change pas l'etat du bloc). On force donc un nouveau
		// rendu des qu'on recoit le contenu.
		if (this.level != null && this.level.isClientSide()) {
			debug("client received contents at {}: filled {} colour {}", this.worldPosition, this.filled,
					Integer.toHexString(this.tintColour()));
			BlockState state = this.getBlockState();
			this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_IMMEDIATE);
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("contents", PotionContents.CODEC, this.contents);
		output.putInt("filled", this.filled);
		output.putInt("cooling_ticks", this.coolingTicks);
	}

	/** La couleur du liquide doit etre connue du client : on synchronise tout. */
	@Override
	public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
		return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
		return this.saveWithoutMetadata(registries);
	}
}
