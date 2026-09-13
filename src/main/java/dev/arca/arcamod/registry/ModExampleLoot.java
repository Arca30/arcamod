package dev.arca.arcamod.registry;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;

import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

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
 * Chaque exemple ci-dessous a son interrupteur : passe-le a false pour le
 * desactiver sans supprimer le code.
 */
public final class ModExampleLoot {

	// =====================================================================
	// INTERRUPTEURS
	// =====================================================================

	public static final boolean ENABLE_MOB_DROP_EXAMPLE = true;
	public static final boolean ENABLE_BLOCK_DROP_EXAMPLE = true;
	public static final boolean ENABLE_CHEST_LOOT_EXAMPLE = true;

	// =====================================================================
	// REGLAGES DES EXEMPLES
	// =====================================================================

	/** Chance qu'un zombie tue par un joueur lache une petite pierre. */
	public static final float ZOMBIE_PEBBLE_CHANCE = 0.25F;

	/** Chance qu'un bloc de gravier casse a la pelle lache une fibre. */
	public static final float GRAVEL_FIBER_CHANCE = 0.10F;

	/** Nombre de cailloux ajoutes dans les coffres de village. */
	public static final float CHEST_PEBBLES_MIN = 2.0F;
	public static final float CHEST_PEBBLES_MAX = 5.0F;

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

	private static final ResourceKey<LootTable> ZOMBIE = vanillaTable("entities/zombie");
	private static final ResourceKey<LootTable> GRAVEL = vanillaTable("blocks/gravel");
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
			// EXEMPLE 1 : ajouter un butin a un MOB
			// -----------------------------------------------------------
			if (ENABLE_MOB_DROP_EXAMPLE && key.equals(ZOMBIE)) {
				builder.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						// une chance sur quatre...
						.when(LootItemRandomChanceCondition.randomChance(ZOMBIE_PEBBLE_CHANCE))
						// ...et seulement si c'est un joueur qui a tue : sans
						// ca, les zombies qui brulent au soleil deviennent une
						// ferme a ressources.
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
						.add(LootItem.lootTableItem(ModItems.PEBBLE)
								// Butin augmente la quantite : 0 a 1 de plus
								// par niveau.
								.apply(EnchantedCountIncreaseFunction.lootingMultiplier(registries,
										UniformGenerator.between(0.0F, 1.0F)))));
			}

			// -----------------------------------------------------------
			// EXEMPLE 2 : ajouter un butin a un BLOC vanilla
			// -----------------------------------------------------------
			if (ENABLE_BLOCK_DROP_EXAMPLE && key.equals(GRAVEL)) {
				builder.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.when(LootItemRandomChanceCondition.randomChance(GRAVEL_FIBER_CHANCE))
						// Filtre sur l'outil employe. Variante utile :
						// ItemPredicate.Builder.item().of(lookup, ItemTags.SHOVELS)
						// pour accepter toute une famille d'outils.
						.when(MatchTool.toolMatches(ItemPredicate.Builder.item()
								.of(registries.lookupOrThrow(Registries.ITEM), Items.IRON_SHOVEL)))
						.add(LootItem.lootTableItem(ModItems.PLANT_FIBER)));
			}

			// -----------------------------------------------------------
			// EXEMPLE 3 : garnir un COFFRE genere
			// -----------------------------------------------------------
			if (ENABLE_CHEST_LOOT_EXAMPLE && key.equals(VILLAGE_HOUSE)) {
				builder.withPool(LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(LootItem.lootTableItem(ModItems.PEBBLE)
								.apply(SetItemCountFunction.setCount(
										UniformGenerator.between(CHEST_PEBBLES_MIN, CHEST_PEBBLES_MAX)))));
			}
		});

		// ---------------------------------------------------------------
		// POUR ALLER PLUS LOIN
		//
		// Remplacer une table entiere (rare, mais parfois plus simple) :
		//   LootTableEvents.REPLACE.register((key, original, source, registries) ->
		//       key.equals(ZOMBIE) ? LootTable.lootTable().withPool(...).build() : null);
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
		//   SetItemCountFunction.setCount(UniformGenerator.between(1, 3))
		//   LootingEnchantFunction.lootingMultiplier(...)
		//   ApplyBonusCount.addOreBonusCount(Enchantments.FORTUNE)   (Fortune)
		//   SmeltItemFunction.smelted()                              (cuit au feu)
		//   EnchantRandomlyFunction.randomEnchantment(...)
		// ---------------------------------------------------------------

	}

	private ModExampleLoot() {
	}
}
