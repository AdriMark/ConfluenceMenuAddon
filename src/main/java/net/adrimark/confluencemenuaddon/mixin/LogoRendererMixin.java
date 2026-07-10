package net.adrimark.confluencemenuaddon.mixin;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.adrimark.confluencemenuaddon.client.TitleLogoController;

// Swaps the title logo for our texture once the sun/moon easter egg fires (TitleLogoController).
// Drawn at native size instead of stretched into vanilla's 256x44 box. Replaces the whole method
// rather than redirecting the blit call, since renderLogo has two near-identical blits (logo,
// then edition banner) and picking the right one by descriptor is fragile across mappings.
@Mixin(LogoRenderer.class)
public abstract class LogoRendererMixin {

    @Shadow
    @Final
    private boolean keepLogoThroughFade;

    @Inject(method = "renderLogo(Lnet/minecraft/client/gui/GuiGraphics;IFI)V", at = @At("HEAD"), cancellable = true)
    private void confluencemenuaddon$customLogo(
            GuiGraphics guiGraphics, int screenWidth, float transparency, int height, CallbackInfo ci) {
        if (!TitleLogoController.isCustomLogoActive()) {
            return;
        }
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.keepLogoThroughFade ? 1.0F : transparency);
        RenderSystem.enableBlend();
        int logoWidth = TitleLogoController.CUSTOM_LOGO_WIDTH;
        int logoHeight = TitleLogoController.CUSTOM_LOGO_HEIGHT;
        int logoX = screenWidth / 2 - logoWidth / 2;
        guiGraphics.blit(TitleLogoController.CUSTOM_LOGO, logoX, height, 0.0F, 0.0F, logoWidth, logoHeight, logoWidth, logoHeight);
        int editionX = screenWidth / 2 - 64;
        int editionY = height + logoHeight - 7;
        guiGraphics.blit(LogoRenderer.MINECRAFT_EDITION, editionX, editionY, 0.0F, 0.0F, 128, 14, 128, 16);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        ci.cancel();
    }
}
