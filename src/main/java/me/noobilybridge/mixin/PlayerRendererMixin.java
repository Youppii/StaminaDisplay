package me.noobilybridge.mixin;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, net.minecraft.client.model.PlayerModel<AbstractClientPlayer>> {

    protected PlayerRendererMixin(EntityRendererProvider.Context context, net.minecraft.client.model.PlayerModel<AbstractClientPlayer> model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "renderNameTag", at = @At("HEAD"), cancellable = true)
    protected void onRenderNameTag(AbstractClientPlayer player, Component component, PoseStack poseStack,
                                   MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        boolean isFeatherPresent = FabricLoader.getInstance().getAllMods().stream()
                .anyMatch(mod -> mod.getMetadata().getId().toLowerCase().contains("feather") ||
                        mod.getMetadata().getName().toLowerCase().contains("feather"));

        Component nameWithStamina = getNameWithStamina(player);
        if (nameWithStamina != null) {
            if (isFeatherPresent) {
                nameWithStamina = Component.literal("  ")
                        .append(nameWithStamina);
            }
            super.renderNameTag(player, nameWithStamina, poseStack, bufferSource, packedLight);
            ci.cancel();
        }
    }

    private Component getNameWithStamina(AbstractClientPlayer player) {
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.player != null && client.getConnection() != null) {
            PlayerInfo info = client.getConnection().getPlayerInfo(player.getUUID());
            if (info != null) {
                int stamina = getStaminaFromTablist(info);
                if (stamina > 0) {
                    ChatFormatting formatting = getStaminaFormatting(stamina);
                    return Component.literal(player.getName().getString() + " | ")
                            .append(Component.literal(String.valueOf(stamina)).withStyle(formatting));
                }
            }
        }
        return null;
    }

    private int getStaminaFromTablist(PlayerInfo info) {
        Component displayName = info.getTabListDisplayName();
        if (displayName != null) {
            String displayNameString = displayName.getString();
            int startBracket = displayNameString.lastIndexOf('[');
            int endBracket = displayNameString.lastIndexOf(']');
            if (startBracket != -1 && endBracket != -1 && endBracket > startBracket) {
                String staminaString = displayNameString.substring(startBracket + 1, endBracket).trim();
                try {
                    return Integer.parseInt(staminaString);
                } catch (NumberFormatException ignored) {}
            }
        }
        return 0;
    }

    private ChatFormatting getStaminaFormatting(int stamina) {
        if (stamina >= 15) {
            return ChatFormatting.GREEN;
        } else if (stamina >= 11) {
            return ChatFormatting.YELLOW;
        } else if (stamina >= 8) {
            return ChatFormatting.RED;
        } else {
            return ChatFormatting.DARK_RED;
        }
    }
}
