package me.friendly.exeter.module.impl.combat;

import me.friendly.api.event.Listener;
import me.friendly.api.stopwatch.Stopwatch;
import me.friendly.exeter.events.PacketEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.api.properties.NumberProperty;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketUseEntity.Action;

public class WTap extends ToggleableModule {
    private final NumberProperty<Integer> delay = new NumberProperty<>(1000, 0, 5000, "Delay", "d", "time", "waittime");

    private final Stopwatch stopwatch = new Stopwatch();

    public WTap() {
        super("W Tap", new String[]{"wtap", "sprinttap"}, ModuleType.COMBAT);
        offerProperties(delay);

        listeners.add(new Listener<PacketEvent>("wtap_packet_sent_listener") {
            @Override
            public void call(PacketEvent event) {
                if (event.getPacket() instanceof CPacketUseEntity) {
                    CPacketUseEntity packet = event.getPacket();
                    if (packet.getAction().equals(Action.ATTACK) && packet.getEntityFromWorld(mc.world) instanceof EntityLivingBase) {

                        if (stopwatch.hasCompleted(delay.getValue())) {
                            stopwatch.reset();

                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                        }
                    }
                }
            }
        });
    }
}
