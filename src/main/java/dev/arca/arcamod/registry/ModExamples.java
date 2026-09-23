package dev.arca.arcamod.registry;

import java.util.function.Function;

import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.example.ExampleBlock;
import dev.arca.arcamod.example.ExampleItem;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * ============================================================
 *  ENREGISTREMENT D'EXEMPLE - le mode d'emploi
 * ============================================================
 *
 * "Enregistrer" = donner un identifiant a quelque chose pour que le jeu sache
 * qu'il existe. Sans ca, ta classe Java ne sert a rien.
 *
 * L'ORDRE COMPTE. Dans ArcaMod.onInitialize() :
 *   1. les blocs           (ModBlocks, ModDecorBlocks)
 *   2. les items           (ModItems) - ils ont besoin des blocs
 *   3. les entites         (ModEntities)
 *   4. les BlockEntity     (ModBlockEntities) - besoin des blocs
 *   5. les menus           (ModMenus)
 *   6. les onglets creatifs, le butin, les evenements
 *   7. la generation du monde
 *
 * POUR AJOUTER TON PROPRE BLOC :
 *   - soit tu ajoutes une ligne dans ModBlocks + ModItems (le plus propre) ;
 *   - soit tu copies ce fichier entier et tu appelles ton init() depuis
 *     ArcaMod, ce qui garde tes ajouts bien separes.
 */
public final class ModExamples {

	// =====================================================================
	// 1. LE BLOC
	// =====================================================================

	/**
	 * Les Properties decrivent tout ce qui ne demande pas de code :
	 *
	 *   .strength(durete)                 temps de minage (pierre = 1.5)
	 *   .strength(durete, resistance)     + resistance aux explosions
	 *   .requiresCorrectToolForDrops()    rien ne tombe sans le bon outil
	 *   .instabreak()                     se casse instantanement
	 *   .lightLevel(state -> 0..15)       emission de lumiere
	 *   .noCollision()                    on le traverse
	 *   .noOcclusion()                    obligatoire si le modele n'est pas
	 *                                     un cube plein, sinon l'eclairage
	 *                                     des blocs voisins est faux
	 *   .randomTicks()                    active randomTick() (plantes, feu)
	 *   .sound(SoundType.X)               bruits de pas et de casse
	 *   .mapColor(MapColor.X)             couleur sur une carte
	 *   .pushReaction(PushReaction.X)     comportement face aux pistons
	 *   .ignitedByLava()                  inflammable
	 */
	public static final ExampleBlock EXAMPLE_BLOCK = registerBlock("example_block", ExampleBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_LIGHT_BLUE)
					.strength(1.5F)
					.requiresCorrectToolForDrops()
					// La lumiere depend de l'etat : eteint 0, allume 12.
					.lightLevel(state -> state.getValue(ExampleBlock.ACTIVE) ? 12 : 0)
					.sound(SoundType.AMETHYST)
					.pushReaction(PushReaction.PUSH_PULL));

	// =====================================================================
	// 2. LES ITEMS
	// =====================================================================

	/** L'item qui pose le bloc. Sans lui, le bloc est inaccessible. */
	public static final Item EXAMPLE_BLOCK_ITEM = registerItem("example_block",
			properties -> new BlockItem(EXAMPLE_BLOCK, properties),
			// useBlockDescriptionPrefix : le nom vient de la cle
			// "block.arcamod.example_block" du fichier de langue.
			new Item.Properties().useBlockDescriptionPrefix());

	/**
	 * L'item d'exemple.
	 *
	 *   .stacksTo(n)                 taille de pile (64 par defaut)
	 *   .durability(n)               barre de durabilite (force la pile a 1)
	 *   .rarity(Rarity.X)            couleur du nom
	 *   .fireResistant()             survit au feu
	 *   .food(...)                   comestible
	 *   .component(TYPE, valeur)     n'importe quel composant de donnees
	 *   .enchantable(n)              enchantable a la table
	 *   .repairable(item ou tag)     reparable a l'enclume
	 */
	public static final Item EXAMPLE_ITEM = registerItem("example_item", ExampleItem::new,
			new Item.Properties().durability(32));

	// =====================================================================
	// 3. LES FABRIQUES D'ENREGISTREMENT
	// =====================================================================

	/**
	 * Enregistre un bloc.
	 *
	 * Piege classique depuis 1.21.2 : les Properties doivent connaitre leur
	 * identifiant AVANT que le bloc ne soit construit, d'ou le setId(key) et
	 * la fabrique (Function) plutot qu'un bloc deja fait. Sans ca, le jeu
	 * plante au demarrage avec "Block id not set".
	 */
	private static <T extends Block> T registerBlock(String name, Function<BlockBehaviour.Properties, T> factory,
			BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ArcaMod.id(name));
		T block = factory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}

	/** Enregistre un item. Meme piege du setId. */
	private static Item registerItem(String name, Function<Item.Properties, Item> factory,
			Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ArcaMod.id(name));
		Item item = factory.apply(properties.setId(key));

		// Indispensable pour un BlockItem : sans cette ligne, le jeu ne sait
		// pas quel item correspond au bloc (pick-block, butin par defaut...).
		if (item instanceof BlockItem blockItem) {
			blockItem.registerBlocks(Item.BY_BLOCK, item);
		}

		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	// =====================================================================
	// 4. LES ONGLETS CREATIFS
	// =====================================================================

	/**
	 * Sans ca, l'item existe mais est introuvable en creatif (il faut passer
	 * par /give). Les onglets vanilla utiles :
	 *   building_blocks, colored_blocks, natural_blocks, functional_blocks,
	 *   redstone_blocks, tools_and_utilities, combat, food_and_drinks,
	 *   ingredients, spawn_eggs
	 */
	private static ResourceKey<CreativeModeTab> vanillaTab(String name) {
		return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace(name));
	}

	public static void init() {
		CreativeModeTabEvents.modifyOutputEvent(vanillaTab("building_blocks"))
				.register(output -> output.accept(EXAMPLE_BLOCK_ITEM));

		CreativeModeTabEvents.modifyOutputEvent(vanillaTab("tools_and_utilities"))
				.register(output -> output.accept(EXAMPLE_ITEM));
	}

	private ModExamples() {
	}
}
