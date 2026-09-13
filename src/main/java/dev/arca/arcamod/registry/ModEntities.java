package dev.arca.arcamod.registry;

import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.entity.ThrownDagger;
import dev.arca.arcamod.entity.ThrownPebble;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/** Les entites du mod. */
public final class ModEntities {

	/** Memes reglages que la boule de neige vanilla. */
	public static final EntityType<ThrownPebble> THROWN_PEBBLE = register("thrown_pebble",
			EntityType.Builder.<ThrownPebble>of(ThrownPebble::new, MobCategory.MISC)
					.noLootTable()
					.sized(0.25F, 0.25F)
					.clientTrackingRange(4)
					.updateInterval(10));

	/**
	 * La dague lancee. updateInterval plus court que pour une fleche : quand
	 * elle est plantee dans un mob qui se deplace, sa position ne vient plus
	 * que du serveur, il faut donc la rafraichir souvent.
	 */
	public static final EntityType<ThrownDagger> THROWN_DAGGER = register("thrown_dagger",
			EntityType.Builder.<ThrownDagger>of(ThrownDagger::new, MobCategory.MISC)
					.noLootTable()
					.sized(0.5F, 0.5F)
					.clientTrackingRange(4)
					.updateInterval(5));

	private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name,
			EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, ArcaMod.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	public static void init() {
	}

	private ModEntities() {
	}
}
