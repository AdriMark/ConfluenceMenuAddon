package net.adrimark.confluencemenuaddon.mixin;

import org.confluence.mod.client.gui.BackgroundLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Exposes BackgroundLayer's private timeOfDay (for the sky gradient and time restore) and
// enabled (so the controller can tell when something else called closeLayers()).
@Mixin(value = BackgroundLayer.class, remap = false)
public interface BackgroundLayerAccessor {

    @Accessor("timeOfDay")
    static float getTimeOfDay() {
        throw new AssertionError("replaced by Mixin");
    }

    @Accessor("timeOfDay")
    static void setTimeOfDay(float timeOfDay) {
        throw new AssertionError("replaced by Mixin");
    }

    @Accessor("enabled")
    static boolean isEnabled() {
        throw new AssertionError("replaced by Mixin");
    }
}
