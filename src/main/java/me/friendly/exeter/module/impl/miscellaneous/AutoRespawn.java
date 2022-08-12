package me.friendly.exeter.module.impl.miscellaneous;

import me.friendly.api.event.Listener;
import me.friendly.exeter.events.TickEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;

public class AutoRespawn extends ToggleableModule {
    public AutoRespawn() {
        super("Auto Respawn", new String[]{"autorespawn", "respawn", "fastrespawn", "nodeathscreen"}, ModuleType.MISCELLANEOUS);

        listeners.add(new Listener<TickEvent>("autorespawn_tick_listener") {
            @Override
            public void call(TickEvent event) {
                if (mc.player.isDead || mc.player.getHealth() <= 0.0f) {
                    mc.player.respawnPlayer();
                }
            }
        });
    }
}
