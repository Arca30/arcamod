package dev.arca.arcamod.item;

import java.util.List;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.registry.ModEffects;

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

	// ---- piment des ames ------------------------------------------------

	public static final FoodProperties SOUL_PEPPER = new FoodProperties.Builder()
			.nutrition(ArcaBalance.SOUL_PEPPER_NUTRITION)
			.saturationModifier(ArcaBalance.SOUL_PEPPER_SATURATION)
			.alwaysEdible() // on le mange pour son effet, meme rassasie
			.build();

	public static final Consumable SOUL_PEPPER_CONSUMABLE = Consumables.defaultFood()
			.onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
					new MobEffectInstance(ModEffects.FIERY_STRIKES, ArcaBalance.SOUL_PEPPER_FIRE_ASPECT_TICKS),
					new MobEffectInstance(MobEffects.FIRE_RESISTANCE, ArcaBalance.SOUL_PEPPER_FIRE_RESISTANCE_TICKS))))
			.build();

	// ---- gousse a pichet (pitcher pod) ----------------------------------

	/**
	 * La gousse crue : 1 gigot, avalee deux fois plus vite qu'un aliment
	 * normal, et elle purge une partie des effets en cours (30 s par gousse).
	 *
	 * C'est la plante carnivore qui digere a ta place : les reglages sont
	 * dans ArcaBalance section 51.
	 */
	public static final FoodProperties PITCHER_POD = buildPitcherPod();

	public static final Consumable PITCHER_POD_CONSUMABLE = Consumables.defaultFood()
			.consumeSeconds(ArcaBalance.PITCHER_POD_CONSUME_SECONDS)
			.onConsume(new ShortenEffectsConsumeEffect(
					ArcaBalance.PITCHER_POD_EFFECT_REDUCTION_SECONDS,
					ArcaBalance.PITCHER_POD_ONLY_SHORTENS_HARMFUL))
			.build();

	private static FoodProperties buildPitcherPod() {
		FoodProperties.Builder builder = new FoodProperties.Builder()
				.nutrition(ArcaBalance.PITCHER_POD_NUTRITION)
				.saturationModifier(ArcaBalance.PITCHER_POD_SATURATION);

		if (ArcaBalance.PITCHER_POD_ALWAYS_EDIBLE) {
			builder.alwaysEdible();
		}

		return builder.build();
	}

	private ModFoods() {
	}
}
