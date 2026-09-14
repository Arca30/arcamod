package dev.arca.arcamod.registry;

import java.util.Set;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.config.ArcaFeature;

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
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * Modifications des tables de butin vanilla.
 *
 * On passe par l'evenement de Fabric plutot que de poser un fichier JSON dans
 * data/minecraft/... : un fichier ecraserait la table vanilla en entier et
 * entrerait en conflit avec tout autre mod qui y touche. Ici on ne fait
 * qu'ajouter une pool a la table existante.
 *
 * Les tables sont construites au chargement du monde : un interrupteur
 * change ici ne prend effet qu'au prochain chargement (ou /reload).
 */
public final class ModLootTables {

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

	private static ResourceKey<LootTable> entityLoot(String entityName) {
		return ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("entities/" + entityName));
	}

	private static final ResourceKey<LootTable> BAT = entityLoot("bat");
	private static final ResourceKey<LootTable> NAUTILUS = entityLoot("nautilus");

	public static void init() {
		LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
			// isBuiltin() = table du jeu ou d'un mod, pas un datapack du
			// joueur : si quelqu'un remplace la table volontairement, on ne
			// l'ecrase pas.
			if (!source.isBuiltin()) {
				return;
			}

			if (key.equals(BAT) && ArcaFeature.BAT_WING_DROPS.isEnabled()) {
				builder.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.when(LootItemRandomChanceCondition.randomChance(ArcaBalance.BAT_WING_DROP_CHANCE))
						// Comme pour l'exemple du zombie : seule une mise a mort
						// par un joueur compte, pour eviter les fermes passives.
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
						.add(LootItem.lootTableItem(ModItems.BAT_WING)));
				return;
			}

			if (key.equals(NAUTILUS) && ArcaFeature.NAUTILUS_SHELL_DROPS.isEnabled()) {
				builder.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.when(LootItemRandomChanceCondition.randomChance(ArcaBalance.NAUTILUS_SHELL_DROP_CHANCE))
						.add(LootItem.lootTableItem(Items.NAUTILUS_SHELL)));
				return;
			}

			if (key.equals(BuiltInLootTables.SNIFFER_DIGGING) && ArcaFeature.SNIFFER_XP_BERRIES.isEnabled()) {
				builder.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.when(LootItemRandomChanceCondition.randomChance(ArcaBalance.SNIFFER_XP_BERRY_CHANCE))
						.add(LootItem.lootTableItem(ModItems.XP_BERRY)));
				return;
			}

			if (FIBER_SOURCES.contains(key) && ArcaFeature.PLANT_FIBER_FROM_GRASS.isEnabled()) {
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
