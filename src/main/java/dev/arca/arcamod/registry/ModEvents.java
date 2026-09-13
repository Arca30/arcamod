package dev.arca.arcamod.registry;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.block.ChickenEggsBlock;
import dev.arca.arcamod.block.EnchantingCrystalBlock;
import dev.arca.arcamod.block.PotionCauldronBlock;
import dev.arca.arcamod.block.entity.PotionCauldronBlockEntity;
import dev.arca.arcamod.block.WeatheringThatch;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Les interactions du mod qui ne tiennent ni dans un bloc ni dans un item. */
public final class ModEvents {

	public static void init() {
		registerStickPlacing();
		registerEggPlacing();
		registerItemFrameHiding();
		registerThatchScraping();
		registerCrystalCharging();
		registerPotionPouring();
		registerThatchWaxing();
	}

	/**
	 * Un rayon de miel sur du chaume (bloc, escalier ou dalle) le fige dans
	 * son etat : il ne vieillira plus. Un coup de hache enleve la cire.
	 */
	private static void registerThatchWaxing() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
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
			ItemStack stack = player.getItemInHand(hand);

			if (!stack.is(Items.POTION) || player.isSpectator()) {
				return InteractionResult.PASS;
			}

			PotionContents poured = stack.get(DataComponents.POTION_CONTENTS);

			if (poured == null || poured.equals(PotionContents.EMPTY)) {
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

			level.setBlock(pos, level.getBlockState(pos)
					.setValue(PotionCauldronBlock.LEVEL, cauldron.visualLevel()), 3);
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
	private static void registerThatchScraping() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			ItemStack stack = player.getItemInHand(hand);

			if (!stack.is(ItemTags.AXES) || player.isSpectator()) {
				return InteractionResult.PASS;
			}

			BlockPos pos = hit.getBlockPos();
			BlockState state = level.getBlockState(pos);
			BlockState result;

			if (state.is(Blocks.HAY_BLOCK)) {
				result = ModDecorBlocks.byName(ModDecorBlocks.THATCH_STAGES.getFirst()).withPropertiesOf(state);
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
	 * Un eclat d'amethyste sur un cadre rempli le rend invisible. Le cadre
	 * reapparait tout seul quand on lui reprend son objet (ItemFrameMixin).
	 */
	private static void registerItemFrameHiding() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			ItemStack stack = player.getItemInHand(hand);

			if (!(entity instanceof ItemFrame frame) || !stack.is(Items.AMETHYST_SHARD)) {
				return InteractionResult.PASS;
			}

			if (frame.getItem().isEmpty() || frame.isInvisible()) {
				return InteractionResult.PASS;
			}

			if (!level.isClientSide()) {
				frame.setInvisible(true);
				level.playSound(null, frame.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
						SoundSource.BLOCKS, 0.8F, 1.2F);

				if (!player.hasInfiniteMaterials()) {
					stack.shrink(1);
				}
			}

			return InteractionResult.SUCCESS;
		});
	}

	private ModEvents() {
	}
}
