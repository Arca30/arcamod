package dev.arca.arcamod.registry;

import java.util.Set;

import dev.arca.arcamod.ArcaBalance;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;

import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * Modifications des tables de butin vanilla.
 *
 * On passe par l'evenement de Fabric plutot que de poser un fichier JSON dans
 * data/minecraft/... : un fichier ecraserait la table vanilla en entier et
 * entrerait en conflit avec tout autre mod qui y touche. Ici on ne fait
 * qu'ajouter une pool a la table existante.
 */
public final class ModLootTables {

	/** Chance qu'un coup de museau de sniffer deterre une baie d'XP. */
	private static final float SNIFFER_XP_BERRY_CHANCE = 0.20F;

	/**
	 * Les herbes qui donnent des fibres quand on les coupe a la dague.
	 *
	 * Pour en ajouter une, il suffit de mettre la cle de sa table de butin
	 * (data/minecraft/loot_table/blocks/<nom>.json) dans cette liste.
	 */
	private static final Set<ResourceKey<LootTable>> FIBER_SOURCES = Set.of(
			blockLoot("short_grass"),
			blockLoot("tall_grass"),
			blockLoot("fern"),
			blockLoot("large_fern"),
			blockLoot("bush"));

	private static ResourceKey<LootTable> blockLoot(String blockName) {
		return ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("blocks/" + blockName));
	}

	public static void init() {
		LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
			// isBuiltin() = table du jeu ou d'un mod, pas un datapack du
			// joueur : si quelqu'un remplace la table volontairement, on ne
			// l'ecrase pas.
			if (!source.isBuiltin()) {
				return;
			}

			if (key.equals(BuiltInLootTables.SNIFFER_DIGGING)) {
				builder.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.when(LootItemRandomChanceCondition.randomChance(SNIFFER_XP_BERRY_CHANCE))
						.add(LootItem.lootTableItem(ModItems.XP_BERRY)));
				return;
			}

			if (FIBER_SOURCES.contains(key)) {
				LootPool.Builder pool = LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						// La fibre ne tombe que si le bloc est coupe a la dague.
						.when(MatchTool.toolMatches(ItemPredicate.Builder.item()
								.of(registries.lookupOrThrow(Registries.ITEM), ModItems.FLINT_DAGGER)))
						.add(LootItem.lootTableItem(ModItems.PLANT_FIBER)
								.apply(SetItemCountFunction.setCount(
										ConstantValue.exactly(ArcaBalance.PLANT_FIBER_PER_GRASS))));

				if (ArcaBalance.PLANT_FIBER_CHANCE < 1.0F) {
					pool.when(LootItemRandomChanceCondition.randomChance(ArcaBalance.PLANT_FIBER_CHANCE));
				}

				builder.withPool(pool);
			}
		});
	}

	private ModLootTables() {
	}
}
