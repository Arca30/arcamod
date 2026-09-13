package dev.arca.arcamod.block.entity;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
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

	public PotionCauldronBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.POTION_CAULDRON, pos, state);
	}

	public PotionContents contents() {
		return this.contents;
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
		this.setChanged();
		return true;
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
