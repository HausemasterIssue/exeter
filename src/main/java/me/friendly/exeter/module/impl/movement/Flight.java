package me.friendly.exeter.module.impl.movement;

import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.exeter.properties.EnumProperty;

public class Flight extends ToggleableModule {
    private final EnumProperty<Mode> mode = new EnumProperty<>(Mode.VANILLA, "Mode", "m");

    public Flight() {
        super("Flight", new String[]{"flight", "fly", "airwalk"}, ModuleType.MOVEMENT);


    }

    public enum Mode {
        VANILLA, CREATIVE, VULCAN_GLIDE, VERUS_COLLIDE
    }
}
