package dev.arca.arcamod.item;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.config.ArcaFeature;

import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;

/**
 * Regle unique decidant ce qui devient empilable, partagee par les mixins.
 *
 * Trois familles :
 *   1. tout ce qui a une durabilite (outils, armures portees, boucliers...) ;
 *   2. tout ce qui est equipable sans durabilite : armures de cheval, de
 *      nautile, selles, harnais... ;
 *   3. une balise ouverte, {@link #STACKABLE}, pour tout le reste : potions,
 *      soupes, et ce que tu voudras y ajouter.
 *
 * Interrupteur : ArcaFeature.GEAR_STACKING. Taille : ArcaBalance.GEAR_STACK_SIZE.
 */
public final class StackingRules {

	/** Taille de pile accordee (reglage : ArcaBalance.GEAR_STACK_SIZE). */
	public static int stackSize() {
		return ArcaBalance.GEAR_STACK_SIZE;
	}

	/**
	 * Balise editable dans data/arcamod/tags/item/stackable.json : ajouter un
	 * item la-dedans suffit a le rendre empilable, sans recompiler.
	 */
	public static final TagKey<Item> STACKABLE = TagKey.create(Registries.ITEM, ArcaMod.id("stackable"));

	public static boolean isEnabled() {
		return ArcaFeature.GEAR_STACKING.isEnabled() && stackSize() > 1;
	}

	public static boolean shouldStack(ItemInstance stack) {
		// (ItemInstance n'expose pas has(), seulement get() : null = absent)
		return stack.get(DataComponents.MAX_DAMAGE) != null     // outils, armures, elytres...
				|| stack.get(DataComponents.EQUIPPABLE) != null  // armures de monture, selles
				|| isTaggedStackable(stack);                     // potions, soupes, extensions
	}

	/**
	 * Lecture de la balise SANS passer par stack.is(TagKey).
	 *
	 * Cette methode est appelee depuis getMaxStackSize(), donc des le demarrage
	 * du jeu, avant que les balises ne soient chargees. Or is(TagKey) leve
	 * IllegalStateException("Tags not bound") dans cette fenetre : le jeu
	 * planterait au lancement. On interroge donc le registre, qui rend un
	 * Optional vide tant que la balise n'existe pas, et on verifie isBound().
	 */
	private static boolean isTaggedStackable(ItemInstance stack) {
		return BuiltInRegistries.ITEM.get(STACKABLE)
				.filter(HolderSet.Named::isBound)
				.map(tagged -> tagged.contains(stack.typeHolder()))
				.orElse(false);
	}

	private StackingRules() {
	}
}
