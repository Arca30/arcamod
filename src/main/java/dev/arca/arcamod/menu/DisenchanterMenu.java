package dev.arca.arcamod.menu;

import java.util.List;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.registry.ModBlocks;
import dev.arca.arcamod.registry.ModMenus;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.minecraft.core.Holder;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Le menu du desenchanteur.
 *
 * Trois slots : l'objet enchante, une pile de livres vierges, et le resultat.
 * Le resultat est un livre portant UN SEUL enchantement, toujours le premier
 * de la liste : on desenchante donc un enchantement a la fois, dans l'ordre,
 * et l'objet d'origine ressort allege a chaque fois.
 *
 * Comme l'enclume, ce menu ne s'appuie pas sur un BlockEntity : les items
 * poses dedans reviennent au joueur a la fermeture.
 */
public class DisenchanterMenu extends AbstractContainerMenu {

	public static final int SLOT_ITEM = 0;
	public static final int SLOT_BOOKS = 1;
	public static final int SLOT_RESULT = 2;

	private final ContainerLevelAccess access;
	private final Player player;
	private final DataSlot cost = DataSlot.standalone();

	/** L'objet a desenchanter et les livres vierges. */
	private final Container input = new SimpleContainer(2) {
		@Override
		public void setChanged() {
			super.setChanged();
			DisenchanterMenu.this.slotsChanged(this);
		}
	};

	private final ResultContainer result = new ResultContainer();

	public DisenchanterMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, ContainerLevelAccess.NULL);
	}

	public DisenchanterMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
		super(ModMenus.DISENCHANTER, containerId);
		this.access = access;
		this.player = inventory.player;

		this.addSlot(new Slot(this.input, SLOT_ITEM, 27, 47) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return !EnchantmentHelper.getEnchantmentsForCrafting(stack).isEmpty();
			}
		});

		this.addSlot(new Slot(this.input, SLOT_BOOKS, 76, 47) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(Items.BOOK);
			}
		});

		this.addSlot(new Slot(this.result, SLOT_RESULT, 134, 47) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}

			@Override
			public boolean mayPickup(Player player) {
				return DisenchanterMenu.this.canAfford(player);
			}

			@Override
			public void onTake(Player player, ItemStack stack) {
				DisenchanterMenu.this.onResultTaken(player);
			}
		});

		this.addStandardInventorySlots(inventory, 8, 84);
		this.addDataSlot(this.cost);
	}

	/** Le cout en niveaux d'experience du transfert en cours (0 = rien a faire). */
	public int getCost() {
		return this.cost.get();
	}

	public boolean canAfford(Player player) {
		return player.experienceLevel >= this.cost.get() || player.hasInfiniteMaterials();
	}

	@Override
	public void slotsChanged(Container container) {
		super.slotsChanged(container);

		if (container == this.input) {
			this.updateResult();
		}
	}

	/**
	 * Recalcule le livre a produire : le premier enchantement de l'objet, au
	 * niveau qu'il porte.
	 */
	private void updateResult() {
		ItemStack source = this.input.getItem(SLOT_ITEM);
		ItemStack books = this.input.getItem(SLOT_BOOKS);
		ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(source);

		if (source.isEmpty() || books.isEmpty() || enchantments.isEmpty()) {
			this.result.setItem(0, ItemStack.EMPTY);
			this.cost.set(0);
			this.broadcastChanges();
			return;
		}

		Object2IntMap.Entry<Holder<Enchantment>> first = firstEnchantment(enchantments);
		ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
		EnchantmentHelper.updateEnchantments(book, mutable -> mutable.set(first.getKey(), first.getIntValue()));

		this.result.setItem(0, book);
		this.cost.set(ArcaBalance.DISENCHANT_XP_BASE
				+ ArcaBalance.DISENCHANT_XP_PER_LEVEL * first.getIntValue());
		this.broadcastChanges();
	}

	/**
	 * Le "premier" enchantement. L'ordre de stockage n'est pas garanti, on
	 * trie donc sur l'identifiant pour que le joueur les recupere toujours
	 * dans le meme ordre d'une session a l'autre.
	 */
	private static Object2IntMap.Entry<Holder<Enchantment>> firstEnchantment(ItemEnchantments enchantments) {
		List<Object2IntMap.Entry<Holder<Enchantment>>> entries = enchantments.entrySet().stream()
				.sorted((a, b) -> keyOf(a.getKey()).compareTo(keyOf(b.getKey())))
				.toList();
		return entries.getFirst();
	}

	private static String keyOf(Holder<Enchantment> enchantment) {
		return enchantment.unwrapKey().<String>map(key -> key.identifier().toString()).orElse("");
	}

	/** Le joueur prend le livre : on encaisse le cout et on met a jour l'objet. */
	private void onResultTaken(Player player) {
		ItemStack source = this.input.getItem(SLOT_ITEM);
		ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(source);

		if (enchantments.isEmpty()) {
			return;
		}

		Holder<Enchantment> taken = firstEnchantment(enchantments).getKey();

		if (!player.hasInfiniteMaterials()) {
			player.giveExperienceLevels(-this.cost.get());
		}

		// L'enchantement quitte l'objet. Un livre enchante qui n'en a plus
		// aucun redevient un livre ordinaire.
		EnchantmentHelper.updateEnchantments(source, mutable -> mutable.removeIf(taken::equals));

		if (source.is(Items.ENCHANTED_BOOK) && EnchantmentHelper.getEnchantmentsForCrafting(source).isEmpty()) {
			this.input.setItem(SLOT_ITEM, new ItemStack(Items.BOOK));
		}

		this.input.removeItem(SLOT_BOOKS, 1);
		this.access.execute((level, pos) -> level.levelEvent(1042, pos, 0));
		this.updateResult();
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.access, player, ModBlocks.DISENCHANTER);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.result.setItem(0, ItemStack.EMPTY);
		this.access.execute((level, pos) -> this.clearContainer(player, this.input));
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotIndex);

		if (slot == null || !slot.hasItem()) {
			return result;
		}

		ItemStack stack = slot.getItem();
		result = stack.copy();
		int menuSlots = 3;

		if (slotIndex < menuSlots) {
			if (!this.moveItemStackTo(stack, menuSlots, this.slots.size(), true)) {
				return ItemStack.EMPTY;
			}

			slot.onQuickCraft(stack, result);
		} else if (!this.moveItemStackTo(stack, 0, menuSlots, false)) {
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
