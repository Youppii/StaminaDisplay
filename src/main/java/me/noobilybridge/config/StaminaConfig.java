package me.noobilybridge.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.GsonConfigInstance;
import dev.isxander.yacl3.impl.controller.TickBoxControllerBuilderImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

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
    public float width = 0.5F;
    @ConfigEntry
    public float height = 0.5F;
    @ConfigEntry
    public float animationSpeed = 15F;
    @ConfigEntry
    public boolean iceTranslucencyDisable = false;
    @ConfigEntry
    public float cornerRounding = 0.25F;
    @ConfigEntry
    public float paddingAmount = 0.25F;
    @ConfigEntry
    public float verticalOffset = 0.25F;
    @ConfigEntry
    public float minStamina = 1;


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
                                .controller(ColorControllerBuilder::create)
                                .binding(Color.WHITE, () -> config.mainColor, color -> config.mainColor = color)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.of("Empty Color"))
                                .controller(ColorControllerBuilder::create)
                                .binding(Color.RED, () -> config.emptyMainColor, color -> config.emptyMainColor = color)
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.of("Width"))
                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(5F, 75F).step(0.5F).valueFormatter(aFloat -> Text.of(String.format("%.1f", aFloat))))
                                .binding(10F, () -> config.width, color -> config.width = color)
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.of("Height"))
                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 10F).step(0.5F).valueFormatter(aFloat -> Text.of(String.format("%.1f", aFloat))))
                                .binding(3F, () -> config.height, color -> config.height = color)
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.of("Vertical Offset"))
                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-10F, 10F).step(0.25F).valueFormatter(aFloat -> Text.of(String.format("%.2f", aFloat))))
                                .binding(0F, () -> config.verticalOffset, color -> config.verticalOffset = color)
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.of("Padding amount"))
                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.05F).valueFormatter(aFloat -> Text.of(String.format("%.2f", aFloat))))
                                .binding(0.25F, () -> config.paddingAmount, color -> config.paddingAmount = color)
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.of("Corner Rounding Amount"))
                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.05F).valueFormatter(aFloat -> Text.of(String.format("%.2f", aFloat))))
                                .binding(0.25F, () -> config.cornerRounding, color -> config.cornerRounding = color)
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.of("Animation Speed"))
                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(1F, 20F).step(0.1F).valueFormatter(aFloat -> Text.of(String.format("%.1f", aFloat))))
                                .binding(15F, () -> config.animationSpeed, color -> config.animationSpeed = color)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.of("Disable Ice Translucency"))
                                .description(OptionDescription.of(Text.of("Disable transparency effects for ice. Improves performance and prevents weirdness. Requires a game restart.")))
                                .controller(TickBoxControllerBuilderImpl::new)
                                .binding(false, () ->  config.iceTranslucencyDisable, newVal -> config.iceTranslucencyDisable = newVal)
                                .build())
                        .build())
                ).generateScreen(parent);
    }
}
