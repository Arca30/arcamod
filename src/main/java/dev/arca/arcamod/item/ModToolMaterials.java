package dev.arca.arcamod.item;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.registry.ModTags;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

/** Les materiaux d'outil ajoutes par le mod. */
public final class ModToolMaterials {

	/**
	 * Le silex : l'etape entre le bois et la pierre.
	 *
	 * Un ToolMaterial regroupe cinq choses : les blocs qu'il ne sait PAS
	 * miner (ici la meme liste que la pierre, donc il descend jusqu'au fer),
	 * la durabilite, la vitesse de minage, le bonus de degats et
	 * l'enchantabilite, plus l'item qui sert a le reparer dans l'enclume.
	 *
	 * Toutes les valeurs viennent d'ArcaBalance.
	 */
	public static final ToolMaterial FLINT = new ToolMaterial(
			BlockTags.INCORRECT_FOR_STONE_TOOL,
			ArcaBalance.FLINT_DURABILITY,
			ArcaBalance.FLINT_MINING_SPEED,
			ArcaBalance.FLINT_ATTACK_DAMAGE_BONUS,
			ArcaBalance.FLINT_ENCHANTMENT_VALUE,
			ModTags.FLINT_TOOL_MATERIALS);

	private ModToolMaterials() {
	}
}
