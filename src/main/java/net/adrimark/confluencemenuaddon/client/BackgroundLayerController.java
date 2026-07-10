package net.adrimark.confluencemenuaddon.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.ConfigurationScreen.ConfigurationSectionScreen;
import net.neoforged.neoforge.client.gui.ModListScreen;

import org.confluence.mod.client.gui.AchievementScreen;
import org.confluence.mod.client.gui.BackgroundLayer;
import org.confluence.mod.client.gui.SecretSeedsSelectionScreen;

import net.adrimark.confluencemenuaddon.config.ClientConfig;
import net.adrimark.confluencemenuaddon.mixin.BackgroundLayerAccessor;

// BackgroundLayer is a static singleton, so this class keeps track of what's currently showing
// it and stops re-inits (which reroll timeOfDay, scenery and clouds) from resetting the sky
// every time you switch menus. Shared by the render mixins.
public final class BackgroundLayerController {

    private BackgroundLayerController() {
    }

    private static int lastWidth = -1;
    private static int lastHeight = -1;

    // Only the first init this launch keeps its random roll; every later one restores this.
    private static float savedTimeOfDay = -1.0F;

    // False while a world is loaded, since Options/Accessibility are also reachable from the
    // pause menu, where the world behind them should stay visible instead of getting covered.
    public static boolean isEnabledFor(Screen screen) {
        if (screen == null || Minecraft.getInstance().level != null) {
            return false;
        }
        if (screen instanceof TitleScreen) {
            return ClientConfig.enableMainMenu;
        }
        if (screen instanceof SelectWorldScreen) {
            return ClientConfig.enableWorldSelectScreen;
        }
        if (screen instanceof JoinMultiplayerScreen) {
            return ClientConfig.enableMultiplayerScreen;
        }
        if (screen instanceof OptionsScreen) {
            return ClientConfig.enableOptionsScreen;
        }
        if (screen instanceof CreateWorldScreen) {
            return ClientConfig.enableCreateWorldScreen;
        }
        if (screen instanceof ModListScreen) {
            return ClientConfig.enableModListScreen;
        }
        if (screen instanceof ConfigurationScreen || screen instanceof ConfigurationSectionScreen) {
            return ClientConfig.enableConfigScreen;
        }
        if (screen instanceof AccessibilityOptionsScreen) {
            return ClientConfig.enableAccessibilityScreen;
        }
        if (screen instanceof LanguageSelectScreen) {
            return ClientConfig.enableLanguageScreen;
        }
        return ClientConfig.additionalScreens.contains(screen.getClass().getName());
    }

    // Returns true if the caller should cancel the vanilla background it's replacing. Only calls
    // renderLayers once per frame even when layered screens stack, since it advances the
    // animation state internally and a second call would double the animation speed.
    public static boolean renderBackground(Screen screen, GuiGraphics guiGraphics, float partialTick) {
        if (!isEnabledFor(screen)) {
            return false;
        }
        if (!coveredByLayerAbove(screen)) {
            ensureInitialized(guiGraphics.guiWidth(), guiGraphics.guiHeight());
            BackgroundLayer.renderLayers(guiGraphics, partialTick);
        }
        // Snapshot even when covered: the screen above is advancing timeOfDay, and if it
        // closes the layers when it pops, this value is what the re-init restores.
        if (BackgroundLayerAccessor.isEnabled()) {
            savedTimeOfDay = BackgroundLayerAccessor.getTimeOfDay();
        }
        return true;
    }

    // True when a screen above this one in the layer stack already draws the layers this frame
    // (Confluence's own achievement/secret-seeds screens, or another screen we hook). Confluence
    // used to flag these with a Backgrounded interface but dropped it in 1.2.4, so class checks.
    private static boolean coveredByLayerAbove(Screen self) {
        Screen top = Minecraft.getInstance().screen;
        return top != null && top != self
                && (top instanceof AchievementScreen || top instanceof SecretSeedsSelectionScreen
                        || isEnabledFor(top));
    }

    // Only re-inits when the layers aren't running or the GUI size changed. Never calls
    // closeLayers ourselves — a dormant BackgroundLayer just ticks its cloud list, and leaving
    // it running across screens is what keeps transitions seamless.
    private static void ensureInitialized(int width, int height) {
        if (BackgroundLayerAccessor.isEnabled() && width == lastWidth && height == lastHeight) {
            return;
        }
        BackgroundLayer.initLayers(width, height);
        restoreSavedTime();
    }

    // Called from BackgroundLayerMixin after any initLayers that actually ran, so the
    // redundancy check below also covers inits Confluence itself triggers.
    public static void noteInited(int width, int height) {
        lastWidth = width;
        lastHeight = height;
    }

    public static boolean shouldSkipReinit(int width, int height) {
        return Minecraft.getInstance().level == null && width == lastWidth && height == lastHeight;
    }

    // With no world loaded, closeLayers can only be Confluence's achievement/secret-seeds screen
    // popping back to the menu below — killing the layers there would just force a re-init, so
    // we suppress it and let them keep running.
    public static boolean shouldKeepLayersRunning() {
        return Minecraft.getInstance().level == null;
    }

    public static void restoreSavedTime() {
        if (savedTimeOfDay >= 0.0F && Minecraft.getInstance().level == null) {
            BackgroundLayerAccessor.setTimeOfDay(savedTimeOfDay);
        }
    }
}
