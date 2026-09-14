package dev.arca.arcamod.mixin;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.config.ArcaFeature;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.BlockHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Une fleche a effet laisse une flaque de potion la ou elle se plante.
 *
 * C'est exactement le nuage d'une potion persistante, mais declenche par un
 * tir : on garde les effets de la fleche, et le rayon comme la duree se
 * reglent dans ArcaBalance (ARROW_CLOUD_*).
 *
 * On vise AbstractArrow (c'est elle qui declare onHitBlock) mais on ne reagit
 * que pour les vraies fleches : ni trident, ni fleche spectrale.
 */
@Mixin(AbstractArrow.class)
public class AbstractArrowMixin {

	@Inject(method = "onHitBlock", at = @At("TAIL"))
	private void arcamod$leaveLingeringCloud(BlockHitResult hitResult, CallbackInfo ci) {
		AbstractArrow self = (AbstractArrow) (Object) this;

		if (!ArcaFeature.TIPPED_ARROW_CLOUDS.isEnabled() || !(self instanceof Arrow)
				|| !(self.level() instanceof ServerLevel level)) {
			return;
		}

		// Les effets d'une fleche a effet sont portes par l'item qu'elle
		// rendrait si on la ramassait.
		PotionContents contents = self.getPickupItemStackOrigin().get(DataComponents.POTION_CONTENTS);

		if (contents == null || contents.equals(PotionContents.EMPTY)) {
			return;
		}

		AreaEffectCloud cloud = new AreaEffectCloud(level, self.getX(), self.getY(), self.getZ());
		cloud.setPotionContents(contents);
		cloud.setRadius(ArcaBalance.ARROW_CLOUD_RADIUS);
		cloud.setDuration(ArcaBalance.ARROW_CLOUD_DURATION_TICKS);
		// Retrecissement par tick : le nuage se resorbe doucement. Calcule
		// pour disparaitre pile a la fin de la duree.
		cloud.setRadiusPerTick(-ArcaBalance.ARROW_CLOUD_RADIUS
				/ Math.max(1.0F, ArcaBalance.ARROW_CLOUD_DURATION_TICKS));
		cloud.setPotionDurationScale(ArcaBalance.ARROW_CLOUD_EFFECT_SCALE);
		cloud.setWaitTime(ArcaBalance.ARROW_CLOUD_WAIT_TICKS);

		Entity owner = self.getOwner();

		if (owner instanceof LivingEntity living) {
			cloud.setOwner(living);
		}

		level.addFreshEntity(cloud);
	}
}
