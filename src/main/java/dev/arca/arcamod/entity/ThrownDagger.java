package dev.arca.arcamod.entity;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.registry.ModEntities;
import dev.arca.arcamod.registry.ModItems;

import net.minecraft.core.BlockPos;
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
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.AABB;
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
 * Plantage dans un bloc : c'est le SERVEUR qui decide de la pose finale
 * (position exacte + orientation) et la synchronise par entityData. Le client
 * predit l'impact comme une fleche, puis se cale au millimetre sur la pose du
 * serveur. Sans ca, les paquets de position/vitesse des fleches (arrondis,
 * parfois en retard d'un tick) deplacent la dague juste apres l'impact : elle
 * "se teleporte" et se replante un peu plus loin.
 *
 * Ce qui est ajoute aussi : plantee dans une creature, elle la suit jusqu'a sa
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

	/**
	 * Position de la dague par rapport a la cible, figee a l'impact, exprimee
	 * dans le repere du CORPS de la cible (rotation du corps annulee). On la
	 * refait tourner avec le corps a chaque tick : la dague reste du bon cote
	 * quand le mob pivote.
	 */
	private static final EntityDataAccessor<Vector3fc> STUCK_OFFSET =
			SynchedEntityData.defineId(ThrownDagger.class, EntityDataSerializers.VECTOR3);

	/** Vrai quand le serveur a fige la dague dans un bloc. */
	private static final EntityDataAccessor<Boolean> STUCK_IN_BLOCK =
			SynchedEntityData.defineId(ThrownDagger.class, EntityDataSerializers.BOOLEAN);

	/**
	 * Pose figee dans le bloc : le bloc, plus la position a l'interieur de ce
	 * bloc (0..1). Decoupee ainsi, elle reste precise en float meme loin du
	 * centre du monde.
	 */
	private static final EntityDataAccessor<BlockPos> STUCK_BLOCK =
			SynchedEntityData.defineId(ThrownDagger.class, EntityDataSerializers.BLOCK_POS);
	private static final EntityDataAccessor<Vector3fc> STUCK_LOCAL_POS =
			SynchedEntityData.defineId(ThrownDagger.class, EntityDataSerializers.VECTOR3);

	/**
	 * Orientation figee : x = yRot, y = xRot.
	 * Plantee dans un mob, x est relatif a la rotation de son corps.
	 */
	private static final EntityDataAccessor<Vector3fc> STUCK_ROTATION =
			SynchedEntityData.defineId(ThrownDagger.class, EntityDataSerializers.VECTOR3);

	/**
	 * Distance, en blocs, a laquelle on mesure la lumiere derriere la pointe
	 * (vers le manche). Sans ca, la lumiere est lue a l'interieur du bloc
	 * touche : une dague plantee dans un mur s'affiche toute noire.
	 */
	private static final double LIGHT_PROBE_BACK_DISTANCE = 0.35;

	/** Petite marge pour rester a l'interieur du bloc touche. */
	private static final double INSIDE_FACE_EPSILON = 0.02;

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
		entityData.define(STUCK_IN_BLOCK, false);
		entityData.define(STUCK_BLOCK, BlockPos.ZERO);
		entityData.define(STUCK_LOCAL_POS, new Vector3f());
		entityData.define(STUCK_ROTATION, new Vector3f());
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
			this.tickInBlockOrFlying();
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
		// retard ni saccade reseau.
		// Repli, si la cible refuse les passagers : suivi a la main.
		if (!this.isPassenger()) {
			this.setOldPosAndRot();
			this.setDeltaMovement(Vec3.ZERO);
			this.setPos(target.position().add(this.worldOffset(target)));
		}

		// Dans les deux cas, la dague pivote avec le corps de sa cible.
		Vector3fc rotation = this.entityData.get(STUCK_ROTATION);
		this.setYRot(rotation.x() - target.getVisualRotationYInDegrees());
		this.setXRot(rotation.y());
	}

	/** Decalage monde de la dague, tourne selon le corps actuel de la cible. */
	private Vec3 worldOffset(Entity target) {
		Vector3fc local = this.entityData.get(STUCK_OFFSET);
		return rotateYaw(new Vec3(local.x(), local.y(), local.z()), -target.getVisualRotationYInDegrees());
	}

	/**
	 * Tourne un vecteur autour de l'axe vertical, dans la convention d'angle
	 * de la fleche (direction = (sin a, cos a)) : (sin a, cos a) devient
	 * (sin(a + degrees), cos(a + degrees)).
	 *
	 * Un mob dont le corps tourne de +d degres fait tourner, dans cette
	 * convention, tout ce qui lui est attache de -d degres.
	 */
	private static Vec3 rotateYaw(Vec3 vector, float degrees) {
		double radians = degrees * Mth.DEG_TO_RAD;
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);
		return new Vec3(vector.x * cos + vector.z * sin, vector.y, -vector.x * sin + vector.z * cos);
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
		return vehicle.getPassengerRidingPosition(this).subtract(vehicle.position())
				.subtract(this.worldOffset(vehicle));
	}

	/**
	 * Point ou le rendu lit la lumiere : un peu en arriere de la pointe, le
	 * long de la lame, donc hors du bloc ou du mob dans lequel elle est
	 * plantee.
	 */
	@Override
	public Vec3 getLightProbePosition(float partialTickTime) {
		float yRot = this.getYRot(partialTickTime) * Mth.DEG_TO_RAD;
		float xRot = this.getXRot(partialTickTime) * Mth.DEG_TO_RAD;
		Vec3 forward = new Vec3(Mth.sin(yRot) * Mth.cos(xRot), Mth.sin(xRot), Mth.cos(yRot) * Mth.cos(xRot));
		return this.getPosition(partialTickTime).subtract(forward.scale(LIGHT_PROBE_BACK_DISTANCE));
	}

	/**
	 * Une dague plantee dans un mob sort souvent de son cube de visibilite :
	 * sans ca, elle clignote ou disparait selon l'angle de la camera.
	 */
	@Override
	public boolean shouldRender(double camX, double camY, double camZ) {
		return true;
	}

	/** Vol normal, ou dague plantee dans un bloc. */
	private void tickInBlockOrFlying() {
		boolean stuck = this.entityData.get(STUCK_IN_BLOCK);

		// Cote client, la pose du serveur fait foi : on s'y recale a chaque
		// tick, ce qui annule tout paquet de position/vitesse arrive entre
		// temps. Le super.tick() qui suit voit alors la dague dans son bloc
		// et la laisse immobile.
		if (stuck && this.level().isClientSide()) {
			this.applyStuckPose();
		}

		super.tick();

		// Cote serveur : le bloc a disparu et la dague retombe (startFalling
		// de la fleche). On libere la pose pour que le client la suive.
		if (stuck && !this.level().isClientSide() && !this.isInGround()) {
			this.entityData.set(STUCK_IN_BLOCK, false);
		}
	}

	private void applyStuckPose() {
		BlockPos block = this.entityData.get(STUCK_BLOCK);
		Vector3fc local = this.entityData.get(STUCK_LOCAL_POS);
		Vector3fc rotation = this.entityData.get(STUCK_ROTATION);
		Vec3 target = new Vec3(block.getX() + local.x(), block.getY() + local.y(), block.getZ() + local.z());

		if (this.position().distanceToSqr(target) > 1.0E-8) {
			this.setPos(target);
		}

		this.setYRot(rotation.x());
		this.setXRot(rotation.y());
		this.setDeltaMovement(Vec3.ZERO);
	}

	/** Fige la pose actuelle et l'envoie au client (serveur uniquement). */
	private void publishStuckPose() {
		if (this.level().isClientSide()) {
			return;
		}

		BlockPos block = this.blockPosition();
		Vec3 local = this.position().subtract(Vec3.atLowerCornerOf(block));
		this.entityData.set(STUCK_BLOCK, block);
		this.entityData.set(STUCK_LOCAL_POS, new Vector3f((float) local.x, (float) local.y, (float) local.z));
		this.entityData.set(STUCK_ROTATION, new Vector3f(this.getYRot(), this.getXRot(), 0.0F));
		this.entityData.set(STUCK_IN_BLOCK, true);
	}

	/**
	 * Oriente la dague exactement dans le sens du mouvement.
	 *
	 * En vol, la fleche ne tourne que de 20 % par tick vers sa direction
	 * (lerpRotation) : a l'impact, elle "regarde" encore un peu en arriere.
	 * On prend la vraie direction, ramenee au plus pres de l'angle actuel
	 * pour que l'interpolation d'image ne fasse pas un tour complet.
	 */
	private void faceMovement(Vec3 movement) {
		if (movement.lengthSqr() < 1.0E-7) {
			return;
		}

		float yRot = (float) (Mth.atan2(movement.x, movement.z) * Mth.RAD_TO_DEG);
		float xRot = (float) (Mth.atan2(movement.y, movement.horizontalDistance()) * Mth.RAD_TO_DEG);
		this.setYRot(this.getYRot() + Mth.wrapDegrees(yRot - this.getYRot()));
		this.setXRot(this.getXRot() + Mth.wrapDegrees(xRot - this.getXRot()));
	}

	/**
	 * Plantage dans un bloc.
	 *
	 * La fleche vanilla recule de 0.05 bloc hors de la face touchee : c'est
	 * ce qui donnait une dague "posee" au-dessus du sol. Ici, l'entite est
	 * placee a l'endroit exact de la POINTE de la lame (le rendu dessine la
	 * dague derriere ce point), enfoncee de DAGGER_EMBED_DEPTH le long de sa
	 * trajectoire, avec au moins DAGGER_EMBED_MIN_SURFACE_DEPTH sous la
	 * surface.
	 *
	 * Tout se fait dans le meme tick que le deplacement : l'interpolation
	 * d'image glisse donc naturellement de la derniere position de vol a la
	 * position plantee, sans saut.
	 */
	@Override
	protected void onHitBlock(BlockHitResult hitResult) {
		// A lire avant super : la fleche remet sa vitesse a zero.
		Vec3 movement = this.getDeltaMovement();

		super.onHitBlock(hitResult);

		this.setPos(this.embeddedTipPosition(hitResult, movement));
		this.faceMovement(movement);
		this.publishStuckPose();
	}

	private Vec3 embeddedTipPosition(BlockHitResult hitResult, Vec3 movement) {
		Vec3 hit = hitResult.getLocation();
		Vec3 intoFace = Vec3.atLowerCornerOf(hitResult.getDirection().getUnitVec3i()).scale(-1.0);
		Vec3 direction = movement.lengthSqr() < 1.0E-7 ? intoFace : movement.normalize();

		Vec3 tip = hit.add(direction.scale(Math.max(0.0, ArcaBalance.DAGGER_EMBED_DEPTH)));

		// Complement de profondeur pour les tirs rasants.
		double surfaceDepth = tip.subtract(hit).dot(intoFace);
		double missing = ArcaBalance.DAGGER_EMBED_MIN_SURFACE_DEPTH - surfaceDepth;

		if (missing > 0.0) {
			tip = tip.add(intoFace.scale(missing));
		}

		// La pointe doit rester dans la forme du bloc touche : sinon la
		// fleche croit etre en l'air (elle retomberait) ou se retrouve de
		// l'autre cote d'un bloc fin (vitre, barriere...). Repli : juste
		// sous la face touchee.
		if (!this.isInsideCollision(hitResult.getBlockPos(), tip)) {
			tip = hit.add(intoFace.scale(INSIDE_FACE_EPSILON));
		}

		return tip;
	}

	private boolean isInsideCollision(BlockPos pos, Vec3 point) {
		if (!BlockPos.containing(point).equals(pos)) {
			return false;
		}

		for (AABB box : this.level().getBlockState(pos).getCollisionShape(this.level(), pos).toAabbs()) {
			if (box.move(pos).contains(point)) {
				return true;
			}
		}

		return false;
	}

	/** Une dague sauvegardee plantee reprend sa pose au chargement. */
	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);

		if (this.isInGround()) {
			this.publishStuckPose();
		}
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
		Vec3 movement = this.getDeltaMovement();
		this.setPos(this.embeddedInEntity(target, movement));

		Vec3 offset = this.position().subtract(target.position());
		float bodyYaw = target.getVisualRotationYInDegrees();

		this.faceMovement(movement);
		this.setDeltaMovement(Vec3.ZERO);
		this.setNoPhysics(true);

		// On annule la rotation du corps pour ranger le decalage et l'angle
		// dans le repere du mob (voir worldOffset et tick).
		Vec3 local = rotateYaw(offset, bodyYaw);
		this.entityData.set(STUCK_OFFSET, new Vector3f((float) local.x, (float) local.y, (float) local.z));
		this.entityData.set(STUCK_ROTATION, new Vector3f(this.getYRot() + bodyYaw, this.getXRot(), 0.0F));
		this.entityData.set(STUCK_TARGET, target.getId());

		// force = true : meme les mobs qui refusent normalement un passager
		// acceptent la dague. En cas d'echec, tick() bascule sur le suivi
		// manuel.
		this.startRiding(target, true, false);
	}

	/**
	 * Enfonce la pointe dans la boite de collision de la cible.
	 *
	 * Deux raisons pour lesquelles la dague flottait a cote du mob :
	 *  - les projectiles touchent avec une marge AUTOUR de la boite
	 *    (ProjectileUtil.getManyEntityHitResult) : le point d'impact peut
	 *    etre dans le vide ;
	 *  - la boite elle-meme est plus large que le modele.
	 * On ramene donc d'abord la pointe sur la boite, puis on l'enfonce de
	 * DAGGER_MOB_EMBED_DEPTH le long de la trajectoire, sans jamais la faire
	 * ressortir de la boite.
	 */
	private Vec3 embeddedInEntity(Entity target, Vec3 movement) {
		AABB box = target.getBoundingBox();
		Vec3 tip = clampInto(box, this.position());

		if (movement.lengthSqr() > 1.0E-7 && ArcaBalance.DAGGER_MOB_EMBED_DEPTH > 0.0) {
			tip = clampInto(box, tip.add(movement.normalize().scale(ArcaBalance.DAGGER_MOB_EMBED_DEPTH)));
		}

		return tip;
	}

	private static Vec3 clampInto(AABB box, Vec3 point) {
		return new Vec3(
				Mth.clamp(point.x, box.minX, box.maxX),
				Mth.clamp(point.y, box.minY, box.maxY),
				Mth.clamp(point.z, box.minZ, box.maxZ));
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
