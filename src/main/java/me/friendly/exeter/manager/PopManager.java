package me.friendly.exeter.manager;

import me.friendly.api.event.Event;
import me.friendly.api.event.Listener;
import me.friendly.exeter.core.Exeter;
import me.friendly.exeter.events.DeathEvent;
import me.friendly.exeter.events.PacketEvent;
import me.friendly.exeter.events.TotemPopEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PopManager {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final Map<Integer, Integer> pops = new ConcurrentHashMap<>();

    public PopManager() {
        Exeter.getInstance().getEventManager().register(new Listener<PacketEvent>("popmanager_packet_listener") {
            @Override
            public void call(PacketEvent event) {
                if (event.getPacket() instanceof SPacketEntityStatus) {
                    SPacketEntityStatus packet = event.getPacket();
                    if (packet.getOpCode() == 35) {
                        Entity entity = packet.getEntity(mc.world);
                        if (entity instanceof EntityPlayer) {
                            int count = pops.getOrDefault(entity.getEntityId(), 0) + 1;
                            Exeter.getInstance().getEventManager().dispatch(
                                    new TotemPopEvent((EntityPlayer) entity, count));
                            pops.put(entity.getEntityId(), count);
                        }
                    }
                }
            }
        });

        Exeter.getInstance().getEventManager().register(new Listener<DeathEvent>("popmanager_death_listener") {
            @Override
            public void call(DeathEvent event) {
                int count = getPops(event.getPlayer().getEntityId());
                if (count > 0) {
                    pops.put(event.getPlayer().getEntityId(), 0);
                }
            }
        });
    }

    public int getPops(int entityId) {
        return pops.getOrDefault(entityId, -1);
    }

    public Map<Integer, Integer> getPops() {
        return pops;
    }
}
