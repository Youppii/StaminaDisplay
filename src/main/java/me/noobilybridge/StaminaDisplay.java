package me.noobilybridge;

import com.mojang.blaze3d.systems.RenderSystem;
import me.noobilybridge.config.StaminaConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Unique;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StaminaDisplay implements ClientModInitializer {
    public static Map<AbstractClientPlayerEntity, Float> staminaValues = new HashMap<>();

    @Override
    public void onInitializeClient() {
        StaminaConfig.INSTANCE.load();
        if (StaminaConfig.INSTANCE.getConfig().iceTranslucencyDisable) {
            BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getSolid(), Blocks.ICE, Blocks.PACKED_ICE, Blocks.FROSTED_ICE, Blocks.BLUE_ICE);
        }
        WorldRenderEvents.BEFORE_ENTITIES.register(worldRenderContext -> {
            for (Entity entity : worldRenderContext.world().getEntities()) {
                if (!(entity instanceof AbstractClientPlayerEntity)) {
                    return;
                }
                int staminaVal;
                if (MinecraftClient.getInstance().player.equals(entity)) {
                    staminaVal = ((AbstractClientPlayerEntity) entity).getHungerManager().getFoodLevel();
                } else {
                    staminaVal = getStaminaFromTablist(MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(entity.getUuid()));
                }
                StaminaDisplay.staminaValues.putIfAbsent((AbstractClientPlayerEntity) entity, (float) staminaVal);
                float lastStamina = StaminaDisplay.staminaValues.get(entity);
                StaminaDisplay.staminaValues.put((AbstractClientPlayerEntity) entity, (float) StaminaDisplay.ease(lastStamina, staminaVal, StaminaConfig.INSTANCE.getConfig().animationSpeed));
            }
        });
    }

    public static float getStamina(Entity e) {
        return MathHelper.clamp(StaminaDisplay.staminaValues.get(e) - StaminaConfig.INSTANCE.getConfig().minStamina, 0, 20);
    }

    public static float getScaledStamina(Entity e) {
        return MathHelper.clamp(StaminaDisplay.staminaValues.get(e) - StaminaConfig.INSTANCE.getConfig().minStamina, 0, 20) / getMaxStamina();
    }

    public static float getMaxStamina() {
        return MathHelper.clamp(20 - StaminaConfig.INSTANCE.getConfig().minStamina, 1, 20);
    }

    public static int getColor(Color col) {
//        if (col.getAlpha() == 0 && col.equals(Color.BLACK)) {
//            return ColorHelper.Argb.getArgb(team.getAlpha(), team.getRed(), team.getGreen(), team.getBlue());
//        }
        return ColorHelper.Argb.getArgb(col.getAlpha(), col.getRed(), col.getGreen(), col.getBlue());
    }

    @Unique
    private Integer getStaminaFromTablist(PlayerListEntry info) {
        if (info == null) {
            return 0;
        }
        Text displayName = info.getDisplayName();
        if (displayName != null) {
            String displayNameString = displayName.getString();
            int startBracket = displayNameString.lastIndexOf('[');
            int endBracket = displayNameString.lastIndexOf(']');
            if (startBracket != -1 && endBracket != -1 && endBracket > startBracket) {
                String staminaString = displayNameString.substring(startBracket + 1, endBracket).trim();
                try {
                    return Integer.parseInt(staminaString);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0;
    }

    public static double ease(double start, double end, float speed) {
        return (start + (end - start) * (1 - Math.exp(-((double) MinecraftClient.getInstance().getRenderTime() / 1000000000) * speed)));
    }

    public static Color getLerpedColor(Color c1, Color c2, float percent) {
        return new Color(MathHelper.clamp(MathHelper.lerp(percent, c1.getRed(), c2.getRed()), 0, 255), MathHelper.clamp(MathHelper.lerp(percent, c1.getGreen(), c2.getGreen()), 0, 255), MathHelper.clamp(MathHelper.lerp(percent, c1.getBlue(), c2.getBlue()), 0, 255));
    }

    public static void renderRoundedQuad(MatrixStack matrices, Color c, double fromX, double fromY, double toX, double toY, double radC1, double radC2, double radC3, double radC4, double samples) {
        int color = c.getRGB();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float k = (float) (color & 255) / 255.0F;
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        renderRoundedQuadInternal(matrix, g, h, k, f, fromX, fromY, toX, toY, radC1, radC2, radC3, radC4, samples);
    }

    @Unique
    public static void fillFloat(MatrixStack matrices, float x1, float y1, float x2, float y2, float z, int color) {
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        if (x1 < x2) {
            float i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            float i = y1;
            y1 = y2;
            y2 = i;
        }

        float f = (float) ColorHelper.Argb.getAlpha(color) / 255.0F;
        float g = (float) ColorHelper.Argb.getRed(color) / 255.0F;
        float h = (float) ColorHelper.Argb.getGreen(color) / 255.0F;
        float j = (float) ColorHelper.Argb.getBlue(color) / 255.0F;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, (float) x1, (float) y1, (float) z).color(g, h, j, f).next();
        bufferBuilder.vertex(matrix4f, (float) x1, (float) y2, (float) z).color(g, h, j, f).next();
        bufferBuilder.vertex(matrix4f, (float) x2, (float) y2, (float) z).color(g, h, j, f).next();
        bufferBuilder.vertex(matrix4f, (float) x2, (float) y1, (float) z).color(g, h, j, f).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void renderRoundedQuadInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radC1, double radC2, double radC3, double radC4, double samples) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        double[][] map = new double[][]{new double[]{toX - radC4, toY - radC4, radC4}, new double[]{toX - radC2, fromY + radC2, radC2}, new double[]{fromX + radC1, fromY + radC1, radC1}, new double[]{fromX + radC3, toY - radC3, radC3}};
        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double rad = current[2];
            for (double r = i * 90d; r < 360 / 4d + i * 90d; r += 90 / samples) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
            }
            float rad1 = (float) Math.toRadians(360 / 4d + i * 90d);
            float sin = (float) (Math.sin(rad1) * rad);
            float cos = (float) (Math.cos(rad1) * rad);
            bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    @Unique
    public static void renderRoundedQuad(MatrixStack stack, Color c, double x, double y, double x1, double y1, double rad, double samples) {
        renderRoundedQuad(stack, c, x, y, x1, y1, rad, rad, rad, rad, samples);
    }
}
