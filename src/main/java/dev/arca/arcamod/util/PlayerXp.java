package dev.arca.arcamod.util;

import net.minecraft.world.entity.player.Player;

/**
 * Calculs d'experience.
 *
 * Minecraft ne stocke pas un "total d'XP" fiable sur le joueur : il garde un
 * niveau + une progression dans la barre. Le champ totalExperience existe mais
 * il est faux des qu'on touche au niveau par commande. On recalcule donc le
 * total a partir du niveau, avec les memes formules que le jeu.
 */
public final class PlayerXp {

	/** XP cumulee necessaire pour atteindre ce niveau depuis 0. */
	private static int totalXpAtLevel(int level) {
		if (level <= 16) {
			return level * level + 6 * level;
		}
		if (level <= 31) {
			return (int) (2.5 * level * level - 40.5 * level + 360.0);
		}
		return (int) (4.5 * level * level - 162.5 * level + 2220.0);
	}

	/** XP totale du joueur, en points. */
	public static int getTotal(Player player) {
		return totalXpAtLevel(player.experienceLevel)
				+ Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
	}

	/**
	 * Retire des points d'XP. giveExperiencePoints() gere les valeurs
	 * negatives : il redescend les niveaux un par un et s'arrete a 0.
	 */
	public static void take(Player player, int amount) {
		if (amount > 0) {
			player.giveExperiencePoints(-amount);
		}
	}

	private PlayerXp() {
	}
}
