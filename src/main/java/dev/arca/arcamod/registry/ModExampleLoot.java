package dev.arca.arcamod.registry;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.config.ArcaFeature;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

/**
 * ============================================================
 *  BUTIN D'EXEMPLE - comment changer ce que lachent mobs,
 *  blocs et coffres
 * ============================================================
 *
 * DEUX FACONS DE FAIRE, ET QUAND CHOISIR L'UNE OU L'AUTRE
 *
 * 1) UN FICHIER JSON, dans data/<namespace>/loot_table/...
 *    Le fichier REMPLACE entierement la table d'origine.
 *    - data/arcamod/loot_table/blocks/<bloc>.json   : pour TES blocs.
 *    - data/minecraft/loot_table/entities/zombie.json : ecraserait la table
 *      du zombie... et casserait tout autre mod qui y touche. A eviter.
 *
 * 2) L'EVENEMENT DE FABRIC, ce fichier-ci.
 *    On AJOUTE une pool a une table existante, sans l'ecraser : deux mods
 *    peuvent modifier le zombie sans se marcher dessus. C'est la bonne
 *    methode pour tout ce qui est vanilla.
 *
 * VOCABULAIRE
 *    pool      un tirage. Une table en enchaine plusieurs, chacune donne
 *              ses propres objets.
 *    rolls     combien de fois la pool est tiree.
 *    entries   les objets possibles ; leur "weight" decide de la repartition
 *              a l'interieur d'un tirage.
 *    condition un filtre : chance, outil utilise, tue par un joueur...
 *    function  une retouche de l'objet obtenu : quantite, enchantement,
 *              nom personnalise...
 *
 * L'exemple ci-dessous a son interrupteur dans ArcaFeature (menu
 * Options > ArcaMod). Effet au prochain chargement du monde. Pour des
 * exemples de butin sur un mob ou un bloc, voir ModLootTables (chauve-souris,
 * nautile, cube de soufre, herbes coupees a la dague).
 */
public final class ModExampleLoot {

	// Interrupteur : ArcaFeature.VILLAGE_CHEST_PEBBLES. Quantites :
	// ArcaBalance, section 20. Les butins reels du mod (mobs, blocs) sont
	// dans ModLootTables ; ce fichier ne garde qu'un exemple commente.

	// =====================================================================
	// LES TABLES VISEES
	// =====================================================================

	/**
	 * Une table de butin se designe par sa cle. Les chemins vanilla :
	 *   entities/<mob>            la mort d'une creature
	 *   blocks/<bloc>             la casse d'un bloc
	 *   chests/<coffre>           le contenu d'un coffre genere
	 *   gameplay/fishing/...      la peche, les echanges, etc.
	 *
	 * Pour trouver un nom exact : le fichier existe dans le jar du jeu, sous
	 * data/minecraft/loot_table/.
	 */
	private static ResourceKey<LootTable> vanillaTable(String path) {
		return ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace(path));
	}

	private static final ResourceKey<LootTable> VILLAGE_HOUSE = vanillaTable("chests/village/village_plains_house");

	public static void init() {
		LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
			// isBuiltin() : la table vient du jeu ou d'un mod, pas d'un
			// datapack pose par le joueur. Si quelqu'un a deliberement
			// remplace la table, on ne lui passe pas dessus.
			if (!source.isBuiltin()) {
				return;
			}

			// -----------------------------------------------------------
			// EXEMPLE : garnir un COFFRE genere
			// -----------------------------------------------------------
			if (ArcaFeature.VILLAGE_CHEST_PEBBLES.isEnabled() && key.equals(VILLAGE_HOUSE)) {
				builder.withPool(LootPool.lootPool()
						.setRolls(ContextIntProviders.exactly(1))
						.add(LootItem.lootTableItem(ModItems.PEBBLE)
								.apply(SetItemCountFunction.setCount(
										ContextIntProviders.between(ArcaBalance.VILLAGE_CHEST_PEBBLES_MIN, ArcaBalance.VILLAGE_CHEST_PEBBLES_MAX)))));
			}
		});

		// ---------------------------------------------------------------
		// POUR ALLER PLUS LOIN
		//
		// Remplacer une table entiere (rare, mais parfois plus simple) :
		//   LootTableEvents.REPLACE.register((key, original, source, registries) ->
		//       key.equals(VILLAGE_HOUSE) ? LootTable.lootTable().withPool(...).build() : null);
		//
		// Conditions les plus utilisees :
		//   LootItemRandomChanceCondition.randomChance(0.1F)
		//   LootItemRandomChanceWithEnchantedBonusCondition.enchantedChance(...)  (Butin)
		//   LootItemKilledByPlayerCondition.killedByPlayer()
		//   MatchTool.toolMatches(ItemPredicate.Builder.item()...)
		//   LootItemBlockStatePropertyCondition.hasBlockStateProperties(bloc)
		//   ExplosionCondition.survivesExplosion()
		//
		// Fonctions les plus utilisees :
		//   SetItemCountFunction.setCount(ContextIntProviders.between(1, 3))
		//   LootingEnchantFunction.lootingMultiplier(...)
		//   ApplyBonusCount.addOreBonusCount(Enchantments.FORTUNE)   (Fortune)
		//   SmeltItemFunction.smelted()                              (cuit au feu)
		//   EnchantRandomlyFunction.randomEnchantment(...)
		// ---------------------------------------------------------------

	}

	private ModExampleLoot() {
	}
}
