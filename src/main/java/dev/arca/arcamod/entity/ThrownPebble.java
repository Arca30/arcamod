package dev.arca.arcamod.entity;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.block.PebblesBlock;
import dev.arca.arcamod.registry.ModBlocks;
import dev.arca.arcamod.registry.ModEntities;
import dev.arca.arcamod.registry.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * La petite pierre lancee.
 *
 * Copie de la boule de neige, avec deux differences : elle fait des degats
 * (ArcaBalance.hearts(ArcaBalance.PEBBLE_DAMAGE_HEARTS)) et surtout elle ne se volatilise pas, elle se
 * repose au sol sous forme de bloc la ou elle atterrit. Si vraiment aucune
 * place n'est libre, elle retombe en item (reglable).
 */
public class ThrownPebble extends ThrowableItemProjectile {

	/** Nombre d'essais de repositionnement autour d'un mob touche. */
	private static final int SCATTER_ATTEMPTS = 24;

	public ThrownPebble(EntityType<? extends ThrownPebble> type, Level level) {
		super(type, level);
	}

	/** Lancer par un joueur ou un mob. */
	public ThrownPebble(Level level, LivingEntity owner, ItemStack stack) {
		super(ModEntities.THROWN_PEBBLE, owner, level, stack);
	}

	/** Tir par un distributeur. */
	public ThrownPebble(Level level, double x, double y, double z, ItemStack stack) {
		super(ModEntities.THROWN_PEBBLE, x, y, z, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.PEBBLE;
	}

	// ------------------------------------------------------------------
	// Impacts
	// ------------------------------------------------------------------

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		super.onHitEntity(hitResult);

		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		Entity target = hitResult.getEntity();
		target.hurtServer(serverLevel, this.damageSources().thrown(this, this.getOwner()), ArcaBalance.hearts(ArcaBalance.PEBBLE_DAMAGE_HEARTS));

		// La pierre retombe a cote du mob touche plutot que de disparaitre.
		if (ArcaBalance.PEBBLE_LANDS_AS_BLOCK && !this.scatterAround(target.blockPosition())) {
			this.dropAsItem();
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult hitResult) {
		super.onHitBlock(hitResult);

		if (this.level().isClientSide() || !ArcaBalance.PEBBLE_LANDS_AS_BLOCK) {
			return;
		}

		// La face touchee donne le point de depart : le dessus d'un bloc, ou
		// la case d'a cote si on a tape un mur. On laisse ensuite "tomber" la
		// pierre jusqu'au premier sol valide.
		if (!this.settleFrom(hitResult.getBlockPos().relative(hitResult.getDirection()))) {
			this.dropAsItem();
		}
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);

		if (!this.level().isClientSide()) {
			this.level().broadcastEntityEvent(this, (byte) 3);
			this.discard();
		}
	}

	// ------------------------------------------------------------------
	// Repose de la pierre
	// ------------------------------------------------------------------

	/** Descend depuis "from" jusqu'a trouver un endroit ou poser la pierre. */
	private boolean settleFrom(BlockPos from) {
		PebblesBlock pebbles = ModBlocks.PEBBLES;

		for (int drop = 0; drop <= ArcaBalance.PEBBLE_MAX_FALL_SEARCH; drop++) {
			BlockPos candidate = from.below(drop);

			if (pebbles.canPlaceAt(this.level(), candidate)) {
				return pebbles.placeOne(this.level(), candidate, this.random);
			}

			// On ne traverse pas un bloc plein : inutile de continuer a
			// chercher plus bas.
			BlockState state = this.level().getBlockState(candidate);
			if (!state.isAir() && !state.canBeReplaced()) {
				return false;
			}
		}

		return false;
	}

	/** Cherche une place libre dans un rayon autour d'un mob touche. */
	private boolean scatterAround(BlockPos center) {
		int radius = ArcaBalance.PEBBLE_MOB_SCATTER_RADIUS;

		if (radius <= 0) {
			return false;
		}

		PebblesBlock pebbles = ModBlocks.PEBBLES;

		for (int attempt = 0; attempt < SCATTER_ATTEMPTS; attempt++) {
			BlockPos candidate = center.offset(
					this.random.nextInt(radius * 2 + 1) - radius,
					this.random.nextInt(radius + 1) - radius / 2,
					this.random.nextInt(radius * 2 + 1) - radius);

			if (pebbles.canPlaceAt(this.level(), candidate)) {
				return pebbles.placeOne(this.level(), candidate, this.random);
			}
		}

		return false;
	}

	/** Dernier recours : la pierre tombe au sol en tant qu'item. */
	private void dropAsItem() {
		if (!ArcaBalance.PEBBLE_DROP_ITEM_IF_NO_ROOM) {
			return;
		}

		ItemStack stack = this.getItem().copyWithCount(1);

		if (stack.isEmpty()) {
			stack = new ItemStack(ModItems.PEBBLE);
		}

		this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), stack));
	}

	// ------------------------------------------------------------------
	// Rendu de l'impact (particules), cote client
	// ------------------------------------------------------------------

	private ParticleOptions getParticle() {
		ItemStack stack = this.getItem();
		return stack.isEmpty()
				? ParticleTypes.ITEM_SNOWBALL
				: new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(stack));
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == 3) {
			ParticleOptions particle = this.getParticle();

			for (int i = 0; i < 8; i++) {
				this.level().addParticle(particle, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
			}
		}
	}
}
