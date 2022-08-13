package me.friendly.exeter.module.impl.render;

import me.friendly.api.properties.EnumProperty;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;

// weird behavior in mode snow where it'll snow and rain?
public class NoWeather extends ToggleableModule {
    public static NoWeather INSTANCE;

    public final EnumProperty<Mode> mode = new EnumProperty<>(Mode.CLEAR, "Mode", "m");

    public NoWeather() {
        super("No Weather", new String[]{"noweather", "antiweather"}, ModuleType.RENDER);
        offerProperties(mode);

        INSTANCE = this;
    }

    public enum Mode {
        CLEAR, RAIN, THUNDER, SNOW
    }
}
