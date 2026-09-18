package dev.arca.arcamod.registry;

import java.util.function.Function;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.block.AshBlock;
import dev.arca.arcamod.block.BurntLogBlock;
import dev.arca.arcamod.block.ChickenEggsBlock;
import dev.arca.arcamod.block.DisenchanterBlock;
import dev.arca.arcamod.block.EnchantingCrystalBlock;
import dev.arca.arcamod.block.EndermanHeadBlock;
import dev.arca.arcamod.block.EndermanWallHeadBlock;
import dev.arca.arcamod.block.FallenSticksBlock;
import dev.arca.arcamod.block.IgnitedBurntLogBlock;
import dev.arca.arcamod.block.PebblesBlock;
import dev.arca.arcamod.block.PotionCauldronBlock;
import dev.arca.arcamod.block.SoulPepperBushBlock;
import dev.arca.arcamod.block.XpBottlerBlock;
import dev.arca.arcamod.block.XpBushBlock;
import dev.arca.arcamod.block.XpBushPlantBlock;
import dev.arca.arcamod.block.XpVines;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
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
					// eclaire comme la table d'enchantement vanilla (7)
					.lightLevel(state -> ArcaBalance.DISENCHANTER_LIGHT)
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

	// ---- Tete d'Enderman -------------------------------------------------------

	/** La tete d'Enderman posee au sol : detecteur de regard. */
	public static final EndermanHeadBlock ENDERMAN_HEAD = register("enderman_head", EndermanHeadBlock::new,
			BlockBehaviour.Properties.of()
					.instrument(NoteBlockInstrument.CUSTOM_HEAD)
					.strength(1.0F)
					.noOcclusion()
					.pushReaction(PushReaction.DESTROY));

	/** Sa variante murale : meme nom et meme butin que la tete au sol. */
	public static final EndermanWallHeadBlock ENDERMAN_WALL_HEAD = register("enderman_wall_head",
			EndermanWallHeadBlock::new,
			BlockBehaviour.Properties.of()
					.overrideLootTable(ENDERMAN_HEAD.getLootTable())
					.overrideDescription(ENDERMAN_HEAD.getDescriptionId())
					.instrument(NoteBlockInstrument.CUSTOM_HEAD)
					.strength(1.0F)
					.noOcclusion()
					.pushReaction(PushReaction.DESTROY));

	// ---- Soufre ----------------------------------------------------------------

	/**
	 * Le soufre epuise : un bloc de soufre vanilla deja brosse (voir
	 * SulfurBrushing). Memes reglages que le soufre ; il se lache lui-meme,
	 * pour qu'on ne puisse pas le reposer en soufre neuf et le rebrosser.
	 */
	public static final Block BRUSHED_SULFUR = register("brushed_sulfur", Block::new,
			BlockBehaviour.Properties.of()
					.sound(SoundType.SULFUR)
					.mapColor(MapColor.COLOR_YELLOW)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.requiresCorrectToolForDrops()
					.strength(1.5F, 6.0F));

	// ---- Piment des ames -------------------------------------------------------

	/** Le buisson de piments des ames : memes reglages que le buisson de baies sucrees. */
	public static final SoulPepperBushBlock SOUL_PEPPER_BUSH = register("soul_pepper_bush", SoulPepperBushBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_CYAN)
					.lightLevel(SoulPepperBushBlock::lightLevel)
					.randomTicks()
					.noCollision()
					.sound(SoundType.SWEET_BERRY_BUSH)
					.pushReaction(PushReaction.DESTROY));

	// ---- Or rose ---------------------------------------------------------------

	/** Minerai d'or rose : ne se trouve qu'au contact des filons d'or et de cuivre. */
	public static final Block PINK_GOLD_ORE = register("pink_gold_ore",
			properties -> new DropExperienceBlock(ConstantInt.of(0), properties),
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.STONE)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.requiresCorrectToolForDrops()
					.strength(3.0F, 3.0F));

	public static final Block DEEPSLATE_PINK_GOLD_ORE = register("deepslate_pink_gold_ore",
			properties -> new DropExperienceBlock(ConstantInt.of(0), properties),
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.DEEPSLATE)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.requiresCorrectToolForDrops()
					.strength(4.5F, 3.0F)
					.sound(SoundType.DEEPSLATE));

	/** Bloc d'or rose brut (9 or rose brut). Memes reglages que le bloc d'or brut vanilla. */
	public static final Block RAW_PINK_GOLD_BLOCK = register("raw_pink_gold_block", Block::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_PINK)
					.instrument(NoteBlockInstrument.BASEDRUM)
					.requiresCorrectToolForDrops()
					.strength(5.0F, 6.0F));

	/** Bloc d'or rose (9 lingots). */
	public static final Block PINK_GOLD_BLOCK = register("pink_gold_block", Block::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_PINK)
					.instrument(NoteBlockInstrument.BELL)
					.requiresCorrectToolForDrops()
					.strength(5.0F, 6.0F)
					.sound(SoundType.METAL));

	/**
	 * Enregistre un bloc. Depuis 1.21.2 il faut passer la ResourceKey aux
	 * Properties (setId) AVANT de construire le bloc, sinon le jeu crashe au
	 * demarrage.
	 */
	/**
	 * Buche brulee : laissee par le feu au pied des troncs (voir BurntLogs).
	 * Pas inflammable, pas dans #minecraft:logs (pas de planches). 6 charbons
	 * de bois a l'etabli.
	 */
	public static final BurntLogBlock BURNT_LOG = register("burnt_log", BurntLogBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_BLACK)
					.instrument(NoteBlockInstrument.BASS)
					.strength(ArcaBalance.BURNT_LOG_HARDNESS)
					.randomTicks()
					.sound(SoundType.WOOD));

	/**
	 * Cendre : couches empilables, comme la neige fine. Deposee par le feu
	 * (BurntLogs) et empilable a la main.
	 */
	public static final AshBlock ASH = register("ash", AshBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_GRAY)
					.strength(ArcaBalance.ASH_HARDNESS)
					.sound(SoundType.SAND)
					// Pas .replaceable() : sinon FallingBlock.isFree considere la
					// cendre comme du vide et un bloc qui tombe dessus se casse en
					// objet au lieu de se poser. L'empilement et le remplacement
					// passent par AshBlock.canBeReplaced.
					.forceSolidOff()
					.isViewBlocking((state, level, pos) -> state.getValue(AshBlock.LAYERS) >= AshBlock.MAX_LAYERS)
					.pushReaction(PushReaction.DESTROY));

	/**
	 * Buche brulee incandescente : ce que laisse le feu. S'eteint en buche
	 * brulee apres une minute, met le feu a ce qui la touche.
	 */
	public static final IgnitedBurntLogBlock IGNITED_BURNT_LOG = register("ignited_burnt_log", IgnitedBurntLogBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.FIRE)
					.instrument(NoteBlockInstrument.BASS)
					.strength(ArcaBalance.BURNT_LOG_HARDNESS)
					.lightLevel(state -> ArcaBalance.IGNITED_BURNT_LOG_LIGHT)
					.emissiveRendering(state -> true)
					.randomTicks()
					.sound(SoundType.WOOD));

	/** Bois brule : version 6 faces de la buche brulee. */
	public static final RotatedPillarBlock BURNT_WOOD = register("burnt_wood", RotatedPillarBlock::new,
			BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_BLACK)
					.instrument(NoteBlockInstrument.BASS)
					.strength(ArcaBalance.BURNT_LOG_HARDNESS)
					.sound(SoundType.WOOD));

	private static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> factory,
			BlockBehaviour.Properties properties) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, ArcaMod.id(name));
		T block = factory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.BLOCK, key, block);
	}

	/** Force le chargement de la classe (donc l'execution des champs static). */
	public static void init() {
		// .ignitedByLava() ne suffit pas pour le feu : il lit ses chances dans
		// ce registre (comme FireBlock.bootStrap en vanilla).
		FlammableBlockRegistry.getDefaultInstance().add(FALLEN_STICKS,
				ArcaBalance.FALLEN_STICKS_IGNITE_ODDS, ArcaBalance.FALLEN_STICKS_BURN_ODDS);
	}

	private ModBlocks() {
	}
}
