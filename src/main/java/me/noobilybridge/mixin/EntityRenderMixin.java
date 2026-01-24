package me.noobilybridge.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.noobilybridge.StaminaDisplay;
import me.noobilybridge.config.StaminaConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.awt.*;

@Mixin(EntityRenderer.class)
public abstract class EntityRenderMixin<T extends Entity> {

    @Shadow
    @Final
    private TextRenderer textRenderer;


    @Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void AUGHHH(T entity, Text name, MatrixStack ms, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
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

        setupRender(ms);
        ((BossBarAccessor) MinecraftClient.getInstance().inGameHud.getBossBarHud()).getBossBars().forEach((uuid, clientBossBar) -> {
            try {
                Color home = new Color(clientBossBar.getName().getSiblings().get(1).getSiblings().get(0).getStyle().getColor().getRgb());
                Color away = new Color(clientBossBar.getName().getSiblings().get(3).getSiblings().get(0).getStyle().getColor().getRgb());
                int team = getTeamFromTablist(MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(entity.getUuid()));
                if (team != -1) {
                    DrawableHelper.drawBorder(ms, -textRenderer.getWidth(name) / 2 - 2, -2, textRenderer.getWidth(name) + 4, 12, getColor(team == 0 ? home : away));
                    //thicker border option
                    DrawableHelper.drawBorder(ms, -textRenderer.getWidth(name) / 2 - 3, -3, textRenderer.getWidth(name) + 6, 14, getColor(team == 0 ? home : away));
                }
            } catch (Exception ignored) {
            }
        });
        ms.push();
        ms.translate(0, -StaminaConfig.INSTANCE.getConfig().verticalOffset, 0);

        Color barColor = getLerpedColor(StaminaConfig.INSTANCE.getConfig().emptyMainColor, StaminaConfig.INSTANCE.getConfig().mainColor, StaminaDisplay.staminaValues.get(entity) / 20);
        float p = StaminaConfig.INSTANCE.getConfig().paddingAmount;
        renderRoundedQuad(ms, StaminaConfig.INSTANCE.getConfig().outlineColor, (-StaminaConfig.INSTANCE.getConfig().width / 2F) - p, -StaminaConfig.INSTANCE.getConfig().height / 2F - p, (StaminaConfig.INSTANCE.getConfig().width / 2F) + p, StaminaConfig.INSTANCE.getConfig().height / 2F + p, StaminaConfig.INSTANCE.getConfig().cornerRounding, 10);
        if (StaminaDisplay.staminaValues.get(entity) > 1) {
            ms.translate(0, 0, -0.1F);
            renderRoundedQuad(ms, getLerpedColor(StaminaConfig.INSTANCE.getConfig().emptyMainColor, StaminaConfig.INSTANCE.getConfig().mainColor, StaminaDisplay.staminaValues.get(entity) / 20), -StaminaConfig.INSTANCE.getConfig().width / 2, -StaminaConfig.INSTANCE.getConfig().height / 2F, MathHelper.lerp(StaminaDisplay.staminaValues.get(entity) / 20F, -StaminaConfig.INSTANCE.getConfig().width / 2, StaminaConfig.INSTANCE.getConfig().width / 2), StaminaConfig.INSTANCE.getConfig().height / 2F, StaminaConfig.INSTANCE.getConfig().cornerRounding, 10);
        }

//        RenderSystem.setShaderColor(0, 0.5F, 0, 1);
//        RenderSystem.setShaderTexture(0, new Identifier("stamina-display:textures/icon_filled.png"));
//        DrawableHelper.drawTexture(ms, (int) (-textRenderer.getWidth(name) * 0.75) + 1, (int) (-StaminaConfig.INSTANCE.getConfig().verticalOffset) - 1, 0, 0, 11, 15, 11, 15);
//        RenderSystem.setShaderColor(1, 1, 1, 1);
//        RenderSystem.setShaderTexture(0, new Identifier("stamina-display:textures/icon_inner.png"));
//        ms.translate(0, 0, -0.01);
//        DrawableHelper.drawTexture(ms, (int) (-textRenderer.getWidth(name) * 0.75) + 1, (int) (-StaminaConfig.INSTANCE.getConfig().verticalOffset) - 1, (float) 0, (float) 0, 11, (int) (15 * StaminaDisplay.staminaValues.get(entity) / 20F), 11, 15);
        ms.pop();
        postRender(ms);
    }

    @Unique
    private static void postRender(MatrixStack ms) {
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        ms.pop();
    }

    @Unique
    private static Color getLerpedColor(Color c1, Color c2, float percent) {
        return new Color(MathHelper.clamp(MathHelper.lerp(percent, c1.getRed(), c2.getRed()), 0, 255), MathHelper.clamp(MathHelper.lerp(percent, c1.getGreen(), c2.getGreen()), 0, 255), MathHelper.clamp(MathHelper.lerp(percent, c1.getBlue(), c2.getBlue()), 0, 255));
    }

    @Unique
    private static void drawGoodTexture(Matrix4f matrix, float x0, float x1, float y0, float y1, float z, float u0, float u1, float v0, float v1) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, x0, y0, z).texture(u0, v0).next();
        bufferBuilder.vertex(matrix, x0, y1, z).texture(u0, v1).next();
        bufferBuilder.vertex(matrix, x1, y1, z).texture(u1, v1).next();
        bufferBuilder.vertex(matrix, x1, y0, z).texture(u1, v0).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    @Unique
    private static void setupRender(MatrixStack ms) {
        ms.push();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
//        RenderSystem.setShaderTexture(0, new Identifier("stamina-display:textures/icon.png"));
    }

    @Unique
    private int getColor(Color outlineColor) {
        return ColorHelper.Argb.getArgb(outlineColor.getAlpha(), outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue());
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

    @Unique
    private int getTeamFromTablist(PlayerListEntry info) {
        /*
        0 - home
        1 - away
        -1 - neither
         */
        Text displayName = info.getDisplayName();
        if (displayName != null) {
            if (displayName.getSiblings().get(2).getStyle().getColor().getName().equals("yellow")) {
                return 1;
            } else if (displayName.getSiblings().get(2).getStyle().getColor().getName().equals("blue")) {
                return 0;
            }
        }
        return -1;
    }

    @Unique
    private static void renderRoundedQuad(MatrixStack matrices, Color c, double fromX, double fromY, double toX, double toY, double radC1, double radC2, double radC3, double radC4, double samples) {
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
    private static void renderRoundedQuadInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radC1, double radC2, double radC3, double radC4, double samples) {
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
    private static void renderRoundedQuad(MatrixStack stack, Color c, double x, double y, double x1, double y1, double rad, double samples) {
        renderRoundedQuad(stack, c, x, y, x1, y1, rad, rad, rad, rad, samples);
    }
}
