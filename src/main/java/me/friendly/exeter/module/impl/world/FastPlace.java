package me.friendly.exeter.module.impl.world;

import me.friendly.api.event.Listener;
import me.friendly.exeter.events.TickEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.api.properties.Property;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;

public class FastPlace extends ToggleableModule {
    private final Property<Boolean> ghostFix = new Property<>(true, "Ghost Fix", "ghostfix", "extrapacket", "noflag");

    public FastPlace() {
        super("Fast Place", new String[]{"fastplace", "fastuse", "quickuse"}, ModuleType.WORLD);
        offerProperties(ghostFix);

        listeners.add(new Listener<TickEvent>("fastplace_tick_listener") {
            @Override
            public void call(TickEvent event) {
                mc.rightClickDelayTimer = 0;

                // this is extremely fucked
//                if (ghostFix.getValue() && mc.gameSettings.keyBindUseItem.isKeyDown()) {
//                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(mc.player.getActiveHand()));
//                }
            }
        });
    }
}
