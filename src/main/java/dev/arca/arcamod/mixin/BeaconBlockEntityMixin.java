package dev.arca.arcamod.mixin;

import java.util.Objects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

import org.jspecify.annotations.Nullable;

/**
 * Un bloc de netherite pose directement sous une balise etend sa portee de
 * 1000 blocs.
 *
 * La netherite fait deja partie des blocs de base acceptes par une balise :
 * poser ce bloc juste sous elle est donc un enchainement naturel.
 */
@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {

	@Unique
	private static final double ARCAMOD_BONUS_RANGE = 1000.0;

	/**
	 * On remplace entierement applyEffects() quand la netherite est presente.
	 *
	 * Pourquoi ne pas juste agrandir la variable "range" du vanilla : celui-ci
	 * construit une AABB et appelle getEntitiesOfClass(), qui parcourt toutes
	 * les sections d'entites contenues dans la boite. Avec 1000 blocs de rayon
	 * ca represente des dizaines de milliers de sections a chaque application,
	 * soit un serveur a genoux. On boucle donc sur la liste des joueurs, qui
	 * est minuscule.
	 */
	@Inject(method = "applyEffects", at = @At("HEAD"), cancellable = true)
	private static void arcamod$netheriteRange(Level level, BlockPos worldPosition, int levels,
			@Nullable Holder<MobEffect> primaryPower, @Nullable Holder<MobEffect> secondaryPower, CallbackInfo ci) {

		if (level.isClientSide() || primaryPower == null) {
			return;
		}
		if (!level.getBlockState(worldPosition.below()).is(Blocks.NETHERITE_BLOCK)) {
			return; // pas de netherite : comportement vanilla
		}

		double range = levels * 10 + 10 + ARCAMOD_BONUS_RANGE;
		double rangeSq = range * range;
		int amplifier = levels >= 4 && Objects.equals(primaryPower, secondaryPower) ? 1 : 0;
		int durationTicks = (9 + levels * 2) * 20;
		boolean hasSecondary = levels >= 4 && secondaryPower != null
				&& !Objects.equals(primaryPower, secondaryPower);

		double cx = worldPosition.getX() + 0.5;
		double cz = worldPosition.getZ() + 0.5;

		for (Player player : level.players()) {
			// Distance horizontale seulement, comme le vanilla dont la boite
			// s'etend sur toute la hauteur du monde.
			double dx = player.getX() - cx;
			double dz = player.getZ() - cz;
			if (dx * dx + dz * dz > rangeSq) {
				continue;
			}

			player.addEffect(new MobEffectInstance(primaryPower, durationTicks, amplifier, true, true));
			if (hasSecondary) {
				player.addEffect(new MobEffectInstance(secondaryPower, durationTicks, 0, true, true));
			}
		}

		ci.cancel();
	}
}
