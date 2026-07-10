package net.adrimark.confluencemenuaddon.mixin;

import net.minecraft.client.gui.GuiGraphics;

import org.confluence.mod.client.gui.BackgroundLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.adrimark.confluencemenuaddon.client.BackgroundLayerController;
import net.adrimark.confluencemenuaddon.client.EnvironmentParallax;
import net.adrimark.confluencemenuaddon.config.ClientConfig;

// Hooks BackgroundLayer's lifecycle and render loop: keeps the background continuous across
// menu changes, applies the animation speed multiplier, and swaps in EnvironmentParallax for
// the sky/scenery draws.
@Mixin(value = BackgroundLayer.class, remap = false)
public abstract class BackgroundLayerMixin {

    @Unique
    private static boolean confluencemenuaddon$wasRunningBeforeInit;

    // Confluence's AchievementScreen.init calls initLayers every time it opens over the title
    // screen, which used to visibly reset the sky. Skip it if we're already running at the same
    // size; real size changes and fresh inits still go through.
    @Inject(method = "initLayers", at = @At("HEAD"), cancellable = true)
    private static void confluencemenuaddon$skipRedundantInit(int width, int height, CallbackInfo ci) {
        boolean running = BackgroundLayerAccessor.isEnabled();
        if (running && BackgroundLayerController.shouldSkipReinit(width, height)) {
            ci.cancel();
            return;
        }
        confluencemenuaddon$wasRunningBeforeInit = running;
    }

    // Record the size, and if this interrupted a running background (only possible via a size
    // change, same-size re-inits are cancelled above) restore the saved time.
    @Inject(method = "initLayers", at = @At("TAIL"))
    private static void confluencemenuaddon$afterInit(int width, int height, CallbackInfo ci) {
        BackgroundLayerController.noteInited(width, height);
        if (confluencemenuaddon$wasRunningBeforeInit) {
            BackgroundLayerController.restoreSavedTime();
        }
    }

    // AchievementScreen/secret-seeds onClose shuts the layers off when popping back to the menu
    // below, which used to force a visible re-init. Keep them running with no world loaded;
    // in-world closes still behave normally.
    @Inject(method = "closeLayers", at = @At("HEAD"), cancellable = true)
    private static void confluencemenuaddon$keepLayersAcrossMenus(CallbackInfo ci) {
        if (BackgroundLayerController.shouldKeepLayersRunning()) {
            ci.cancel();
        }
    }

    // partialTick drives everything downstream (timeOfDay, cloud drift, our parallax), so
    // scaling it here speeds up the whole background uniformly.
    @ModifyVariable(method = "renderLayers", at = @At("HEAD"), argsOnly = true)
    private static float confluencemenuaddon$scaleAnimationSpeed(float partialTick) {
        return partialTick * (float) ClientConfig.backgroundAnimationSpeed;
    }

    // Redirects each layer's render() call: ENVIRONMENT_* gets our scrolling parallax, SKY gets
    // a real fullscreen draw. This is the only stable seam upstream offers - the layers are
    // anonymous enum subclasses with unstable $n names, so we dispatch on the enum constant
    // rather than hooking them directly. Sun/moon/clouds pass through untouched.
    @Redirect(
            method = "renderLayers",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/confluence/mod/client/gui/BackgroundLayer;render(Lnet/minecraft/client/gui/GuiGraphics;F)V"))
    private static void confluencemenuaddon$scrollEnvironment(
            BackgroundLayer layer, GuiGraphics guiGraphics, float partialTick) {
        if ("SKY".equals(layer.name())) {
            EnvironmentParallax.renderSky(guiGraphics, partialTick);
            return;
        }
        int index = EnvironmentParallax.environmentIndex(layer);
        if (index >= 0) {
            EnvironmentParallax.render(index, guiGraphics, partialTick);
        } else {
            layer.render(guiGraphics, partialTick);
        }
    }
}
