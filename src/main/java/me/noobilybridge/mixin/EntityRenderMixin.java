package me.noobilybridge.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.noobilybridge.config.StaminaConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
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

    @Shadow
    public abstract void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light);

    @Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void AUGHHH(T entity, Text name, MatrixStack ms, VertexConsumerProvider vcp, int light, CallbackInfo ci) {
        if(!(entity instanceof AbstractClientPlayerEntity)) {
            return;
        }
        int yeah = getStaminaFromTablist(MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(entity.getUuid()));
        if (yeah != 0) {
            setupRender(ms);
            Color c = StaminaConfig.INSTANCE.getConfig().mainColor;
            RenderSystem.setShaderColor(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
            DrawableHelper.drawTexture(ms, (int) (-textRenderer.getWidth(name) * 0.75) + 1, -16, 0, 0, 11, 15, 11, 15);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            DrawableHelper.fill(ms, (int) (-textRenderer.getWidth(name) * 0.5), -10, (int) (textRenderer.getWidth(name) * 0.75F), -5, getColor(StaminaConfig.INSTANCE.getConfig().outlineColor));
            ms.translate(0, 0, -0.01);
            DrawableHelper.fill(ms, (int) (-textRenderer.getWidth(name) * 0.5) + 1, -9, (int) MathHelper.lerp(yeah / 20F, -textRenderer.getWidth(name) * 0.5 + 1, textRenderer.getWidth(name) * 0.75F - 1), -6, getColor(StaminaConfig.INSTANCE.getConfig().mainColor));
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
}
