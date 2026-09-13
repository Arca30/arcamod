package dev.arca.arcamod.registry;

import java.util.function.Function;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.item.FlintDaggerItem;
import dev.arca.arcamod.item.ModFoods;
import dev.arca.arcamod.item.ModToolMaterials;
import dev.arca.arcamod.item.PebbleItem;
import dev.arca.arcamod.item.XpBerryItem;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.util.Unit;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.DispenserBlock;

/**
 * Tous les items du mod.
 *
 * Note : le buisson n'a PAS d'item a lui. Comme les glow berries pour les
 * lianes des cavernes, c'est la baie qui pose le bloc.
 */
public final class ModItems {

	public static final Item XP_BERRY = register("xp_berry",
			properties -> new XpBerryItem(ModBlocks.XP_BUSH, properties),
			// useItemDescriptionPrefix : le nom affiche vient de
			// item.arcamod.xp_berry et pas de la cle du bloc.
			new Item.Properties().useItemDescriptionPrefix());

	/** Betterave doree : Celerite I pendant 10 minutes. */
	public static final Item GOLDEN_BEETROOT = register("golden_beetroot", Item::new,
			new Item.Properties().food(ModFoods.GOLDEN_BEETROOT, ModFoods.GOLDEN_BEETROOT_CONSUMABLE));

	/** Betterave doree enchantee : Celerite II pendant 20 minutes. */
	public static final Item ENCHANTED_GOLDEN_BEETROOT = register("enchanted_golden_beetroot", Item::new,
			new Item.Properties()
					.rarity(Rarity.RARE)
					.food(ModFoods.ENCHANTED_GOLDEN_BEETROOT, ModFoods.ENCHANTED_GOLDEN_BEETROOT_CONSUMABLE)
					// le reflet violet, comme la pomme doree enchantee
					.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));

	/** Item classique de pose de bloc, celui-la (nom tire de la cle du bloc). */
	public static final Item XP_BOTTLER = register("xp_bottler",
			properties -> new BlockItem(ModBlocks.XP_BOTTLER, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	/**
	 * La petite pierre : projectile facon boule de neige, et item de pose du
	 * bloc quand on s'accroupit.
	 */
	public static final Item PEBBLE = register("pebble",
			properties -> new PebbleItem(ModBlocks.PEBBLES, properties),
			// useItemDescriptionPrefix : le nom vient de item.arcamod.pebble,
			// pour nommer l'item ("petite pierre") differemment du bloc
			// ("petites pierres").
			new Item.Properties().useItemDescriptionPrefix());

	/** Les branches au sol. Casser le bloc donne des batons vanilla. */
	public static final Item FALLEN_STICKS = register("fallen_sticks",
			properties -> new BlockItem(ModBlocks.FALLEN_STICKS, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	/** Fibre vegetale : recoltee en coupant les herbes a la dague. */
	public static final Item PLANT_FIBER = register("plant_fiber", Item::new, new Item.Properties());

	/** Ficelle de fibres : le lien qui tient l'outil en silex sur son manche. */
	public static final Item PLANT_CORD = register("plant_cord", Item::new, new Item.Properties());

	/**
	 * L'outil en silex : pioche + hache + pelle en un seul objet.
	 *
	 * tool(materiau, blocs mines vite, degats, vitesse d'attaque, secondes de
	 * blocage du bouclier). Le dernier parametre reste a 0 : contrairement a
	 * une hache, l'outil en silex ne desactive pas les boucliers.
	 */
	public static final Item FLINT_TOOL = register("flint_tool", Item::new,
			new Item.Properties().tool(
					ModToolMaterials.FLINT,
					ModTags.MINEABLE_WITH_FLINT_TOOL,
					ArcaBalance.attackDamageFromTotal(
							ArcaBalance.FLINT_TOTAL_ATTACK_DAMAGE, ArcaBalance.FLINT_ATTACK_DAMAGE_BONUS),
					ArcaBalance.FLINT_ATTACK_SPEED,
					0.0F));

	/**
	 * La dague en silex : une epee rapide, lancable au clic droit.
	 *
	 * sword() pose le comportement d'epee (toiles d'araignee, bambou...),
	 * puis on ecrase la durabilite du materiau par celle de la dague.
	 */
	public static final Item FLINT_DAGGER = register("flint_dagger", FlintDaggerItem::new,
			new Item.Properties()
					.sword(ModToolMaterials.FLINT, ArcaBalance.daggerAttackDamage(), ArcaBalance.daggerAttackSpeed())
					.durability(ArcaBalance.DAGGER_DURABILITY)
					.enchantable(ArcaBalance.DAGGER_ENCHANTMENT_VALUE));

	/**
	 * Les elytres de fortune : des membranes de phantom cousues ensemble.
	 *
	 * Memes composants que les elytres vanilla (planeur + equipement de
	 * torse, avec leur modele d'ailes), mais sans .enchantable() : impossible
	 * d'y mettre Solidite ou Raccommodage, donc la durabilite reste le vrai
	 * frein.
	 */
	public static final Item PATCHWORK_ELYTRA = register("patchwork_elytra", Item::new,
			new Item.Properties()
					.durability(ArcaBalance.PATCHWORK_ELYTRA_DURABILITY)
					.component(DataComponents.GLIDER, Unit.INSTANCE)
					.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST)
							.setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA)
							.setAsset(EquipmentAssets.ELYTRA)
							.setDamageOnHurt(false)
							.build())
					.repairable(Items.PHANTOM_MEMBRANE));

	/** Le cristal d'enchantement. */
	public static final Item ENCHANTING_CRYSTAL = register("enchanting_crystal",
			properties -> new BlockItem(ModBlocks.ENCHANTING_CRYSTAL, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	/** Le desenchanteur. */
	public static final Item DISENCHANTER = register("disenchanter",
			properties -> new BlockItem(ModBlocks.DISENCHANTER, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ArcaMod.id(name));
		Item item = factory.apply(properties.setId(key));

		// Comme le fait Items.registerItem() en vanilla : sans cette ligne,
		// block.asItem() renverrait AIR pour le buisson.
		if (item instanceof BlockItem blockItem) {
			blockItem.registerBlocks(Item.BY_BLOCK, item);
		}

		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	public static void init() {
		// Un distributeur charge de petites pierres ou de dagues les lance
		// (c'est asProjectile() de chaque item qui fournit le projectile).
		DispenserBlock.registerProjectileBehavior(PEBBLE);
		DispenserBlock.registerProjectileBehavior(FLINT_DAGGER);
	}

	private ModItems() {
	}
}
