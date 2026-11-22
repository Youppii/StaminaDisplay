package me.noobilybridge.config26;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.Color;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static me.noobilybridge.config26.StaminaSettings26.*;

public final class StaminaYaclScreen26 {
    private StaminaYaclScreen26() {}

    public static Screen create(Screen parent) {
        return YetAnotherConfigLib.createBuilder().title(Component.literal("Stamina Display"))
                .save(StaminaSettings26::save).category(bar()).category(nametag()).category(number())
                .category(colors()).category(hud()).category(misc(parent)).build().generateScreen(parent);
    }

    private static ConfigCategory bar() {
        var c = ConfigCategory.createBuilder().name(Component.literal("Bar"));
        c.option(bool("Render Bar", true, () -> renderBar, v -> renderBar = v));
        c.option(bool("Low Stamina Cue", false, () -> lowStaminaCue, v -> lowStaminaCue = v));
        c.option(bool("Teammates Only", false, () -> teammatesOnly, v -> teammatesOnly = v));
        c.group(group("Transform", decimal("Width",75,30,125,.5F,()->width,v->width=v),
                bool("Fit To Name",true,()->fitToName,v->fitToName=v), decimal("Thickness",4,3,10,.5F,()->height,v->height=v),
                decimal("Vertical Offset",7.5F,-15,15,.25F,()->verticalOffset,v->verticalOffset=v)));
        c.group(group("Appearance",integer("Reduce Max Stamina By",0,19,()->minStamina,v->minStamina=v),
                decimal("Padding amount",.75F,0,2,.05F,()->paddingAmount,v->paddingAmount=v),
                decimal("Corner Rounding Amount",.25F,0,1,.01F,()->cornerRounding,v->cornerRounding=v)));
        return c.build();
    }

    private static ConfigCategory nametag() {
        var c = ConfigCategory.createBuilder().name(Component.literal("Nametag"));
        c.option(bool("Render Team Outline",true,()->drawNametagTeam,v->drawNametagTeam=v));
        c.group(group("yeah",decimal("Nametag Outline Thickness",.25F,0,1,.05F,()->outlineThickness,v->outlineThickness=v),
                bool("Outline IS The Bar",false,()->outlineIsTheBar,v->outlineIsTheBar=v),
                bool("Text Shadow",true,()->nametagShadow,v->nametagShadow=v)));
        return c.build();
    }

    private static ConfigCategory number() {
        var c = ConfigCategory.createBuilder().name(Component.literal("Number"));
        c.option(bool("Render Number",true,()->showNumber,v->showNumber=v));
        c.group(group("Number",decimal("Number Scale",.5F,.25F,1,.05F,()->numberScale,v->numberScale=v),
                enumeration("Text Style",TextStyle26.class,TextStyle26.SHADOW,()->textStyle,v->textStyle=v),
                enumeration("Position",NumberPosition26.class,NumberPosition26.AFTER_NAME,()->numberPosition,v->numberPosition=v),
                string("Inner Text"," - ",()->innerText,v->innerText=v), string("Outer Text","",()->outerText,v->outerText=v)));
        return c.build();
    }

    private static ConfigCategory colors() {
        var c = ConfigCategory.createBuilder().name(Component.literal("Colors"));
        c.group(group("Teams", bool("Override Home Color",false,()->overrideHomeColor,v->overrideHomeColor=v),
                color("Custom Home Color",-1,()->homeTeamColorOverride,v->homeTeamColorOverride=v),
                color("Secondary Custom Home Color",-1,()->secondaryHomeTeamColorOverride,v->secondaryHomeTeamColorOverride=v),
                bool("Override Away Color",false,()->overrideAwayColor,v->overrideAwayColor=v),
                color("Custom Away Color",-1,()->awayTeamColorOverride,v->awayTeamColorOverride=v),
                color("Secondary Custom Away Color",-1,()->secondaryAwayTeamColorOverride,v->secondaryAwayTeamColorOverride=v)));
        c.group(group("Main Bar Colors",color("Main (Full) Color",-1,()->mainColor,v->mainColor=v),
                color("Secondary Main (Full) Color",-1,()->secondaryMainColor,v->secondaryMainColor=v),
                bool("Use Team For Main Color",true,()->useTeamForMain,v->useTeamForMain=v),
                bool("Vertical Gradient",true,()->mainColorGradientDirection,v->mainColorGradientDirection=v),
                bool("Evil Mode",false,()->evilMainColor,v->evilMainColor=v)));
        c.group(group("Number Color",color("Number Color (Full)",-1,()->numberColor,v->numberColor=v),
                color("Number Color (Empty)",0xFFFF0000,()->emptyNumberColor,v->emptyNumberColor=v),
                bool("Use Team For Number Color",true,()->teamForNumber,v->teamForNumber=v),
                color("Number Background Color",0xFF000000,()->numberBGColor,v->numberBGColor=v),
                bool("Use Team For Background Color (Tinted)",true,()->teamForNumberBG,v->teamForNumberBG=v)));
        c.group(group("Nametag",color("Background Color",0x20000000,()->nametagBGColor,v->nametagBGColor=v),
                bool("Use Team For Background Color",true,()->teamForNametagBG,v->teamForNametagBG=v),
                color("Text Color",-1,()->nametagTextCol,v->nametagTextCol=v),
                bool("Use Team For Text Color",true,()->teamForNametagText,v->teamForNametagText=v)));
        c.group(group("Bar Extras",color("Background Color",0x40000000,()->outlineColor,v->outlineColor=v),
                color("Secondary Background Color",0x80000000,()->secondaryOutlineColor,v->secondaryOutlineColor=v),
                bool("Vertical Gradient For Background",true,()->backgroundDirection,v->backgroundDirection=v),
                color("Empty Color",0xFFFF0000,()->emptyMainColor,v->emptyMainColor=v),
                color("Secondary Empty Color",0xFFB20000,()->secondaryEmptyMainColor,v->secondaryEmptyMainColor=v),
                bool("Use Empty Color",false,()->useEmptyColor,v->useEmptyColor=v),
                color("Stroke Color",0x80004080,()->strokeColor,v->strokeColor=v),
                color("Outer Stroke Color",0x80000000,()->outerStrokeColor,v->outerStrokeColor=v)));
        c.group(group("First Person Bar",color("Color",0xFFFF8000,()->hungerBarMainColor,v->hungerBarMainColor=v),
                color("Secondary Color",0xFFFF0080,()->hungerBarSecondaryColor,v->hungerBarSecondaryColor=v),
                bool("Vertical Gradient",false,()->hungerBarGradientDirection,v->hungerBarGradientDirection=v),
                color("Bar Number Color",-1,()->barNumberColor,v->barNumberColor=v)));
        return c.build();
    }

