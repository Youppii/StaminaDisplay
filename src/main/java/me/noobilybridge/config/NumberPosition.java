package me.noobilybridge.config;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.text.Text;

public enum NumberPosition implements NameableEnum {
        AFTER_NAME,
        BAR_CENTER,
        FOLLOW_BAR,
    ;

    @Override
    public Text getDisplayName() {
        return switch (this){
            case AFTER_NAME -> Text.of("After Name");
            case BAR_CENTER -> Text.of("Bar Center");
            case FOLLOW_BAR -> Text.of("Follow Bar");
        };
    }
}
