package net.adrimark.confluencemenuaddon.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.neoforged.neoforge.client.DimensionSpecialEffectsManager;

import org.confluence.mod.client.gui.BackgroundLayer;
import org.joml.Matrix4f;

import net.adrimark.confluencemenuaddon.config.ClientConfig;
import net.adrimark.confluencemenuaddon.mixin.BackgroundLayerAccessor;

// Terraria-style horizontal parallax for BackgroundLayer's ENVIRONMENT_* scenery, which
// upstream just draws as static fullscreen stretches. BackgroundLayerMixin redirects the
// per-layer render call here so we inherit the blend state / day-night tint already set up.
//
// Each copy fills the screen height at the texture's native aspect (upstream stretches to the
// window aspect and distorts the art). Textures don't tile horizontally, so every other copy
// is mirrored via inverted U coords to make the seams line up.
public final class EnvironmentParallax {

    private EnvironmentParallax() {
    }

    private record Texture(ResourceLocation location, int width, int height) {

        private static Texture of(String name, int width, int height) {
            return new Texture(ResourceLocation.fromNamespaceAndPath(
                    "confluence", "textures/gui/sprites/background/" + name + ".png"), width, height);
        }
    }

    /** One scrolling picture plane; {@code offset} accumulates its drift between frames. */
    private static final class Plane {

        private final Texture texture;
        // GUI pixels of drift per unit of partialTick, tuned against upstream's cloud speeds
        // (0.2-0.7) so the scenery drifts a bit slower.
        private final float speed;
        private float offset;

        private Plane(Texture texture, float speed) {
            this.texture = texture;
            this.speed = speed;
        }
    }

    /** Back to front: mountain ridge, rocky terrain, trees, foreground grass. */
    private static final Plane[] LAYER_PLANES = {
            new Plane(Texture.of("environment_0_0", 512, 320), 0.05F),
            new Plane(Texture.of("environment_1_0", 528, 336), 0.13F),
            new Plane(Texture.of("environment_2_0", 528, 336), 0.17F),
            new Plane(Texture.of("environment_3_0", 528, 336), 0.22F),
    };

    private static final Texture SKY_TEXTURE = Texture.of("sky_0", 528, 336);

    // Upstream's sky sprite only covers a fixed 528x336 patch top-left, leaving garbage in the
    // rest of the frame on bigger GUI sizes. Stretch it fullscreen instead, then redo the
    // dawn/dusk gradient on top same as upstream.
    public static void renderSky(GuiGraphics guiGraphics, float partialTick) {
        guiGraphics.blit(SKY_TEXTURE.location(), 0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(),
                0.0F, 0.0F, SKY_TEXTURE.width(), SKY_TEXTURE.height(), SKY_TEXTURE.width(), SKY_TEXTURE.height());

        float[] color = DimensionSpecialEffectsManager.getForType(BuiltinDimensionTypes.OVERWORLD_EFFECTS)
                .getSunriseColor(BackgroundLayerAccessor.getTimeOfDay(), partialTick);
        if (color != null) {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            Matrix4f matrix4f = poseStack.last().pose();
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            float maxX = guiGraphics.guiWidth();
            float maxY = guiGraphics.guiHeight();
            float r = color[0];
            float g = color[1];
            float b = color[2];
            float a = color[3];
            builder.addVertex(matrix4f, 0, 0, 0).setColor(r, g, b, 0);
            builder.addVertex(matrix4f, 0, maxY, 0).setColor(r, g, b, a);
            builder.addVertex(matrix4f, maxX, maxY, 0).setColor(r, g, b, a);
            builder.addVertex(matrix4f, maxX, 0, 0).setColor(r, g, b, 0);
            BufferUploader.drawWithShader(builder.buildOrThrow());
            poseStack.popPose();
        }
    }

    /** Maps a {@link BackgroundLayer} constant to its index here, or -1 if it isn't scenery. */
    public static int environmentIndex(BackgroundLayer layer) {
        return switch (layer.name()) {
            case "ENVIRONMENT_0" -> 0;
            case "ENVIRONMENT_1" -> 1;
            case "ENVIRONMENT_2" -> 2;
            case "ENVIRONMENT_3" -> 3;
            default -> -1;
        };
    }

    public static void render(int layerIndex, GuiGraphics guiGraphics, float partialTick) {
        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();
        if (screenWidth <= 0 || screenHeight <= 0) {
            return;
        }
        renderPlane(LAYER_PLANES[layerIndex], guiGraphics, partialTick, screenWidth, screenHeight);
    }

    private static void renderPlane(
            Plane plane, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Texture texture = plane.texture;
        int copyWidth = Math.max(1, Math.round(screenHeight * (float) texture.width() / texture.height()));

        // Growing offset = drift to the left; flip the sign for left-to-right. Wrap keeps it
        // in [0, period).
        float drift = partialTick * plane.speed * (float) ClientConfig.environmentScrollSpeed;
        if (ClientConfig.environmentScrollDirection == ClientConfig.ScrollDirection.LEFT_TO_RIGHT) {
            drift = -drift;
        }
        int period = copyWidth * 2;
        float offset = (plane.offset + drift) % period;
        if (offset < 0) {
            offset += period;
        }
        plane.offset = offset;

        // Screen shows [offset, offset + screenWidth) of an infinite strip; segment n is one
        // copy wide, mirrored when odd, starting at n * copyWidth. Sub-pixel remainder goes
        // into the pose translation so the drift doesn't step in whole pixels.
        int wholeOffset = (int) offset;
        float fraction = offset - wholeOffset;
        int segment = wholeOffset / copyWidth;
        int segmentX = segment * copyWidth - wholeOffset;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(-fraction, 0, 0);
        // + 1 covers the sliver the fractional translation shifts in from the right edge.
        for (int x = segmentX, n = segment; x < screenWidth + 1; x += copyWidth, n++) {
            drawCopy(guiGraphics, texture, x, copyWidth, screenHeight, (n & 1) == 1);
        }
        guiGraphics.pose().popPose();
    }

    private static void drawCopy(
            GuiGraphics guiGraphics, Texture texture, int x, int width, int height, boolean mirrored) {
        // Negative u-width flips the U range to mirror the texture without touching winding.
        int uWidth = mirrored ? -texture.width() : texture.width();
        float uOffset = mirrored ? texture.width() : 0.0F;
        guiGraphics.blit(texture.location(), x, 0, width, height,
                uOffset, 0.0F, uWidth, texture.height(), texture.width(), texture.height());
    }
}
