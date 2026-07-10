package net.adrimark.confluencemenuaddon.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.adrimark.confluencemenuaddon.client.BackgroundLayerController;

// Generic hook for screens using the inherited Screen#renderBackground (TitleScreen overrides
// it differently and has its own mixin).
@Mixin(Screen.class)
public abstract class ScreenRenderBackgroundMixin {

    // Must cancel - vanilla's body would otherwise follow up with renderPanorama, a blur pass,
    // and the dirt overlay, which blurs away the layers we just drew.
    @Inject(method = "renderBackground(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("HEAD"), cancellable = true)
    private void confluencemenuaddon$renderOtherworldBackground(
            GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (self instanceof TitleScreen) {
            return;
        }
        if (BackgroundLayerController.renderBackground(self, guiGraphics, partialTick)) {
            ci.cancel();
        }
    }
}
