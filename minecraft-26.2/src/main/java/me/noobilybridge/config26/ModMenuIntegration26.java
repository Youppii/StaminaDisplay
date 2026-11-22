package me.noobilybridge.config26;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;

public final class ModMenuIntegration26 implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModMenuIntegration26::open;
    }

    public static Screen open(Screen parent) {
        if (FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) {
            try {
                Class<?> screen = Class.forName("me.noobilybridge.config26.StaminaYaclScreen26");
                return (Screen) screen.getMethod("create", Screen.class).invoke(null, parent);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return new MissingYaclScreen(parent);
    }

    private static final class MissingYaclScreen extends Screen {
        private final Screen parent;

        private MissingYaclScreen(Screen parent) {
            super(Component.literal("Stamina Display"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            addRenderableWidget(Button.builder(Component.literal("YACL is required to edit settings"), button -> {})
                    .bounds(width / 2 - 145, height / 2 - 10, 290, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Done"), button -> Minecraft.getInstance().gui.setScreen(parent))
                    .bounds(width / 2 - 100, height / 2 + 20, 200, 20).build());
        }
    }
}
