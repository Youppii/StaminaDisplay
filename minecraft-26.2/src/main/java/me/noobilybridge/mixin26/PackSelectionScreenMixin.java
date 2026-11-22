package me.noobilybridge.mixin26;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class PackSelectionScreenMixin {
    @Unique private static final Identifier staminaDisplay$icon = Identifier.fromNamespaceAndPath("stamina-display", "textures/icon_real.png");
    @Unique private int staminaDisplay$keyCount;

    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void staminaDisplay$countSecretKey(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PackSelectionScreen && event.key() == GLFW.GLFW_KEY_G) staminaDisplay$keyCount++;
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void staminaDisplay$drawSecretIcon(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!((Object) this instanceof PackSelectionScreen) || staminaDisplay$keyCount < 4) return;
        float pulse = (float) (Math.sin(System.currentTimeMillis() / 160.0) * 0.5 + 0.5);
        int size = 12 + Math.round(pulse * 8);
        graphics.blit(staminaDisplay$icon, graphics.guiWidth() / 2 - size / 2,
                graphics.guiHeight() / 2 - size / 2, size, size, 0, 0, 1, 1);
    }
}
