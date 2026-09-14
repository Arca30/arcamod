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

	@Override
	public int color(BlockState state) {
		return PotionCauldronBlockEntity.DEFAULT_COLOUR;
	}

	/**
	 * Attention a la couleur : en 26.x la teinte est un ARGB qui multiplie
	 * aussi l'alpha du liquide. Une couleur sans alpha (0x3F76E4) rend le
	 * liquide invisible ; tintColour() renvoie donc toujours une couleur
	 * opaque.
	 */
	@Override
	public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof PotionCauldronBlockEntity cauldron) {
			int colour = cauldron.tintColour();
			cauldron.markRendered(colour);
			return colour;
		}

		PotionCauldronBlockEntity.debug("tint at {} found no block entity", pos);
		return PotionCauldronBlockEntity.DEFAULT_COLOUR;
	}
}
