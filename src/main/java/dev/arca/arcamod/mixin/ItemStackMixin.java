package dev.arca.arcamod.mixin;

import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.config.ArcaFeature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiConsumer;

/**
 * Les outils ne disparaissent plus quand leur durabilite tombe a zero.
 *
 * Un outil casse reste dans l'inventaire, s'affiche casse (le choix de la
 * texture est fait cote client, voir ItemModelResolverMixin) et
 * ne sert plus a rien (vitesse de minage nulle, aucun butin, aucun bonus
 * d'attaque, aucune utilisation au clic droit : arc, canne a peche, bouclier,
 * briquet, labour, ecorcage...) tant qu'il n'est pas repare.
 *
 * Deux cas differents au moment de la casse :
 *  - pile de plusieurs outils : comportement d'origine du mod, on consomme un
 *    exemplaire et on remet la barre a neuf pour les suivants ;
 *  - dernier exemplaire : il est conserve, casse.
 */
@Mixin(ItemStack.class)
public class ItemStackMixin {

	/**
	 * Ancien modele force sur les outils casses (avant les textures par
	 * outil). Il n'est plus pose, mais on le retire encore des objets de
	 * mondes existants lors de leur reparation.
	 */
	private static final Identifier BROKEN_MODEL = ArcaMod.id("broken_tool");

	private boolean arcamod$isBrokenTool() {
		ItemStack self = (ItemStack) (Object) this;
		return ArcaFeature.BROKEN_TOOLS_KEPT.isEnabled() && self.getCount() == 1 && self.isBroken();
	}

	/**
	 * Remplace le shrink(1) de vanilla, c'est-a-dire le moment exact ou
	 * l'outil est detruit.
	 */
	@WrapOperation(method = "applyDamage",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
	private void arcamod$keepBrokenTool(ItemStack stack, int count, Operation<Void> original) {
		if (!ArcaFeature.BROKEN_TOOLS_KEPT.isEnabled()) {
			original.call(stack, count); // vanilla : l'outil disparait
			return;
		}

		if (stack.getCount() > 1) {
			// La durabilite est portee par la pile : sans remise a neuf, les
			// exemplaires restants se briseraient tous au coup suivant.
			original.call(stack, count);
			stack.setDamageValue(0);
			return;
		}

		// Dernier exemplaire : on ne fait rien, il reste dans l'inventaire
		// avec sa durabilite a zero. L'apparence cassee est choisie par le
		// client (une texture par outil si elle existe).
	}

	/** Un outil deja casse ne s'use plus (et ne rejoue pas le son de casse). */
	@Inject(
			method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
			at = @At("HEAD"), cancellable = true)
	private void arcamod$stopDamagingBrokenTool(CallbackInfo ci) {
		if (this.arcamod$isBrokenTool()) {
			ci.cancel();
		}
	}

	/** Repare = on enleve l'ancien modele d'outil brise (mondes existants). */
	@Inject(method = "setDamageValue", at = @At("TAIL"))
	private void arcamod$clearBrokenModel(int value, CallbackInfo ci) {
		ItemStack self = (ItemStack) (Object) this;

		if (!self.isBroken() && BROKEN_MODEL.equals(self.get(DataComponents.ITEM_MODEL))) {
			self.remove(DataComponents.ITEM_MODEL);
		}
	}

	/**
	 * Clic droit inutilisable : dans le vide (arc, arbalete, canne a peche,
	 * trident, bouclier...). Les objets equipables (armures, elytres) restent
	 * enfilables et retirables.
	 */
	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void arcamod$brokenToolCannotBeUsed(Level level, Player player, InteractionHand hand,
			CallbackInfoReturnable<InteractionResult> cir) {
		if (this.arcamod$isBrokenTool() && !((ItemStack) (Object) this).has(DataComponents.EQUIPPABLE)) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	/** Clic droit sur un bloc : briquet, houe, hache, pelle, pinceau... */
	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
	private void arcamod$brokenToolCannotBeUsedOnBlock(UseOnContext context,
			CallbackInfoReturnable<InteractionResult> cir) {
		if (this.arcamod$isBrokenTool()) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	/** Clic droit sur une creature : cisailles sur un mouton, etc. */
	@Inject(method = "interactLivingEntity", at = @At("HEAD"), cancellable = true)
	private void arcamod$brokenToolCannotBeUsedOnEntity(Player player, LivingEntity target, InteractionHand hand,
			CallbackInfoReturnable<InteractionResult> cir) {
		if (this.arcamod$isBrokenTool()) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	@Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
	private void arcamod$brokenToolMinesSlowly(BlockState state, CallbackInfoReturnable<Float> cir) {
		if (this.arcamod$isBrokenTool()) {
			cir.setReturnValue(1.0F);
		}
	}

	@Inject(method = "isCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
	private void arcamod$brokenToolDropsNothing(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (this.arcamod$isBrokenTool()) {
			cir.setReturnValue(false);
		}
	}

	/** Plus de bonus de degats ni de vitesse d'attaque non plus. */
	@Inject(
			method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
			at = @At("HEAD"), cancellable = true)
	private void arcamod$brokenToolHasNoAttributes(EquipmentSlot slot,
			BiConsumer<net.minecraft.core.Holder<Attribute>, net.minecraft.world.entity.ai.attributes.AttributeModifier> consumer,
			CallbackInfo ci) {
		if (this.arcamod$isBrokenTool()) {
			ci.cancel();
		}
	}
}
