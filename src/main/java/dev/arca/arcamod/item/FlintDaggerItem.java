package dev.arca.arcamod.item;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.entity.ThrownDagger;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;

/**
 * La dague en silex.
 *
 * Clic gauche : une attaque d'epee, plus forte et plus rapide (voir
 * ArcaBalance.DAGGER_*). Clic droit : on la lance.
 *
 * Contrairement au trident, le lancer est immediat - pas besoin de charger.
 */
public class FlintDaggerItem extends Item implements ProjectileItem {

	public FlintDaggerItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		// Une dague sur le point de casser ne se lance pas : elle serait
		// detruite en vol et le joueur la perdrait sans comprendre.
		if (stack.nextDamageWillBreak()) {
			return InteractionResult.FAIL;
		}

		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS,
				1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

		if (level instanceof ServerLevel serverLevel) {
			stack.hurtWithoutBreaking(ArcaBalance.DAGGER_THROW_DURABILITY_COST, player);

			// consumeAndReturn retire la dague de la main et renvoie la pile
			// a mettre dans le projectile : c'est elle qu'on recuperera au
			// sol, avec sa durabilite et ses enchantements.
			ItemStack thrown = stack.consumeAndReturn(1, player);
			ThrownDagger dagger = Projectile.spawnProjectileFromRotation(ThrownDagger::new, serverLevel, thrown, player,
					0.0F, ArcaBalance.DAGGER_THROW_POWER, ArcaBalance.DAGGER_THROW_INACCURACY);

			if (player.hasInfiniteMaterials()) {
				dagger.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
			}
		}

		if (ArcaBalance.DAGGER_THROW_COOLDOWN_TICKS > 0) {
			player.getCooldowns().addCooldown(stack, ArcaBalance.DAGGER_THROW_COOLDOWN_TICKS);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResult.SUCCESS;
	}

	/** Permet aux distributeurs de lancer des dagues. */
	@Override
	public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
		ThrownDagger dagger = new ThrownDagger(level, position.x(), position.y(), position.z(), stack.copyWithCount(1));
		dagger.pickup = AbstractArrow.Pickup.ALLOWED;
		return dagger;
	}
}
