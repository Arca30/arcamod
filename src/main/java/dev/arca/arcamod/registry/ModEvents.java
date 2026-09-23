package dev.arca.arcamod.registry;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.entity.Scarecrow;
import dev.arca.arcamod.block.ChickenEggsBlock;
import dev.arca.arcamod.block.EnchantingCrystalBlock;
import dev.arca.arcamod.block.PotionCauldronBlock;
import dev.arca.arcamod.block.ArcaWeathering;
import dev.arca.arcamod.block.VanillaBricks;
import dev.arca.arcamod.util.FlintHint;
import dev.arca.arcamod.block.AshCauldronBlock;
import dev.arca.arcamod.block.entity.PotionCauldronBlockEntity;
import dev.arca.arcamod.block.CampfireLogsBlock;
import dev.arca.arcamod.config.ArcaFeature;
import dev.arca.arcamod.util.PitcherFeeding;
import dev.arca.arcamod.menu.FletchingMenu;
import dev.arca.arcamod.util.AllayBucket;
import dev.arca.arcamod.util.CampfireRest;
import dev.arca.arcamod.util.CauldronWashing;
import dev.arca.arcamod.util.CopperOxidation;
import dev.arca.arcamod.util.HideableSign;
import dev.arca.arcamod.util.Hints;
import dev.arca.arcamod.util.TntBarrels;

import net.minecraft.util.Mth;
import net.minecraft.util.Prediction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Les interactions du mod qui ne tiennent ni dans un bloc ni dans un item. */
public final class ModEvents {

	public static void init() {
		registerStickPlacing();
		registerEggPlacing();
		registerItemFrameHiding();
		registerItemFrameReveal();
		registerSignHiding();
		registerScarecrowConversion();
		registerScraping();
		registerCrystalCharging();
		registerPotionPouring();
		registerCauldronWashing();
		registerWaxing();
		registerDrownedTridentDropChance();
		registerFireChargeThrowing();
		registerFletchingTable();
		registerArrowDipping();
		registerCampfireResting();
		registerFieryStrikes();
		registerCopperOxidation();
		registerBareHandLogHint();
		registerAllayBucketing();
		// Avant registerTurtleBrushing : les evenements passent dans l'ordre.
		registerBrokenBrush();
		registerTurtleBrushing();
		registerTntBarrelConversion();
		registerFeatherPush();
		registerBladeInstantBreakWear();
		registerElytraWashing();
		registerPitcherFeeding();
		registerCampfireScraping();
		ServerTickEvents.END_SERVER_TICK.register(FlintHint::tick);
	}

	/**
	 * Pelle sur un feu de camp ETEINT : on en gratte la cendre.
	 *
	 * Le bloc devient un tas de buches (arcamod:campfire_logs, ou sa version
	 * des ames) et la cendre tombe au sol. Sur un feu ALLUME, rien ne change :
	 * la pelle l'eteint d'abord, comme en vanilla. Il faut donc deux coups de
	 * pelle pour arriver aux buches nues.
	 */
	private static void registerCampfireScraping() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (!ArcaFeature.CAMPFIRE_LOGS.isEnabled() || player.isSpectator()) {
				return InteractionResult.PASS;
			}

			ItemStack stack = player.getItemInHand(hand);

			if (!stack.is(ItemTags.SHOVELS)) {
				return InteractionResult.PASS;
			}

			BlockPos pos = hit.getBlockPos();
			BlockState state = level.getBlockState(pos);

			if (!(state.getBlock() instanceof CampfireBlock) || state.getValue(CampfireBlock.LIT)) {
				return InteractionResult.PASS;
			}

			Block logs = state.is(Blocks.SOUL_CAMPFIRE) ? ModBlocks.SOUL_CAMPFIRE_LOGS : ModBlocks.CAMPFIRE_LOGS;

			if (level instanceof ServerLevel serverLevel) {
				// Le feu de camp a un BlockEntity (ce qui cuit dessus) : le
				// remplacement le vide tout seul, comme quand on le casse.
				serverLevel.setBlockAndUpdate(pos, logs.defaultBlockState()
						.setValue(CampfireLogsBlock.FACING, state.getValue(CampfireBlock.FACING))
						.setValue(CampfireLogsBlock.WATERLOGGED, state.getValue(CampfireBlock.WATERLOGGED)));

				if (ArcaBalance.CAMPFIRE_ASH_SCRAPED > 0) {
					Block.popResource(serverLevel, pos, new ItemStack(ModItems.ASH, ArcaBalance.CAMPFIRE_ASH_SCRAPED));
				}

				serverLevel.sendParticles(ParticleTypes.ASH, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5,
						12, 0.3, 0.1, 0.3, 0.0);
				serverLevel.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

				if (!player.hasInfiniteMaterials()) {
					stack.hurtAndBreak(ArcaBalance.CAMPFIRE_SCRAPE_DURABILITY_COST, player, hand);
				}
			}

