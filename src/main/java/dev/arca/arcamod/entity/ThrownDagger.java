package dev.arca.arcamod.entity;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.registry.ModEntities;
import dev.arca.arcamod.registry.ModItems;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * La dague en silex lancee.
 *
 * Elle herite de la fleche : trajectoire, gravite, plantage dans les blocs,
 * ramassage et orientation viennent de la. Le rendu (ThrownDaggerRenderer,
 * cote client) l'oriente comme une fleche, lame en avant.
 *
 * Ce qui est ajoute ici : plantee dans une creature, elle la suit jusqu'a sa
 * mort. L'identifiant de la cible et le decalage sont synchronises, donc le
 * client refait le suivi lui-meme, image par image : sans ca la dague
 * avancerait par a-coups, au rythme des paquets reseau.
 *
 * Note : l'accrochage n'est pas sauvegarde. Si le monde est quitte avec une
 * dague plantee dans un mob, elle se decroche au rechargement et tombe au
 * sol - rien n'est perdu.
 */
public class ThrownDagger extends AbstractArrow implements ItemSupplier {

	private static final int NO_TARGET = -1;

	private static final EntityDataAccessor<Integer> STUCK_TARGET =
			SynchedEntityData.defineId(ThrownDagger.class, EntityDataSerializers.INT);

	/** Position de la dague par rapport a la cible, figee a l'impact. */
	private static final EntityDataAccessor<Vector3fc> STUCK_OFFSET =
			SynchedEntityData.defineId(ThrownDagger.class, EntityDataSerializers.VECTOR3);

	public ThrownDagger(EntityType<? extends ThrownDagger> type, Level level) {
		super(type, level);
	}

	/** Lancee par un joueur ou un mob. */
	public ThrownDagger(Level level, LivingEntity owner, ItemStack daggerStack) {
		super(ModEntities.THROWN_DAGGER, owner, level, daggerStack, null);
	}

	/** Tiree par un distributeur. */
	public ThrownDagger(Level level, double x, double y, double z, ItemStack daggerStack) {
		super(ModEntities.THROWN_DAGGER, x, y, z, level, daggerStack, null);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(STUCK_TARGET, NO_TARGET);
		entityData.define(STUCK_OFFSET, new Vector3f());
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(ModItems.FLINT_DAGGER);
	}

	@Override
	public ItemStack getItem() {
		// Sert au rendu : la dague s'affiche comme son item.
		ItemStack stack = this.getPickupItemStackOrigin();
		return stack.isEmpty() ? new ItemStack(ModItems.FLINT_DAGGER) : stack;
	}

	@Override
	public ItemStack getWeaponItem() {
		return this.getPickupItemStackOrigin();
	}

	@Override
	protected SoundEvent getDefaultHitGroundSoundEvent() {
		return SoundEvents.TRIDENT_HIT_GROUND;
	}

	/** La creature dans laquelle la dague est plantee, ou null. */
	private Entity stuckTarget() {
		int id = this.entityData.get(STUCK_TARGET);
		return id == NO_TARGET ? null : this.level().getEntity(id);
	}

	public boolean isStuckInEntity() {
		return this.entityData.get(STUCK_TARGET) != NO_TARGET;
	}

	@Override
	public void tick() {
		if (!this.isStuckInEntity()) {
			super.tick();
			return;
		}

		Entity target = this.stuckTarget();

		if (target == null || target.isRemoved() || !target.isAlive()) {
			// Le serveur fait tomber la dague ; le client attend simplement
			// que la disparition lui soit annoncee.
			if (!this.level().isClientSide()) {
				this.dropToGround();
			}

			return;
		}

		// Cas normal : la dague est "passagere" de sa cible. C'est la cible
		// qui la repositionne apres s'etre deplacee (positionRider), donc le
		// mouvement est aussi fluide que celui du mob, sans un tick de
		// retard ni saccade reseau. Rien a faire ici.
		if (this.isPassenger()) {
			return;
		}

		// Repli, si la cible refuse les passagers : suivi a la main.
		this.setOldPosAndRot();
		this.setDeltaMovement(Vec3.ZERO);
		Vector3fc offset = this.entityData.get(STUCK_OFFSET);
		this.setPos(target.position().add(offset.x(), offset.y(), offset.z()));
	}

