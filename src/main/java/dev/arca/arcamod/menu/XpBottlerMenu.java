package dev.arca.arcamod.menu;

import dev.arca.arcamod.block.entity.XpBottlerBlockEntity;
import dev.arca.arcamod.registry.ModMenus;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Menu de l'embouteilleur : deux slots, rien d'autre. Toute la machine est
 * pilotee par la redstone, le GUI ne sert qu'a poser les bouteilles et a
 * recuperer les fioles.
 *
 * Un menu existe en DOUBLE : une instance serveur (branchee sur le vrai
 * BlockEntity) et une instance client (conteneur vide, remplie par le reseau).
 * D'ou les deux constructeurs.
 */
public class XpBottlerMenu extends AbstractContainerMenu {

	private final Container container;
	private final ContainerData data;

	/** Constructeur cote client : conteneur et donnees factices. */
	public XpBottlerMenu(int containerId, Inventory inventory) {
		this(containerId, inventory,
				new SimpleContainer(XpBottlerBlockEntity.CONTAINER_SIZE),
				new SimpleContainerData(XpBottlerBlockEntity.NUM_DATA_VALUES));
	}

	/** Constructeur cote serveur : le conteneur EST le BlockEntity. */
	public XpBottlerMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
		super(ModMenus.XP_BOTTLER, containerId);
		checkContainerSize(container, XpBottlerBlockEntity.CONTAINER_SIZE);
		this.container = container;
		this.data = data;

		// Ces coordonnees doivent correspondre a la texture du GUI.
		addSlot(new Slot(container, XpBottlerBlockEntity.SLOT_INPUT, 44, 35));
		addSlot(new Slot(container, XpBottlerBlockEntity.SLOT_OUTPUT, 116, 35) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false; // slot de sortie : on ne peut rien y deposer
			}
		});

		addStandardInventorySlots(inventory, 8, 84);
		addDataSlots(data); // synchronise le tampon d'XP vers le client

		container.startOpen(inventory.player);
	}

	/** Avancement de la fiole en cours, entre 0 et 1 : anime la fleche. */
	public float getProgress() {
		int buffered = this.data.get(XpBottlerBlockEntity.DATA_BUFFERED_XP);
		return Math.clamp(buffered / (float) XpBottlerBlockEntity.XP_PER_BOTTLE, 0.0F, 1.0F);
	}

	@Override
	public boolean stillValid(Player player) {
		return this.container.stillValid(player);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.container.stopOpen(player);
	}

	/** Shift-clic : machine -> inventaire, et inventaire -> machine. */
	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotIndex);
		if (slot == null || !slot.hasItem()) {
			return result;
		}

		ItemStack stack = slot.getItem();
		result = stack.copy();
		int containerSlots = XpBottlerBlockEntity.CONTAINER_SIZE;

		if (slotIndex < containerSlots) {
			// depuis la machine vers l'inventaire du joueur
			if (!moveItemStackTo(stack, containerSlots, this.slots.size(), true)) {
				return ItemStack.EMPTY;
			}
		} else if (!moveItemStackTo(stack, 0, containerSlots, false)) {
			// depuis l'inventaire vers la machine (canPlaceItem filtre les slots)
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		return result;
	}
}
