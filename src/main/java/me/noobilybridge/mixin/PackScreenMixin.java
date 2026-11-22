package me.noobilybridge.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackScreen.class)
public abstract class PackScreenMixin extends Screen {
    @Unique private int evilCounter;
    @Unique private float yeah;

    protected PackScreenMixin(net.minecraft.text.Text title) { super(title); }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_G) evilCounter++;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void staminaDisplay$drawSecretIcon(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        yeah += 0.025F;
        if (evilCounter != 4) return;
        context.getMatrices().push();
        context.getMatrices().scale((float) (1 + Math.sin(yeah)), (float) (1 + Math.cos(yeah)), 1);
        context.drawTexture(new Identifier("stamina-display", "textures/icon_real.png"), 0, 0,
                width / 2, height / 2, 0, 0, 230, 113, 230, 113);
        context.getMatrices().pop();
    }
}
