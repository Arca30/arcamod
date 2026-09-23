package dev.arca.arcamod.registry;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.features.CaveFeatures;
import dev.arca.arcamod.block.MossSlabBlock;
import dev.arca.arcamod.block.MossStairBlock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.block.BrickStageBlock;
import dev.arca.arcamod.block.BrickStageSlabBlock;
import dev.arca.arcamod.block.BrickStageStairBlock;
import dev.arca.arcamod.block.BrickStageWallBlock;
import dev.arca.arcamod.block.ThatchBlock;
import dev.arca.arcamod.block.ThatchSlabBlock;
import dev.arca.arcamod.block.ThatchStairBlock;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Les blocs de decoration : escaliers et dalles pour des blocs vanilla qui
 * n'en avaient pas, plus la famille du chaume.
 *
 * Tout est genere par boucle : ajouter une couleur ou une matiere revient a
 * ajouter une ligne dans les listes ci-dessous, puis a relancer
 * tools/gen_decor_assets.py pour les fichiers de ressources.
 */
public final class ModDecorBlocks {

	/** Les seize couleurs du jeu, dans l'ordre vanilla. */
	public static final List<String> DYE_COLORS = List.of(
			"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
			"light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black");

	/** Les blocs vanilla qui recoivent escaliers et dalles. */
	public static final List<String> VANILLA_BASES = buildVanillaBases();

	/**
	 * Les quatre etapes du chaume, de la plus jaune a la plus grise. L'ordre
	 * compte : c'est lui qui definit le vieillissement.
	 */
	public static final List<String> THATCH_STAGES = List.of(
			"thatch_block", "weathered_thatch_block", "aged_thatch_block", "gray_thatch_block");

	/**
	 * Les etapes de la brique du mod, de la plus nette a la plus abimee.
	 * L'ordre compte : c'est lui qui definit le vieillissement.
	 *
	 * La toute premiere etape est la brique VANILLA (minecraft:bricks, ses
	 * escaliers, sa dalle et son muret) : elle vieillit en
	 * FIRST_WORN_BRICK_STAGE (voir VanillaBricks). Soit 5 aspects :
	 * vanilla, delavee, usee, patinee, fendillee.
	 */
	public static final List<String> BRICK_STAGES = List.of(
			"faded_brick_block", "worn_brick_block", "weathered_brick_block", "cracked_brick_block");

	/** L'etape ou arrive la brique VANILLA en vieillissant. */
	public static final String FIRST_WORN_BRICK_STAGE = BRICK_STAGES.getFirst();

	/**
	 * La brique CIREE : meme aspect que la brique vanilla, mais elle ne
	 * vieillit plus. C'est ce que devient une brique vanilla enduite de
	 * resine (le bloc vanilla n'a pas d'etat "cire"). Un coup de hache la
	 * rend vanilla.
	 */
	public static final String WAXED_BRICK = "waxed_brick_block";

	/** Tous les blocs poses par cette classe, dans l'ordre d'enregistrement. */
	private static final List<Block> ALL = new ArrayList<>();

	private static final Map<String, Block> BY_NAME = new LinkedHashMap<>();

	private static List<String> buildVanillaBases() {
		List<String> bases = new ArrayList<>();

		// La terre cuite "classique", sans couleur, avant les seize teintes.
		bases.add("terracotta");

		for (String colour : DYE_COLORS) {
			bases.add(colour + "_terracotta");
			bases.add(colour + "_concrete_powder");
		}

		bases.add("moss_block");
		bases.add("pale_moss_block");
		bases.add("hay_block");
		return List.copyOf(bases);
	}

	static {
		// --- escaliers et dalles pour les blocs vanilla ---
		for (String base : VANILLA_BASES) {
			Block vanilla = BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(base));
			// ofFullCopy reprend durete, son, outil et couleur sur la carte du
			// bloc d'origine : les escaliers se comportent comme leur matiere.
			stairsAndSlab(base, vanilla.defaultBlockState(), () -> BlockBehaviour.Properties.ofFullCopy(vanilla));
		}

