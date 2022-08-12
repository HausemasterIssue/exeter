package me.friendly.exeter.module.impl.combat;

import me.friendly.api.event.Listener;
import me.friendly.exeter.events.TickEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.exeter.properties.Property;

public class SelfFill extends ToggleableModule {
    private final Property<Boolean> rotate = new Property<>(true, "Rotate", "rot", "face");
    private final Property<Boolean> strict = new Property<>(false, "Strict", "ncpstrict", "2b2t");

    public SelfFill() {
        super("Self Fill", new String[]{"selffill", "burrow", "blocklag"}, ModuleType.COMBAT);
        offerProperties(rotate, strict);

        listeners.add(new Listener<TickEvent>("selffill_tick_listener") {
            @Override
            public void call(TickEvent event) {

            }
        });
    }
}
