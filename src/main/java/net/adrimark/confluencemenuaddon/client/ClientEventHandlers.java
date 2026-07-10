package net.adrimark.confluencemenuaddon.client;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

import org.confluence.mod.client.gui.BackgroundLayer;
import org.slf4j.Logger;

import net.adrimark.confluencemenuaddon.ConfluenceMenuAddon;

// Forwards mouse input to BackgroundLayer so the sun/moon stay draggable on our hooked screens.
// No tick handler here - Confluence already ticks the layers every client tick. No Closing
// handler either, the layers are meant to keep running across screen changes.
@EventBusSubscriber(modid = ConfluenceMenuAddon.MODID, value = Dist.CLIENT)
public final class ClientEventHandlers {

    private static final Logger LOGGER = LogUtils.getLogger();

    private ClientEventHandlers() {
    }

    @SubscribeEvent
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() == 0 && BackgroundLayerController.isEnabledFor(event.getScreen())) {
            // Must run before clickedLayers - that snaps the sun/moon to the cursor on a hit,
            // which would throw off the position check.
            if (PlanetClickDetector.isSunOrMoonClick(event.getMouseX(), event.getMouseY())) {
                TitleLogoController.activateCustomLogo();
            }
            if (safeClicked(event.getMouseX(), event.getMouseY())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseDragged(ScreenEvent.MouseDragged.Pre event) {
        if (event.getMouseButton() == 0 && BackgroundLayerController.isEnabledFor(event.getScreen())
                && safeDragged(event.getMouseX(), event.getMouseY(), event.getDragX(), event.getDragY())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (event.getButton() == 0 && BackgroundLayerController.isEnabledFor(event.getScreen())
                && safeReleased(event.getMouseX(), event.getMouseY())) {
            event.setCanceled(true);
        }
    }

    // Dragging the sun/moon tries to award a hidden achievement, which needs a running world -
    // on our menu screens there isn't one, so Confluence throws IllegalAccessError. Swallow it;
    // the drag itself already happened by then, only the achievement award no-ops.
    private static boolean safeClicked(double mouseX, double mouseY) {
        try {
            return BackgroundLayer.clickedLayers(mouseX, mouseY);
        } catch (LinkageError e) {
            LOGGER.warn("Confluence: Otherworld's sun/moon click handler failed (likely no world loaded); "
                    + "the drag itself still works, but it may not have awarded its hidden achievement", e);
            return true;
        }
    }

    private static boolean safeDragged(double mouseX, double mouseY, double dragX, double dragY) {
        try {
            return BackgroundLayer.dragLayers(mouseX, mouseY, dragX, dragY);
        } catch (LinkageError e) {
            LOGGER.warn("Confluence: Otherworld's sun/moon drag handler failed", e);
            return true;
        }
    }

    private static boolean safeReleased(double mouseX, double mouseY) {
        try {
            return BackgroundLayer.releasedLayers(mouseX, mouseY);
        } catch (LinkageError e) {
            LOGGER.warn("Confluence: Otherworld's sun/moon release handler failed", e);
            return true;
        }
    }
}
