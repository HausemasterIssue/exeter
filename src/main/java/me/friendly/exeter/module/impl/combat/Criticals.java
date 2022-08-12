package me.friendly.exeter.module.impl.combat;

import me.friendly.api.event.Listener;
import me.friendly.exeter.events.PacketEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.exeter.properties.EnumProperty;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketUseEntity.Action;

public class Criticals extends ToggleableModule {
    private final EnumProperty<Mode> mode = new EnumProperty<>(Mode.PACKET, "Mode", "m");

    public Criticals() {
        super("Criticals", new String[]{"criticals", "crits", "crit", "damageboost"}, ModuleType.COMBAT);
        offerProperties(mode);

        listeners.add(new Listener<PacketEvent>("criticals_packet_send_event") {
            @Override
            public void call(PacketEvent event) {
                if (event.getPacket() instanceof CPacketUseEntity) {
                    CPacketUseEntity packet = event.getPacket();
                    if (packet.getAction().equals(Action.ATTACK) && packet.getEntityFromWorld(mc.world) instanceof EntityLivingBase && mc.player.onGround) {
                        switch (mode.getValue()) {
                            case PACKET: {
                                mc.player.connection.sendPacket(new Position(
                                        mc.player.posX, mc.player.posY + 0.0625, mc.player.posZ, false));
                                mc.player.connection.sendPacket(new Position(
                                        mc.player.posX, mc.player.posY, mc.player.posZ, false));
                                break;
                            }

                            case NCP: {
                                mc.player.connection.sendPacket(new Position(
                                        mc.player.posX, mc.player.posY + 0.11, mc.player.posZ, false));
                                mc.player.connection.sendPacket(new Position(
                                        mc.player.posX, mc.player.posY, mc.player.posZ, false));
                                break;
                            }

                            case STRICT_NCP: {
                                mc.player.connection.sendPacket(new Position(
                                        mc.player.posX, mc.player.posY + 0.11, mc.player.posZ, false));
                                mc.player.connection.sendPacket(new Position(
                                        mc.player.posX, mc.player.posY, mc.player.posZ + 0.1100013579, false));
                                mc.player.connection.sendPacket(new Position(
                                        mc.player.posX, mc.player.posY + 0.0000013579, mc.player.posZ, false));
                                break;
                            }

                            case MOTION: {
                                mc.player.motionY = 0.11;
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    public enum Mode {
        PACKET, NCP, STRICT_NCP, MOTION
    }
}
