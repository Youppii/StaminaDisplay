package me.noobilybridge.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.noobilybridge.StaminaDisplay;
import me.noobilybridge.config.StaminaConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().enable();
            matrices.push();
            matrices.translate((scaledWidth / 2) + 50.625, scaledHeight - 34.5 - StaminaConfig.INSTANCE.getConfig().hungerBarVerticalOffset, 0);
            float p = StaminaConfig.INSTANCE.getConfig().paddingAmount;
            float width = StaminaConfig.INSTANCE.getConfig().hungerBarWidth;
            var outlineColor = StaminaConfig.INSTANCE.getConfig().outlineColor;
            var height = StaminaConfig.INSTANCE.getConfig().hungerBarHeight;
            renderRoundedQuad(matrices.peek().getPositionMatrix(), (-width / 2F) - p, -height / 2F - p, (width / 2F) + p, height / 2F + p, 10, false, getColor(outlineColor), getColor(outlineColor), false, 0, false);
                matrices.translate(0, 0, 0.1F);
                float scaledStamina = getScaledStamina(client.player);
                if(MinecraftClient.getInstance().player.hasVehicle()){
                    scaledStamina = MathHelper.clamp(getStaminaFromTablist(MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(MinecraftClient.getInstance().player.getUuid())) - StaminaConfig.INSTANCE.getConfig().minStamina, 0, 20) / getMaxStamina();
                }
                renderRoundedQuad(matrices.peek().getPositionMatrix(), -width / 2, -height / 2F, MathHelper.lerp(scaledStamina, -width / 2, width / 2), height / 2F, 10, false, getColor(StaminaConfig.INSTANCE.getConfig().hungerBarMainColor), getColor(StaminaConfig.INSTANCE.getConfig().hungerBarSecondaryColor), StaminaConfig.INSTANCE.getConfig().hungerBarGradientDirection, 0, false);
            if (StaminaConfig.INSTANCE.getConfig().showNumberInBar) {
                matrices.translate(0, 0, 0.1F);
                matrices.scale(StaminaConfig.INSTANCE.getConfig().barNumberScale, StaminaConfig.INSTANCE.getConfig().barNumberScale, 1);
                String text = String.valueOf(Math.round(clientStamina));
                //TODO: yet another shitty workaround
                if(MinecraftClient.getInstance().player.hasVehicle()){
                    text = String.valueOf(StaminaDisplay.getStaminaFromTablist(MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(MinecraftClient.getInstance().player.getUuid())));
                }
                DrawableHelper.drawCenteredTextWithShadow(matrices, MinecraftClient.getInstance().textRenderer, text, 0, -getTextRenderer().fontHeight / 2, getColor(StaminaConfig.INSTANCE.getConfig().barNumberColor));
                RenderSystem.setShaderTexture(0, GUI_ICONS_TEXTURE);
            }
            matrices.pop();
        }
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
