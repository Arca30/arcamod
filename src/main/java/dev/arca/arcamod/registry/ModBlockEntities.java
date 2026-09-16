package dev.arca.arcamod.registry;

import java.util.Set;

import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.block.entity.DisenchanterBlockEntity;
import dev.arca.arcamod.block.entity.EndermanHeadBlockEntity;
import dev.arca.arcamod.block.entity.PotionCauldronBlockEntity;
import dev.arca.arcamod.block.entity.XpBottlerBlockEntity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Les BlockEntity du mod (la "memoire" des blocs qui stockent des donnees). */
public final class ModBlockEntities {

	public static final BlockEntityType<XpBottlerBlockEntity> XP_BOTTLER = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			ArcaMod.id("xp_bottler"),
			// Le Set liste les blocs auxquels ce BlockEntity a le droit d'etre
			// attache : si tu oublies le bloc ici, le jeu refuse le BlockEntity.
			new BlockEntityType<>(XpBottlerBlockEntity::new, Set.of(ModBlocks.XP_BOTTLER)));

	public static final BlockEntityType<PotionCauldronBlockEntity> POTION_CAULDRON = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			ArcaMod.id("potion_cauldron"),
			new BlockEntityType<>(PotionCauldronBlockEntity::new, Set.of(ModBlocks.POTION_CAULDRON)));

	/** Sert uniquement a l'animation du livre au-dessus du desenchanteur. */
	public static final BlockEntityType<DisenchanterBlockEntity> DISENCHANTER = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			ArcaMod.id("disenchanter"),
			new BlockEntityType<>(DisenchanterBlockEntity::new, Set.of(ModBlocks.DISENCHANTER)));

	/** Tete d'Enderman : detection du regard (serveur) et animation (client). */
	public static final BlockEntityType<EndermanHeadBlockEntity> ENDERMAN_HEAD = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			ArcaMod.id("enderman_head"),
			new BlockEntityType<>(EndermanHeadBlockEntity::new, Set.of(ModBlocks.ENDERMAN_HEAD, ModBlocks.ENDERMAN_WALL_HEAD)));

	public static void init() {
	}

	private ModBlockEntities() {
	}
}
