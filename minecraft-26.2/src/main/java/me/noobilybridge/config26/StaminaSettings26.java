package me.noobilybridge.config26;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.isxander.yacl3.api.NameableEnum;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class StaminaSettings26 {
    private static final Gson GSON = new Gson();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("staminadisplay.json");

    public static int outlineColor = 0x40000000, secondaryOutlineColor = 0x80000000;
    public static boolean useTeamForMain = true;
    public static int mainColor = -1, secondaryMainColor = -1, hungerBarMainColor = 0xFFFF8000;
    public static boolean evilMainColor, iceTranslucencyDisable;
    public static int hungerBarSecondaryColor = 0xFFFF0080, emptyMainColor = 0xFFFF0000;
    public static float width = 75, height = 4, verticalOffset = 7.5F, animationSpeed = 15;
    public static boolean customHungerBar = true, renderBar = true, showNumber = true;
    public static int numberColor = -1;
    public static float numberScale = 0.5F, cornerRounding = 0.25F, paddingAmount = 0.75F;
    public static int minStamina;
    public static float outlineThickness = 0.25F;
    public enum TextStyle26 implements NameableEnum {
        NO_STYLE, SHADOW, OUTLINE;
        @Override public Component getDisplayName() {
            return Component.literal(switch (this) { case NO_STYLE -> "None"; case SHADOW -> "Shadow"; case OUTLINE -> "Outline"; });
        }
    }
    public enum NumberPosition26 implements NameableEnum {
        AFTER_NAME, BAR_CENTER, FOLLOW_BAR;
        @Override public Component getDisplayName() {
            return Component.literal(switch (this) { case AFTER_NAME -> "After Name"; case BAR_CENTER -> "Bar Center"; case FOLLOW_BAR -> "Follow Bar"; });
        }
    }
    public static TextStyle26 textStyle = TextStyle26.SHADOW;
    public static NumberPosition26 numberPosition = NumberPosition26.AFTER_NAME;
    public static int numberBGColor = 0xFF000000, strokeColor = 0x80004080, outerStrokeColor = 0x80000000;
    public static float hungerBarWidth = 79, hungerBarHeight = 5.5F, hungerBarVerticalOffset;
    public static boolean fitToName = true, drawNametagTeam = true, overrideHomeColor, overrideAwayColor;
    public static int homeTeamColorOverride = -1, secondaryHomeTeamColorOverride = -1;
    public static int awayTeamColorOverride = -1, secondaryAwayTeamColorOverride = -1;
    public static String innerText = " - ", outerText = "";
    public static boolean mainColorGradientDirection = true, hungerBarGradientDirection, outlineIsTheBar;
    public static float zAxisOffset = 0.1F;
    public static boolean nametagShadow = true, teamForNumber = true, teamForNumberBG = true;
    public static int emptyNumberColor = 0xFFFF0000, secondaryEmptyMainColor = 0xFFB20000;
    public static int nametagBGColor = 0x20000000;
    public static boolean teamForNametagBG = true;
    public static int nametagTextCol = -1;
    public static boolean teamForNametagText = true, backgroundDirection = true, useEmptyColor;
    public static boolean showNumberInBar = true;
    public static int barNumberColor = -1;
    public static float barNumberScale = 0.5F;
    public static boolean lowStaminaCue, teammatesOnly;

    private StaminaSettings26() {}

    public static void load() {
        if (!Files.exists(PATH)) { save(); return; }
        try { apply(JsonParser.parseString(Files.readString(PATH)).getAsJsonObject()); }
        catch (IOException | RuntimeException ignored) { }
    }

    public static boolean loadJson(String json) {
        JsonObject before = new JsonObject();
        put(before);
        try {
            JsonObject input = JsonParser.parseString(json).getAsJsonObject();
            apply(input);
            save();
            return true;
        } catch (RuntimeException ignored) {
            apply(before);
            return false;
        }
    }

    private static void apply(JsonObject j) {
        outlineColor = color(j, outlineColor, "outline_color", "outlineColor");
        secondaryOutlineColor = color(j, secondaryOutlineColor, "secondary_outline_color", "secondaryOutlineColor");
        useTeamForMain = bool(j, useTeamForMain, "use_team_for_main", "useTeamForMain");
        mainColor = color(j, mainColor, "main_color", "mainColor");
        evilMainColor = bool(j, evilMainColor, "evil_main_color", "evilMainColor");
        secondaryMainColor = color(j, secondaryMainColor, "secondary_main_color", "secondaryMainColor");
        hungerBarMainColor = color(j, hungerBarMainColor, "hunger_bar_main_color", "hungerBarMainColor");
        hungerBarSecondaryColor = color(j, hungerBarSecondaryColor, "hunger_bar_secondary_color", "hungerBarSecondaryColor");
        emptyMainColor = color(j, emptyMainColor, "empty_main_color", "emptyMainColor");
        width = number(j, width, "width"); height = number(j, height, "height");
        verticalOffset = number(j, verticalOffset, "vertical_offset", "verticalOffset");
        animationSpeed = number(j, animationSpeed, "animation_speed", "animationSpeed");
        iceTranslucencyDisable = bool(j, iceTranslucencyDisable, "ice_translucency_disable", "iceTranslucencyDisable");
        customHungerBar = bool(j, customHungerBar, "custom_hunger_bar", "customHungerBar");
        renderBar = bool(j, renderBar, "render_bar", "renderBar"); showNumber = bool(j, showNumber, "show_number", "showNumber");
        numberColor = color(j, numberColor, "number_color", "numberColor"); numberScale = number(j, numberScale, "number_scale", "numberScale");
        cornerRounding = number(j, cornerRounding, "corner_rounding", "cornerRounding");
        paddingAmount = number(j, paddingAmount, "padding_amount", "paddingAmount"); minStamina = integer(j, minStamina, "min_stamina", "minStamina");
        outlineThickness = number(j, outlineThickness, "outline_thickness", "outlineThickness");
        textStyle = enumValue(j, textStyle, "text_style", "textStyle"); numberBGColor = color(j, numberBGColor, "number_bg_color", "numberBGColor");
        strokeColor = color(j, strokeColor, "stroke_color", "strokeColor"); outerStrokeColor = color(j, outerStrokeColor, "outer_stroke_color", "outerStrokeColor");
        numberPosition = enumValue(j, numberPosition, "number_position", "numberPosition");
        hungerBarWidth = number(j, hungerBarWidth, "hunger_bar_width", "hungerBarWidth");
        hungerBarHeight = number(j, hungerBarHeight, "hunger_bar_height", "hungerBarHeight");
        hungerBarVerticalOffset = number(j, hungerBarVerticalOffset, "hunger_bar_vertical_offset", "hungerBarVerticalOffset");
        fitToName = bool(j, fitToName, "fit_to_name", "fitToName"); drawNametagTeam = bool(j, drawNametagTeam, "draw_nametag_team", "drawNametagTeam");
        overrideHomeColor = bool(j, overrideHomeColor, "override_home_color", "overrideHomeColor");
        homeTeamColorOverride = color(j, homeTeamColorOverride, "home_team_color_override", "homeTeamColorOverride");
        secondaryHomeTeamColorOverride = color(j, secondaryHomeTeamColorOverride, "secondary_home_team_color_override", "secondaryHomeTeamColorOverride");
        overrideAwayColor = bool(j, overrideAwayColor, "override_away_color", "overrideAwayColor");
        awayTeamColorOverride = color(j, awayTeamColorOverride, "away_team_color_override", "awayTeamColorOverride");
        secondaryAwayTeamColorOverride = color(j, secondaryAwayTeamColorOverride, "secondary_away_team_color_override", "secondaryAwayTeamColorOverride");
        innerText = string(j, innerText, "inner_text", "innerText"); outerText = string(j, outerText, "outer_text", "outerText");
        mainColorGradientDirection = bool(j, mainColorGradientDirection, "main_color_gradient_direction", "mainColorGradientDirection");
        hungerBarGradientDirection = bool(j, hungerBarGradientDirection, "hunger_bar_gradient_direction", "hungerBarGradientDirection");
        outlineIsTheBar = bool(j, outlineIsTheBar, "outline_is_the_bar", "outlineIsTheBar");
        zAxisOffset = number(j, zAxisOffset, "z_axis_offset", "zAxisOffset"); nametagShadow = bool(j, nametagShadow, "nametag_shadow", "nametagShadow");
        teamForNumber = bool(j, teamForNumber, "team_for_number", "teamForNumber");
        teamForNumberBG = bool(j, teamForNumberBG, "team_for_number_bg", "teamForNumberBG");
        emptyNumberColor = color(j, emptyNumberColor, "empty_number_color", "emptyNumberColor");
        secondaryEmptyMainColor = color(j, secondaryEmptyMainColor, "secondary_empty_main_color", "secondaryEmptyMainColor");
        nametagBGColor = color(j, nametagBGColor, "nametag_bg_color", "nametagBGColor");
        teamForNametagBG = bool(j, teamForNametagBG, "team_for_nametag_bg", "teamForNametagBG");
        nametagTextCol = color(j, nametagTextCol, "nametag_text_col", "nametagTextCol");
        teamForNametagText = bool(j, teamForNametagText, "team_for_nametag_text", "teamForNametagText");
        backgroundDirection = bool(j, backgroundDirection, "background_direction", "backgroundDirection");
        useEmptyColor = bool(j, useEmptyColor, "use_empty_color", "useEmptyColor");
        showNumberInBar = bool(j, showNumberInBar, "show_number_in_bar", "showNumberInBar");
        barNumberColor = color(j, barNumberColor, "bar_number_color", "barNumberColor");
        barNumberScale = number(j, barNumberScale, "bar_number_scale", "barNumberScale");
        lowStaminaCue = bool(j, lowStaminaCue, "low_stamina_cue", "lowStaminaCue");
        teammatesOnly = bool(j, teammatesOnly, "teammates_only", "teammatesOnly");
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            JsonObject j = Files.exists(PATH) ? JsonParser.parseString(Files.readString(PATH)).getAsJsonObject() : new JsonObject();
            put(j);
            Path temp = PATH.resolveSibling(PATH.getFileName() + ".tmp");
            Files.writeString(temp, GSON.toJson(j));
            try { Files.move(temp, PATH, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE); }
            catch (IOException e) { Files.move(temp, PATH, java.nio.file.StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException | RuntimeException ignored) { }
    }

    private static void put(JsonObject j) {
        j.addProperty("outline_color", outlineColor); j.addProperty("secondary_outline_color", secondaryOutlineColor);
        j.addProperty("use_team_for_main", useTeamForMain); j.addProperty("main_color", mainColor); j.addProperty("evil_main_color", evilMainColor);
        j.addProperty("secondary_main_color", secondaryMainColor); j.addProperty("hunger_bar_main_color", hungerBarMainColor);
        j.addProperty("hunger_bar_secondary_color", hungerBarSecondaryColor); j.addProperty("empty_main_color", emptyMainColor);
        j.addProperty("width", width); j.addProperty("height", height); j.addProperty("vertical_offset", verticalOffset); j.addProperty("animation_speed", animationSpeed);
        j.addProperty("ice_translucency_disable", iceTranslucencyDisable); j.addProperty("custom_hunger_bar", customHungerBar); j.addProperty("render_bar", renderBar);
        j.addProperty("show_number", showNumber); j.addProperty("number_color", numberColor); j.addProperty("number_scale", numberScale);
        j.addProperty("corner_rounding", cornerRounding); j.addProperty("padding_amount", paddingAmount); j.addProperty("min_stamina", minStamina);
        j.addProperty("outline_thickness", outlineThickness); j.addProperty("text_style", textStyle.name()); j.addProperty("number_bg_color", numberBGColor);
        j.addProperty("stroke_color", strokeColor); j.addProperty("outer_stroke_color", outerStrokeColor); j.addProperty("number_position", numberPosition.name());
        j.addProperty("hunger_bar_width", hungerBarWidth); j.addProperty("hunger_bar_height", hungerBarHeight); j.addProperty("hunger_bar_vertical_offset", hungerBarVerticalOffset);
        j.addProperty("fit_to_name", fitToName); j.addProperty("draw_nametag_team", drawNametagTeam); j.addProperty("override_home_color", overrideHomeColor);
        j.addProperty("home_team_color_override", homeTeamColorOverride); j.addProperty("secondary_home_team_color_override", secondaryHomeTeamColorOverride);
        j.addProperty("override_away_color", overrideAwayColor); j.addProperty("away_team_color_override", awayTeamColorOverride);
        j.addProperty("secondary_away_team_color_override", secondaryAwayTeamColorOverride); j.addProperty("inner_text", innerText); j.addProperty("outer_text", outerText);
        j.addProperty("main_color_gradient_direction", mainColorGradientDirection); j.addProperty("hunger_bar_gradient_direction", hungerBarGradientDirection);
        j.addProperty("outline_is_the_bar", outlineIsTheBar); j.addProperty("z_axis_offset", zAxisOffset); j.addProperty("nametag_shadow", nametagShadow);
        j.addProperty("team_for_number", teamForNumber); j.addProperty("team_for_number_bg", teamForNumberBG); j.addProperty("empty_number_color", emptyNumberColor);
        j.addProperty("secondary_empty_main_color", secondaryEmptyMainColor); j.addProperty("nametag_bg_color", nametagBGColor);
        j.addProperty("team_for_nametag_bg", teamForNametagBG); j.addProperty("nametag_text_col", nametagTextCol);
        j.addProperty("team_for_nametag_text", teamForNametagText); j.addProperty("background_direction", backgroundDirection);
        j.addProperty("use_empty_color", useEmptyColor); j.addProperty("show_number_in_bar", showNumberInBar); j.addProperty("bar_number_color", barNumberColor);
        j.addProperty("bar_number_scale", barNumberScale); j.addProperty("low_stamina_cue", lowStaminaCue); j.addProperty("teammates_only", teammatesOnly);
    }

    public static String exportJson() {
        save();
        try { return Files.readString(PATH); } catch (IOException ignored) { return "{}"; }
    }

    private static boolean bool(JsonObject j, boolean d, String... keys) { return primitive(j, keys) == null ? d : primitive(j, keys).getAsBoolean(); }
    private static int integer(JsonObject j, int d, String... keys) { return primitive(j, keys) == null ? d : primitive(j, keys).getAsInt(); }
    private static float number(JsonObject j, float d, String... keys) { return primitive(j, keys) == null ? d : primitive(j, keys).getAsFloat(); }
    private static String string(JsonObject j, String d, String... keys) { return primitive(j, keys) == null ? d : primitive(j, keys).getAsString(); }
    private static com.google.gson.JsonPrimitive primitive(JsonObject j, String... keys) {
        for (String key : keys) if (j.has(key) && j.get(key).isJsonPrimitive()) return j.getAsJsonPrimitive(key);
        return null;
    }
    private static int color(JsonObject j, int d, String... keys) {
        var p = primitive(j, keys); if (p == null) return d;
        try {
            String raw = p.getAsString();
            if (raw.startsWith("#")) { long value = Long.parseLong(raw.substring(1), 16); return raw.length() == 7 ? (int) (0xFF000000L | value) : (int) value; }
            if (raw.startsWith("0x") || raw.startsWith("0X")) return (int) Long.parseLong(raw.substring(2), 16);
            return p.getAsInt();
        } catch (RuntimeException ignored) { return d; }
    }
    private static <E extends Enum<E>> E enumValue(JsonObject j, E d, String... keys) {
        String value = string(j, d.name(), keys).toUpperCase(Locale.ROOT);
        try { return Enum.valueOf(d.getDeclaringClass(), value); } catch (IllegalArgumentException ignored) { return d; }
    }
}
