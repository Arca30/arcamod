package dev.arca.arcamod.registry;

import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.entity.ThrownDagger;
import dev.arca.arcamod.entity.Scarecrow;
import dev.arca.arcamod.entity.ScarecrowDamageNumber;
import dev.arca.arcamod.entity.SlingshotShot;
import dev.arca.arcamod.entity.SmokeCloud;
import dev.arca.arcamod.entity.ThrownPebble;
import dev.arca.arcamod.entity.ThrownSmokeBomb;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.ArmorStand;

/** Les entites du mod. */
public final class ModEntities {

	/** Memes reglages que la boule de neige vanilla. */
	public static final EntityType<ThrownPebble> THROWN_PEBBLE = register("thrown_pebble",
			EntityType.Builder.<ThrownPebble>of(ThrownPebble::new, MobCategory.MISC)
					.noLootTable()
					.sized(0.25F, 0.25F)
					.clientTrackingRange(4)
					.updateInterval(10));

	/** Le projectile du lance-pierre : memes reglages que la boule de neige. */
	public static final EntityType<SlingshotShot> SLINGSHOT_SHOT = register("slingshot_shot",
			EntityType.Builder.<SlingshotShot>of(SlingshotShot::new, MobCategory.MISC)
					.noLootTable()
					.sized(0.25F, 0.25F)
					.clientTrackingRange(4)
					.updateInterval(10));

	/**
	 * La dague lancee. Memes reglages reseau que le trident (updateInterval
	 * 20) : chaque paquet de position ecrase la trajectoire predite par le
	 * client, donc plus il y en a, plus la dague saccade en vol. Plantee dans
	 * un mob, le client la replace lui-meme a chaque image (voir ThrownDagger).
	 */
	public static final EntityType<ThrownDagger> THROWN_DAGGER = register("thrown_dagger",
			EntityType.Builder.<ThrownDagger>of(ThrownDagger::new, MobCategory.MISC)
					.noLootTable()
					.sized(0.5F, 0.5F)
					.clientTrackingRange(4)
					.updateInterval(20));

	/** L'epouvantail d'entrainement : memes dimensions que le porte-armure. */
	public static final EntityType<Scarecrow> SCARECROW = register("scarecrow",
			EntityType.Builder.<Scarecrow>of(Scarecrow::new, MobCategory.MISC)
					.noLootTable()
					.sized(0.5F, 1.975F)
					.clientTrackingRange(10));

	/**
	 * Le chiffre de degats au-dessus de l'epouvantail. Memes reglages reseau
	 * que le text display vanilla.
	 */
	public static final EntityType<ScarecrowDamageNumber> SCARECROW_DAMAGE_NUMBER = register("scarecrow_damage_number",
			EntityType.Builder.<ScarecrowDamageNumber>of(ScarecrowDamageNumber::new, MobCategory.MISC)
					.noLootTable()
					.noSummon()
					.sized(0.0F, 0.0F)
					.clientTrackingRange(10)
					.updateInterval(1));

	/** La bombe fumigene lancee : memes reglages que la boule de neige. */
	public static final EntityType<ThrownSmokeBomb> THROWN_SMOKE_BOMB = register("thrown_smoke_bomb",
			EntityType.Builder.<ThrownSmokeBomb>of(ThrownSmokeBomb::new, MobCategory.MISC)
					.noLootTable()
					.sized(0.25F, 0.25F)
					.clientTrackingRange(4)
					.updateInterval(10));

	/** Le nuage de fumee pose par la bombe : invisible, il ne fait que souffler des particules. */
	public static final EntityType<SmokeCloud> SMOKE_CLOUD = register("smoke_cloud",
			EntityType.Builder.<SmokeCloud>of(SmokeCloud::new, MobCategory.MISC)
					.noLootTable()
					.noSummon()
					.sized(0.5F, 0.5F)
					.clientTrackingRange(10)
					.updateInterval(20));

	private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name,
			EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, ArcaMod.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	public static void init() {
		// Une entite vivante sans attributs fait planter le jeu a l'apparition.
		FabricDefaultAttributeRegistry.register(SCARECROW, ArmorStand.createAttributes());
	}

	private ModEntities() {
	}
}
