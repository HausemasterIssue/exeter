package me.friendly.exeter.module.impl.movement;

import me.friendly.api.event.Listener;
import me.friendly.exeter.events.PacketEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.exeter.properties.EnumProperty;
import me.friendly.exeter.properties.NumberProperty;
import me.friendly.exeter.properties.Property;
import net.minecraft.network.play.client.CPacketPlayer;

public class NoFall extends ToggleableModule {
    private final Property<Mode> mode = new EnumProperty<>(Mode.VANILLA, "Mode", "m");
    private final NumberProperty<Float> distance = new NumberProperty<>(3.0f, 3.0f, 30.0f, "Distance", "dist", "falldist");

    public NoFall() {
        super("No Fall", new String[]{"nofall", "nofalldamage", "antifalldamage"}, ModuleType.MOVEMENT);
        offerProperties(mode, distance);

        listeners.add(new Listener<PacketEvent>("nofall_packet_listener") {
            @Override
            public void call(PacketEvent event) {
                if (event.getPacket() instanceof CPacketPlayer) {
                    CPacketPlayer packet = event.getPacket();

                    if (mc.player.fallDistance > distance.getValue()) {
                        if (mode.getValue().equals(Mode.VANILLA)) {
                            packet.onGround = true;
                        } else if (mode.getValue().equals(Mode.RESET)) {
                            packet.onGround = true;
                            mc.player.fallDistance = 0.0f;
                        }
                    }
                }
            }
        });
    }

    public enum Mode {
        VANILLA, RESET
    }
}
