package dev.arca.arcamod.registry;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.jspecify.annotations.Nullable;

/**
 * Range les items du mod dans les onglets creatifs vanilla.
 *
 * Chaque item est insere A COTE de son equivalent vanilla (insertAfter /
 * insertBefore) et pas a la fin de l'onglet : l'or rose suit l'or, le chaume
 * suit le foin, les escaliers suivent leur bloc plein... Si l'objet repere est
 * absent (un autre mod l'a retire), l'item atterrit en fin d'onglet.
 *
 * En 26.2 les onglets sont des entrees de registre : on les cible par leur
 * ResourceKey (il n'y a plus de constantes publiques dans CreativeModeTabs).
 */
public final class ModCreativeTabs {

	private static final ResourceKey<CreativeModeTab> NATURAL_BLOCKS = vanillaTab("natural_blocks");
	private static final ResourceKey<CreativeModeTab> FOOD_AND_DRINKS = vanillaTab("food_and_drinks");
	private static final ResourceKey<CreativeModeTab> FUNCTIONAL_BLOCKS = vanillaTab("functional_blocks");
	private static final ResourceKey<CreativeModeTab> TOOLS_AND_UTILITIES = vanillaTab("tools_and_utilities");
	private static final ResourceKey<CreativeModeTab> COMBAT = vanillaTab("combat");
	private static final ResourceKey<CreativeModeTab> INGREDIENTS = vanillaTab("ingredients");
	private static final ResourceKey<CreativeModeTab> BUILDING_BLOCKS = vanillaTab("building_blocks");
	private static final ResourceKey<CreativeModeTab> COLORED_BLOCKS = vanillaTab("colored_blocks");
	private static final ResourceKey<CreativeModeTab> REDSTONE_BLOCKS = vanillaTab("redstone_blocks");

