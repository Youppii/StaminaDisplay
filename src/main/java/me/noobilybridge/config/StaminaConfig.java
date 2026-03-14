package me.noobilybridge.config;

import com.google.gson.Gson;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.GsonConfigInstance;
import dev.isxander.yacl3.impl.controller.EnumControllerBuilderImpl;
import dev.isxander.yacl3.impl.controller.StringControllerBuilderImpl;
import dev.isxander.yacl3.impl.controller.TickBoxControllerBuilderImpl;
import me.noobilybridge.StaminaDisplay;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.include.com.google.gson.JsonSyntaxException;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaminaConfig {
    public static GsonConfigInstance<StaminaConfig> INSTANCE = GsonConfigInstance.createBuilder(StaminaConfig.class).setPath(FabricLoader.getInstance().getConfigDir().resolve("staminadisplay.json")).build();

    @ConfigEntry
    public Color outlineColor = new Color(0, 0, 0, 64);
    @ConfigEntry
    public Color secondaryOutlineColor = new Color(0, 0, 0, 128);
    @ConfigEntry
    public boolean useTeamForMain = true;
    @ConfigEntry
    public Color mainColor = Color.WHITE;
    @ConfigEntry
    public boolean evilMainColor = false;
    @ConfigEntry
    public Color secondaryMainColor = Color.WHITE;
    @ConfigEntry
    public Color hungerBarMainColor = new Color(255, 128, 0);
    @ConfigEntry
    public Color hungerBarSecondaryColor = new Color(255, 0, 128);
    @ConfigEntry
    public Color emptyMainColor = Color.RED;
    @ConfigEntry
    public float width = 75F;
    @ConfigEntry
    public float height = 4F;
    @ConfigEntry
    public float verticalOffset = 7.5F;
    //    @ConfigEntry
//    public float trimLeftWidth = 0F;
    @ConfigEntry
    public float animationSpeed = 15F;
    @ConfigEntry
    public boolean iceTranslucencyDisable = false;
    @ConfigEntry
    public boolean customHungerBar = true;
    @ConfigEntry
    public boolean renderBar = true;
    @ConfigEntry
    public boolean showNumber = true;
    @ConfigEntry
    public Color numberColor = Color.WHITE;
    @ConfigEntry
    public float numberScale = 0.5F;
    @ConfigEntry
    public float cornerRounding = 0.25F;
    @ConfigEntry
    public float paddingAmount = 0.75F;
    @ConfigEntry
    public int minStamina = 0;
    @ConfigEntry
    public float outlineThickness = 0.25F;
    @ConfigEntry
    public TextStyle textStyle = TextStyle.SHADOW;
    @ConfigEntry
    public Color numberBGColor = Color.BLACK;
    @ConfigEntry
    public Color strokeColor = new Color(0, 64, 128, 128);
    @ConfigEntry
    public Color outerStrokeColor = new Color(0, 0, 0, 128);
    @ConfigEntry
    public NumberPosition numberPosition = NumberPosition.AFTER_NAME;
    @ConfigEntry
    public float hungerBarWidth = 79F;
    @ConfigEntry
    public float hungerBarHeight = 5.5F;
    @ConfigEntry
    public float hungerBarVerticalOffset = 0F;
    @ConfigEntry
    public boolean fitToName = true;
    @ConfigEntry
    public boolean drawNametagTeam = true;
    @ConfigEntry
    public boolean overrideHomeColor = false;
    @ConfigEntry
    public Color homeTeamColorOverride = Color.WHITE;
    @ConfigEntry
    public Color secondaryHomeTeamColorOverride = Color.WHITE;
    @ConfigEntry
    public boolean overrideAwayColor = false;
    @ConfigEntry
    public Color awayTeamColorOverride = Color.WHITE;
    @ConfigEntry
    public Color secondaryAwayTeamColorOverride = Color.WHITE;
    @ConfigEntry
    public String innerText = " - ";
    @ConfigEntry
    public String outerText = "";
    @ConfigEntry
    public boolean mainColorGradientDirection = true;
    @ConfigEntry
    public boolean hungerBarGradientDirection = false;
    @ConfigEntry
    public boolean outlineIsTheBar = false;
    @ConfigEntry
    public float zAxisOffset = 0.1F;
    @ConfigEntry
    public boolean nametagShadow = true;
    @ConfigEntry
    public boolean teamForNumber = true;
    @ConfigEntry
    public boolean teamForNumberBG = true;
    @ConfigEntry
    public Color emptyNumberColor = Color.RED;
    @ConfigEntry
    public Color secondaryEmptyMainColor = Color.RED.darker();
    @ConfigEntry
    public Color nametagBGColor = new Color(0, 0, 0, 32);
    @ConfigEntry
    public boolean teamForNametagBG = true;
    @ConfigEntry
    public Color nametagTextCol = Color.WHITE;
    @ConfigEntry
    public boolean teamForNametagText = true;
    @ConfigEntry
    public boolean backgroundDirection = true;
    @ConfigEntry
    public boolean useEmptyColor = false;
    @ConfigEntry
    public boolean showNumberInBar = true;
    @ConfigEntry
    public Color barNumberColor = Color.WHITE;
    @ConfigEntry
    public float barNumberScale = 0.5F;


    public static Screen getScreen(Screen parent) {
        return YetAnotherConfigLib.create(INSTANCE, (defaults, config, builder) -> builder
                        .title(Text.of("Stamina Display"))
                        .category(ConfigCategory.createBuilder()
                                .name(Text.of("Bar"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.of("Render Bar"))
                                        .controller(TickBoxControllerBuilderImpl::new)
                                        .binding(true, () -> config.renderBar, newVal -> config.renderBar = newVal)
                                        .build())
                                .group(OptionGroup.createBuilder()
                                        .name(Text.of("Transform"))
                                        .option(Option.<Float>createBuilder()
                                                .name(Text.of("Width"))
                                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(30F, 125F).step(0.5F).valueFormatter(aFloat -> Text.of(String.format("%.1f", aFloat))))
                                                .binding(75F, () -> config.width, color -> config.width = color)
                                                .build())
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.of("Fit To Name"))
                                                .controller(TickBoxControllerBuilderImpl::new)
                                                .binding(true, () -> config.fitToName, newVal -> config.fitToName = newVal)
                                                .build())
//                                .option(Option.<Float>createBuilder()
//                                        .name(Text.of("Trim Width From Left"))
//                                        .description(OptionDescription.of(Text.of("How much you want to trim the bar from the left side, to make space for either a number or icon.")))
//                                        .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.1F).valueFormatter(aFloat -> Text.of(String.format("%f.1f", aFloat * 100) + "%")))
//                                        .binding(0F, () -> config.trimLeftWidth, color -> config.trimLeftWidth = color)
//                                        .build())
                                        .option(Option.<Float>createBuilder()
                                                .name(Text.of("Thickness"))
                                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(3F, 10F).step(0.5F).valueFormatter(aFloat -> Text.of(String.format("%.1f", aFloat))))
                                                .binding(4F, () -> config.height, color -> config.height = color)
                                                .build())
                                        .option(Option.<Float>createBuilder()
                                                .name(Text.of("Vertical Offset"))
                                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-15F, 15F).step(0.25F).valueFormatter(aFloat -> Text.of(String.format("%.2f", aFloat))))
                                                .binding(7.5F, () -> config.verticalOffset, color -> config.verticalOffset = color)
                                                .build())
                                        .build())
                                .group(OptionGroup.createBuilder()
                                        .name(Text.of("Appearance"))
                                        .option(Option.<Integer>createBuilder()
                                                .name(Text.of("Reduce Max Stamina By"))
                                                .description(OptionDescription.of(Text.of("By default, stamina is measured as out of 20, but you can change this number if you care more about a specific range like between 10-20.")))
                                                .controller(floatOption -> IntegerSliderControllerBuilder.create(floatOption).range(0, 19).step(1))
                                                .binding(0, () -> config.minStamina, color -> config.minStamina = color)
                                                .build())
                                        .option(Option.<Float>createBuilder()
                                                .name(Text.of("Padding amount"))
                                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 2F).step(0.05F).valueFormatter(aFloat -> Text.of(String.format("%.2f", aFloat))))
                                                .binding(0.75F, () -> config.paddingAmount, color -> config.paddingAmount = color)
                                                .build())
                                        .option(Option.<Float>createBuilder()
                                                .name(Text.of("Corner Rounding Amount"))
                                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.01F).valueFormatter(aFloat -> Text.of(String.format("%.2f", aFloat))))
                                                .binding(0.25F, () -> config.cornerRounding, color -> config.cornerRounding = color)
                                                .build())
                                        .build())
                                .build())
                        .category(ConfigCategory.createBuilder()
                                .name(Text.of("Nametag"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.of("Render Team Outline"))
                                        .controller(TickBoxControllerBuilderImpl::new)
                                        .binding(true, () -> config.drawNametagTeam, newVal -> config.drawNametagTeam = newVal)
                                        .build())
                                .group(OptionGroup.createBuilder()
                                        .name(Text.of("yeah"))
                                        .option(Option.<Float>createBuilder()
                                                .name(Text.of("Nametag Outline Thickness"))
                                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 1F).step(0.05F).valueFormatter(aFloat -> Text.of(String.format("%.2f", aFloat))))
                                                .binding(0.25F, () -> config.outlineThickness, color -> config.outlineThickness = color)
                                                .build())
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.of("Outline IS The Bar"))
                                                .controller(TickBoxControllerBuilderImpl::new)
                                                .binding(false, () -> config.outlineIsTheBar, newVal -> config.outlineIsTheBar = newVal)
                                                .build())
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.of("Text Shadow"))
                                                .controller(TickBoxControllerBuilderImpl::new)
                                                .binding(true, () -> config.nametagShadow, newVal -> config.nametagShadow = newVal)
                                                .build())
                                        .build())
                                .build())
                        .category(ConfigCategory.createBuilder()
                                .name(Text.of("Number"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.of("Render Number"))
                                        .controller(TickBoxControllerBuilderImpl::new)
                                        .binding(true, () -> config.showNumber, newVal -> config.showNumber = newVal)
                                        .build())
                                .group(OptionGroup.createBuilder()
                                        .name(Text.of("Number"))
                                        .option(Option.<Float>createBuilder()
                                                .name(Text.of("Number Scale"))
                                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0.25F, 1F).step(0.05F).valueFormatter(aFloat -> Text.of(String.format("%.2f", aFloat))))
                                                .binding(0.5F, () -> config.numberScale, color -> config.numberScale = color)
                                                .build())
                                        .option(Option.<TextStyle>createBuilder()
                                                .name(Text.of("Text Style"))
                                                .controller(textStyleOption -> EnumControllerBuilder.create(textStyleOption).enumClass(TextStyle.class))
                                                .binding(TextStyle.SHADOW, () -> config.textStyle, newVal -> config.textStyle = newVal)
                                                .build())
                                        .option(Option.<NumberPosition>createBuilder()
                                                .name(Text.of("Position"))
                                                .controller(numberPositionOption -> EnumControllerBuilder.create(numberPositionOption).enumClass(NumberPosition.class))
                                                .binding(NumberPosition.AFTER_NAME, () -> config.numberPosition, newVal -> config.numberPosition = newVal)
                                                .build())
                                        .option(Option.<String>createBuilder()
                                                .name(Text.of("Inner Text"))
                                                .binding(" - ", () -> config.innerText, newVal -> config.innerText = newVal)
                                                .controller(StringControllerBuilderImpl::new)
                                                .build())
                                        .option(Option.<String>createBuilder()
                                                .name(Text.of("Outer Text"))
                                                .binding("", () -> config.outerText, newVal -> config.outerText = newVal)
                                                .controller(StringControllerBuilderImpl::new)
                                                .build())
                                        .build())
                                .build())
                        .category(ConfigCategory.createBuilder()
                                .name(Text.of("Colors"))
                                .group(OptionGroup.createBuilder()
                                        .name(Text.of("Teams"))
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.of("Override Home Color"))
                                                .controller(TickBoxControllerBuilderImpl::new)
                                                .binding(false, () -> config.overrideHomeColor, newVal -> config.overrideHomeColor = newVal)
                                                .build())
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Custom Home Color"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(Color.WHITE, () -> config.homeTeamColorOverride, color -> config.homeTeamColorOverride = color)
                                                .build())
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Secondary Custom Home Color"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(Color.WHITE, () -> config.secondaryHomeTeamColorOverride, color -> config.secondaryHomeTeamColorOverride = color)
                                                .build())
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.of("Override Away Color"))
                                                .controller(TickBoxControllerBuilderImpl::new)
                                                .binding(false, () -> config.overrideAwayColor, newVal -> config.overrideAwayColor = newVal)
                                                .build())
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Custom Away Color"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(Color.WHITE, () -> config.awayTeamColorOverride, color -> config.awayTeamColorOverride = color)
                                                .build())
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Secondary Custom Away Color"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(Color.WHITE, () -> config.secondaryAwayTeamColorOverride, color -> config.secondaryAwayTeamColorOverride = color)
                                                .build())
                                        .build())
                                .group(OptionGroup.createBuilder()
                                        .name(Text.of("Main Bar Colors"))
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Main (Full) Color"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(Color.WHITE, () -> config.mainColor, color -> config.mainColor = color)
                                                .build())
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Secondary Main (Full) Color"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(Color.WHITE, () -> config.secondaryMainColor, color -> config.secondaryMainColor = color)
                                                .build())
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.of("Use Team For Main Color"))
                                                .controller(TickBoxControllerBuilderImpl::new)
                                                .binding(true, () -> config.useTeamForMain, newVal -> config.useTeamForMain = newVal)
                                                .build())
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.of("Vertical Gradient"))
                                                .controller(TickBoxControllerBuilderImpl::new)
                                                .binding(true, () -> config.mainColorGradientDirection, newVal -> config.mainColorGradientDirection = newVal)
                                                .build())
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.of("Evil Mode"))
                                                .controller(TickBoxControllerBuilderImpl::new)
                                                .binding(false, () -> config.evilMainColor, newVal -> config.evilMainColor = newVal)
                                                .build())
                                        .build())
                                .group(OptionGroup.createBuilder()
                                        .name(Text.of("Number Color"))
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Number Color (Full)"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(Color.WHITE, () -> config.numberColor, color -> config.numberColor = color)
                                                .build())
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Number Color (Empty)"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(Color.RED, () -> config.emptyNumberColor, color -> config.emptyNumberColor = color)
                                                .build())
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.of("Use Team For Number Color"))
                                                .controller(TickBoxControllerBuilderImpl::new)
                                                .binding(true, () -> config.teamForNumber, newVal -> config.teamForNumber = newVal)
                                                .build())
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Number Background Color"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(Color.BLACK, () -> config.numberBGColor, color -> config.numberBGColor = color)
                                                .build())
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.of("Use Team For Background Color (Tinted)"))
                                                .controller(TickBoxControllerBuilderImpl::new)
                                                .binding(true, () -> config.teamForNumberBG, newVal -> config.teamForNumberBG = newVal)
                                                .build())
                                        .build())
                                .group(OptionGroup.createBuilder()
                                        .name(Text.of("Nametag"))
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Background Color"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(new Color(0, 0, 0, 32), () -> config.nametagBGColor, color -> config.nametagBGColor = color)
                                                .build())
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.of("Use Team For Background Color"))
                                                .controller(TickBoxControllerBuilderImpl::new)
                                                .binding(true, () -> config.teamForNametagBG, newVal -> config.teamForNametagBG = newVal)
                                                .build())
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Text Color"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(Color.WHITE, () -> config.nametagTextCol, color -> config.nametagTextCol = color)
                                                .build())
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.of("Use Team For Text Color"))
                                                .controller(TickBoxControllerBuilderImpl::new)
                                                .binding(true, () -> config.teamForNametagText, newVal -> config.teamForNametagText = newVal)
                                                .build())
                                        .build())
                                .group(OptionGroup.createBuilder()
                                        .name(Text.of("Bar Extras"))
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Background Color"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(new Color(0, 0, 0, 0.25F), () -> config.outlineColor, color -> config.outlineColor = color)
                                                .build())
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Secondary Background Color"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(new Color(0, 0, 0, 0.5F), () -> config.secondaryOutlineColor, color -> config.secondaryOutlineColor = color)
                                                .build())
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.of("Vertical Gradient For Background"))
                                                .controller(TickBoxControllerBuilderImpl::new)
                                                .binding(true, () -> config.backgroundDirection, newVal -> config.backgroundDirection = newVal)
                                                .build())
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Empty Color"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(Color.RED, () -> config.emptyMainColor, color -> config.emptyMainColor = color)
                                                .build())
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Secondary Empty Color"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(Color.RED.darker(), () -> config.secondaryEmptyMainColor, color -> config.secondaryEmptyMainColor = color)
                                                .build())
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.of("Use Empty Color"))
                                                .controller(TickBoxControllerBuilderImpl::new)
                                                .binding(false, () -> config.useEmptyColor, newVal -> config.useEmptyColor = newVal)
                                                .build())
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Stroke Color"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(new Color(0, 64, 128, 128), () -> config.strokeColor, color -> config.strokeColor = color)
                                                .build())
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Outer Stroke Color"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(new Color(0, 0, 0, 128), () -> config.outerStrokeColor, color -> config.outerStrokeColor = color)
                                                .build())
                                        .build())
                                .group(OptionGroup.createBuilder()
                                        .name(Text.of("First Person Bar"))
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Color"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(new Color(255, 128, 0, 255), () -> config.hungerBarMainColor, color -> config.hungerBarMainColor = color)
                                                .build())
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Secondary Color"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(new Color(255, 0, 128, 255), () -> config.hungerBarSecondaryColor, color -> config.hungerBarSecondaryColor = color)
                                                .build())
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.of("Vertical Gradient"))
                                                .controller(TickBoxControllerBuilderImpl::new)
                                                .binding(false, () -> config.hungerBarGradientDirection, newVal -> config.hungerBarGradientDirection = newVal)
                                                .build())
                                        .option(Option.<Color>createBuilder()
                                                .name(Text.of("Bar Number Color"))
                                                .controller(colorOption -> ColorControllerBuilder.create(colorOption).allowAlpha(true))
                                                .binding(Color.WHITE, () -> config.barNumberColor, color -> config.barNumberColor = color)
                                                .build())
                                        .build())
                                .build())
                        .category(ConfigCategory.createBuilder()
                                .name(Text.of("HUD"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.of("Custom Hunger Bar"))
                                        .description(OptionDescription.of(Text.of("Replace the normal hunger bar with the custom bar.")))
                                        .controller(TickBoxControllerBuilderImpl::new)
                                        .binding(true, () -> config.customHungerBar, newVal -> config.customHungerBar = newVal)
                                        .build())
                                .group(OptionGroup.createBuilder()
                                        .name(Text.of("Transform"))
                                        .option(Option.<Float>createBuilder()
                                                .name(Text.of("Width"))
                                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(20F, 100F).step(0.5F).valueFormatter(aFloat -> Text.of(String.format("%.1f", aFloat))))
                                                .binding(79F, () -> config.hungerBarWidth, color -> config.hungerBarWidth = color)
                                                .build())
                                        .option(Option.<Float>createBuilder()
                                                .name(Text.of("Thickness"))
                                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0F, 10F).step(0.5F).valueFormatter(aFloat -> Text.of(String.format("%.1f", aFloat))))
                                                .binding(5.5F, () -> config.hungerBarHeight, color -> config.hungerBarHeight = color)
                                                .build())
                                        .option(Option.<Float>createBuilder()
                                                .name(Text.of("Vertical Offset"))
                                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(-10F, 10F).step(0.25F).valueFormatter(aFloat -> Text.of(String.format("%.2f", aFloat))))
                                                .binding(0F, () -> config.hungerBarVerticalOffset, color -> config.hungerBarVerticalOffset = color)
                                                .build())
                                        .build())
                                .group(OptionGroup.createBuilder()
                                        .name(Text.of("Number"))
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.of("Show Number"))
                                                .controller(TickBoxControllerBuilderImpl::new)
                                                .binding(true, () -> config.showNumberInBar, newVal -> config.showNumberInBar = newVal)
                                                .build())
                                        .option(Option.<Float>createBuilder()
                                                .name(Text.of("Number Scale"))
                                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0.25F, 1F).step(0.05F).valueFormatter(aFloat -> Text.of(String.format("%.2f", aFloat))))
                                                .binding(0.5F, () -> config.barNumberScale, color -> config.barNumberScale = color)
                                                .build())
                                        .build())
                                .build())
                        .category(ConfigCategory.createBuilder()
                                .name(Text.of("Misc."))
                                .group(OptionGroup.createBuilder()
                                        .name(Text.of("yeah"))
                                        .option(Option.<Float>createBuilder()
                                                .name(Text.of("Animation Speed"))
                                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(1F, 20F).step(0.1F).valueFormatter(aFloat -> Text.of(String.format("%.1f", aFloat))))
                                                .binding(15F, () -> config.animationSpeed, color -> config.animationSpeed = color)
                                                .build())
                                        .option(Option.<Boolean>createBuilder()
                                                .name(Text.of("Disable Ice Translucency"))
                                                .description(OptionDescription.of(Text.of("Disable transparency effects for ice. Improves performance and prevents weirdness. Requires a game restart.")))
                                                .controller(TickBoxControllerBuilderImpl::new)
                                                .binding(false, () -> config.iceTranslucencyDisable, newVal -> config.iceTranslucencyDisable = newVal)
                                                .build())
                                        .option(Option.<Float>createBuilder()
                                                .name(Text.of("Z Axis Offset"))
                                                .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0.01F, 1F).step(0.01F).valueFormatter(aFloat -> Text.of(String.format("%.2f", aFloat))))
                                                .binding(0.1F, () -> config.zAxisOffset, color -> config.zAxisOffset = color)
                                                .build())
                                        .build())
                                .group(OptionGroup.createBuilder()
                                        .name(Text.of("Config"))
                                        .option(ButtonOption.createBuilder()
                                                .name(Text.of("Copy Current Config"))
                                                .description(OptionDescription.of(Text.of("Copies the current configuration as text to your clipboard. Go share your configs with your buddies! (Make sure to save the config first.)")))
                                                .action((yaclScreen, buttonOption) -> {
                                                    MinecraftClient.getInstance().keyboard.setClipboard(StaminaDisplay.gson.toJson(INSTANCE.getConfig()));
                                                    System.out.println("Copied!");
                                                })
                                                .text(Text.of("Copy"))
                                                .build())
                                        .option(ButtonOption.createBuilder()
                                                .name(Text.literal("Load Config From Clipboard").formatted(Formatting.DARK_RED, Formatting.BOLD))
                                                .description(OptionDescription.of(Text.of("Loads a configuration from your clipboard if it's valid. WARNING: LOADING A VALID CONFIGURATION WILL OVERWRITE YOUR CONFIGURATION FILE. The screen will close itself, reopen it to see your new values.")))
                                                .text(Text.of("Load"))
                                                .action((yaclScreen, buttonOption) -> {
                                                    //this sucks but it works!!
                                                    try {
                                                        StaminaDisplay.gson.fromJson(MinecraftClient.getInstance().keyboard.getClipboard(), StaminaConfig.class);
                                                    } catch (JsonSyntaxException e) {
                                                        System.out.println("invalid config!!!");
                                                        return;
                                                    }
                                                    try {
                                                        Path path = FabricLoader.getInstance().getConfigDir().resolve("staminadisplay.json");
                                                        Files.delete(path);
                                                        Files.createFile(path);
                                                        Files.writeString(path, MinecraftClient.getInstance().keyboard.getClipboard(), StandardCharsets.UTF_8);
                                                        StaminaConfig.INSTANCE.load();
                                                        MinecraftClient.getInstance().setScreen(parent);
                                                        System.out.println("Loaded!");
                                                    } catch (IOException e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                })
                                                .build())
                                        .build())
                                .build())
        ).generateScreen(parent);
    }
}