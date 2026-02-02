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

import static me.noobilybridge.StaminaDisplay.*;
import static net.minecraft.client.gui.DrawableHelper.GUI_ICONS_TEXTURE;

@Mixin(EntityRenderer.class)
public abstract class EntityRenderMixin {

    @Shadow
    @Final
    private TextRenderer textRenderer;




    @Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void AUGHHH(Entity entity, Text name, MatrixStack ms, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!(entity instanceof AbstractClientPlayerEntity)) {
            return;
        }
        if (!StaminaDisplay.staminaValues.containsKey(entity)) {
            return;
        }
        if (!StaminaConfig.INSTANCE.getConfig().renderBar) {
            return;
        }
        setupRender(ms);
        ((BossBarAccessor) MinecraftClient.getInstance().inGameHud.getBossBarHud()).getBossBars().forEach((uuid, clientBossBar) -> {
            try {
                Color home = new Color(clientBossBar.getName().getSiblings().get(1).getSiblings().get(0).getStyle().getColor().getRgb());
                Color away = new Color(clientBossBar.getName().getSiblings().get(3).getSiblings().get(0).getStyle().getColor().getRgb());
                int team = getTeamFromTablist(MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(entity.getUuid()));
                if (team != -1) {
                    drawBorder(ms, -textRenderer.getWidth(name) / 2 - 2, -2, textRenderer.getWidth(name) + 4, 12, getColor(team == 0 ? home : away), StaminaConfig.INSTANCE.getConfig().outlineThickness);
                    //thicker border option
                }
            } catch (Exception ignored) {
            }
        });
        ms.push();
        ms.translate(0, -StaminaConfig.INSTANCE.getConfig().verticalOffset, 0);

        float p = StaminaConfig.INSTANCE.getConfig().paddingAmount;
        renderRoundedQuad(ms, StaminaConfig.INSTANCE.getConfig().outlineColor, (-StaminaConfig.INSTANCE.getConfig().width / 2F) - p, -StaminaConfig.INSTANCE.getConfig().height / 2F - p, (StaminaConfig.INSTANCE.getConfig().width / 2F) + p, StaminaConfig.INSTANCE.getConfig().height / 2F + p, StaminaConfig.INSTANCE.getConfig().cornerRounding, 10);
        if (getStamina(entity) > 0.25) {
            ms.translate(0, 0, -0.1F);
            renderRoundedQuad(ms, getLerpedColor(StaminaConfig.INSTANCE.getConfig().emptyMainColor, StaminaConfig.INSTANCE.getConfig().mainColor, getScaledStamina(entity)), -StaminaConfig.INSTANCE.getConfig().width / 2, -StaminaConfig.INSTANCE.getConfig().height / 2F, MathHelper.lerp(getScaledStamina(entity), -StaminaConfig.INSTANCE.getConfig().width / 2, StaminaConfig.INSTANCE.getConfig().width / 2), StaminaConfig.INSTANCE.getConfig().height / 2F, StaminaConfig.INSTANCE.getConfig().cornerRounding, 10);
        }
        if (StaminaConfig.INSTANCE.getConfig().showNumber) {
            ms.translate(0, 0, -0.1F);

            ms.scale(StaminaConfig.INSTANCE.getConfig().numberScale, StaminaConfig.INSTANCE.getConfig().numberScale, 1);
            //manually draw shadow as we have reverse z
            textRenderer.draw(ms, String.valueOf(Math.round(StaminaDisplay.getStamina(entity))), (float) -textRenderer.getWidth(String.valueOf(Math.round(StaminaDisplay.getStamina(entity)))) / 2 + 1, ((float) -textRenderer.fontHeight / 2) + 1, ColorHelper.Argb.lerp(0.75F, getColor(StaminaConfig.INSTANCE.getConfig().numberColor), 0xFF000000));
            ms.translate(0, 0, -0.1F);
            textRenderer.draw(ms, String.valueOf(Math.round(StaminaDisplay.getStamina(entity))), (float) -textRenderer.getWidth(String.valueOf(Math.round(StaminaDisplay.getStamina(entity)))) / 2, (float) -textRenderer.fontHeight / 2, getColor(StaminaConfig.INSTANCE.getConfig().numberColor));
            RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
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
    private static void drawBorder(MatrixStack matrices, int x, int y, int width, int height, int color, float thickness) {
        StaminaDisplay.fillFloat(matrices, x - thickness, y - thickness, x + width + thickness, y + 1, 0, color);
        StaminaDisplay.fillFloat(matrices, x - thickness, y + height - 1, x + width + thickness, y + height + thickness, 0, color);
        StaminaDisplay.fillFloat(matrices, x - thickness, y + 1, x + 1, y + height - 1, 0, color);
        StaminaDisplay.fillFloat(matrices, x + width - 1, y + 1, x + width + thickness, y + height - 1, 0, color);
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