	private static ResourceKey<CreativeModeTab> vanillaTab(String name) {
		return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace(name));
	}

	/** Les familles de blocs declinees en seize couleurs qui recoivent escaliers et dalles. */
	private static final List<String> COLOURED_FAMILIES = List.of("terracotta", "concrete_powder");

	/**
	 * Les blocs pleins d'une famille dans l'ordre ou l'onglet les affiche
	 * VRAIMENT, la version sans couleur comprise (la terre cuite "nue" est du
	 * voyage, a sa place dans la serie).
	 *
	 * Vanilla ne range pas les blocs colores dans l'ordre des teintes
	 * (blanc, orange, magenta...) mais dans un ordre visuel (blanc, gris
	 * clair, gris, noir, brun, rouge...). On le relit donc dans la sortie de
	 * l'onglet au lieu de le deviner : escaliers et dalles suivent exactement
	 * la meme succession que les blocs pleins.
	 */
	private static List<String> familyOrderOf(FabricCreativeModeTabOutput output, String family) {
		List<String> names = new ArrayList<>();

		for (ItemStack stack : output.getDisplayStacks()) {
			String name = familyMember(stack, family, "");

			if (name != null && !names.contains(name)) {
				names.add(name);
			}
		}

		// Famille absente de l'onglet : on retombe sur l'ordre des teintes.
		return names.isEmpty()
				? ModDecorBlocks.DYE_COLORS.stream().map(colour -> colour + "_" + family).toList()
				: names;
	}

	/**
	 * Le nom du bloc plein correspondant si cet objet appartient bien a la
	 * famille demandee (<famille><suffixe> ou <teinte>_<famille><suffixe>),
	 * sinon null. La comparaison porte sur le nom complet : la terre cuite
	 * vernissee ("*_glazed_terracotta") n'est pas de la partie.
	 */
	private static @Nullable String familyMember(ItemStack stack, String family, String suffix) {
		String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

		if (path.equals(family + suffix)) {
			return family;
		}

		for (String colour : ModDecorBlocks.DYE_COLORS) {
			if (path.equals(colour + "_" + family + suffix)) {
				return colour + "_" + family;
			}
		}

		return null;
	}

	/** Les escaliers (ou les dalles) d'une famille, dans l'ordre de ses blocs pleins. */
	private static List<ItemStack> familyVariants(List<String> names, String suffix) {
		return names.stream()
				.map(name -> new ItemStack(ModDecorBlocks.byName(name + suffix)))
				.toList();
	}

	public static void init() {
		// ---- Blocs colores : terre cuite et beton ---------------------------
		CreativeModeTabEvents.modifyOutputEvent(COLORED_BLOCKS)
				.register(output -> {
					// Comme les blocs vanilla : tous les blocs pleins d'une
					// famille se suivent (terre cuite nue comprise), puis tous
					// les escaliers, puis toutes les dalles. Chaque serie
					// s'accroche donc APRES LE DERNIER element de la serie
					// precedente, ou qu'il soit dans l'onglet (d'ou le predicat
					// plutot qu'un bloc repere choisi a l'avance).
					for (String family : COLOURED_FAMILIES) {
						List<String> names = familyOrderOf(output, family);

						output.insertAfter(stack -> familyMember(stack, family, "") != null,
								familyVariants(names, "_stairs"),
								CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
						output.insertAfter(stack -> familyMember(stack, family, "_stairs") != null,
								familyVariants(names, "_slab"),
								CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
					}
				});

		// ---- Blocs de construction ------------------------------------------
		CreativeModeTabEvents.modifyOutputEvent(BUILDING_BLOCKS)
				.register(output -> {
					// Les deux mousses, avec leurs escaliers et dalles, juste
					// avant la pierre (elles n'etaient pas dans cet onglet).
					output.insertBefore(Items.STONE,
							Items.MOSS_BLOCK,
							ModDecorBlocks.byName("moss_block_stairs"),
							ModDecorBlocks.byName("moss_block_slab"),
							Items.PALE_MOSS_BLOCK,
							ModDecorBlocks.byName("pale_moss_block_stairs"),
							ModDecorBlocks.byName("pale_moss_block_slab"));

					// L'or rose apres tout ce qui est en or.
					output.insertAfter(Items.LIGHT_WEIGHTED_PRESSURE_PLATE, ModItems.PINK_GOLD_BLOCK);

					// Le foin (absent de cet onglet) ouvre la famille du chaume.
					output.accept(Items.HAY_BLOCK);
					output.accept(ModDecorBlocks.byName("hay_block_stairs"));
					output.accept(ModDecorBlocks.byName("hay_block_slab"));

					for (String stage : ModDecorBlocks.THATCH_STAGES) {
						output.accept(ModDecorBlocks.byName(stage));
						output.accept(ModDecorBlocks.byName(stage + "_stairs"));
						output.accept(ModDecorBlocks.byName(stage + "_slab"));
					}

					// La brique qui vieillit, juste apres la brique vanilla :
					// une seule insertion, sinon chaque etape passerait devant
					// la precedente.
					List<ItemStack> bricks = new ArrayList<>();

					List<String> families = new ArrayList<>(ModDecorBlocks.BRICK_STAGES);
					families.add(ModDecorBlocks.WAXED_BRICK);

					for (String family : families) {
						bricks.add(new ItemStack(ModDecorBlocks.byName(family)));
						bricks.add(new ItemStack(ModDecorBlocks.byName(family + "_stairs")));
						bricks.add(new ItemStack(ModDecorBlocks.byName(family + "_slab")));
						bricks.add(new ItemStack(ModDecorBlocks.byName(family + "_wall")));
					}

					output.insertAfter(Items.BRICK_WALL, bricks,
							CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
				});

		// ---- Blocs naturels ---------------------------------------------------
		// (la petite pierre n'est pas ici : c'est une munition, rangee avec les
		// projectiles et les ingredients. Elle pose le bloc en s'accroupissant.)
		CreativeModeTabEvents.modifyOutputEvent(NATURAL_BLOCKS)
				.register(output -> {
					// Les branches au sol : le meme genre de decor que le
					// buisson mort, juste apres lui.
					output.insertAfter(Items.DEAD_BUSH, ModItems.FALLEN_STICKS);
					output.insertAfter(Items.DEEPSLATE_GOLD_ORE,
							ModItems.PINK_GOLD_ORE, ModItems.DEEPSLATE_PINK_GOLD_ORE);
					output.insertAfter(Items.RAW_GOLD_BLOCK, ModItems.RAW_PINK_GOLD_BLOCK);
					output.insertAfter(Items.GLOW_BERRIES, ModItems.XP_BERRY);
					output.insertAfter(Items.SULFUR, ModItems.BRUSHED_SULFUR);
					output.insertAfter(Items.CHERRY_LOG, ModItems.BURNT_LOG, ModItems.IGNITED_BURNT_LOG,
							ModItems.BURNT_WOOD, ModItems.PACKED_ASH);
				});

		// ---- Blocs fonctionnels ------------------------------------------------
		// Le baril de TNT juste apres la TNT.
		CreativeModeTabEvents.modifyOutputEvent(REDSTONE_BLOCKS)
				.register(output -> output.insertAfter(Items.TNT, ModItems.TNT_BARREL));

		CreativeModeTabEvents.modifyOutputEvent(FUNCTIONAL_BLOCKS)
				.register(output -> {
					output.insertAfter(Items.ENCHANTING_TABLE,
							ModItems.DISENCHANTER, ModItems.ENCHANTING_CRYSTAL, ModItems.CHARGED_ENCHANTING_CRYSTAL,
							ModItems.XP_BOTTLER);
					// L'epouvantail se pose et porte une armure : a cote du
					// porte-armure.
					output.insertAfter(Items.ARMOR_STAND, ModItems.SCARECROW);
					output.insertAfter(Items.CREEPER_HEAD, ModItems.ENDERMAN_HEAD);
					// La corde descend la ou l'echelle monte : juste apres elle.
					output.insertAfter(Items.LADDER, ModItems.ROPE, ModItems.ROPE_PLATE);
					// La lanterne d'eyeblossom suit les deux lanternes vanilla.
					output.insertAfter(Items.SOUL_LANTERN, ModItems.EYEBLOSSOM_LANTERN);
					// Les tas de buches suivent leur feu de camp.
					output.insertAfter(Items.CAMPFIRE, ModItems.CAMPFIRE_LOGS);
					output.insertAfter(Items.SOUL_CAMPFIRE, ModItems.SOUL_CAMPFIRE_LOGS);
				});

		// ---- Outils et utilitaires ---------------------------------------------
		CreativeModeTabEvents.modifyOutputEvent(TOOLS_AND_UTILITIES)
				.register(output -> {
					// L'outil en silex vient avant les outils en bois : c'est
					// le tout debut de la progression.
					output.insertBefore(Items.WOODEN_SHOVEL, ModItems.FLINT_TOOL);
					output.insertAfter(Items.RECOVERY_COMPASS, ModItems.ALTIMETER);
					output.insertAfter(Items.BUNDLE, ModItems.TOOL_BELT);
					output.insertAfter(Items.FLINT_AND_STEEL, ModItems.HOT_COAL_IN_A_BOTTLE);
					// Le support de canne a peche, juste apres la canne.
					output.insertAfter(Items.FISHING_ROD, ModItems.FISHING_ROD_STAND);
					output.insertAfter(Items.FIRE_CHARGE, ModItems.SMOKE_BOMB);
					// Avec les autres seaux a mob.
					output.insertAfter(Items.AXOLOTL_BUCKET, ModItems.ALLAY_BUCKET);
					// La cendre s'epand a la main comme la poudre d'os.
					output.insertAfter(Items.BONE_MEAL, ModItems.ASH);
					// L'onglet range les outils par matiere : la panoplie en or
					// rose reste groupee, juste apres celle en or.
					output.insertAfter(Items.GOLDEN_HOE,
							ModItems.PINK_GOLD_SHOVEL, ModItems.PINK_GOLD_PICKAXE,
							ModItems.PINK_GOLD_AXE, ModItems.PINK_GOLD_HOE);
					// Le carquois range des fleches : juste avant les sacs.
					output.insertBefore(Items.BUNDLE, ModItems.QUIVER);
					// Les ailes de fortune : la version pauvre des elytres.
					output.insertBefore(Items.ELYTRA, ModItems.PATCHWORK_ELYTRA);
				});

		// ---- Combat --------------------------------------------------------------
		CreativeModeTabEvents.modifyOutputEvent(COMBAT)
				.register(output -> {
					output.insertBefore(Items.WOODEN_SWORD, ModItems.FLINT_DAGGER);
					output.insertAfter(Items.GOLDEN_SWORD, ModItems.PINK_GOLD_SWORD);
					output.insertAfter(Items.GOLDEN_AXE, ModItems.PINK_GOLD_AXE);
					output.insertAfter(Items.GOLDEN_SPEAR, ModItems.PINK_GOLD_SPEAR);
					// La parure complete, juste apres celle en or.
					output.insertAfter(Items.GOLDEN_BOOTS,
							ModItems.PINK_GOLD_HELMET, ModItems.PINK_GOLD_CHESTPLATE,
							ModItems.PINK_GOLD_LEGGINGS, ModItems.PINK_GOLD_BOOTS);
					// Armes de jet et munitions.
					output.insertAfter(Items.CROSSBOW, ModItems.SLINGSHOT);
					output.insertAfter(Items.SNOWBALL, ModItems.PEBBLE);
				});

		// ---- Ingredients ----------------------------------------------------------
		CreativeModeTabEvents.modifyOutputEvent(INGREDIENTS)
				.register(output -> {
					// Fibres et ficelle vegetale : ce qui remplace la ficelle,
					// donc juste avant elle.
					output.insertBefore(Items.STRING, ModItems.PLANT_FIBER, ModItems.PLANT_CORD);
					output.insertAfter(Items.PHANTOM_MEMBRANE, ModItems.BAT_WING);
					output.insertAfter(Items.GUNPOWDER, ModItems.SULFUR_POWDER);
					output.insertAfter(ModItems.SULFUR_POWDER, ModItems.ASH);
					output.insertAfter(Items.SNOWBALL, ModItems.PEBBLE);
					output.insertAfter(Items.RAW_GOLD, ModItems.RAW_PINK_GOLD);
					output.insertAfter(Items.GOLD_INGOT, ModItems.PINK_GOLD_INGOT);
					output.insertAfter(Items.GOLD_NUGGET, ModItems.PINK_GOLD_NUGGET);
					output.insertAfter(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
							ModItems.PINK_GOLD_UPGRADE_SMITHING_TEMPLATE);
				});

		// ---- Nourriture -------------------------------------------------------------
		CreativeModeTabEvents.modifyOutputEvent(FOOD_AND_DRINKS)
				.register(output -> {
					output.insertAfter(Items.BEETROOT,
							ModItems.GOLDEN_BEETROOT, ModItems.ENCHANTED_GOLDEN_BEETROOT);
					output.insertAfter(Items.GLOW_BERRIES, ModItems.XP_BERRY);
					output.insertAfter(Items.CHORUS_FRUIT, ModItems.SOUL_PEPPER);
				});
	}

	private ModCreativeTabs() {
	}
}
