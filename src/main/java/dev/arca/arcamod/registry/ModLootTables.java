package dev.arca.arcamod.registry;

import java.util.Set;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.config.ArcaFeature;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;

import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.core.component.predicates.DamagePredicate;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.entity.EntityTypePredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetInstrumentFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

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
	private static final ResourceKey<LootTable> SULFUR_CUBE = entityLoot("sulfur_cube");
	private static final ResourceKey<LootTable> GOAT = entityLoot("goat");

	/**
	 * Le bonus de l'enchantement Butin applique a un butin de mob : de 0 a
	 * <bonus> objets de plus par niveau, exactement comme la barre de blaze
	 * vanilla. Tous les butins de mob ajoutes par le mod y passent.
	 */
	private static EnchantedCountIncreaseFunction.Builder looting(HolderLookup.Provider registries, float bonus) {
		return EnchantedCountIncreaseFunction.lootingMultiplier(
				registries.lookupOrThrow(Registries.ENCHANTMENT),
				ContextFloatProviders.between(0.0F, bonus));
	}

	public static void init() {
		registerSnifferEggArchaeology();
		registerStarterChest();

		LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
			// isBuiltin() = table du jeu ou d'un mod, pas un datapack du
			// joueur : si quelqu'un remplace la table volontairement, on ne
			// l'ecrase pas.
			if (!source.isBuiltin()) {
				return;
			}

			if (key.equals(BAT) && ArcaFeature.BAT_WING_DROPS.isEnabled()) {
				builder.withPool(LootPool.lootPool()
						.setRolls(ContextIntProviders.exactly(1))
						.when(LootItemRandomChanceCondition.randomChance(ArcaBalance.BAT_WING_DROP_CHANCE))
						// Comme pour l'exemple du zombie : seule une mise a mort
						// par un joueur compte, pour eviter les fermes passives.
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
						.add(LootItem.lootTableItem(ModItems.BAT_WING)
								.apply(looting(registries, ArcaBalance.BAT_WING_LOOTING_BONUS))));
				return;
			}

			if (key.equals(NAUTILUS) && ArcaFeature.NAUTILUS_SHELL_DROPS.isEnabled()) {
				builder.withPool(LootPool.lootPool()
						.setRolls(ContextIntProviders.exactly(1))
						.when(LootItemRandomChanceCondition.randomChance(ArcaBalance.NAUTILUS_SHELL_DROP_CHANCE))
						// Sans ca, un nautile mort tout seul suffirait : le
						// conduit deviendrait une simple question de patience.
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
						.add(LootItem.lootTableItem(Items.NAUTILUS_SHELL)
								.apply(looting(registries, ArcaBalance.NAUTILUS_SHELL_LOOTING_BONUS))));
				return;
			}

			// Le cube de soufre ne lache rien en vanilla : il laisse ici de 0 a
			// 2 poudres de soufre (voir ArcaBalance.SULFUR_CUBE_POWDER_MIN / MAX).
			if (key.equals(SULFUR_CUBE) && ArcaFeature.SULFUR_CUBE_DROPS.isEnabled()) {
				LootPool.Builder pool = LootPool.lootPool()
						.setRolls(ContextIntProviders.exactly(1))
						.add(LootItem.lootTableItem(ModItems.SULFUR_POWDER)
								.apply(SetItemCountFunction.setCount(ContextIntProviders.between(
										ArcaBalance.SULFUR_CUBE_POWDER_MIN, ArcaBalance.SULFUR_CUBE_POWDER_MAX)))
								.apply(looting(registries, ArcaBalance.SULFUR_CUBE_LOOTING_BONUS)));

				if (ArcaBalance.SULFUR_CUBE_REQUIRES_PLAYER_KILL) {
					pool.when(LootItemKilledByPlayerCondition.killedByPlayer());
				}

				if (ArcaBalance.SULFUR_CUBE_POWDER_CHANCE < 1.0F) {
					pool.when(LootItemRandomChanceCondition.randomChance(ArcaBalance.SULFUR_CUBE_POWDER_CHANCE));
				}

				builder.withPool(pool);
				return;
			}

			// Chevre : de la laine (elle en a bien l'air) et, beaucoup plus
			// rarement, une corne dont l'instrument est tire au hasard parmi
			// le tag minecraft:goat_horns - donc les huit airs du jeu.
			if (key.equals(GOAT) && ArcaFeature.GOAT_EXTRA_DROPS.isEnabled()) {
				LootPool.Builder wool = LootPool.lootPool()
						.setRolls(ContextIntProviders.exactly(1))
						.when(LootItemRandomChanceCondition.randomChance(ArcaBalance.GOAT_WOOL_DROP_CHANCE))
						.add(LootItem.lootTableItem(Items.WOOL.pick(ArcaBalance.GOAT_WOOL_COLOR))
								.apply(SetItemCountFunction.setCount(ContextIntProviders.between(
										ArcaBalance.GOAT_WOOL_MIN, ArcaBalance.GOAT_WOOL_MAX)))
								.apply(looting(registries, ArcaBalance.GOAT_WOOL_LOOTING_BONUS)));

				LootPool.Builder horn = LootPool.lootPool()
						.setRolls(ContextIntProviders.exactly(1))
						.when(LootItemRandomChanceCondition.randomChance(ArcaBalance.GOAT_HORN_DROP_CHANCE))
						.add(LootItem.lootTableItem(Items.GOAT_HORN)
								.apply(SetInstrumentFunction.setInstrumentOptions(
										registries.lookupOrThrow(Registries.INSTRUMENT)
												.getOrThrow(InstrumentTags.GOAT_HORNS))));

				if (ArcaBalance.GOAT_DROPS_REQUIRE_PLAYER_KILL) {
					wool.when(LootItemKilledByPlayerCondition.killedByPlayer());
					horn.when(LootItemKilledByPlayerCondition.killedByPlayer());
				}

				builder.withPool(wool);
				builder.withPool(horn);
				return;
			}

			// Creeper charge : la table vanilla choisit le crane selon le type
			// du mob tue. On ajoute une pool pour l'Enderman ; Creeper.killedEntity
			// garantit toujours une seule tete par explosion.
			if (key.equals(BuiltInLootTables.CHARGED_CREEPER) && ArcaFeature.ENDERMAN_HEAD_DROPS.isEnabled()) {
				LootPool.Builder pool = LootPool.lootPool()
						.setRolls(ContextIntProviders.exactly(1))
						.when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
								EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(
										registries.lookupOrThrow(Registries.ENTITY_TYPE), EntityTypes.ENDERMAN))))
						.add(LootItem.lootTableItem(ModItems.ENDERMAN_HEAD));

				if (ArcaBalance.ENDERMAN_HEAD_CHARGED_CREEPER_CHANCE < 1.0F) {
					pool.when(LootItemRandomChanceCondition.randomChance(ArcaBalance.ENDERMAN_HEAD_CHARGED_CREEPER_CHANCE));
				}

				builder.withPool(pool);
				return;
			}

			// Epaves : le modele d'amelioration en or rose, dans le coffre au tresor.
			if (key.equals(BuiltInLootTables.SHIPWRECK_TREASURE) && ArcaFeature.PINK_GOLD_TEMPLATE_LOOT.isEnabled()) {
				builder.withPool(LootPool.lootPool()
						.setRolls(ContextIntProviders.exactly(1))
						.when(LootItemRandomChanceCondition.randomChance(ArcaBalance.PINK_GOLD_TEMPLATE_SHIPWRECK_CHANCE))
						.add(LootItem.lootTableItem(ModItems.PINK_GOLD_UPGRADE_SMITHING_TEMPLATE)));
				return;
			}

			if (key.equals(BuiltInLootTables.SNIFFER_DIGGING) && ArcaFeature.SNIFFER_XP_BERRIES.isEnabled()) {
				builder.withPool(LootPool.lootPool()
						.setRolls(ContextIntProviders.exactly(1))
						.when(LootItemRandomChanceCondition.randomChance(ArcaBalance.SNIFFER_XP_BERRY_CHANCE))
						.add(LootItem.lootTableItem(ModItems.XP_BERRY)));
				return;
			}

			// Feuilles cassees a MAIN NUE par un joueur : une chance de branche,
			// en plus des 2 % vanilla. La condition "joueur present" ecarte les
			// feuilles qui tombent toutes seules (pas de casseur, main vide aussi).
			String path = key.identifier().getPath();

			if (path.startsWith("blocks/") && path.endsWith("_leaves") && ArcaFeature.LEAVES_HAND_STICKS.isEnabled()
					&& ArcaBalance.LEAVES_HAND_STICK_CHANCE > 0.0F) {
				builder.withPool(LootPool.lootPool()
						.setRolls(ContextIntProviders.exactly(1))
						.when(LootItemEntityPropertyCondition.entityPresent(LootContext.EntityTarget.THIS))
						.when(InvertedLootItemCondition.invert(MatchTool.toolMatches(ItemPredicate.Builder.item()
								.withCount(MinMaxBounds.Ints.atLeast(1)))))
						.when(LootItemRandomChanceCondition.randomChance(ArcaBalance.LEAVES_HAND_STICK_CHANCE))
						.add(LootItem.lootTableItem(Items.STICK)));
				return;
			}

			if (FIBER_SOURCES.contains(key) && ArcaFeature.PLANT_FIBER_FROM_GRASS.isEnabled()) {
				LootPool.Builder pool = LootPool.lootPool()
						.setRolls(ContextIntProviders.exactly(1))
						// La fibre ne tombe que si le bloc est coupe a une lame :
						// dague ou epee (tag arcamod:cuts_plant_fiber). Une lame
						// CASSEE (durabilite restante nulle) ne coupe plus rien.
						.when(MatchTool.toolMatches(ItemPredicate.Builder.item()
								.of(registries.lookupOrThrow(Registries.ITEM), ModTags.CUTS_PLANT_FIBER)
								.withComponents(DataComponentMatchers.Builder.components()
										.partial(DataComponentPredicates.DAMAGE,
												DamagePredicate.durability(MinMaxBounds.Ints.atLeast(1)))
										.build())))
						.add(LootItem.lootTableItem(ModItems.PLANT_FIBER)
								.apply(SetItemCountFunction.setCount(
										ContextIntProviders.exactly(ArcaBalance.PLANT_FIBER_PER_GRASS))));

				if (ArcaBalance.PLANT_FIBER_CHANCE < 1.0F) {
					pool.when(LootItemRandomChanceCondition.randomChance(ArcaBalance.PLANT_FIBER_CHANCE));
				}

				builder.withPool(pool);
			}
		});
	}

	/**
	 * Oeuf de renifleur dans TOUT sable ou gravier suspect.
	 *
	 * Le pinceau ne rend qu'un seul objet par bloc : ajouter une pool a la
	 * table en donnerait deux (et le jeu ne garderait que le premier). On
	 * remplace donc, apres le tirage, l'objet trouve par un oeuf, avec la
	 * chance ArcaBalance.SNIFFER_EGG_ARCHAEOLOGY_CHANCE. Toutes les tables
	 * "archaeology/..." sont concernees, y compris celles d'autres mods.
	 * Dans les ruines oceaniques chaudes, ca s'ajoute a la chance vanilla
	 * (1 sur 15).
	 */
	private static void registerSnifferEggArchaeology() {
		LootTableEvents.MODIFY_DROPS.register((table, context, drops) -> {
			if (!ArcaFeature.SNIFFER_EGG_ARCHAEOLOGY.isEnabled() || ArcaBalance.SNIFFER_EGG_ARCHAEOLOGY_CHANCE <= 0.0F) {
				return;
			}

			boolean archaeology = table.unwrapKey()
					.map(key -> key.identifier().getPath().startsWith("archaeology/"))
					.orElse(false);

			if (!archaeology || context.getRandom().nextFloat() >= ArcaBalance.SNIFFER_EGG_ARCHAEOLOGY_CHANCE) {
				return;
			}

			drops.clear();
			drops.add(new ItemStack(Items.SNIFFER_EGG));
		});
	}

	/**
	 * Coffre bonus du point d'apparition (option "Coffre bonus" a la creation
	 * du monde). En vanilla il donne des haches et pioches en bois et des
	 * buches : de quoi sauter toute la progression du silex. On le remplace
	 * par des objets du tout debut : branches, cailloux, fibres et nourriture.
	 * Le silex reste a trouver (ArcaBalance.STARTER_CHEST_FLINT_MAX = 0).
	 */
	private static void registerStarterChest() {
		LootTableEvents.REPLACE.register((key, original, source, registries) -> {
			if (!key.equals(BuiltInLootTables.SPAWN_BONUS_CHEST) || !source.isBuiltin()
					|| !ArcaFeature.STARTER_CHEST_PROGRESSION.isEnabled()) {
				return null;
			}

			LootTable.Builder table = LootTable.lootTable().setParamSet(LootContextParamSets.CHEST);
			addCountedPool(table, Items.STICK, ArcaBalance.STARTER_CHEST_STICKS_MIN, ArcaBalance.STARTER_CHEST_STICKS_MAX);
			addCountedPool(table, ModItems.PEBBLE, ArcaBalance.STARTER_CHEST_PEBBLES_MIN, ArcaBalance.STARTER_CHEST_PEBBLES_MAX);
			addCountedPool(table, ModItems.PLANT_FIBER, ArcaBalance.STARTER_CHEST_FIBERS_MIN, ArcaBalance.STARTER_CHEST_FIBERS_MAX);
			addCountedPool(table, Items.FLINT, ArcaBalance.STARTER_CHEST_FLINT_MIN, ArcaBalance.STARTER_CHEST_FLINT_MAX);

			// La nourriture du coffre vanilla (pomme, pain, saumon).
			if (ArcaBalance.STARTER_CHEST_FOOD_ROLLS > 0) {
				table.withPool(LootPool.lootPool()
						.setRolls(ContextIntProviders.exactly(ArcaBalance.STARTER_CHEST_FOOD_ROLLS))
						.add(LootItem.lootTableItem(Items.APPLE).setWeight(5)
								.apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 2))))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(3)
								.apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 2))))
						.add(LootItem.lootTableItem(Items.SALMON).setWeight(3)
								.apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 2)))));
			}

			return table.build();
		});
	}

	/** Une pool qui donne entre min et max exemplaires (rien si max <= 0). */
	private static void addCountedPool(LootTable.Builder table, net.minecraft.world.level.ItemLike item, int min, int max) {
		if (max <= 0) {
			return;
		}

		table.withPool(LootPool.lootPool()
				.setRolls(ContextIntProviders.exactly(1))
				.add(LootItem.lootTableItem(item)
						.apply(SetItemCountFunction.setCount(ContextIntProviders.between(Math.max(0, min), max)))));
	}

	private ModLootTables() {
	}
}
