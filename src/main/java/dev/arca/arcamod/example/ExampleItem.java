package dev.arca.arcamod.example;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * ============================================================
 *  ITEM D'EXEMPLE - a copier pour creer tes propres items
 * ============================================================
 *
 * CE QU'IL FAIT
 *   - clic droit en l'air : donne un effet au joueur, use de la durabilite,
 *     declenche un temps de recharge et lache des particules ;
 *   - clic droit sur un bloc en etant accroupi : ne fait rien de special,
 *     mais montre ou brancher ce genre de comportement.
 *
 * LES QUATRE FICHIERS D'UN ITEM
 *   1. cette classe Java (facultative : un item sans comportement se
 *      contente de Item::new) ;
 *   2. son enregistrement dans ModExamples ;
 *   3. assets/arcamod/models/item/<nom>.json + assets/arcamod/items/<nom>.json ;
 *   4. assets/arcamod/textures/item/<nom>.png et la traduction dans lang/.
 */
public class ExampleItem extends Item {

	// =====================================================================
	// REGLAGES
	// =====================================================================

	/** Temps de recharge apres usage, en ticks. 0 = aucun. */
	public static final int COOLDOWN_TICKS = 60;

	/** Durabilite consommee a chaque usage. 0 = l'item ne s'use pas. */
	public static final int DURABILITY_COST = 1;

	/** Effet donne et sa duree, en ticks. */
	public static final int EFFECT_DURATION_TICKS = 200;
	public static final int EFFECT_AMPLIFIER = 0;

	/** Nombre de particules au moment de l'usage. */
	public static final int PARTICLE_COUNT = 12;

	public ExampleItem(Properties properties) {
		super(properties);
	}

	/**
	 * Clic droit en l'air (ou sur un bloc qui ne repond pas).
	 *
	 * Attention a l'ordre : le jeu appelle d'abord useOn(), et seulement si
	 * celui-ci renvoie PASS il appelle use().
	 */
	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		// Cote serveur : tout ce qui compte vraiment.
		if (level instanceof ServerLevel serverLevel) {
			player.addEffect(new MobEffectInstance(MobEffects.SPEED, EFFECT_DURATION_TICKS, EFFECT_AMPLIFIER));

			if (DURABILITY_COST > 0) {
				// hurtAndBreak gere tout : Solidite, casse, son. Le dernier
				// argument sert a reagir a la casse de l'item.
				stack.hurtAndBreak(DURABILITY_COST, player, EquipmentSlot.MAINHAND);
			}

			serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
					player.getX(), player.getY() + 1.0, player.getZ(),
					PARTICLE_COUNT, 0.4, 0.4, 0.4, 0.0);
		}

		if (COOLDOWN_TICKS > 0) {
			// Le temps de recharge s'affiche tout seul sur l'icone.
			player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
		}

		// Pour les statistiques et les avancements.
		player.awardStat(Stats.ITEM_USED.get(this));

		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.0F);

		return InteractionResult.SUCCESS;
	}

	/**
	 * Clic droit SUR UN BLOC.
	 *
	 * Renvoyer PASS laisse la main a use() : c'est ce qu'on veut ici. Pour
	 * poser quelque chose ou modifier le bloc vise, c'est ici qu'il faut
	 * ecrire, en s'aidant de context.getClickedPos() et
	 * context.getClickedFace().
	 */
	@Override
	public InteractionResult useOn(UseOnContext context) {
		return InteractionResult.PASS;
	}
}
