package dev.arca.arcamod.item;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.config.ArcaFeature;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Baie d'XP. Elle a deux usages sur le meme clic droit :
 *
 * - visee sur un bloc qui accepte la vegetation -> on plante le buisson
 *   (comportement herite de BlockItem) ;
 * - sinon -> on consomme UNE baie et on gagne de l'XP, instantanement.
 *
 * On ne passe volontairement pas par le systeme de nourriture (components
 * "food" + "consumable") : il impose une animation de plusieurs ticks et une
 * barre de faim, alors qu'on veut un effet immediat.
 */
public class XpBerryItem extends BlockItem {

	public XpBerryItem(Block block, Properties properties) {
		super(block, properties);
	}

	/**
	 * Clic droit en visant un bloc. BlockItem.useOn() tente la plantation ; si
	 * elle echoue (mauvais support, place occupee...) il renvoie FAIL, ce qui
	 * bloquerait le clic. On rattrape ce cas pour manger la baie a la place.
	 */
	@Override
	public InteractionResult useOn(UseOnContext context) {
		InteractionResult placeResult = super.useOn(context);
		Player player = context.getPlayer();

		if (placeResult.consumesAction() || player == null) {
			return placeResult;
		}

		return use(context.getLevel(), player, context.getHand());
	}

	/** Clic droit dans le vide : on mange. */
	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!ArcaFeature.XP_BERRY_EATING.isEnabled()) {
			return InteractionResult.PASS;
		}

		// Avant le consume() : sinon le stack peut etre vide et le cooldown
		// s'appliquerait a "air". Pose des deux cotes (serveur + client) pour
		// que l'affichage du cooldown soit immediat.
		if (ArcaBalance.XP_BERRY_COOLDOWN_TICKS > 0) {
			player.getCooldowns().addCooldown(stack, ArcaBalance.XP_BERRY_COOLDOWN_TICKS);
		}

		if (level instanceof ServerLevel serverLevel) {
			player.giveExperiencePoints(ArcaBalance.XP_BERRY_XP);
			serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
					0.6F, 1.5F + serverLevel.getRandom().nextFloat() * 0.3F);

			// consume() gere le mode creatif (n'enleve rien) et les stats.
			stack.consume(1, player);
		}

		return InteractionResult.SUCCESS;
	}
}