	/**
	 * Ou la dague se place par rapport a sa monture.
	 *
	 * Minecraft calcule la position d'un passager comme
	 * "point d'accroche du vehicule moins point d'accroche du passager" : on
	 * renvoie donc la difference qui remet la dague exactement la ou elle
	 * s'est plantee.
	 */
	@Override
	public Vec3 getVehicleAttachmentPoint(Entity vehicle) {
		Vector3fc offset = this.entityData.get(STUCK_OFFSET);
		return vehicle.getPassengerRidingPosition(this).subtract(vehicle.position())
				.subtract(offset.x(), offset.y(), offset.z());
	}

	/**
	 * Une dague plantee dans un mob sort souvent de son cube de visibilite :
	 * sans ca, elle clignote ou disparait selon l'angle de la camera.
	 */
	@Override
	public boolean shouldRender(double camX, double camY, double camZ) {
		return true;
	}

	/**
	 * Plantage dans un bloc : on enfonce legerement la lame dans la surface
	 * touchee.
	 *
	 * Sans ca, la dague s'arrete pile sur la face du bloc et, sur un tir
	 * horizontal qui finit au sol, elle a l'air posee dessus plutot que
	 * plantee dedans.
	 */
	@Override
	protected void onHitBlock(BlockHitResult hitResult) {
		super.onHitBlock(hitResult);

		if (ArcaBalance.DAGGER_EMBED_DEPTH <= 0.0) {
			return;
		}

		Vec3 intoTheBlock = Vec3.atLowerCornerOf(hitResult.getDirection().getUnitVec3i())
				.scale(-ArcaBalance.DAGGER_EMBED_DEPTH);
		this.setPos(this.position().add(intoTheBlock));
		this.setOldPosAndRot();
	}

	/** Fait tomber la dague en item la ou elle se trouve, puis disparait. */
	private void dropToGround() {
		if (this.level() instanceof ServerLevel && this.pickup == Pickup.ALLOWED) {
			ItemStack stack = this.getPickupItem();

			if (!stack.isEmpty()) {
				this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), stack));
			}
		}

		this.discard();
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult) {
		Entity target = hitResult.getEntity();
		Entity owner = this.getOwner();
		DamageSource source = this.damageSources().arrow(this, owner == null ? this : owner);
		float damage = ArcaBalance.hearts(ArcaBalance.DAGGER_THROWN_DAMAGE_HEARTS);

		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		boolean hurt = target.hurtServer(serverLevel, source, damage);

		if (hurt && target instanceof LivingEntity living) {
			this.doKnockback(living, source);
			this.doPostHurtEffects(living);
		}

		this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);

		// Une creature blessee garde la dague plantee dedans jusqu'a sa mort
		// (sauf si DAGGER_DROPS_AFTER_HIT est vrai). Tout le reste (bateau,
		// cadre, cible invulnerable) la fait tomber au sol : sans ca la dague
		// resterait en vol et retoucherait sa cible a chaque tick.
		if (!ArcaBalance.DAGGER_DROPS_AFTER_HIT && hurt
				&& target instanceof LivingEntity living && living.isAlive()) {
			this.stickInto(living);
		} else {
			this.dropToGround();
		}
	}

	private void stickInto(LivingEntity target) {
		// On s'arrete juste avant le centre de la cible, sinon la lame
		// disparait entierement dans le mob.
		Vec3 offset = this.position().subtract(target.position());

		this.setDeltaMovement(Vec3.ZERO);
		this.setNoPhysics(true);
		this.entityData.set(STUCK_OFFSET, new Vector3f((float) offset.x, (float) offset.y, (float) offset.z));
		this.entityData.set(STUCK_TARGET, target.getId());

		// force = true : meme les mobs qui refusent normalement un passager
		// acceptent la dague. En cas d'echec, tick() bascule sur le suivi
		// manuel.
		this.startRiding(target, true, false);
	}

	/**
	 * Une dague plantee dans le sol attend son proprietaire aussi longtemps
	 * qu'il faut, contrairement a une fleche qui disparait au bout d'une
	 * minute.
	 */
	@Override
	protected void tickDespawn() {
		if (this.pickup != Pickup.ALLOWED) {
			super.tickDespawn();
		}
	}
}
