package me.friendly.exeter.module.impl.world;

import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.api.properties.NumberProperty;
import me.friendly.api.properties.Property;

public class AutoMine extends ToggleableModule {
    private final NumberProperty<Double> range = new NumberProperty<>(4.5, 1.0, 6.0, "Range", "breakrange");
    private final Property<Boolean> rotate = new Property<>(true, "Rotate", "rot", "face");
    private final Property<Boolean> strict = new Property<>(true, "Strict", "ncpstrict", "swapstrict");

    public AutoMine() {
        super("Auto Mine", new String[]{"automine", "minebot"}, ModuleType.WORLD);
        offerProperties(range, rotate, strict);


    }
}
