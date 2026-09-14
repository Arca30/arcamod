package dev.arca.arcamod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.arca.arcamod.config.ArcaFeature;
import dev.arca.arcamod.registry.ModItems;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.PotionIds;

/**
 * Ajoute une recette d'alambic : baie d'XP + bouteilles d'eau -> fioles d'XP.
 *
 * Pourquoi un mixin ? Le systeme de brassage vanilla ne sait produire que des
 * potions (addMix) ou changer le contenant en gardant la potion
 * (addContainerRecipe). Les deux passent par PotionContents.createItemStack(),
 * donc la fiole obtenue porterait un component "potion_contents" invisible et
 * ne s'empilerait pas avec les vraies fioles d'XP vanilla.
 *
 * On se greffe donc directement sur les trois methodes que l'alambic
 * interroge, pour rendre une fiole d'XP parfaitement vanilla.
 */
@Mixin(PotionBrewing.class)
public class PotionBrewingMixin {

	/** Accepte la baie dans le slot du haut (l'ingredient). */
	@Inject(method = "isIngredient", at = @At("HEAD"), cancellable = true)
	private void arcamod$acceptXpBerryAsIngredient(ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
		if (ArcaFeature.XP_BERRY_BREWING.isEnabled() && ingredient.is(ModItems.XP_BERRY)) {
			cir.setReturnValue(true);
		}
	}

	/** Declare que "bouteille d'eau + baie" est une combinaison brassable. */
	@Inject(method = "hasMix", at = @At("HEAD"), cancellable = true)
	private void arcamod$allowWaterBottleMix(ItemStack source, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
		if (ArcaFeature.XP_BERRY_BREWING.isEnabled() && ingredient.is(ModItems.XP_BERRY) && isWaterBottle(source)) {
			cir.setReturnValue(true);
		}
	}

	/** Produit le resultat : une fiole d'XP vanilla, sans aucun component. */
	@Inject(method = "mix", at = @At("HEAD"), cancellable = true)
	private void arcamod$mixIntoExperienceBottle(ItemStack ingredient, ItemStack source, CallbackInfoReturnable<ItemStack> cir) {
		if (ArcaFeature.XP_BERRY_BREWING.isEnabled() && ingredient.is(ModItems.XP_BERRY) && isWaterBottle(source)) {
			cir.setReturnValue(new ItemStack(Items.EXPERIENCE_BOTTLE));
		}
	}

	/** Une bouteille d'eau = l'item POTION dont le contenu est la potion "water". */
	private static boolean isWaterBottle(ItemStack stack) {
		if (!stack.is(Items.POTION)) {
			return false;
		}
		PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
		return contents.potion().filter(potion -> potion.is(PotionIds.WATER)).isPresent();
	}
}
