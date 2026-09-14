package dev.arca.arcamod.block.entity;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.block.XpBottlerBlock;
import dev.arca.arcamod.config.ArcaFeature;
import dev.arca.arcamod.menu.XpBottlerMenu;
import dev.arca.arcamod.registry.ModBlockEntities;
import dev.arca.arcamod.util.PlayerXp;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.Nullable;

/**
 * Embouteilleur d'XP.
 *
 * Sous signal de redstone, et tant qu'il a des bouteilles en verre, il aspire
 * l'experience du joueur le plus proche dans un rayon donne, jusqu'a remplir
 * une fiole. Le joueur paie aussi en saturation.
 */
public class XpBottlerBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {

	// Reglages : ArcaBalance.XP_BOTTLER_*. Interrupteur : ArcaFeature.XP_BOTTLER.

	/** XP a prelever pour fabriquer une fiole. */
	public static final int XP_PER_BOTTLE = Math.max(1, ArcaBalance.XP_BOTTLER_XP_PER_BOTTLE);

	/** Derniere image de l'animation du dessus (les textures vont de 0 a 7). */
	public static final int MAX_FRAME = 7;

	/** Un souffle de particules tous les N ticks. */
	private static final int PARTICLE_INTERVAL_TICKS = 4;

	/** Nombre de points visibles en meme temps sur le trajet joueur -> bloc. */
	private static final int PARTICLE_TRAIL_COUNT = 4;

	/** Duree d'un aller du joueur vers le bloc, en ticks. */
	private static final int PARTICLE_CYCLE_TICKS = 60;

	public static final int SLOT_INPUT = 0;
	public static final int SLOT_OUTPUT = 1;
	public static final int CONTAINER_SIZE = 2;

	/** Une seule valeur synchronisee : l'XP en tampon, pour animer la fleche. */
	public static final int DATA_BUFFERED_XP = 0;
	public static final int NUM_DATA_VALUES = 1;

	private static final int[] SLOTS_INSERT = new int[]{SLOT_INPUT};
	private static final int[] SLOTS_EXTRACT = new int[]{SLOT_OUTPUT};

	private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

	/** XP deja aspiree pour la fiole en cours. Toujours < XP_PER_BOTTLE. */
	private int bufferedXp;

	private int siphonTimer;

	/** Compte a rebours avant de passer a l'image suivante. */
	private int frameTimer;

