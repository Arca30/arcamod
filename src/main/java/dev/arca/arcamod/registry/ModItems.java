package dev.arca.arcamod.registry;

import java.util.function.Function;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.item.AllayBucketItem;
import dev.arca.arcamod.item.AltimeterItem;
import dev.arca.arcamod.item.AshItem;
import dev.arca.arcamod.item.ChargedEnchantingCrystalItem;
import dev.arca.arcamod.item.FlintDaggerItem;
import dev.arca.arcamod.item.HotCoalBottleItem;
import dev.arca.arcamod.item.ModArmorMaterials;
import dev.arca.arcamod.item.ModFoods;
import dev.arca.arcamod.item.ModToolMaterials;
import dev.arca.arcamod.item.PebbleItem;
import dev.arca.arcamod.item.PinkGoldUpgradeTemplate;
import dev.arca.arcamod.item.QuiverItem;
import dev.arca.arcamod.item.RopeItem;
import dev.arca.arcamod.item.ScarecrowItem;
import dev.arca.arcamod.item.ShortenEffectsConsumeEffect;
import dev.arca.arcamod.item.SlingshotItem;
import dev.arca.arcamod.item.SmokeBombItem;
import dev.arca.arcamod.item.ToolBeltItem;
import dev.arca.arcamod.item.XpBerryItem;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.providers.number.floats.ResolvableFloat;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.equipment.ArmorType;
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

	/** Betterave doree : Celerite (duree et niveau dans ArcaBalance). */
	public static final Item GOLDEN_BEETROOT = register("golden_beetroot", Item::new,
			new Item.Properties().food(ModFoods.GOLDEN_BEETROOT, ModFoods.GOLDEN_BEETROOT_CONSUMABLE));

	/** Betterave doree enchantee : Celerite plus longue et plus forte. */
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

	/** Le tas de buches gratte d'un feu de camp (bloc a poser). */
	public static final Item CAMPFIRE_LOGS = register("campfire_logs",
			properties -> new BlockItem(ModBlocks.CAMPFIRE_LOGS, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	/** Celui d'un feu des ames. */
	public static final Item SOUL_CAMPFIRE_LOGS = register("soul_campfire_logs",
			properties -> new BlockItem(ModBlocks.SOUL_CAMPFIRE_LOGS, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	/** La lanterne d'eyeblossom (bloc a poser). */
	public static final Item EYEBLOSSOM_LANTERN = register("eyeblossom_lantern",
			properties -> new BlockItem(ModBlocks.EYEBLOSSOM_LANTERN, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	/** Le support de canne a peche (bloc a poser). */
	public static final Item FISHING_ROD_STAND = register("fishing_rod_stand",
			properties -> new BlockItem(ModBlocks.FISHING_ROD_STAND, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	/** Les branches au sol. Casser le bloc donne des batons vanilla. */
	public static final Item FALLEN_STICKS = register("fallen_sticks",
			properties -> new BlockItem(ModBlocks.FALLEN_STICKS, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	/**
	 * La corde : elle se pose par le bas et se deroule d'un coup en accroupi
	 * (voir RopeItem).
	 */
	public static final Item ROPE = register("rope",
			properties -> new RopeItem(ModBlocks.ROPE, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	/**
	 * La plaquette : le point d'amarrage de la corde, a poser sous un bloc.
	 */
	public static final Item ROPE_PLATE = register("rope_plate",
			properties -> new BlockItem(ModBlocks.ROPE_PLATE, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	/** Fibre vegetale : recoltee en coupant les herbes a la dague. */
	public static final Item PLANT_FIBER = register("plant_fiber", Item::new, new Item.Properties());

	/** Ficelle de fibres : le lien qui tient l'outil en silex sur son manche. */
	public static final Item PLANT_CORD = register("plant_cord", Item::new, new Item.Properties());

	/**
	 * L'aile de chauve-souris : lachee par les chauves-souris (voir
	 * ModLootTables.BAT_WING_DROP_CHANCE dans ArcaBalance). Simple ingredient
	 * pour l'instant, sans usage particulier.
	 */
	public static final Item BAT_WING = register("bat_wing", Item::new, new Item.Properties());

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
							// Ailes portees : assets/arcamod/equipment/patchwork_elytra.json
							// -> textures/entity/equipment/wings/patchwork_elytra.png
							.setAsset(ResourceKey.create(EquipmentAssets.ROOT_ID, ArcaMod.id("patchwork_elytra")))
							.setDamageOnHurt(false)
							.build())
					.repairable(Items.PHANTOM_MEMBRANE));

	/** Le cristal d'enchantement. */
	public static final Item ENCHANTING_CRYSTAL = register("enchanting_crystal",
			properties -> new BlockItem(ModBlocks.ENCHANTING_CRYSTAL, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	/**
	 * Sa version chargee : ne se trouve qu'en cassant un cristal charge (voir
	 * la table de butin), jamais en craft. Se pose directement pret a
	 * l'emploi.
	 */
	public static final Item CHARGED_ENCHANTING_CRYSTAL = register("charged_enchanting_crystal",
			ChargedEnchantingCrystalItem::new,
			new Item.Properties());

	/**
	 * Le carquois : 9 emplacements a fleches. Le composant CONTAINER vide par
	 * defaut permet a l'infobulle vanilla de lister son contenu.
	 */
	public static final Item QUIVER = register("quiver", QuiverItem::new,
			new Item.Properties()
					.stacksTo(1)
					.component(DataComponents.CONTAINER, net.minecraft.world.item.component.ItemContainerContents.EMPTY));

	/** Le desenchanteur. */
	public static final Item DISENCHANTER = register("disenchanter",
			properties -> new BlockItem(ModBlocks.DISENCHANTER, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	/** L'epouvantail d'entrainement (porte-armure + paille). */
	public static final Item SCARECROW = register("scarecrow", ScarecrowItem::new,
			new Item.Properties().stacksTo(16));

	// ---- Lance-pierre ----------------------------------------------------------

	/** Le lance-pierre : Puissance, Recul et Solidite (voir les tags d'enchantement). */
	public static final Item SLINGSHOT = register("slingshot", SlingshotItem::new,
			new Item.Properties()
					.durability(ArcaBalance.SLINGSHOT_DURABILITY)
					.enchantable(ArcaBalance.SLINGSHOT_ENCHANTMENT_VALUE)
					.repairable(Items.LEATHER));

	// ---- Tete d'Enderman -------------------------------------------------------

	/**
	 * Se pose au sol ou au mur, et se porte sur la tete comme un crane. Lachee
	 * par un Enderman tue par l'explosion d'un creeper charge.
	 */
	public static final Item ENDERMAN_HEAD = register("enderman_head",
			properties -> new StandingAndWallBlockItem(ModBlocks.ENDERMAN_HEAD, ModBlocks.ENDERMAN_WALL_HEAD,
					Direction.DOWN, properties),
			new Item.Properties()
					.rarity(Rarity.UNCOMMON)
					.equippableUnswappable(EquipmentSlot.HEAD)
					.useBlockDescriptionPrefix());

	// ---- Soufre ----------------------------------------------------------------

	/** Poudre de soufre : obtenue en brossant un bloc de soufre. */
	public static final Item SULFUR_POWDER = register("sulfur_powder", Item::new, new Item.Properties());

	public static final Item BRUSHED_SULFUR = register("brushed_sulfur",
			properties -> new BlockItem(ModBlocks.BRUSHED_SULFUR, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	// ---- Piment des ames -------------------------------------------------------

	/**
	 * Le piment des ames : se mange (Frappe ardente + Resistance au feu) et,
	 * comme les baies sucrees, se plante sur le sable ou la terre des ames.
	 */
	public static final Item SOUL_PEPPER = register("soul_pepper",
			properties -> new BlockItem(ModBlocks.SOUL_PEPPER_BUSH, properties),
			new Item.Properties()
					.useItemDescriptionPrefix()
					.food(ModFoods.SOUL_PEPPER, ModFoods.SOUL_PEPPER_CONSUMABLE));

	// ---- Or rose ---------------------------------------------------------------

	public static final Item RAW_PINK_GOLD = register("raw_pink_gold", Item::new, new Item.Properties());

	/** Le lingot donne aussi une couleur de garniture a la table de forge. */
	public static final Item PINK_GOLD_INGOT = register("pink_gold_ingot", Item::new,
			new Item.Properties().trimMaterial(ModArmorMaterials.PINK_GOLD_TRIM));

	public static final Item PINK_GOLD_NUGGET = register("pink_gold_nugget", Item::new, new Item.Properties());

	/**
	 * Modele de forge "Amelioration en or rose" : exige par toutes les
	 * recettes d'amelioration (or/cuivre -> or rose) de la table de forge.
	 * Trouve dans les coffres au tresor des epaves, se duplique au craft.
	 */
	public static final Item PINK_GOLD_UPGRADE_SMITHING_TEMPLATE = register("pink_gold_upgrade_smithing_template",
			PinkGoldUpgradeTemplate::create,
			new Item.Properties().rarity(Rarity.UNCOMMON));

	public static final Item PINK_GOLD_ORE = register("pink_gold_ore",
			properties -> new BlockItem(ModBlocks.PINK_GOLD_ORE, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	public static final Item DEEPSLATE_PINK_GOLD_ORE = register("deepslate_pink_gold_ore",
			properties -> new BlockItem(ModBlocks.DEEPSLATE_PINK_GOLD_ORE, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	public static final Item RAW_PINK_GOLD_BLOCK = register("raw_pink_gold_block",
			properties -> new BlockItem(ModBlocks.RAW_PINK_GOLD_BLOCK, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	public static final Item TNT_BARREL = register("tnt_barrel",
			properties -> new BlockItem(ModBlocks.TNT_BARREL, properties), new Item.Properties().useBlockDescriptionPrefix());

	public static final Item PINK_GOLD_BLOCK = register("pink_gold_block",
			properties -> new BlockItem(ModBlocks.PINK_GOLD_BLOCK, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	public static final Item PINK_GOLD_SWORD = register("pink_gold_sword", Item::new,
			new Item.Properties().sword(ModToolMaterials.PINK_GOLD,
					ArcaBalance.PINK_GOLD_SWORD_DAMAGE, ArcaBalance.PINK_GOLD_SWORD_SPEED));

	public static final Item PINK_GOLD_SHOVEL = register("pink_gold_shovel", Item::new,
			new Item.Properties().shovel(ModToolMaterials.PINK_GOLD,
					ArcaBalance.PINK_GOLD_SHOVEL_DAMAGE, ArcaBalance.PINK_GOLD_SHOVEL_SPEED));

	public static final Item PINK_GOLD_PICKAXE = register("pink_gold_pickaxe", Item::new,
			new Item.Properties().pickaxe(ModToolMaterials.PINK_GOLD,
					ArcaBalance.PINK_GOLD_PICKAXE_DAMAGE, ArcaBalance.PINK_GOLD_PICKAXE_SPEED));

	public static final Item PINK_GOLD_AXE = register("pink_gold_axe", Item::new,
			new Item.Properties().axe(ModToolMaterials.PINK_GOLD,
					ArcaBalance.PINK_GOLD_AXE_DAMAGE, ArcaBalance.PINK_GOLD_AXE_SPEED));

	public static final Item PINK_GOLD_HOE = register("pink_gold_hoe", Item::new,
			new Item.Properties().hoe(ModToolMaterials.PINK_GOLD,
					ArcaBalance.PINK_GOLD_HOE_DAMAGE, ArcaBalance.PINK_GOLD_HOE_SPEED));

	/** La lance : les 7 derniers parametres (timings de charge) sont ceux de la lance en fer. */
	public static final Item PINK_GOLD_SPEAR = register("pink_gold_spear", Item::new,
			new Item.Properties().spear(ModToolMaterials.PINK_GOLD,
					ArcaBalance.PINK_GOLD_SPEAR_ATTACK_DURATION, ArcaBalance.PINK_GOLD_SPEAR_DAMAGE_MULTIPLIER,
					0.6F, 2.5F, 11.0F, 6.75F, 5.1F, 11.25F, 4.6F));

	public static final Item PINK_GOLD_HELMET = register("pink_gold_helmet", Item::new,
			new Item.Properties().humanoidArmor(ModArmorMaterials.PINK_GOLD, ArmorType.HELMET));

	public static final Item PINK_GOLD_CHESTPLATE = register("pink_gold_chestplate", Item::new,
			new Item.Properties().humanoidArmor(ModArmorMaterials.PINK_GOLD, ArmorType.CHESTPLATE));

	public static final Item PINK_GOLD_LEGGINGS = register("pink_gold_leggings", Item::new,
			new Item.Properties().humanoidArmor(ModArmorMaterials.PINK_GOLD, ArmorType.LEGGINGS));

	public static final Item PINK_GOLD_BOOTS = register("pink_gold_boots", Item::new,
			new Item.Properties().humanoidArmor(ModArmorMaterials.PINK_GOLD, ArmorType.BOOTS));

	/** Altimetre : affiche l'altitude, et compare a une altitude memorisee (shift + clic droit). */
	public static final Item ALTIMETER = register("altimeter", AltimeterItem::new,
			new Item.Properties().stacksTo(ArcaBalance.ALTIMETER_MAX_STACK_SIZE));

	/**
	 * Seau a allay : un allay range dedans, avec l'objet qu'il tient. Il ne se
	 * fabrique pas, il s'obtient en attrapant un allay avec un seau vide.
	 */
	public static final Item ALLAY_BUCKET = register("allay_bucket", AllayBucketItem::new,
			new Item.Properties().stacksTo(1));

	/** Ceinture a outils : se porte dans la case au-dessus du bouclier (voir ToolBelt). */
	public static final Item TOOL_BELT = register("tool_belt", ToolBeltItem::new,
			new Item.Properties().stacksTo(1));

	/**
	 * Buche brulee : combustible au four. Depuis 26.3 la duree de combustion
	 * est un component de l'objet (il n'y a plus d'evenement Fabric pour ca).
	 */
	public static final Item BURNT_LOG = register("burnt_log",
			properties -> new BlockItem(ModBlocks.BURNT_LOG, properties),
			new Item.Properties().useBlockDescriptionPrefix()
					.component(DataComponents.COOKING_FUEL, burntLogFuel()));

	public static final Item IGNITED_BURNT_LOG = register("ignited_burnt_log",
			properties -> new BlockItem(ModBlocks.IGNITED_BURNT_LOG, properties),
			new Item.Properties().useBlockDescriptionPrefix()
					.component(DataComponents.COOKING_FUEL, burntLogFuel()));

	/** Un charbon de bois = 8 cuissons ; la vitesse de cuisson reste normale. */
	private static CookingFuel burntLogFuel() {
		int ticks = ArcaBalance.BURNT_LOG_FUEL_CHARCOAL * 8 * ArcaBalance.BASE_SMELT_TIME_TICKS;
		return new CookingFuel(new ResolvableInt.Constant(ticks), new ResolvableFloat.Constant(1.0F));
	}

	/** Braises en bouteille : allument un feu de camp ou un bloc tres inflammable. */
	public static final Item HOT_COAL_IN_A_BOTTLE = register("hot_coal_in_a_bottle", HotCoalBottleItem::new,
			new Item.Properties().stacksTo(16));

	/** Cendre : se pose et s'empile couche par couche, comme la neige fine. */
	public static final Item ASH = register("ash",
			properties -> new AshItem(ModBlocks.ASH, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	public static final Item PACKED_ASH = register("packed_ash",
			properties -> new BlockItem(ModBlocks.PACKED_ASH, properties),
			new Item.Properties().useBlockDescriptionPrefix());

	/** Bombe fumigene : cendre + soufre, pour se degager d'un combat. */
	public static final Item SMOKE_BOMB = register("smoke_bomb", SmokeBombItem::new,
			new Item.Properties().stacksTo(16));

	public static final Item BURNT_WOOD = register("burnt_wood",
			properties -> new BlockItem(ModBlocks.BURNT_WOOD, properties),
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
		// L'effet de consommation "raccourcit les effets" (gousse a pichet).
		// A enregistrer AVANT que les composants des items ne partent vers le
		// client : sans son type, l'effet ne peut pas etre envoye.
		ShortenEffectsConsumeEffect.register();

		// Un distributeur charge de petites pierres ou de dagues les lance
		// (c'est asProjectile() de chaque item qui fournit le projectile).
		DispenserBlock.registerProjectileBehavior(PEBBLE);
		DispenserBlock.registerProjectileBehavior(FLINT_DAGGER);

		// La touffe de resine vanilla devient une couleur de garniture a la
		// table de forge (data/arcamod/trim_material/resin_clump.json). Meme
		// resolution que Item.Properties.trimMaterial() : la couleur est un
		// registre de donnees, lue quand le monde charge.
		DefaultItemComponentEvents.MODIFY.register(context -> context.modify(Items.RESIN_CLUMP,
				(builder, registries, item) -> builder.set(DataComponents.PROVIDES_TRIM_MATERIAL,
						registries.getOrThrow(ModArmorMaterials.RESIN_CLUMP_TRIM))));
	}

	private ModItems() {
	}
}
