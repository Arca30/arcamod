package dev.arca.arcamod.client.render;

import dev.arca.arcamod.block.entity.PotionCauldronBlockEntity;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Donne au liquide du chaudron la couleur de la potion qu'il contient.
 *
 * Le modele du chaudron marque sa surface de liquide avec tintindex 0 ; c'est
 * cette source de teinte qui repond a la question "de quelle couleur ?".
 */
public class PotionCauldronTint implements BlockTintSource {

	/** Couleur de repli (l'eau vanilla) quand le bloc n'est pas encore lu. */
	private static final int DEFAULT_COLOUR = 0x3F76E4;

	@Override
	public int color(BlockState state) {
		return DEFAULT_COLOUR;
	}

	@Override
	public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron) {
			return cauldron.contents().getColorOr(DEFAULT_COLOUR);
		}

		return DEFAULT_COLOUR;
	}
}
