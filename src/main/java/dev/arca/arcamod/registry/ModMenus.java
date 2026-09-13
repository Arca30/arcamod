package dev.arca.arcamod.registry;

import dev.arca.arcamod.ArcaMod;
import dev.arca.arcamod.menu.DisenchanterMenu;
import dev.arca.arcamod.menu.XpBottlerMenu;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

/**
 * Les types de menu (= les GUI a conteneur) du mod.
 *
 * Le MenuType fait le lien entre les deux cotes : le serveur envoie son id au
 * client, qui utilise le constructeur enregistre ici pour recreer le menu
 * localement. C'est pour ca que le constructeur (containerId, inventory) doit
 * exister et se suffire a lui-meme.
 */
public final class ModMenus {

	public static final MenuType<XpBottlerMenu> XP_BOTTLER = Registry.register(
			BuiltInRegistries.MENU,
			ArcaMod.id("xp_bottler"),
			new MenuType<>(XpBottlerMenu::new, FeatureFlags.VANILLA_SET));

	public static final MenuType<DisenchanterMenu> DISENCHANTER = Registry.register(
			BuiltInRegistries.MENU,
			ArcaMod.id("disenchanter"),
			new MenuType<>(DisenchanterMenu::new, FeatureFlags.VANILLA_SET));

	public static void init() {
	}

	private ModMenus() {
	}
}
