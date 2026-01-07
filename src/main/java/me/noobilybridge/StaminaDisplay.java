package me.noobilybridge;

import me.noobilybridge.config.StaminaConfig;
import net.fabricmc.api.ClientModInitializer;

public class StaminaDisplay implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        StaminaConfig.INSTANCE.load();
    }
}
