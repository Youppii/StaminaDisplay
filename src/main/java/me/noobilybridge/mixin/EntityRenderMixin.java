package me.noobilybridge.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.noobilybridge.StaminaDisplay;
import me.noobilybridge.config.StaminaConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
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

    @Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void AUGHHH(T entity, Text name, MatrixStack ms, VertexConsumerProvider vcp, int light, CallbackInfo ci) {
        if (!(entity instanceof AbstractClientPlayerEntity)) {
            return;
        }
        int yeah = getStaminaFromTablist(MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(entity.getUuid()));
        StaminaDisplay.staminaValues.putIfAbsent((AbstractClientPlayerEntity) entity, (float) yeah);
        float a = StaminaDisplay.staminaValues.get(entity);
        StaminaDisplay.staminaValues.put((AbstractClientPlayerEntity) entity, (float) StaminaDisplay.ease(a, yeah, 15));
        if (yeah != 0) {
            setupRender(ms);
            Color c = getLerpedColor(StaminaConfig.INSTANCE.getConfig().emptyMainColor, StaminaConfig.INSTANCE.getConfig().mainColor, StaminaDisplay.staminaValues.get(entity) / 20);

            DrawableHelper.fill(ms, (int) (-textRenderer.getWidth(name) * 0.5), -10, (int) (textRenderer.getWidth(name) * 0.75F), -5, getColor(StaminaConfig.INSTANCE.getConfig().outlineColor));
            ms.translate(0, 0, -0.01);
            fillFloat(ms, (float) ((int)(-textRenderer.getWidth(name) * 0.5) + 1), (float) -9, (float) MathHelper.lerp(StaminaDisplay.staminaValues.get(entity) / 20F, ((int)-textRenderer.getWidth(name) * 0.5) + 1, (int)textRenderer.getWidth(name) * 0.75F - 1), -6, 0, getColor(getLerpedColor(StaminaConfig.INSTANCE.getConfig().emptyMainColor, StaminaConfig.INSTANCE.getConfig().mainColor, StaminaDisplay.staminaValues.get(entity) / 20)));
            ((BossBarAccessor) MinecraftClient.getInstance().inGameHud.getBossBarHud()).getBossBars().forEach((uuid, clientBossBar) -> {
                try {
                    Color home = new Color(clientBossBar.getName().getSiblings().get(1).getSiblings().get(0).getStyle().getColor().getRgb());
                    Color away = new Color(clientBossBar.getName().getSiblings().get(3).getSiblings().get(0).getStyle().getColor().getRgb());
                    int yeahhh = getTeamFromTablist(MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(entity.getUuid()));
                    ms.translate(0, 0, 0.01);
                    //TODO: fix team colors and scaling
                    DrawableHelper.drawBorder(ms, -textRenderer.getWidth(name)-2, -3, textRenderer.getWidth(name) * 2 + 6, 21, getColor(yeahhh == 0 ? home : away));
                } catch (Exception ignored) {
                }
            });
            ms.translate(0, 0, -0.01);
            RenderSystem.setShaderColor(c.getRed() / 255F, c.getGreen()  / 255F, c.getBlue()  / 255F, c.getAlpha() / 255F);
            DrawableHelper.drawTexture(ms, (int) (-textRenderer.getWidth(name) * 0.75) + 1, -16, 0, 0, 11, 15, 11, 15);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            postRender(ms);
        }
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
    private static void fillFloat(MatrixStack matrices, float x1, float y1, float x2, float y2, float z, int color) {
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
        bufferBuilder.vertex(matrix4f, (float)x1, (float)y1, (float)z).color(g, h, j, f).next();
        bufferBuilder.vertex(matrix4f, (float)x1, (float)y2, (float)z).color(g, h, j, f).next();
        bufferBuilder.vertex(matrix4f, (float)x2, (float)y2, (float)z).color(g, h, j, f).next();
        bufferBuilder.vertex(matrix4f, (float)x2, (float)y1, (float)z).color(g, h, j, f).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    @Unique
    private static void setupRender(MatrixStack ms) {
        ms.push();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        ms.scale(StaminaConfig.INSTANCE.getConfig().scale, StaminaConfig.INSTANCE.getConfig().scale, StaminaConfig.INSTANCE.getConfig().scale);
        RenderSystem.setShaderTexture(0, new Identifier("stamina-display:textures/icon.png"));
    }

    @Unique
    private int getColor(Color outlineColor) {
        return ColorHelper.Argb.getArgb(outlineColor.getAlpha(), outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue());
    }

    @Unique
    private Integer getStaminaFromTablist(PlayerListEntry info) {
        if(info == null){
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
}
