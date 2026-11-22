package me.noobilybridge.mixin26;

import me.noobilybridge.config26.StaminaSettings26;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BakedQuad.MaterialInfo.class)
public abstract class IceMaterialInfoMixin {
    @Shadow private TextureAtlasSprite sprite;

    @Inject(method = "layer", at = @At("HEAD"), cancellable = true)
    private void staminaDisplay$forceIceSolid(CallbackInfoReturnable<ChunkSectionLayer> cir) {
        if (!StaminaSettings26.iceTranslucencyDisable) return;
        String path = sprite.contents().name().getPath();
        if (path.equals("block/ice") || path.equals("block/packed_ice") || path.equals("block/blue_ice")
                || path.matches("block/frosted_ice_[0-3]")) {
            cir.setReturnValue(ChunkSectionLayer.SOLID);
        }
    }
}
