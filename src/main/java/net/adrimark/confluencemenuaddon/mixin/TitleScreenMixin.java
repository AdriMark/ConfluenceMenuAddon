package net.adrimark.confluencemenuaddon.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.language.I18n;

import org.confluence.mod.client.ClientConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.adrimark.confluencemenuaddon.client.BackgroundLayerController;
import net.adrimark.confluencemenuaddon.config.ClientConfig;

// TitleScreen draws its panorama cube via renderPanorama() instead of going through
// Screen#renderBackground, so we hook that directly. The init hook below also swaps the splash
// text for one of Confluence's window title lines.
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {

    @Shadow
    @Mutable
    private SplashRenderer splash;

    @Inject(method = "renderPanorama(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At("HEAD"), cancellable = true)
    private void confluencemenuaddon$replacePanorama(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        TitleScreen self = (TitleScreen) (Object) this;
        if (BackgroundLayerController.renderBackground(self, guiGraphics, partialTick)) {
            ci.cancel();
        }
    }

    // Runs before vanilla's null check so it sees the splash already set. Picks from the same
    // line pool Confluence uses for its window title, so it shares the same toggle
    // (customTitle <= 0 disables both) and random pick.
    @Inject(method = "init", at = @At("HEAD"))
    private void confluencemenuaddon$replaceSplashText(CallbackInfo ci) {
        if (ClientConfig.enableConfluenceSplashText && this.splash == null && ClientConfigs.customTitle > 0) {
            String line = I18n.get("title.confluence.window." + (int) (Math.random() * ClientConfigs.customTitle));
            // The lines all start with "Confluence: " for the window title bar; the splash
            // reads better without it.
            String prefix = "Confluence: ";
            if (line.startsWith(prefix)) {
                line = line.substring(prefix.length());
            }
            this.splash = new SplashRenderer(line);
        }
    }
}
