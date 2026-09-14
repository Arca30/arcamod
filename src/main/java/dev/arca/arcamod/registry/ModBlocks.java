package dev.arca.arcamod.registry;

import java.util.function.Function;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.block.ChickenEggsBlock;
import dev.arca.arcamod.block.DisenchanterBlock;
import dev.arca.arcamod.block.EnchantingCrystalBlock;
import dev.arca.arcamod.block.FallenSticksBlock;
import dev.arca.arcamod.block.PebblesBlock;
import dev.arca.arcamod.block.PotionCauldronBlock;
import dev.arca.arcamod.block.XpBottlerBlock;
import dev.arca.arcamod.block.XpBushBlock;
import dev.arca.arcamod.block.XpBushPlantBlock;
import dev.arca.arcamod.block.XpVines;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/** Tous les blocs du mod. */
public final class ModBlocks {

	/** La tete : le segment du sommet, le seul qui pousse (randomTicks). */
	public static final XpBushBlock XP_BUSH = register("xp_bush", XpBushBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.PLANT)
					.randomTicks()
					.noCollision()
					.lightLevel(XpVines.emission(ArcaBalance.XP_BUSH_LIGHT_WITH_BERRIES))
					.instabreak()
					.sound(SoundType.CAVE_VINES)
					.pushReaction(PushReaction.DESTROY));

	/**
	 * Le corps : les segments du dessous. Il ne grandit pas, mais il a besoin
	 * de .randomTicks() pour que ses baies puissent repousser : sans ca, son
	 * randomTick() n'est jamais appele par le jeu.
	 */
	public static final XpBushPlantBlock XP_BUSH_PLANT = register("xp_bush_plant", XpBushPlantBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.PLANT)
					.randomTicks()
					.noCollision()
					.lightLevel(XpVines.emission(ArcaBalance.XP_BUSH_LIGHT_WITH_BERRIES))
					.instabreak()
					.sound(SoundType.CAVE_VINES)
					.pushReaction(PushReaction.DESTROY));

	/**
	 * L'embouteilleur d'XP. noOcclusion() est obligatoire : le bloc ne remplit
	 * pas son cube, sans ca le jeu masquerait les faces des blocs voisins et
	 * l'eclairage serait faux.
	 */
	public static final XpBottlerBlock XP_BOTTLER = register("xp_bottler", XpBottlerBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.METAL)
					.strength(3.5F)
					.requiresCorrectToolForDrops()
					.noOcclusion()
					.sound(SoundType.COPPER));

	/**
	 * Les petites pierres au sol.
	 *
	 * instabreak() : casse instantanement, a la main comme a l'outil.
	 * replaceable() : comme les feuilles mortes, on peut poser un bloc
	 * par-dessus sans avoir a les casser d'abord.
	 *
	 * Pas d'offsetType ici, volontairement : le decalage aleatoire du bloc
	 * decalerait aussi sa boite de selection. La variete de placement est
	 * dans les modeles (plusieurs variantes tirees au hasard par position).
	 */
	public static final PebblesBlock PEBBLES = register("pebbles", PebblesBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.STONE)
					.instabreak()
					.replaceable()
					.noCollision()
					.noOcclusion()
					.sound(SoundType.STONE)
					.pushReaction(PushReaction.DESTROY));

	/** Les branches mortes au sol. Memes reglages, sons de bois. */
	public static final FallenSticksBlock FALLEN_STICKS = register("fallen_sticks", FallenSticksBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.WOOD)
					.instabreak()
					.replaceable()
					.noCollision()
					.noOcclusion()
					.ignitedByLava()
					.sound(SoundType.WOOD)
					.pushReaction(PushReaction.DESTROY));

	/**
	 * Les oeufs de poule poses au sol. Fragiles, mais pas casses par les pas
	 * contrairement aux oeufs de tortue.
	 */
	public static final ChickenEggsBlock CHICKEN_EGGS = register("chicken_eggs", ChickenEggsBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.SAND)
					.strength(0.2F)
					.noOcclusion()
					.sound(SoundType.METAL)
					.pushReaction(PushReaction.DESTROY));

	/**
	 * Le cristal d'enchantement : taille de lanterne, pose sur les
	 * bibliotheques autour d'une table.
	 */
	public static final EnchantingCrystalBlock ENCHANTING_CRYSTAL = register("enchanting_crystal",
			EnchantingCrystalBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_PURPLE)
					// Pas de requiresCorrectToolForDrops() : combine a instabreak()
					// et a l'absence de tag mineable/*, AUCUN outil n'etait
					// "le bon", donc le cristal ne lachait jamais rien.
					.noCollision()
					.instabreak()
					.noOcclusion()
					.sound(SoundType.AMETHYST)
					// il s'allume doucement une fois charge
					.lightLevel(state -> state.getValue(EnchantingCrystalBlock.CHARGED) ? ArcaBalance.CRYSTAL_CHARGED_LIGHT : 0)
					.pushReaction(PushReaction.DESTROY));

	/** Le desenchanteur : un pupitre de pierre et d'obsidienne. */
	public static final DisenchanterBlock DISENCHANTER = register("disenchanter", DisenchanterBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_BLACK)
					.strength(3.5F)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE));

	/** Le chaudron rempli de potion. Il remplace le chaudron vanilla. */
	public static final PotionCauldronBlock POTION_CAULDRON = register("potion_cauldron", PotionCauldronBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.STONE)
					.strength(2.0F)
					.requiresCorrectToolForDrops()
					.noOcclusion());

	/**
	 * Enregistre un bloc. Depuis 1.21.2 il faut passer la ResourceKey aux
	 * Properties (setId) AVANT de construire le bloc, sinon le jeu crashe au
	 * demarrage.
	 */
	private static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> factory,
			BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ArcaMod.id(name));
		T block = factory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}

	/** Force le chargement de la classe (donc l'execution des champs static). */
	public static void init() {
	}

	private ModBlocks() {
	}
}