	private final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int dataId) {
			return dataId == DATA_BUFFERED_XP ? XpBottlerBlockEntity.this.bufferedXp : 0;
		}

		@Override
		public void set(int dataId, int value) {
			if (dataId == DATA_BUFFERED_XP) {
				XpBottlerBlockEntity.this.bufferedXp = value;
			}
		}

		@Override
		public int getCount() {
			return NUM_DATA_VALUES;
		}
	};

	public XpBottlerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.XP_BOTTLER, pos, state);
	}

	// =====================================================================
	// Cycle de fonctionnement
	// =====================================================================

	public static void serverTick(Level level, BlockPos pos, BlockState state, XpBottlerBlockEntity bottler) {
		boolean ready = ArcaFeature.XP_BOTTLER.isEnabled() && bottler.canWork(level, pos);

		// La machine n'est vraiment "active" que si elle a quelqu'un a siphonner :
		// l'animation doit donc repartir en arriere des que le joueur s'eloigne,
		// pas seulement quand les bouteilles ou la redstone manquent.
		Player target = ready ? bottler.findTargetPlayer(level, pos) : null;
		boolean active = target != null;

		// Avant tout retour anticipe : l'animation doit pouvoir se rejouer a
		// l'envers alors meme que la machine ne travaille plus.
		bottler.advanceFrame(level, pos, state, active);

		if (!ready) {
			bottler.siphonTimer = 0;
			return;
		}

		// Fiole en attente : la sortie a pu se liberer entre-temps alors que le
		// tampon etait deja plein, auquel cas plus aucune XP ne pouvait entrer.
		boolean changed = bottler.tryProduceBottle(level, pos);

		if (!active) {
			bottler.siphonTimer = 0;
		} else {
			if (ArcaBalance.XP_BOTTLER_PARTICLES && level instanceof ServerLevel serverLevel
					&& level.getGameTime() % PARTICLE_INTERVAL_TICKS == 0) {
				bottler.emitSiphonTrail(serverLevel, pos, target);
			}

			if (++bottler.siphonTimer >= ArcaBalance.XP_BOTTLER_SIPHON_INTERVAL_TICKS) {
				bottler.siphonTimer = 0;
				if (bottler.siphonFrom(target)) {
					changed = true;
					changed |= bottler.tryProduceBottle(level, pos);
				}
			}
		}

		if (changed) {
			// setChanged() sans argument : advanceFrame() a pu remplacer le
			// BlockState entre-temps, le "state" local serait perime.
			bottler.setChanged();
		}
	}

	/**
	 * Fait avancer l'image du dessus vers 7 quand la machine tourne, et vers 0
	 * quand elle s'arrete. Une fois arrivee au bout, plus aucun changement
	 * d'etat n'est emis : le bloc reste fige sur l'image 0 ou l'image 7.
	 */
	private void advanceFrame(Level level, BlockPos pos, BlockState state, boolean active) {
		int frame = state.getValue(XpBottlerBlock.FRAME);
		int target = active ? MAX_FRAME : 0;

		if (frame == target) {
			this.frameTimer = 0;
			return;
		}

		if (++this.frameTimer < ArcaBalance.XP_BOTTLER_TICKS_PER_FRAME) {
			return;
		}
		this.frameTimer = 0;

		int next = frame + (active ? 1 : -1);
		level.setBlock(pos, state.setValue(XpBottlerBlock.FRAME, next), Block.UPDATE_CLIENTS);
	}

	/** Trois conditions : redstone, bouteilles en entree, place en sortie. */
	private boolean canWork(Level level, BlockPos pos) {
		if (!level.hasNeighborSignal(pos)) {
			return false;
		}
		if (!this.items.get(SLOT_INPUT).is(Items.GLASS_BOTTLE)) {
			return false;
		}

		ItemStack output = this.items.get(SLOT_OUTPUT);
		return output.isEmpty()
				|| output.is(Items.EXPERIENCE_BOTTLE) && output.getCount() < output.getMaxStackSize();
	}

	/** Joueur le plus proche, dans le rayon, non spectateur et avec de l'XP. */
	private @Nullable Player findTargetPlayer(Level level, BlockPos pos) {
		Vec3 center = Vec3.atCenterOf(pos);
		return level.getNearestPlayer(center.x, center.y, center.z, ArcaBalance.XP_BOTTLER_RADIUS,
				entity -> entity instanceof Player candidate
						&& !candidate.isSpectator()
						&& PlayerXp.getTotal(candidate) > 0);
	}

	/** @return true si de l'XP a effectivement ete prelevee. */
	private boolean siphonFrom(Player player) {
		// On ne prend jamais plus que ce qu'il reste a remplir, ni plus que ce
		// que le joueur possede.
		int amount = Math.min(ArcaBalance.XP_BOTTLER_XP_PER_SIPHON,
				Math.min(PlayerXp.getTotal(player), XP_PER_BOTTLE - this.bufferedXp));
		if (amount <= 0) {
			return false;
		}

		PlayerXp.take(player, amount);
		this.bufferedXp += amount;

		// La fatigue est proportionnelle : sur une fiole entiere, le joueur aura
		// bien paye XP_BOTTLER_EXHAUSTION_PER_BOTTLE au total.
		player.causeFoodExhaustion(ArcaBalance.XP_BOTTLER_EXHAUSTION_PER_BOTTLE * amount / XP_PER_BOTTLE);
		return true;
	}

	/**
	 * Filet de particules vertes qui remonte du joueur vers le bloc.
	 *
	 * Les particules ne se deplacent pas d'elles-memes : c'est leur POINT
	 * D'APPARITION qui avance le long du segment au fil des ticks, ce qui donne
	 * l'illusion d'un flux. Plusieurs points decales se partagent le trajet.
	 */
	private void emitSiphonTrail(ServerLevel level, BlockPos pos, Player player) {
		Vec3 target = Vec3.atCenterOf(pos).add(0.0, 0.15, 0.0);
		Vec3 source = new Vec3(player.getX(), player.getY() + player.getBbHeight() * 0.5, player.getZ());
		float phase = level.getGameTime() % PARTICLE_CYCLE_TICKS / (float) PARTICLE_CYCLE_TICKS;

		for (int i = 0; i < PARTICLE_TRAIL_COUNT; i++) {
			float progress = (phase + i / (float) PARTICLE_TRAIL_COUNT) % 1.0F;
			Vec3 at = source.lerp(target, progress);
			level.sendParticles(ParticleTypes.ENCHANT, at.x, at.y, at.z, 1, 0.02, 0.02, 0.02, 0.0);
		}
	}

	/** @return true si une fiole vient d'etre fabriquee. */
	private boolean tryProduceBottle(Level level, BlockPos pos) {
		if (this.bufferedXp < XP_PER_BOTTLE || !canWork(level, pos)) {
			return false;
		}

		this.bufferedXp -= XP_PER_BOTTLE;
		this.items.get(SLOT_INPUT).shrink(1);

		ItemStack output = this.items.get(SLOT_OUTPUT);
		if (output.isEmpty()) {
			this.items.set(SLOT_OUTPUT, new ItemStack(Items.EXPERIENCE_BOTTLE));
		} else {
			output.grow(1);
		}

		playSound(level, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, 1.2F);
		return true;
	}

	private void playSound(Level level, BlockPos pos, SoundEvent sound, float pitch) {
		level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.2F, pitch);
	}

	/** L'XP en tampon n'est pas perdue si on casse le bloc. */
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state); // lache les items des slots
		if (this.bufferedXp > 0 && this.level instanceof ServerLevel serverLevel) {
			ExperienceOrb.award(serverLevel, Vec3.atCenterOf(pos), this.bufferedXp);
			this.bufferedXp = 0;
		}
	}

	// =====================================================================
	// Entonnoirs : entree par le haut et les cotes, sortie par le bas
	// =====================================================================

	@Override
	public int[] getSlotsForFace(Direction side) {
		return side == Direction.DOWN ? SLOTS_EXTRACT : SLOTS_INSERT;
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
		return side != Direction.DOWN && canPlaceItem(slot, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
		return side == Direction.DOWN && slot == SLOT_OUTPUT;
	}

	// =====================================================================
	// Container / menu / sauvegarde
	// =====================================================================

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.arcamod.xp_bottler");
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
		return new XpBottlerMenu(containerId, inventory, this, this.dataAccess);
	}

	@Override
	public int getContainerSize() {
		return CONTAINER_SIZE;
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	/** Seules les bouteilles en verre vides entrent, et rien n'entre en sortie. */
	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return slot == SLOT_INPUT && stack.is(Items.GLASS_BOTTLE);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, this.items);
		this.bufferedXp = input.getIntOr("BufferedXp", 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items);
		output.putInt("BufferedXp", this.bufferedXp);
	}
}
