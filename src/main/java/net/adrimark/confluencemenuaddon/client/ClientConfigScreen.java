package net.adrimark.confluencemenuaddon.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Registers NeoForge's built-in generic config screen for this mod, so the config is reachable
 * from the vanilla Mods screen without requiring Configured or Mod Menu to be installed.
 */
public final class ClientConfigScreen {

    private ClientConfigScreen() {
    }

    public static void register(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
