package dev.arca.arcamod.registry;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.entity.Scarecrow;
import dev.arca.arcamod.block.ChickenEggsBlock;
import dev.arca.arcamod.block.EnchantingCrystalBlock;
import dev.arca.arcamod.block.PotionCauldronBlock;
import dev.arca.arcamod.block.WeatheringThatch;
import dev.arca.arcamod.block.entity.PotionCauldronBlockEntity;
import dev.arca.arcamod.config.ArcaFeature;
import dev.arca.arcamod.entity.CampfireSeat;
import dev.arca.arcamod.menu.FletchingMenu;
import dev.arca.arcamod.util.CauldronWashing;
import dev.arca.arcamod.util.CopperOxidation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Les interactions du mod qui ne tiennent ni dans un bloc ni dans un item. */
public final class ModEvents {

	public static void init() {
		registerStickPlacing();
		registerEggPlacing();
		registerItemFrameHiding();
		registerScarecrowConversion();
		registerThatchScraping();
		registerCrystalCharging();
		registerPotionPouring();
		registerCauldronWashing();
		registerThatchWaxing();
		registerDrownedTridentDropChance();
		registerFireChargeThrowing();
		registerFletchingTable();
		registerArrowDipping();
		registerCampfireResting();
		registerFieryStrikes();
		registerCopperOxidation();
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
				player.getInventory().placeItemBackInInventory(tipped);
			} else if (count >= stack.getCount()) {
				player.setItemInHand(hand, tipped);
			} else {
				stack.shrink(count);
				player.getInventory().placeItemBackInInventory(tipped);
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
	 * S'asseoir pres d'un feu de camp : main vide, clic droit sur le dessus
	 * d'un bloc du tag arcamod:campfire_seats (buches, dalles, escaliers,
	 * tapis...) avec un feu de camp allume a portee. Le repos lui-meme est
	 * gere par CampfireSeat ; on se releve en s'accroupissant.
	 */
	private static void registerCampfireResting() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (!ArcaFeature.CAMPFIRE_RESTING.isEnabled() || hand != InteractionHand.MAIN_HAND
					|| !player.getMainHandItem().isEmpty() || player.isSecondaryUseActive()
					|| player.isSpectator() || player.isPassenger() || hit.getDirection() != Direction.UP) {
				return InteractionResult.PASS;
			}

			BlockPos pos = hit.getBlockPos();
			BlockState state = level.getBlockState(pos);

			if (!state.is(ModTags.CAMPFIRE_SEATS)) {
				return InteractionResult.PASS;
			}

			// La hauteur du point clique : sur un escalier, la marche basse ou
			// la marche haute selon l'endroit vise.
			double seatY = hit.getLocation().y;
			BlockPos seatBlock = BlockPos.containing(pos.getX() + 0.5, seatY, pos.getZ() + 0.5);

			if (!CampfireSeat.hasLitCampfireNearby(level, seatBlock)) {
				return InteractionResult.PASS;
			}

			// Deja un siege occupe a cet endroit.
			AABB area = new AABB(pos.getX(), seatY - 0.1, pos.getZ(), pos.getX() + 1.0, seatY + 0.1, pos.getZ() + 1.0);

			if (!level.getEntitiesOfClass(CampfireSeat.class, area).isEmpty()) {
				return InteractionResult.PASS;
			}

			if (!level.isClientSide()) {
				CampfireSeat seat = CampfireSeat.create(level, pos, seatY);
				level.addFreshEntity(seat);

				if (!player.startRiding(seat)) {
					seat.discard();
				}
			}

			return InteractionResult.SUCCESS;
		});
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

			if (!state.is(Blocks.WATER_CAULDRON)) {
				return InteractionResult.PASS;
			}

			var undyed = CauldronWashing.undyed(stack.getItem());

			if (undyed.isEmpty()) {
				return InteractionResult.PASS;
			}

			if (level.isClientSide()) {
				return InteractionResult.SUCCESS;
			}

			int count = Math.min(stack.getCount(), Math.max(1, ArcaBalance.CAULDRON_WASH_ITEMS_PER_LEVEL));
			// transmuteCopy garde les donnees de l'objet (nom, contenu d'un sac...).
			ItemStack washed = stack.transmuteCopy(undyed.get(), count);

			if (player.hasInfiniteMaterials()) {
				player.getInventory().placeItemBackInInventory(washed);
			} else if (count >= stack.getCount()) {
				player.setItemInHand(hand, washed);
			} else {
				stack.shrink(count);
				player.getInventory().placeItemBackInInventory(washed);
			}

			LayeredCauldronBlock.lowerFillLevel(state, level, pos);
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
	 * Un rayon de miel sur du chaume (bloc, escalier ou dalle) le fige dans
	 * son etat : il ne vieillira plus. Un coup de hache enleve la cire.
	 */
	private static void registerThatchWaxing() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (!ArcaFeature.THATCH.isEnabled()) {
				return InteractionResult.PASS;
			}

			ItemStack stack = player.getItemInHand(hand);

			if (!stack.is(Items.HONEYCOMB) || player.isSpectator()) {
				return InteractionResult.PASS;
			}

			if (!WeatheringThatch.applyWax(level, hit.getBlockPos(), level.getBlockState(hit.getBlockPos()))) {
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

	private static void registerThatchScraping() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (!ArcaFeature.THATCH.isEnabled()) {
				return InteractionResult.PASS;
			}

			ItemStack stack = player.getItemInHand(hand);

			if (!stack.is(ItemTags.AXES) || player.isSpectator()) {
				return InteractionResult.PASS;
			}

			BlockPos pos = hit.getBlockPos();
			BlockState state = level.getBlockState(pos);
			BlockState result;
			Block thatch = thatchFromHay(state);

			if (thatch != null) {
				// withPropertiesOf garde l'orientation et la moitie : un
				// escalier de foin donne le meme escalier en chaume.
				result = thatch.withPropertiesOf(state);
			} else {
				// Sur du chaume : la hache enleve la cire, ou rajeunit d'une
				// etape s'il n'y en a pas.
				result = WeatheringThatch.scrape(state);
			}

			if (result == null) {
				return InteractionResult.PASS;
			}

			if (!level.isClientSide()) {
				level.setBlockAndUpdate(pos, result);
				level.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
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
