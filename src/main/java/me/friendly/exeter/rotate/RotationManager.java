package me.friendly.exeter.rotate;

import me.friendly.api.event.Event;
import me.friendly.api.event.Listener;
import me.friendly.exeter.core.Exeter;
import me.friendly.exeter.events.MoveUpdateEvent;
import me.friendly.exeter.events.PacketEvent;
import me.friendly.exeter.events.RenderRotationsEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;

public class RotationManager {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private final float[] serverRotation = {0.0f, 0.0f};
    private float[] clientRotation;

    private long rotateTime = 0L;

    public RotationManager() {
        Exeter.getInstance().getEventManager().register(new Listener<MoveUpdateEvent>("rotationmanager_moveupdate_listener") {
            @Override
            public void call(MoveUpdateEvent event) {
                if (System.currentTimeMillis() - rotateTime >= 300L) {
                    nullifyRotations();
                } else {
                    if (clientRotation != null) {
                        event.yaw = clientRotation[0];
                        event.pitch = clientRotation[1];
                    }
                }
            }
        });

        Exeter.getInstance().getEventManager().register(new Listener<RenderRotationsEvent>("rotationmanager_renderrots_listener") {
            @Override
            public void call(RenderRotationsEvent event) {
                if (clientRotation != null) {
                    mc.player.rotationYawHead = serverRotation[0];
                    mc.player.renderYawOffset = serverRotation[0];

                    //event.yaw = serverRotation[0];
                    event.pitch = serverRotation[1];

                    event.setCanceled(true);
                }
            }
        });

        Exeter.getInstance().getEventManager().register(new Listener<PacketEvent>("rotationmanager_packet_listener") {
            @Override
            public void call(PacketEvent event) {
                if (event.getPacket() instanceof CPacketPlayer) {
                    CPacketPlayer packet = event.getPacket();
                    if (packet.rotating) {
                        serverRotation[0] = packet.yaw;
                        serverRotation[1] = packet.pitch;
                    }
                } else if (event.getPacket() instanceof SPacketPlayerPosLook) {
                    SPacketPlayerPosLook packet = event.getPacket();

                    serverRotation[0] = packet.yaw;
                    serverRotation[1] = packet.pitch;
                }
            }
        });
    }

    public void setRotation(float yaw, float pitch) {
        rotateTime = System.currentTimeMillis();
        clientRotation = new float[]{ yaw, pitch };
    }

    public void setRotation(float[] angles) {
        setRotation(angles[0], angles[1]);
    }

    public void nullifyRotations() {
        clientRotation = null;
        rotateTime = 0L;
    }

    public float[] getClientRotation() {
        return clientRotation;
    }

    public float[] getServerRotation() {
        return serverRotation;
    }
}
