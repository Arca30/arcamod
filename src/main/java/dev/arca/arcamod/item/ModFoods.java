package dev.arca.arcamod.item;

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

	// ---- reglages -------------------------------------------------------

	private static final int MINUTE = 60 * 20; // en ticks

	public static final int HASTE_DURATION = 3 * MINUTE;
	public static final int HASTE_AMPLIFIER = 0; // 0 = Celerite I

	public static final int ENCHANTED_HASTE_DURATION = 8 * MINUTE;
	public static final int ENCHANTED_HASTE_AMPLIFIER = 1; // 1 = Celerite II

	// ---- betterave doree ------------------------------------------------

	public static final FoodProperties GOLDEN_BEETROOT = new FoodProperties.Builder()
			.nutrition(4)
			.saturationModifier(0.6F)
			.alwaysEdible() // mangeable meme le ventre plein, comme la pomme doree
			.build();

	public static final Consumable GOLDEN_BEETROOT_CONSUMABLE = Consumables.defaultFood()
			.onConsume(new ApplyStatusEffectsConsumeEffect(
					new MobEffectInstance(MobEffects.HASTE, HASTE_DURATION, HASTE_AMPLIFIER)))
			.build();

	// ---- betterave doree enchantee --------------------------------------

	public static final FoodProperties ENCHANTED_GOLDEN_BEETROOT = new FoodProperties.Builder()
			.nutrition(4)
			.saturationModifier(1.2F)
			.alwaysEdible()
			.build();

	public static final Consumable ENCHANTED_GOLDEN_BEETROOT_CONSUMABLE = Consumables.defaultFood()
			.onConsume(new ApplyStatusEffectsConsumeEffect(
					new MobEffectInstance(MobEffects.HASTE, ENCHANTED_HASTE_DURATION, ENCHANTED_HASTE_AMPLIFIER)))
			.build();

	private ModFoods() {
	}
}
