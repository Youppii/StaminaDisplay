package me.noobilybridge.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(YACLScreen.class)
public class YaclScreenMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V", shift = At.Shift.AFTER))
    void ough(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null) {
            InventoryScreen.drawEntity(matrices, MinecraftClient.getInstance().currentScreen.width / 2, 300, 100, mouseX, mouseY, MinecraftClient.getInstance().player);
            Entity entity =  MinecraftClient.getInstance().player;
            MatrixStack matrixStack = RenderSystem.getModelViewStack();
            matrixStack.push();
            matrixStack.translate(0.0, 0.0, 1000.0);
            RenderSystem.applyModelViewMatrix();
            matrices.push();
            matrices.translate((double)300, (double)100, -950.0);
            Text text = Text.of("yeahh");
                boolean bl = !entity.isSneaky();
                float f = entity.getHeight() + 0.5F;
                int i = "deadmau5".equals(text.getString()) ? -10 : 0;
                matrices.push();
                matrices.translate(0.0F, f, 0.0F);
                matrices.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
                matrices.scale(-0.025F, -0.025F, 0.025F);
                Matrix4f matrix4f = matrices.peek().getPositionMatrix();
                float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
                int j = (int)(g * 255.0F) << 24;
                TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                float h = -textRenderer.getWidth(text) / 2;
                if (bl) {
                    textRenderer.draw(text, h, (float)i, -1, false, matrix4f, MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
                }

                matrices.pop();
            matrices.pop();
            DiffuseLighting.enableGuiDepthLighting();
            matrixStack.pop();
            RenderSystem.applyModelViewMatrix();
//            ((EntityRendererInvoker<ClientPlayerEntity>)((LivingEntityRenderer) MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(MinecraftClient.getInstance().player))).yeah(MinecraftClient.getInstance().player, Text.of("yeahh"), matrices, MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE);
        }
    }
}
