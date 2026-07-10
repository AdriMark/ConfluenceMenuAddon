package net.adrimark.confluencemenuaddon.client;

import net.minecraft.resources.ResourceLocation;

import net.adrimark.confluencemenuaddon.ConfluenceMenuAddon;
import net.adrimark.confluencemenuaddon.config.ClientConfig;

// Tracks whether the sun/moon easter egg has fired this session. Never reset on purpose - the
// logo swap sticks until the game restarts.
public final class TitleLogoController {

    // Native size, drawn 1:1.
    public static final int CUSTOM_LOGO_WIDTH = 274;
    public static final int CUSTOM_LOGO_HEIGHT = 44;

    public static final ResourceLocation CUSTOM_LOGO = ResourceLocation.fromNamespaceAndPath(
            ConfluenceMenuAddon.MODID, "textures/gui/title/minecraft_custom.png");

    private static boolean customLogoActive;

    private TitleLogoController() {
    }

    /** Does nothing if the easter egg is disabled in the config. */
    public static void activateCustomLogo() {
        if (ClientConfig.enableSunClickLogo) {
            customLogoActive = true;
        }
    }

    public static boolean isCustomLogoActive() {
        return customLogoActive;
    }
}
