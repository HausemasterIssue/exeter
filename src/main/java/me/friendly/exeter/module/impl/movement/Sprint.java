package me.friendly.exeter.module.impl.movement;

import me.friendly.api.event.Listener;
import me.friendly.exeter.events.TickEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.api.properties.EnumProperty;
import me.friendly.api.properties.Property;

public class Sprint extends ToggleableModule {
    private final EnumProperty<Mode> mode = new EnumProperty<>(Mode.LEGIT, "Mode", "m");

    public Sprint() {
        super("Sprint", new String[]{"keepsprint", "alwayssprint"}, ModuleType.MOVEMENT);
        offerProperties(mode);

        listeners.add(new Listener<TickEvent>("sprint_tick_listener") {
            @Override
            public void call(TickEvent event) {
                setTag(mode.getFixedValue());

                if (mc.player != null && mc.world != null && !mc.player.isSprinting()) {
                    if (mode.getValue().equals(Mode.LEGIT)) {
                        mc.player.setSprinting(!mc.player.isCollidedHorizontally &&
                                !mc.player.isSneaking() &&
                                !mc.player.isHandActive() &&
                                mc.player.getFoodStats().getFoodLevel() > 6 &&
                                mc.player.movementInput.moveForward > 0.0f);
                    } else {
                        mc.player.setSprinting(true);
                    }
                }
            }
        });
    }

    @Override
    protected void onDisable() {
        super.onDisable();

        if (!mc.gameSettings.keyBindSprint.isKeyDown()) {
            mc.player.setSprinting(false);
        }
    }

    public enum Mode {
        LEGIT, RAGE
    }
}
