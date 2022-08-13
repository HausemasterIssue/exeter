package me.friendly.exeter.module.impl.world;

import me.friendly.api.event.Listener;
import me.friendly.exeter.events.TickEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.api.properties.NumberProperty;

public class GameSpeed extends ToggleableModule {
    private final NumberProperty<Float> speed = new NumberProperty<>(1.5f, 0.1f, 20.0f, "Speed", "timerspeed");

    public GameSpeed() {
        super("Game Speed", new String[]{"gamespeed", "timer"}, ModuleType.WORLD);
        offerProperties(speed);

        listeners.add(new Listener<TickEvent>("gamespeed_tick_listener") {
            @Override
            public void call(TickEvent event) {
                if (mc.timer != null) {
                    if (mc.player != null && mc.world != null) {
                        mc.timer.tickLength = 50.0f / speed.getValue();
                    } else {
                        mc.timer.tickLength = 50.0f;
                    }
                }
            }
        });
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        mc.timer.tickLength = 50.0f;
    }
}