    private static ConfigCategory hud() {
        var c = ConfigCategory.createBuilder().name(Component.literal("HUD"));
        c.option(bool("Custom Hunger Bar",true,()->customHungerBar,v->customHungerBar=v));
        c.group(group("Transform",decimal("Width",79,20,100,.5F,()->hungerBarWidth,v->hungerBarWidth=v),
                decimal("Thickness",5.5F,0,10,.5F,()->hungerBarHeight,v->hungerBarHeight=v),
                decimal("Vertical Offset",0,-10,10,.25F,()->hungerBarVerticalOffset,v->hungerBarVerticalOffset=v)));
        c.group(group("Number",bool("Show Number",true,()->showNumberInBar,v->showNumberInBar=v),
                decimal("Number Scale",.5F,.25F,1,.05F,()->barNumberScale,v->barNumberScale=v),
                color("Number Color",-1,()->barNumberColor,v->barNumberColor=v)));
        return c.build();
    }

    private static ConfigCategory misc(Screen parent) {
        var c = ConfigCategory.createBuilder().name(Component.literal("Misc."));
        c.group(group("yeah",decimal("Animation Speed",15,1,20,.1F,()->animationSpeed,v->animationSpeed=v),
                bool("Disable Ice Translucency",false,()->iceTranslucencyDisable,v->iceTranslucencyDisable=v),
                decimal("Z Axis Offset",.1F,.01F,1,.01F,()->zAxisOffset,v->zAxisOffset=v)));
        c.group(OptionGroup.createBuilder().name(Component.literal("Config"))
                .option(dev.isxander.yacl3.api.ButtonOption.createBuilder().name(Component.literal("Copy Current Config"))
                        .text(Component.literal("Copy")).action((screen, option) -> Minecraft.getInstance().keyboardHandler.setClipboard(exportJson())).build())
                .option(dev.isxander.yacl3.api.ButtonOption.createBuilder().name(Component.literal("Load Config From Clipboard"))
                        .text(Component.literal("Load")).action((screen, option) -> {
                            if (loadJson(Minecraft.getInstance().keyboardHandler.getClipboard())) {
                                Minecraft.getInstance().setScreenAndShow(create(parent));
                            }
                        }).build()).build());
        return c.build();
    }

    private static OptionGroup group(String title, Option<?>... options) {
        var g = OptionGroup.createBuilder().name(Component.literal(title));
        for (Option<?> option : options) g.option(option);
        return g.build();
    }
    private static Option<Boolean> bool(String n, boolean d, Supplier<Boolean> get, Consumer<Boolean> set) {
        return Option.<Boolean>createBuilder().name(Component.literal(n)).binding(d,get,set).controller(TickBoxControllerBuilder::create).build();
    }
    private static Option<Float> decimal(String n,float d,float min,float max,float step,Supplier<Float> get,Consumer<Float> set) {
        return Option.<Float>createBuilder().name(Component.literal(n)).binding(d,get,set)
                .controller(o->FloatSliderControllerBuilder.create(o).range(min,max).step(step)).build();
    }
    private static Option<Integer> integer(String n,int d,int max,Supplier<Integer> get,Consumer<Integer> set) {
        return Option.<Integer>createBuilder().name(Component.literal(n)).binding(d,get,set)
                .controller(o->IntegerSliderControllerBuilder.create(o).range(0,max).step(1)).build();
    }
    private static <E extends Enum<E>> Option<E> enumeration(String n,Class<E> type,E d,Supplier<E> get,Consumer<E> set) {
        return Option.<E>createBuilder().name(Component.literal(n)).binding(d,get,set)
                .controller(o->EnumControllerBuilder.create(o).enumClass(type)).build();
    }
    private static Option<String> string(String n,String d,Supplier<String> get,Consumer<String> set) {
        return Option.<String>createBuilder().name(Component.literal(n)).binding(d,get,set).controller(StringControllerBuilder::create).build();
    }
    private static Option<Color> color(String n,int d,Supplier<Integer> get,Consumer<Integer> set) {
        return Option.<Color>createBuilder().name(Component.literal(n)).binding(new Color(d,true),()->new Color(get.get(),true),v->set.accept(v.getRGB()))
                .controller(o->ColorControllerBuilder.create(o).allowAlpha(true)).build();
    }
}
