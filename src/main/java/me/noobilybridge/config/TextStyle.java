package me.noobilybridge.config;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.text.Text;

public enum TextStyle implements NameableEnum {
    NO_STYLE,
    SHADOW,
    OUTLINE;

    @Override
    public Text getDisplayName() {
        return switch (this){
            case NO_STYLE -> Text.of("None");
            case SHADOW -> Text.of("Shadow");
            case OUTLINE -> Text.of("Outline");
        };
    }
}
