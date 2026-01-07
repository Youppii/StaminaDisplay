package me.noobilybridge.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.GsonConfigInstance;
import dev.isxander.yacl3.impl.controller.FloatSliderControllerBuilderImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class StaminaConfig {
    public static GsonConfigInstance<StaminaConfig> INSTANCE = GsonConfigInstance.createBuilder(StaminaConfig.class).setPath(FabricLoader.getInstance().getConfigDir().resolve("staminadisplay.json")).build();

    @ConfigEntry
    public Color outlineColor = Color.BLACK;
    @ConfigEntry
    public Color mainColor = Color.WHITE;
    @ConfigEntry
    public Color emptyMainColor = Color.RED;
    @ConfigEntry
    public float scale = 0.5F;

    public static Screen getScreen(Screen parent) {
        return YetAnotherConfigLib.create(INSTANCE, (defaults, config, builder) -> builder
                .title(Text.of("Stamina Display"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.of("General"))
                        .option(Option.<Color>createBuilder()
                                .name(Text.of("Outline Color"))
                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                .binding(Color.BLACK, () -> config.outlineColor, color -> config.outlineColor = color)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.of("Main Color"))
                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                .binding(Color.WHITE, () -> config.mainColor, color -> config.mainColor = color)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.of("Empty Color"))
                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                .binding(Color.RED, () -> config.emptyMainColor, color -> config.emptyMainColor = color)
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.of("Scale"))
                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1.5F).step(0.05F).valueFormatter(aFloat -> Text.of(String.valueOf(aFloat))))
                                .binding(0.5F, () -> config.scale, color -> config.scale = color)
                                .build())
                        .build())
                ).generateScreen(parent);
    }
}
