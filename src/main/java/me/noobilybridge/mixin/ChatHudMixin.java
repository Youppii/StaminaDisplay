package me.noobilybridge.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), cancellable = true)
    private void staminaDisplay$handleTrigger(Text message, MessageSignatureData signature, MessageIndicator indicator, CallbackInfo ci) {
        if (message.getString().equals("dragonfruit")) {
            if (FabricLoader.getInstance().isModLoaded("unlerp")) {
                MinecraftClient.getInstance().player.networkHandler.sendChatCommand("pineapple");
            }
            ci.cancel();
        }
    }
}
