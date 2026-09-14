package dev.arca.arcamod.item;

import dev.arca.arcamod.ArcaBalance;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

/**
 * Definitions de nourriture du mod.
 *
 * Depuis 1.21.2, une nourriture se decrit en DEUX morceaux :
 *   - FoodProperties : ce qu'elle rend (faim, saturation) ;
 *   - Consumable     : comment on la mange (duree, son, animation) et les
 *                      effets appliques a la fin.
 * Le vanilla fait exactement ce decoupage dans Foods et Consumables.
 */
public final class ModFoods {

	// Reglages : ArcaBalance.GOLDEN_BEETROOT_* et ENCHANTED_GOLDEN_BEETROOT_*.

	// ---- betterave doree ------------------------------------------------

	public static final FoodProperties GOLDEN_BEETROOT = new FoodProperties.Builder()
			.nutrition(ArcaBalance.GOLDEN_BEETROOT_NUTRITION)
			.saturationModifier(ArcaBalance.GOLDEN_BEETROOT_SATURATION)
			.alwaysEdible() // mangeable meme le ventre plein, comme la pomme doree
			.build();

	public static final Consumable GOLDEN_BEETROOT_CONSUMABLE = Consumables.defaultFood()
			.onConsume(new ApplyStatusEffectsConsumeEffect(
					new MobEffectInstance(MobEffects.HASTE,
							ArcaBalance.seconds(ArcaBalance.GOLDEN_BEETROOT_HASTE_SECONDS),
							ArcaBalance.GOLDEN_BEETROOT_HASTE_LEVEL)))
			.build();

	// ---- betterave doree enchantee --------------------------------------

	public static final FoodProperties ENCHANTED_GOLDEN_BEETROOT = new FoodProperties.Builder()
			.nutrition(ArcaBalance.ENCHANTED_GOLDEN_BEETROOT_NUTRITION)
			.saturationModifier(ArcaBalance.ENCHANTED_GOLDEN_BEETROOT_SATURATION)
			.alwaysEdible()
			.build();

	public static final Consumable ENCHANTED_GOLDEN_BEETROOT_CONSUMABLE = Consumables.defaultFood()
			.onConsume(new ApplyStatusEffectsConsumeEffect(
					new MobEffectInstance(MobEffects.HASTE,
							ArcaBalance.seconds(ArcaBalance.ENCHANTED_GOLDEN_BEETROOT_HASTE_SECONDS),
							ArcaBalance.ENCHANTED_GOLDEN_BEETROOT_HASTE_LEVEL)))
			.build();

	private ModFoods() {
	}
}