		// --- la famille du chaume ---
		for (int i = 0; i < THATCH_STAGES.size(); i++) {
			String name = THATCH_STAGES.get(i);
			int index = i;

			// Etape suivante (vieillissement) et precedente (coup de hache).
			Block block = register(name,
					properties -> new ThatchBlock(properties,
							index + 1 < THATCH_STAGES.size() ? () -> BY_NAME.get(THATCH_STAGES.get(index + 1)) : null,
							index > 0 ? () -> BY_NAME.get(THATCH_STAGES.get(index - 1)) : null),
					thatchProperties(index));

			// Escaliers et dalles de chaume : memes etapes, donc mêmes
			// suppliers que le bloc plein.
			java.util.function.Supplier<Block> next =
					index + 1 < THATCH_STAGES.size() ? () -> BY_NAME.get(THATCH_STAGES.get(index + 1) + "_stairs") : null;
			java.util.function.Supplier<Block> previous =
					index > 0 ? () -> BY_NAME.get(THATCH_STAGES.get(index - 1) + "_stairs") : null;
			register(name + "_stairs",
					properties -> new ThatchStairBlock(block.defaultBlockState(), properties, next, previous),
					thatchProperties(index));

			java.util.function.Supplier<Block> nextSlab =
					index + 1 < THATCH_STAGES.size() ? () -> BY_NAME.get(THATCH_STAGES.get(index + 1) + "_slab") : null;
			java.util.function.Supplier<Block> previousSlab =
					index > 0 ? () -> BY_NAME.get(THATCH_STAGES.get(index - 1) + "_slab") : null;
			register(name + "_slab",
					properties -> new ThatchSlabBlock(properties, nextSlab, previousSlab),
					thatchProperties(index));
		}

		// --- la famille de la brique ---
		// Meme montage que le chaume, precede de la brique vanilla.
		for (int i = 0; i < BRICK_STAGES.size(); i++) {
			String name = BRICK_STAGES.get(i);
			int index = i;

			// La premiere etape redevient de la brique vanilla sous la hache.
			Block block = register(name,
					properties -> new BrickStageBlock(properties,
							index + 1 < BRICK_STAGES.size() ? () -> BY_NAME.get(BRICK_STAGES.get(index + 1)) : null,
							index > 0 ? () -> BY_NAME.get(BRICK_STAGES.get(index - 1)) : () -> Blocks.BRICKS),
					brickProperties());

			java.util.function.Supplier<Block> nextStairs =
					index + 1 < BRICK_STAGES.size() ? () -> BY_NAME.get(BRICK_STAGES.get(index + 1) + "_stairs") : null;
			java.util.function.Supplier<Block> previousStairs =
					index > 0 ? () -> BY_NAME.get(BRICK_STAGES.get(index - 1) + "_stairs") : () -> Blocks.BRICK_STAIRS;
			register(name + "_stairs",
					properties -> new BrickStageStairBlock(block.defaultBlockState(), properties, nextStairs, previousStairs),
					brickProperties());

			java.util.function.Supplier<Block> nextSlab =
					index + 1 < BRICK_STAGES.size() ? () -> BY_NAME.get(BRICK_STAGES.get(index + 1) + "_slab") : null;
			java.util.function.Supplier<Block> previousSlab =
					index > 0 ? () -> BY_NAME.get(BRICK_STAGES.get(index - 1) + "_slab") : () -> Blocks.BRICK_SLAB;
			register(name + "_slab",
					properties -> new BrickStageSlabBlock(properties, nextSlab, previousSlab),
					brickProperties());

			java.util.function.Supplier<Block> nextWall =
					index + 1 < BRICK_STAGES.size() ? () -> BY_NAME.get(BRICK_STAGES.get(index + 1) + "_wall") : null;
			java.util.function.Supplier<Block> previousWall =
					index > 0 ? () -> BY_NAME.get(BRICK_STAGES.get(index - 1) + "_wall") : () -> Blocks.BRICK_WALL;
			register(name + "_wall",
					properties -> new BrickStageWallBlock(properties, nextWall, previousWall),
					brickProperties());
		}

