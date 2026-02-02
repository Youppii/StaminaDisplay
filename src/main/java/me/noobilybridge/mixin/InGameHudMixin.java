package me.noobilybridge.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import me.noobilybridge.StaminaDisplay;
import me.noobilybridge.config.StaminaConfig;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.awt.*;

import static me.noobilybridge.StaminaDisplay.*;
import static net.minecraft.client.gui.DrawableHelper.GUI_ICONS_TEXTURE;


@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private int scaledHeight;

    @Shadow
    private int scaledWidth;

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Inject(
            method = "renderStatusBars",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=food")
    )
    private void renderCustomFood(MatrixStack matrices, CallbackInfo ci) {
        if (StaminaConfig.INSTANCE.getConfig().customHungerBar) {
            matrices.push();
            matrices.translate((scaledWidth / 2) + 50.625, scaledHeight - 34.5, 0);
            float p = StaminaConfig.INSTANCE.getConfig().paddingAmount;
            float width = 79;
            var outlineColor = StaminaConfig.INSTANCE.getConfig().outlineColor;
            var minColor = StaminaConfig.INSTANCE.getConfig().mainColor;
            var emptyMainColor = StaminaConfig.INSTANCE.getConfig().emptyMainColor;
            var height = StaminaConfig.INSTANCE.getConfig().height;
            var cornerRounding = StaminaConfig.INSTANCE.getConfig().cornerRounding;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            renderRoundedQuad(matrices, outlineColor, (-width / 2F) - p, -height / 2F - p, (width / 2F) + p, height / 2F + p, cornerRounding, 10);
            if (getStamina(client.player) > 0.25) {
                matrices.translate(0, 0, 0.1F);
                var color = getLerpedColor(emptyMainColor, minColor, StaminaDisplay.getScaledStamina(client.player));
                renderRoundedQuad(matrices, color, -width / 2, -height / 2F, MathHelper.lerp(StaminaDisplay.getScaledStamina(client.player), -width / 2, width / 2), height / 2F, cornerRounding, 10);
            }
            if (StaminaConfig.INSTANCE.getConfig().showNumber) {
                matrices.translate(0, 0, 0.1F);
                matrices.scale(StaminaConfig.INSTANCE.getConfig().numberScale, StaminaConfig.INSTANCE.getConfig().numberScale, 1);
                DrawableHelper.drawCenteredTextWithShadow(matrices, MinecraftClient.getInstance().textRenderer, String.valueOf(Math.round(StaminaDisplay.getStamina(client.player))), 0, -getTextRenderer().fontHeight / 2,getColor(StaminaConfig.INSTANCE.getConfig().numberColor));
                RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
            }
            matrices.pop();
        }
    }
    @Unique
    private static int tweakTransparency(int argb) {
        return (argb & -67108864) == 0 ? argb | 0xFF000000 : argb;
    }

    @ModifyConstant(
            method = "renderStatusBars",
            constant = @Constant(intValue = 10),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=food")
            )
    )
    private int disableVanillaFoodLoop(int original) {
        return StaminaConfig.INSTANCE.getConfig().customHungerBar ? 0 : original;
    }
}
