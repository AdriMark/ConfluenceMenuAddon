package net.adrimark.confluencemenuaddon.client;

import java.lang.reflect.Field;

import com.mojang.logging.LogUtils;

import org.confluence.mod.client.gui.BackgroundLayer;
import org.joml.Vector2f;
import org.slf4j.Logger;

// Hit test for the sun/moon. Their positions are private fields on an anonymous enum subclass
// (BackgroundLayer.PLANET), so reflection is easier here than a mixin targeting a synthetic
// class name that'd shift around whenever Confluence touches the enum. If it breaks, log once
// and just disable the easter egg instead of crashing.
public final class PlanetClickDetector {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean reflectionBroken;

    private PlanetClickDetector() {
    }

    /** Whether the click landed inside the sun's or moon's current hit circle. */
    public static boolean isSunOrMoonClick(double mouseX, double mouseY) {
        if (reflectionBroken) {
            return false;
        }
        try {
            Object planet = BackgroundLayer.PLANET;
            Class<?> planetClass = planet.getClass();
            if (withinHitCircle(planet, planetClass, "sunPos", "sunSize", mouseX, mouseY)) {
                return true;
            }
            return withinHitCircle(planet, planetClass, "moonPos", "moonSize", mouseX, mouseY);
        } catch (ReflectiveOperationException | ClassCastException e) {
            reflectionBroken = true;
            LOGGER.warn("Confluence: Otherworld's sun/moon position fields changed shape; "
                    + "the sun/moon-click title logo easter egg is disabled", e);
            return false;
        }
    }

    private static boolean withinHitCircle(
            Object planet, Class<?> planetClass, String posField, String sizeField, double mouseX, double mouseY)
            throws ReflectiveOperationException {
        Field posF = planetClass.getDeclaredField(posField);
        Field sizeF = planetClass.getDeclaredField(sizeField);
        posF.setAccessible(true);
        sizeF.setAccessible(true);
        Vector2f pos = (Vector2f) posF.get(planet);
        int size = sizeF.getInt(planet);
        return pos.distance((float) mouseX, (float) mouseY) < size;
    }
}