		// --- la brique ciree (aspect vanilla, ne vieillit pas) ---
		// Pas de randomTicks() et pas de propriete "waxed" : rien ne bouge
		// tant qu'un coup de hache ne l'a pas rendue vanilla (voir
		// VanillaBricks.unwax).
		register(WAXED_BRICK, Block::new, waxedBrickProperties());
		Block waxed = BY_NAME.get(WAXED_BRICK);
		register(WAXED_BRICK + "_stairs",
				properties -> new StairBlock(waxed.defaultBlockState(), properties), waxedBrickProperties());
		register(WAXED_BRICK + "_slab", SlabBlock::new, waxedBrickProperties());
		register(WAXED_BRICK + "_wall", WallBlock::new, waxedBrickProperties());
	}

	/** La brique ciree : comme la brique vanilla, sans ticks aleatoires. */
	private static BlockBehaviour.Properties waxedBrickProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS);
	}

	/**
	 * La brique du mod se comporte comme la brique vanilla (durete, son,
	 * pioche obligatoire), avec en plus les ticks aleatoires qui la font
	 * vieillir.
	 */
	private static BlockBehaviour.Properties brickProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).randomTicks();
	}

	/** Le chaume grise au fil des etapes, y compris sur la carte. */
	private static BlockBehaviour.Properties thatchProperties(int stage) {
		MapColor colour = switch (stage) {
			case 0 -> MapColor.COLOR_YELLOW;
			case 1 -> MapColor.SAND;
			case 2 -> MapColor.TERRACOTTA_WHITE;
			default -> MapColor.COLOR_LIGHT_GRAY;
		};

		return BlockBehaviour.Properties.of()
				.mapColor(colour)
				.strength(0.5F)
				.sound(SoundType.GRASS)
				.ignitedByLava()
				.randomTicks();
	}

	private static void stairsAndSlab(String base, net.minecraft.world.level.block.state.BlockState baseState,
			java.util.function.Supplier<BlockBehaviour.Properties> properties) {
		// La mousse (claire comme pale) garde sa propagation a la poudre d'os,
		// dalle et escalier compris : chacune pose sa propre structure.
		ResourceKey<Feature> mossPatch = switch (base) {
			case "moss_block" -> CaveFeatures.MOSS_PATCH_BONEMEAL;
			case "pale_moss_block" -> VegetationFeatures.PALE_MOSS_PATCH_BONEMEAL;
			default -> null;
		};

		register(base + "_stairs",
				stairProperties -> mossPatch != null
						? new MossStairBlock(baseState, stairProperties, mossPatch)
						: new StairBlock(baseState, stairProperties),
				properties.get());
		register(base + "_slab",
				slabProperties -> mossPatch != null
						? new MossSlabBlock(slabProperties, mossPatch)
						: new SlabBlock(slabProperties),
				properties.get());
	}

	/**
	 * Enregistre le bloc, puis son item, et le garde pour l'onglet creatif.
	 *
	 * Depuis 1.21.2, les Properties doivent connaitre leur cle AVANT que le
	 * bloc ne soit construit : d'ou la fabrique plutot qu'un bloc tout fait.
	 */
	private static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> factory,
			BlockBehaviour.Properties properties) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, ArcaMod.id(name));
		T registered = Registry.register(BuiltInRegistries.BLOCK, blockKey, factory.apply(properties.setId(blockKey)));

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, ArcaMod.id(name));
		Item item = new BlockItem(registered, new Item.Properties().useBlockDescriptionPrefix().setId(itemKey));
		((BlockItem) item).registerBlocks(Item.BY_BLOCK, item);
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);

		ALL.add(registered);
		BY_NAME.put(name, registered);
		return registered;
	}

	public static List<Block> all() {
		return List.copyOf(ALL);
	}

	public static Block byName(String name) {
		return BY_NAME.get(name);
	}

	public static void init() {
	}

	private ModDecorBlocks() {
	}
}
