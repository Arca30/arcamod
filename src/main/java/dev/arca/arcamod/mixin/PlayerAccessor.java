package dev.arca.arcamod.mixin;

import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Donne acces a la graine d'enchantement du joueur.
 *
 * C'est elle qui decide des trois propositions d'une table d'enchantement ;
 * vanilla ne la change que lorsqu'un enchantement est paye. Le cristal
 * d'enchantement en a besoin pour relancer le tirage.
 */
@Mixin(Player.class)
public interface PlayerAccessor {

	@Accessor("enchantmentSeed")
	void arcamod$setEnchantmentSeed(int seed);
}
