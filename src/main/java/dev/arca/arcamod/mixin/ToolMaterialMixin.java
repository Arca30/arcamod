package dev.arca.arcamod.mixin;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.config.ArcaFeature;

import net.minecraft.world.item.ToolMaterial;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Nerf des outils en bois.
 *
 * ToolMaterial.WOOD est lu UNE seule fois, quand la classe Items se charge, et
 * ses valeurs sont alors recopiees dans les composants de chaque outil en bois
 * (durabilite, vitesse de minage). Il suffit donc de remplacer la constante a
 * la fin du bloc statique de ToolMaterial : tout ce qui est construit ensuite
 * (pioche, hache, pelle, houe et epee en bois, plus les outils en bois des
 * autres mods) prend automatiquement les nouvelles valeurs.
 *
 * Les valeurs se reglent dans ArcaBalance.
 */
@Mixin(ToolMaterial.class)
public class ToolMaterialMixin {

	@Shadow
	@Final
	@Mutable
	public static ToolMaterial WOOD;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void arcamod$nerfWoodenTools(CallbackInfo ci) {
		// Lu une seule fois au demarrage : changer l'interrupteur demande de
		// relancer le jeu.
		if (!ArcaFeature.WOODEN_TOOL_NERF.isEnabled()) {
			return;
		}

		// Au minimum 1 utilisation : une durabilite de 0 rendrait l'outil
		// inutilisable des le craft.
		int durability = Math.max(1, Math.round(WOOD.durability() * ArcaBalance.WOOD_DURABILITY_MULTIPLIER));
		float speed = WOOD.speed() * ArcaBalance.WOOD_MINING_SPEED_MULTIPLIER;

		ArcaMod.LOGGER.info("Outils en bois : durabilite {} -> {}, vitesse {} -> {}",
				WOOD.durability(), durability, WOOD.speed(), speed);

		WOOD = new ToolMaterial(
				WOOD.incorrectBlocksForDrops(),
				durability,
				speed,
				WOOD.attackDamageBonus(),
				WOOD.enchantmentValue(),
				WOOD.repairItems());
	}
}
