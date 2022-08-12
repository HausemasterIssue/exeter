package me.friendly.exeter.module.impl.miscellaneous;

import me.friendly.api.event.Listener;
import me.friendly.exeter.events.PacketEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

public class NoRotate extends ToggleableModule {
    public NoRotate() {
        super("No Rotate", new String[]{"norotate"}, ModuleType.MISCELLANEOUS);

        listeners.add(new Listener<PacketEvent>("norotate_packet_listener") {
            @Override
            public void call(PacketEvent event) {
                if (event.getPacket() instanceof SPacketPlayerPosLook) {
                    SPacketPlayerPosLook packet = event.getPacket();

                    packet.yaw = mc.player.rotationYaw;
                    packet.pitch = mc.player.rotationPitch;
                }
            }
        });
    }
}
