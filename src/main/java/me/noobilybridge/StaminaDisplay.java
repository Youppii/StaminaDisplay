package me.noobilybridge;

import me.noobilybridge.config.StaminaConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;

import java.util.HashMap;
import java.util.Map;

public class StaminaDisplay implements ClientModInitializer {
    public static Map<AbstractClientPlayerEntity, Float> staminaValues = new HashMap<>();
    @Override
    public void onInitializeClient() {
        StaminaConfig.INSTANCE.load();
    }

    public static double ease(double start, double end, float speed) {
        return (start + (end - start) * (1 - Math.exp(-((double) MinecraftClient.getInstance().getRenderTime() / 1000000000) * speed)));
    }
}
