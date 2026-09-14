package dev.arca.arcamod.item;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.config.ArcaFeature;
import dev.arca.arcamod.entity.ThrownPebble;

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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * La petite pierre.
 *
 * Elle se comporte comme une boule de neige : un clic droit la lance. Mais
 * c'est aussi un BlockItem : en s'accroupissant, le clic droit sur un bloc
 * pose (ou agrandit) un tas de pierres, ce qui evite de devoir viser au lancer
 * quand on veut decorer.
 */
public class PebbleItem extends BlockItem implements ProjectileItem {

	public PebbleItem(Block block, Properties properties) {
		super(block, properties);
	}

	/**
	 * Accroupi = on pose le bloc (comportement BlockItem normal).
	 * Debout = on renvoie PASS, et le jeu enchaine sur use() qui lance.
	 */
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();

		if (player != null && player.isSecondaryUseActive()) {
			return super.useOn(context);
		}

		return InteractionResult.PASS;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!ArcaFeature.PEBBLE_THROWING.isEnabled()) {
			return InteractionResult.PASS;
		}

		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL,
				0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

		if (level instanceof ServerLevel serverLevel) {
			Projectile.spawnProjectileFromRotation(ThrownPebble::new, serverLevel, stack, player,
					0.0F, ArcaBalance.PEBBLE_THROW_POWER, ArcaBalance.PEBBLE_THROW_INACCURACY);
		}

		if (ArcaBalance.PEBBLE_COOLDOWN_TICKS > 0) {
			player.getCooldowns().addCooldown(stack, ArcaBalance.PEBBLE_COOLDOWN_TICKS);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}

	/** Permet aux distributeurs de lancer des pierres. */
	@Override
	public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
		return new ThrownPebble(level, position.x(), position.y(), position.z(), stack);
	}
}
