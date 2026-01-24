package me.noobilybridge;

import com.mojang.blaze3d.systems.RenderSystem;
import me.noobilybridge.config.StaminaConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StaminaDisplay implements ClientModInitializer {
    public static Map<AbstractClientPlayerEntity, Float> staminaValues = new HashMap<>();

    @Override
    public void onInitializeClient() {
        StaminaConfig.INSTANCE.load();
        if (StaminaConfig.INSTANCE.getConfig().iceTranslucencyDisable) {
            BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getSolid(), Blocks.ICE, Blocks.PACKED_ICE, Blocks.FROSTED_ICE, Blocks.BLUE_ICE);
        }
    }

    public static double ease(double start, double end, float speed) {
        return (start + (end - start) * (1 - Math.exp(-((double) MinecraftClient.getInstance().getRenderTime() / 1000000000) * speed)));
    }
}