			level.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
			return InteractionResult.SUCCESS;
		});
	}

	/**
	 * Viande crue ou chair putrefiee sur une pitcher plant : la plante
	 * carnivore l'avale (voir util/PitcherFeeding).
	 *
	 * Le clic est intercepte avant le bloc lui-meme, ce qui permet de traiter
	 * de la meme facon la plante VANILLA (premiere bouchee) et celle du mod
	 * (les suivantes).
	 */
	private static void registerPitcherFeeding() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) ->
				PitcherFeeding.feed(player, level, hand, hit.getBlockPos()));
	}

	/**
	 * Les LAMES (dague, epees : tag arcamod:cuts_plant_fiber) s'usent aussi
	 * sur les blocs qui cassent en un coup (herbes, fleurs, pousses...).
	 * Sans ca, recolter des fibres ne couterait rien. Sur les autres blocs,
	 * c'est deja le cas en vanilla (composant "tool" : 2 points par bloc).
	 */
	private static void registerBladeInstantBreakWear() {
		PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
			if (ArcaBalance.BLADE_INSTANT_BREAK_DURABILITY_COST <= 0 || player.isSpectator()) {
				return;
			}

			ItemStack stack = player.getMainHandItem();

			if (!stack.is(ModTags.CUTS_PLANT_FIBER) || state.getDestroySpeed(level, pos) != 0.0F) {
				return;
			}

			stack.hurtAndBreak(ArcaBalance.BLADE_INSTANT_BREAK_DURABILITY_COST, player, EquipmentSlot.MAINHAND);
		});
	}

	private static void registerElytraWashing() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			ItemStack stack = player.getItemInHand(hand);

			if (!ArcaFeature.ELYTRA_DYEING.isEnabled() || player.isSpectator()
					|| !stack.has(DataComponents.GLIDER) || !stack.has(DataComponents.DYED_COLOR)) {
				return InteractionResult.PASS;
			}

			BlockPos pos = hit.getBlockPos();
			BlockState state = level.getBlockState(pos);
			boolean lye = state.is(ModBlocks.ASH_CAULDRON);

			if (!state.is(Blocks.WATER_CAULDRON) && !lye) {
				return InteractionResult.PASS;
			}

			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			}

			stack.remove(DataComponents.DYED_COLOR);

			if (lye) {
				AshCauldronBlock.lowerFillLevel(state, level, pos);
			} else {
				LayeredCauldronBlock.lowerFillLevel(state, level, pos);
			}

			level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.6F, 1.4F);
			player.awardStat(Stats.CLEAN_ARMOR);
			return InteractionResult.SUCCESS;
		});
	}

	/**
	 * Clic droit avec une TNT sur un tonneau contenant de la poudre de blaze :
	 * il devient un baril de TNT (voir TntBarrels).
	 */
	private static void registerTntBarrelConversion() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (!ArcaFeature.TNT_BARREL.isEnabled() || player.isSpectator()
					|| !player.getItemInHand(hand).is(Items.TNT)
					|| !level.getBlockState(hit.getBlockPos()).is(Blocks.BARREL)) {
				return InteractionResult.PASS;
			}

			// Le client ne connait pas le contenu du tonneau : il laisse le
			// serveur decider (et ouvrir le tonneau si ca ne marche pas).
			if (!(level instanceof ServerLevel serverLevel)) {
				return InteractionResult.PASS;
			}

			return TntBarrels.tryConvert(serverLevel, hit.getBlockPos(), player, player.getItemInHand(hand))
					? InteractionResult.SUCCESS
					: InteractionResult.PASS;
		});
	}

	/**
	 * Frapper une creature avec une plume en main : aucun degat, aucune
	 * colere, juste une poussee. Pratique pour guider les animaux.
	 */
	private static void registerFeatherPush() {
		AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (!ArcaFeature.FEATHER_PUSH.isEnabled() || player.isSpectator()
					|| !player.getItemInHand(hand).is(Items.FEATHER)
					|| !(entity instanceof LivingEntity living)
					|| living instanceof Player && !ArcaBalance.FEATHER_PUSH_AFFECTS_PLAYERS) {
				return InteractionResult.PASS;
			}

			if (level instanceof ServerLevel serverLevel) {
				float charge = player.getAttackStrengthScale(0.5F);
				float yaw = player.getYRot() * Mth.DEG_TO_RAD;
				living.knockback(ArcaBalance.FEATHER_PUSH_STRENGTH * charge, Mth.sin(yaw), -Mth.cos(yaw),
						player.damageSources().playerAttack(player), 0.0F);

				if (ArcaBalance.FEATHER_PUSH_UPWARD > 0.0) {
					living.setDeltaMovement(living.getDeltaMovement().add(0.0, ArcaBalance.FEATHER_PUSH_UPWARD * charge, 0.0));
				}

				living.needsSync = true;
				serverLevel.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE,
						player.getSoundSource(), 1.0F, 1.2F);
			}

			player.resetAttackStrengthTicker();
			return InteractionResult.SUCCESS;
		});
	}

	/**
	 * Pinceau casse (outils casses conserves) : il ne brosse plus rien, ni
	 * tatou (interaction vanilla de la creature, que ItemStackMixin ne voit
	 * pas) ni tortue. Il faut le reparer (plume a l'enclume).
	 */
	private static void registerBrokenBrush() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			ItemStack stack = player.getItemInHand(hand);

			if (ArcaFeature.BROKEN_TOOLS_KEPT.isEnabled() && stack.is(Items.BRUSH)
					&& stack.getCount() == 1 && stack.isBroken()) {
				return InteractionResult.FAIL;
			}

			return InteractionResult.PASS;
		});
	}

	/**
	 * Brosser une tortue adulte : une chance d'en detacher une ecaille.
	 *
	 * Meme geste que le tatou vanilla, avec deux garde-fous pour que ca ne
	 * devienne pas une usine a ecailles : un tirage au sort
	 * (ArcaBalance.TURTLE_BRUSH_SCUTE_CHANCE) et un temps de recharge sur le
	 * pinceau, applique meme quand le tirage rate.
	 */
	private static void registerTurtleBrushing() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (!ArcaFeature.TURTLE_BRUSHING.isEnabled() || player.isSpectator()
					|| !(entity instanceof Turtle turtle)) {
				return InteractionResult.PASS;
			}

			ItemStack stack = player.getItemInHand(hand);

			if (!stack.is(Items.BRUSH) || player.getCooldowns().isOnCooldown(stack)) {
				return InteractionResult.PASS;
			}

			if (ArcaBalance.TURTLE_BRUSH_ADULTS_ONLY && turtle.isBaby()) {
				return InteractionResult.PASS;
			}

			if (!(level instanceof ServerLevel serverLevel)) {
				return InteractionResult.SUCCESS;
			}

			player.getCooldowns().addCooldown(stack, ArcaBalance.TURTLE_BRUSH_COOLDOWN_TICKS);
			turtle.playSound(SoundEvents.BRUSH_GENERIC);
			turtle.gameEvent(GameEvent.ENTITY_INTERACT);
			stack.hurtAndBreak(ArcaBalance.TURTLE_BRUSH_TOOL_DAMAGE, player, hand.asEquipmentSlot());

			if (serverLevel.getRandom().nextFloat() < ArcaBalance.TURTLE_BRUSH_SCUTE_CHANCE) {
				turtle.spawnAtLocation(serverLevel,
						new ItemStack(Items.TURTLE_SCUTE, ArcaBalance.TURTLE_BRUSH_SCUTE_COUNT));
			}

			return InteractionResult.SUCCESS;
		});
	}

	/**
	 * Seau vide sur un allay : l'allay est range dans le seau, avec ce qu'il
	 * tient (voir AllayBucket). Le seau plein se vide d'un clic droit sur un
	 * bloc (AllayBucketItem).
	 */
	private static void registerAllayBucketing() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (!ArcaFeature.ALLAY_BUCKET.isEnabled() || player.isSpectator()
					|| !(entity instanceof Allay allay)) {
				return InteractionResult.PASS;
			}

			ItemStack stack = player.getItemInHand(hand);

			// Seau vide uniquement, et accroupi si le reglage l'exige : sinon
			// on ne pourrait plus donner un seau a porter a l'allay.
			if (!stack.is(Items.BUCKET) || (ArcaBalance.ALLAY_BUCKET_REQUIRES_SNEAK && !player.isShiftKeyDown())) {
				return InteractionResult.PASS;
			}

			if (!(level instanceof ServerLevel serverLevel)) {
				return InteractionResult.SUCCESS;
			}

			ItemStack filled = AllayBucket.fill(serverLevel, allay);

			if (filled == null) {
				return InteractionResult.PASS;
			}

			// Meme mecanique que les seaux a poisson : le seau vide est
			// consomme (sauf en creatif) et le seau plein prend sa place, ou
			// part dans l'inventaire si la main en tenait plusieurs.
			player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filled));
			return InteractionResult.SUCCESS;
		});
	}

	/**
	 * Premiere fois qu'un joueur tape du bois sans l'outil qu'il faut : une
	 * ligne d'explication, une seule fois dans la partie (voir Hints). Sans
	 * ca, la regle LOGS_REQUIRE_TOOL n'est visible nulle part.
	 */
	private static void registerBareHandLogHint() {
		AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
			if (!ArcaFeature.LOGS_REQUIRE_TOOL.isEnabled() || level.isClientSide()
					|| !(player instanceof ServerPlayer serverPlayer) || player.isCreative()) {
				return InteractionResult.PASS;
			}

			BlockState state = level.getBlockState(pos);

			// Le bon outil en main : le joueur a compris, on se tait.
			if (state.is(ModTags.REQUIRES_TOOL_FOR_DROPS) && !player.hasCorrectToolForDrops(state)) {
				Hints.showOnce(serverPlayer, "logs_require_tool");
			}

			// On n'empeche jamais le coup : le bloc casse, il ne donne juste rien.
			return InteractionResult.PASS;
		});
	}

	/**
	 * Frappe ardente (piment des ames), au corps a corps uniquement
	 * ("corps a corps" = l'attaquant est aussi l'entite qui touche : pas une
	 * fleche, pas un projectile) :
	 *  - les coups de celui qui a l'effet enflamment la cible (Aura de feu) ;
	 *  - ceux qui le frappent prennent feu a leur tour (riposte).
	 */
	private static void registerFieryStrikes() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			if (blocked || !ArcaFeature.SOUL_PEPPER.isEnabled()
					|| !(source.getEntity() instanceof LivingEntity attacker) || source.getDirectEntity() != attacker) {
				return;
			}

			if (attacker.hasEffect(ModEffects.FIERY_STRIKES)) {
				entity.igniteForSeconds(ArcaBalance.SOUL_PEPPER_IGNITE_SECONDS);
			}

			if (entity.hasEffect(ModEffects.FIERY_STRIKES) && ArcaBalance.SOUL_PEPPER_RETALIATION_IGNITE_SECONDS > 0) {
				attacker.igniteForSeconds(ArcaBalance.SOUL_PEPPER_RETALIATION_IGNITE_SECONDS);
			}
		});
	}

	/** Oxydation du cuivre et foudre attiree par le cuivre (voir CopperOxidation). */
	private static void registerCopperOxidation() {
		ServerTickEvents.END_LEVEL_TICK.register(CopperOxidation::tickLevel);
	}

	/**
	 * Clic droit sur une table d'archerie : ouvre le menu de fabrication de
	 * fleches (FletchingMenu). Accroupi avec un bloc en main, on pose le bloc
	 * normalement.
	 */
	private static void registerFletchingTable() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (!ArcaFeature.FLETCHING_TABLE.isEnabled() || player.isSpectator()) {
				return InteractionResult.PASS;
			}

			BlockPos pos = hit.getBlockPos();

			if (!level.getBlockState(pos).is(Blocks.FLETCHING_TABLE)) {
				return InteractionResult.PASS;
			}

			if (player.isSecondaryUseActive() && !player.getItemInHand(hand).isEmpty()) {
				return InteractionResult.PASS;
			}

			if (!level.isClientSide()) {
				player.openMenu(new SimpleMenuProvider(
						(containerId, inventory, p) -> new FletchingMenu(containerId, inventory,
								ContainerLevelAccess.create(level, pos)),
						Component.translatable("container.arcamod.fletching_table")));
			}

			return InteractionResult.SUCCESS;
		});
	}

	/**
	 * Clic droit sur un chaudron a potions avec des fleches : elles deviennent
	 * des fleches a effet. ArcaBalance.ARROW_DIP_ARROWS_PER_POTION fleches par
	 * fiole, ARROW_DIP_POTIONS_USED fioles retirees par trempage. Les pieces
	 * de la table d'archerie sont conservees.
	 */
	private static void registerArrowDipping() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (!ArcaFeature.CAULDRON_ARROW_DIPPING.isEnabled()) {
				return InteractionResult.PASS;
			}

			ItemStack stack = player.getItemInHand(hand);

			if (!stack.is(Items.ARROW) || player.isSpectator()) {
				return InteractionResult.PASS;
			}

			BlockPos pos = hit.getBlockPos();
			BlockState state = level.getBlockState(pos);

			if (!state.is(ModBlocks.POTION_CAULDRON)) {
				return InteractionResult.PASS;
			}

			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			}

			if (!(level.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron) || cauldron.filled() <= 0) {
				return InteractionResult.CONSUME;
			}

			// Copie AVANT de vider : le contenu reste lisible meme si le
			// chaudron redevient vanilla juste apres.
			PotionContents contents = cauldron.contents();
			int drained = cauldron.drain(Math.max(1, ArcaBalance.ARROW_DIP_POTIONS_USED));
			int count = Math.min(stack.getCount(), drained * Math.max(1, ArcaBalance.ARROW_DIP_ARROWS_PER_POTION));

			// transmuteCopy garde les composants (pieces de la fleche, nom...).
			ItemStack tipped = stack.transmuteCopy(Items.TIPPED_ARROW, count);
			tipped.set(DataComponents.POTION_CONTENTS, contents);

			if (player.hasInfiniteMaterials()) {
				player.getInventory().placeItemBackInInventory(tipped, Prediction.SERVER_ONLY);
			} else if (count >= stack.getCount()) {
				player.setItemInHand(hand, tipped);
			} else {
				stack.shrink(count);
				player.getInventory().placeItemBackInInventory(tipped, Prediction.SERVER_ONLY);
			}

			if (cauldron.filled() <= 0) {
				level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
			} else {
				BlockState updated = state.setValue(PotionCauldronBlock.LEVEL, cauldron.visualLevel());
				level.setBlock(pos, updated, 3);
				level.sendBlockUpdated(pos, updated, updated, Block.UPDATE_CLIENTS);
			}

			level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 0.8F, 1.2F);
			player.awardStat(Stats.USE_CAULDRON);
			return InteractionResult.SUCCESS;
		});
	}

	/**
	 * Repos au coin du feu : le bonus s'applique au joueur assis sur un
	 * COUSSIN (entite vanilla depuis 26.3) avec un feu de camp allume a
	 * portee. Voir CampfireRest.
	 *
	 * Avant 26.3, le mod posait un siege invisible pour pouvoir s'asseoir sur
	 * n'importe quelle buche ou dalle : le coussin vanilla fait ce travail,
	 * donc ce siege a ete retire.
	 */
	private static void registerCampfireResting() {
		ServerTickEvents.END_LEVEL_TICK.register(CampfireRest::tickLevel);
	}

	/**
	 * Clic droit sur un chaudron d'eau avec un bloc ou objet colore (laine,
	 * tapis, lit, terre cuite, verre teinte, bougie, sac) : il retrouve sa
	 * couleur d'origine, comme une armure en cuir. Chaque niveau d'eau lave
	 * jusqu'a ArcaBalance.CAULDRON_WASH_ITEMS_PER_LEVEL objets.
	 * La liste des objets lavables est dans CauldronWashing.
	 */
	private static void registerCauldronWashing() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (!ArcaFeature.CAULDRON_WASHING.isEnabled()) {
				return InteractionResult.PASS;
			}

			ItemStack stack = player.getItemInHand(hand);

			if (stack.isEmpty() || player.isSpectator()) {
				return InteractionResult.PASS;
			}

			BlockPos pos = hit.getBlockPos();
			BlockState state = level.getBlockState(pos);

			boolean lye = state.is(ModBlocks.ASH_CAULDRON);

			if (!state.is(Blocks.WATER_CAULDRON) && !lye) {
				return InteractionResult.PASS;
			}

			var undyed = CauldronWashing.undyed(stack.getItem());

			if (undyed.isEmpty()) {
				return InteractionResult.PASS;
			}

			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			}

			// La lessive lave bien plus d'objets par niveau que l'eau claire.
			int perLevel = Math.max(1, ArcaBalance.CAULDRON_WASH_ITEMS_PER_LEVEL)
					* (lye ? Math.max(1, ArcaBalance.ASH_CAULDRON_WASH_MULTIPLIER) : 1);
			int count = Math.min(stack.getCount(), perLevel);
			// transmuteCopy garde les donnees de l'objet (nom, contenu d'un sac...).
			ItemStack washed = stack.transmuteCopy(undyed.get(), count);

			if (player.hasInfiniteMaterials()) {
				player.getInventory().placeItemBackInInventory(washed, Prediction.SERVER_ONLY);
			} else if (count >= stack.getCount()) {
				player.setItemInHand(hand, washed);
			} else {
				stack.shrink(count);
				player.getInventory().placeItemBackInInventory(washed, Prediction.SERVER_ONLY);
			}

			if (lye) {
				AshCauldronBlock.lowerFillLevel(state, level, pos);
			} else {
				LayeredCauldronBlock.lowerFillLevel(state, level, pos);
			}
			level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.6F, 1.4F);
			player.awardStat(Stats.USE_CAULDRON);
			return InteractionResult.SUCCESS;
		});
	}

	/**
	 * Lancer une boule de feu a la main avec une charge de feu, exactement
	 * comme celle d'un ghast (meme entite, meme puissance d'explosion) : les
	 * dispensers tirent deja une SmallFireball inoffensive avec cet item,
	 * mais rien ne permettait au joueur de le faire lui-meme.
	 */
	private static void registerFireChargeThrowing() {
		UseItemCallback.EVENT.register((player, level, hand) -> {
			if (!ArcaFeature.FIRE_CHARGE_THROWING.isEnabled()) {
				return InteractionResult.PASS;
			}

			ItemStack stack = player.getItemInHand(hand);

			if (!stack.is(Items.FIRE_CHARGE) || player.isSpectator()
					|| player.getCooldowns().isOnCooldown(stack)) {
				return InteractionResult.PASS;
			}

			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GHAST_SHOOT,
					SoundSource.NEUTRAL, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

			if (level instanceof ServerLevel serverLevel) {
				Vec3 look = player.getViewVector(1.0F);
				LargeFireball fireball = new LargeFireball(serverLevel, player, look,
						ArcaBalance.FIRE_CHARGE_EXPLOSION_POWER);
				fireball.setPos(player.getX() + look.x, player.getEyeY() - 0.1, player.getZ() + look.z);
				serverLevel.addFreshEntity(fireball);

				if (!player.hasInfiniteMaterials()) {
					stack.shrink(1);
				}
			}

			player.getCooldowns().addCooldown(stack, ArcaBalance.FIRE_CHARGE_THROW_COOLDOWN_TICKS);
			player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
			return InteractionResult.SUCCESS;
		});
	}

	/**
	 * Le noye equipe d'un trident au spawn le lache selon la meme mecanique
	 * qu'un zombie avec une arme ramassee : une chance d'equipement fixe,
	 * ici reglable via ArcaBalance.DROWNED_TRIDENT_DROP_CHANCE (0.085 =
	 * comportement vanilla).
	 *
	 * On la reapplique a chaque chargement plutot qu'une seule fois a la
	 * generation : c'est idempotent, et ca couvre aussi bien les noyes
	 * generes par une ferme a spawner que ceux qui rechargent avec le monde.
	 */
	private static void registerDrownedTridentDropChance() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (!ArcaFeature.DROWNED_TRIDENT_DROPS.isEnabled()) {
				return;
			}

			if (!(entity instanceof Drowned drowned)) {
				return;
			}

			if (drowned.getMainHandItem().is(Items.TRIDENT)) {
				drowned.setDropChance(EquipmentSlot.MAINHAND, ArcaBalance.DROWNED_TRIDENT_DROP_CHANCE);
			}
		});
	}

	/**
	 * Une touffe de resine sur un bloc qui vieillit (chaume ou brique, bloc
	 * plein, escalier ou dalle) le fige dans son etat : il ne vieillira plus.
	 * Un coup de hache enleve la cire.
	 *
	 * La resine remplace ici le rayon de miel, qui reste reserve au cuivre.
	 */
	private static void registerWaxing() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			ItemStack stack = player.getItemInHand(hand);

			if (!stack.is(Items.RESIN_CLUMP) || player.isSpectator()) {
				return InteractionResult.PASS;
			}

			// applyWax verifie lui-meme l'interrupteur de la famille visee
			// (chaume ou brique). La brique vanilla a son propre cirage.
			BlockState target = level.getBlockState(hit.getBlockPos());

			if (!VanillaBricks.applyWax(level, hit.getBlockPos(), target)
					&& !ArcaWeathering.applyWax(level, hit.getBlockPos(), target)) {
				return InteractionResult.PASS;
			}

			if (!level.isClientSide() && !player.hasInfiniteMaterials()) {
				stack.shrink(1);
			}

			return InteractionResult.SUCCESS;
		});
	}

	/**
	 * Verser une potion dans un chaudron.
	 *
	 * Seules les potions normales sont acceptees : ni jet, ni persistante. La
	 * premiere fiole transforme le chaudron vanilla en chaudron a potions, les
	 * suivantes font monter le niveau - a condition que ce soit la meme
	 * potion.
	 */
	private static void registerPotionPouring() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (!ArcaFeature.POTION_CAULDRON.isEnabled()) {
				return InteractionResult.PASS;
			}

			ItemStack stack = player.getItemInHand(hand);

			if (!stack.is(Items.POTION) || player.isSpectator()) {
				return InteractionResult.PASS;
			}

			PotionContents poured = stack.get(DataComponents.POTION_CONTENTS);

			// La bouteille d'eau garde son comportement vanilla : elle remplit
			// le chaudron d'eau, au lieu d'en faire un chaudron "a potion d'eau".
			if (poured == null || poured.equals(PotionContents.EMPTY) || poured.is(Potions.WATER)) {
				return InteractionResult.PASS;
			}

			BlockPos pos = hit.getBlockPos();
			BlockState state = level.getBlockState(pos);
			boolean emptyCauldron = state.is(Blocks.CAULDRON);

			if (!emptyCauldron && !state.is(ModBlocks.POTION_CAULDRON)) {
				return InteractionResult.PASS;
			}

			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			}

			if (emptyCauldron) {
				level.setBlock(pos, ModBlocks.POTION_CAULDRON.defaultBlockState(), 3);
			}

			if (!(level.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron) || !cauldron.pour(poured)) {
				return InteractionResult.CONSUME;
			}

			BlockState updated = level.getBlockState(pos).setValue(PotionCauldronBlock.LEVEL, cauldron.visualLevel());
			level.setBlock(pos, updated, 3);

			// Sur la toute premiere fiole, le niveau visuel vaut deja 1 (l'etat
			// par defaut du bloc pose juste au-dessus) : le setBlock ci-dessus
			// ne change alors rien et le jeu ne renvoie donc rien au client,
			// qui garde un chaudron a l'air vide jusqu'a un rafraichissement
			// quelconque du chunk (update de bloc voisin, rechargement...).
			// On force explicitement l'envoi du bloc et de son contenu.
			level.sendBlockUpdated(pos, updated, updated, Block.UPDATE_CLIENTS);
			level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);

			if (!player.hasInfiniteMaterials()) {
				player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
			}

			return InteractionResult.SUCCESS;
		});
	}

	/**
	 * La mort d'une creature charge le cristal d'enchantement le plus proche,
	 * dans un rayon de ArcaBalance.CRYSTAL_CHARGE_RADIUS blocs.
	 */
	private static void registerCrystalCharging() {
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (!ArcaFeature.ENCHANTING_CRYSTAL.isEnabled()) {
				return;
			}

			if (entity instanceof net.minecraft.world.entity.player.Player) {
				return;
			}

			int radius = ArcaBalance.CRYSTAL_CHARGE_RADIUS;
			BlockPos death = entity.blockPosition();
			BlockPos closest = null;
			double closestDistance = Double.MAX_VALUE;

			for (BlockPos candidate : BlockPos.betweenClosed(death.offset(-radius, -radius, -radius),
					death.offset(radius, radius, radius))) {
				BlockState state = entity.level().getBlockState(candidate);

				if (!(state.getBlock() instanceof EnchantingCrystalBlock)
						|| state.getValue(EnchantingCrystalBlock.CHARGED)) {
					continue;
				}

				double distance = candidate.distSqr(death);

				if (distance < closestDistance) {
					closestDistance = distance;
					closest = candidate.immutable();
				}
			}

			if (closest != null) {
				EnchantingCrystalBlock.charge(entity.level(), closest);
			}
		});
	}

	/**
	 * La hache sur une botte de foin en fait sauter le lien : on obtient du
	 * chaume, qui grisera tout seul avec le temps. Sur du chaume deja
	 * patine, la hache le decape d'une etape, comme sur le cuivre.
	 */
	/**
	 * Le bloc de chaume correspondant a une botte de foin : le bloc plein,
	 * mais aussi sa dalle et son escalier (poses par ModDecorBlocks). Rend
	 * null si ce n'est pas du foin.
	 */
	private static Block thatchFromHay(BlockState state) {
		String thatch = ModDecorBlocks.THATCH_STAGES.getFirst();

		if (state.is(Blocks.HAY_BLOCK)) {
			return ModDecorBlocks.byName(thatch);
		}

		if (state.is(ModDecorBlocks.byName("hay_block_slab"))) {
			return ModDecorBlocks.byName(thatch + "_slab");
		}

		if (state.is(ModDecorBlocks.byName("hay_block_stairs"))) {
			return ModDecorBlocks.byName(thatch + "_stairs");
		}

		return null;
	}

	private static void registerScraping() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {

			ItemStack stack = player.getItemInHand(hand);

			if (!stack.is(ItemTags.AXES) || player.isSpectator()) {
				return InteractionResult.PASS;
			}

			BlockPos pos = hit.getBlockPos();
			BlockState state = level.getBlockState(pos);
			BlockState result;
			Block thatch = ArcaFeature.THATCH.isEnabled() ? thatchFromHay(state) : null;

			if (thatch != null) {
				// withPropertiesOf garde l'orientation et la moitie : un
				// escalier de foin donne le meme escalier en chaume.
				result = thatch.withPropertiesOf(state);
			} else {
				// Sur une brique ciree : elle redevient vanilla. Sinon (chaume,
				// brique du mod) : la hache enleve la cire, ou rajeunit d'une
				// etape s'il n'y en a pas.
				BlockState unwaxed = VanillaBricks.unwax(state);
				result = unwaxed != null ? unwaxed : ArcaWeathering.scrape(state);
			}

			if (result == null) {
				return InteractionResult.PASS;
			}

			if (!level.isClientSide()) {
				level.setBlockAndUpdate(pos, result);
				level.playSound(null, pos, SoundEvents.AXE_SCRAPE.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
				level.levelEvent(null, 3005, pos, 0);

				if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
					stack.hurtAndBreak(1, serverPlayer.level(), serverPlayer, item -> {
					});
				}
			}

			return InteractionResult.SUCCESS;
		});
	}

	/**
	 * Poser des oeufs au sol plutot que de les lancer : accroupi + clic droit,
	 * comme les branches et les petites pierres.
	 */
	private static void registerEggPlacing() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (!ArcaFeature.EGG_PLACING.isEnabled()) {
				return InteractionResult.PASS;
			}

			ItemStack stack = player.getItemInHand(hand);
			ChickenEggsBlock.EggColor colour = ChickenEggsBlock.EggColor.fromItem(stack.getItem());

			if (colour == null || !player.isSecondaryUseActive() || player.isSpectator()) {
				return InteractionResult.PASS;
			}

			BlockState clicked = level.getBlockState(hit.getBlockPos());
			boolean addToPile = clicked.is(ModBlocks.CHICKEN_EGGS)
					&& clicked.getValue(ChickenEggsBlock.COLOR) == colour
					&& clicked.getValue(ChickenEggsBlock.EGGS) < ArcaBalance.EGGS_MAX;
			BlockPos target = addToPile ? hit.getBlockPos() : hit.getBlockPos().relative(hit.getDirection());

			BlockState placed;

			if (addToPile) {
				placed = clicked.setValue(ChickenEggsBlock.EGGS, clicked.getValue(ChickenEggsBlock.EGGS) + 1);
			} else {
				BlockState existing = level.getBlockState(target);

				if (!existing.isAir() && !existing.canBeReplaced()) {
					return InteractionResult.PASS;
				}

				placed = ModBlocks.CHICKEN_EGGS.defaultBlockState().setValue(ChickenEggsBlock.COLOR, colour);

				if (!placed.canSurvive(level, target)) {
					return InteractionResult.PASS;
				}
			}

			if (!level.isClientSide()) {
				level.setBlock(target, placed, 3);
				ChickenEggsBlock.scheduleHatching(level, target);
				level.playSound(null, target, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 0.7F, 1.4F);

				if (!player.hasInfiniteMaterials()) {
					stack.shrink(1);
				}
			}

			return InteractionResult.SUCCESS;
		});
	}

	/** Poser une branche au sol avec un simple baton. */
	private static void registerStickPlacing() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (!ArcaFeature.STICK_PLACING.isEnabled()) {
				return InteractionResult.PASS;
			}

			ItemStack stack = player.getItemInHand(hand);

			// Poser une branche : baton en main + accroupi, comme la petite
			// pierre. L'accroupissement est indispensable, sinon un clic
			// droit avec un baton sur un coffre poserait une branche au lieu
			// d'ouvrir le coffre.
			if (!stack.is(Items.STICK) || !player.isSecondaryUseActive() || player.isSpectator()) {
				return InteractionResult.PASS;
			}

			BlockState clicked = level.getBlockState(hit.getBlockPos());
			BlockPos target = clicked.is(ModBlocks.FALLEN_STICKS)
					? hit.getBlockPos()
					: hit.getBlockPos().relative(hit.getDirection());

			if (!ModBlocks.FALLEN_STICKS.canPlaceAt(level, target)) {
				return InteractionResult.PASS;
			}

			if (!level.isClientSide()) {
				ModBlocks.FALLEN_STICKS.placeOne(level, target, level.getRandom());
				level.playSound(null, target, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 0.9F);

				if (!player.hasInfiniteMaterials()) {
					stack.shrink(1);
				}
			}

			return InteractionResult.SUCCESS;
		});
	}

	/**
	 * Une membrane de phantom sur un cadre rempli le rend invisible. Le cadre
	 * reapparait tout seul quand on lui reprend son objet (ItemFrameMixin).
	 */
	private static void registerItemFrameHiding() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (!ArcaFeature.ITEM_FRAME_HIDING.isEnabled()) {
				return InteractionResult.PASS;
			}

			ItemStack stack = player.getItemInHand(hand);

			if (!(entity instanceof ItemFrame frame) || !stack.is(Items.PHANTOM_MEMBRANE)) {
				return InteractionResult.PASS;
			}

			if (frame.getItem().isEmpty() || frame.isInvisible()) {
				return InteractionResult.PASS;
			}

			if (!level.isClientSide()) {
				frame.setInvisible(true);
				level.playSound(null, frame.blockPosition(), SoundEvents.PHANTOM_FLAP,
						SoundSource.BLOCKS, 0.8F, 1.2F);

				if (!player.hasInfiniteMaterials()) {
					stack.shrink(1);
				}
			}

			return InteractionResult.SUCCESS;
		});
	}

	/**
	 * La poudre d'os rend un cadre invisible de nouveau visible.
	 *
	 * Deux chemins, parce qu'un cadre invisible est intangible
	 * (BlockAttachedEntityPickMixin) : le clic vise alors le BLOC derriere
	 * lui, jamais le cadre. On ecoute donc les deux.
	 *
	 * L'autre facon de faire reapparaitre un cadre reste de lui reprendre son
	 * objet (ItemFrameMixin).
	 */
	private static void registerItemFrameReveal() {
		// Clic direct sur le cadre : sert quand la traversee des clics est
		// coupee (ArcaBalance.ITEM_FRAME_INVISIBLE_CLICK_THROUGH).
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (!(entity instanceof ItemFrame frame)) {
				return InteractionResult.PASS;
			}

			return revealItemFrame(player, level, hand, frame);
		});

		// Clic sur le bloc, a travers le cadre devenu intangible.
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			ItemFrame frame = invisibleFrameOn(player, level, hit.getBlockPos(), hit.getDirection());

			if (frame == null) {
				return InteractionResult.PASS;
			}

			return revealItemFrame(player, level, hand, frame);
		});
	}

	/** Le cadre invisible accroche a cette face, s'il y en a un. */
	private static ItemFrame invisibleFrameOn(Player player, Level level, BlockPos supportPos, Direction face) {
		if (!canRevealItemFrames(player)) {
			return null;
		}

		BlockPos framePos = supportPos.relative(face);

		// La boite d'un cadre colle a son bloc deborde sur le cube du
		// support : on cherche large, puis on trie sur le bloc et la face.
		for (ItemFrame frame : level.getEntitiesOfClass(ItemFrame.class, new AABB(supportPos).inflate(1.0))) {
			if (frame.isInvisible() && frame.getDirection() == face && framePos.equals(frame.getPos())) {
				return frame;
			}
		}

		return null;
	}

	private static boolean canRevealItemFrames(Player player) {
		return ArcaFeature.ITEM_FRAME_HIDING.isEnabled()
				&& ArcaBalance.ITEM_FRAME_BONE_MEAL_REVEALS
				&& !player.isSpectator();
	}

	private static InteractionResult revealItemFrame(Player player, Level level, InteractionHand hand, ItemFrame frame) {
		if (!canRevealItemFrames(player) || !frame.isInvisible()) {
			return InteractionResult.PASS;
		}

		ItemStack stack = player.getItemInHand(hand);

		if (!stack.is(Items.BONE_MEAL)) {
			return InteractionResult.PASS;
		}

		if (level instanceof ServerLevel serverLevel) {
			frame.setInvisible(false);

			serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
					frame.getX(), frame.getY(), frame.getZ(),
					ArcaBalance.ITEM_FRAME_REVEAL_PARTICLES, 0.2, 0.2, 0.2, 0.0);
			level.playSound(null, frame.blockPosition(), SoundEvents.BONE_MEAL_USE,
					SoundSource.BLOCKS, 1.0F, 1.0F);

			if (!player.hasInfiniteMaterials()) {
				stack.shrink(1);
			}
		}

		return InteractionResult.SUCCESS;
	}

	/**
	 * Membrane de phantom sur un panneau ecrit : la planche disparait, seul ce
	 * qui est ecrit reste. Poudre d'os : la planche revient.
	 *
	 * Le panneau garde sa forme et son contour : on peut toujours le viser, le
	 * casser et le modifier, contrairement au cadre invisible qui devient
	 * intangible.
	 */
	private static void registerSignHiding() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (!ArcaFeature.SIGN_HIDING.isEnabled() || player.isSpectator()) {
				return InteractionResult.PASS;
			}

			BlockPos pos = hit.getBlockPos();

			if (!(level.getBlockEntity(pos) instanceof SignBlockEntity sign)
					|| !(sign instanceof HideableSign hideable)) {
				return InteractionResult.PASS;
			}

			ItemStack stack = player.getItemInHand(hand);
			boolean hide = stack.is(Items.PHANTOM_MEMBRANE) && !hideable.arcamod$isHidden();
			boolean reveal = stack.is(Items.BONE_MEAL) && hideable.arcamod$isHidden();

			if (!hide && !reveal) {
				return InteractionResult.PASS;
			}

			// Un panneau vierge deviendrait un bloc fantome introuvable : on
			// exige du texte.
			if (hide && ArcaBalance.SIGN_HIDING_REQUIRES_TEXT && !signHasText(sign)) {
				return InteractionResult.PASS;
			}

			if (level instanceof ServerLevel serverLevel) {
				hideable.arcamod$setHidden(hide);

				serverLevel.sendParticles(hide ? ParticleTypes.POOF : ParticleTypes.HAPPY_VILLAGER,
						pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
						ArcaBalance.ITEM_FRAME_REVEAL_PARTICLES, 0.25, 0.25, 0.25, 0.0);
				level.playSound(null, pos, hide ? SoundEvents.PHANTOM_FLAP : SoundEvents.BONE_MEAL_USE,
						SoundSource.BLOCKS, 0.8F, hide ? 1.2F : 1.0F);

				if (!player.hasInfiniteMaterials()) {
					stack.shrink(1);
				}
			}

			return InteractionResult.SUCCESS;
		});
	}

	/** Le panneau porte-t-il au moins un mot, sur l'une ou l'autre face ? */
	private static boolean signHasText(SignBlockEntity sign) {
		for (SignTextSlot slot : SignTextSlot.values()) {
			if (sign.getText(slot).hasMessage(false)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Fabrication de l'epouvantail : clic droit avec une botte de foin sur un
	 * porte-armure qui porte un plastron et des jambieres en cuir. Le
	 * porte-armure est remplace par un epouvantail, au meme endroit et dans le
	 * meme sens ; le reste de son equipement (casque, bottes, objets en main)
	 * et son nom sont conserves.
	 */
	private static void registerScarecrowConversion() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (!ArcaFeature.SCARECROW.isEnabled() || player.isSpectator()) {
				return InteractionResult.PASS;
			}

			ItemStack hay = player.getItemInHand(hand);

			// Scarecrow herite de ArmorStand : on ne transforme que le vanilla.
			if (!(entity instanceof ArmorStand stand) || entity instanceof Scarecrow || !hay.is(Items.HAY_BLOCK)
					|| !stand.getItemBySlot(EquipmentSlot.CHEST).is(Items.LEATHER_CHESTPLATE)
					|| !stand.getItemBySlot(EquipmentSlot.LEGS).is(Items.LEATHER_LEGGINGS)) {
				return InteractionResult.PASS;
			}

			int cost = Math.max(0, ArcaBalance.SCARECROW_CONVERSION_HAY_COST);

			if (!player.hasInfiniteMaterials() && hay.getCount() < cost) {
				return InteractionResult.FAIL;
			}

			if (level instanceof ServerLevel serverLevel) {
				Scarecrow scarecrow = ModEntities.SCARECROW.create(serverLevel, EntitySpawnReason.CONVERSION);

				if (scarecrow == null) {
					return InteractionResult.FAIL;
				}

				scarecrow.snapTo(stand.getX(), stand.getY(), stand.getZ(), stand.getYRot(), 0.0F);
				scarecrow.setCustomName(stand.getCustomName());
				scarecrow.setCustomNameVisible(stand.isCustomNameVisible());

				for (EquipmentSlot slot : EquipmentSlot.values()) {
					boolean leather = slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS;

					if (leather && ArcaBalance.SCARECROW_CONVERSION_CONSUMES_LEATHER) {
						continue;
					}

					ItemStack worn = stand.getItemBySlot(slot);

					if (!worn.isEmpty()) {
						scarecrow.setItemSlot(slot, worn.copy());
					}
				}

				// Vide le porte-armure avant de le retirer : rien ne doit tomber.
				for (EquipmentSlot slot : EquipmentSlot.values()) {
					stand.setItemSlot(slot, ItemStack.EMPTY);
				}

				stand.discard();
				serverLevel.addFreshEntity(scarecrow);
				scarecrow.gameEvent(GameEvent.ENTITY_PLACE, player);

				serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HAY_BLOCK.defaultBlockState()),
						scarecrow.getX(), scarecrow.getY(0.6), scarecrow.getZ(), 30, 0.25, 0.4, 0.25, 0.05);
				serverLevel.playSound(null, scarecrow.getX(), scarecrow.getY(), scarecrow.getZ(),
						SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
				serverLevel.playSound(null, scarecrow.getX(), scarecrow.getY(), scarecrow.getZ(),
						SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);

				if (!player.hasInfiniteMaterials()) {
					hay.shrink(cost);
				}
			}

			return InteractionResult.SUCCESS;
		});
	}

	private ModEvents() {
	}
}
