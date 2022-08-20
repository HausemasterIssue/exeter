package me.friendly.exeter.module.impl.combat;

import com.mojang.authlib.GameProfile;
import io.netty.util.internal.ConcurrentSet;
import me.friendly.api.event.Event;
import me.friendly.api.event.Listener;
import me.friendly.api.io.logging.Logger;
import me.friendly.api.properties.EnumProperty;
import me.friendly.api.properties.Property;
import me.friendly.exeter.events.PacketEvent;
import me.friendly.exeter.events.TickEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketSpawnPlayer;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AntiBot extends ToggleableModule {
    public static final Set<Integer> BOTS = new ConcurrentSet<>();

    private final EnumProperty<Mode> mode = new EnumProperty<>(Mode.NPC, "Mode", "m");
    private final Property<Boolean> remove = new Property<>(false, "Remove", "delete");
    private final Property<Boolean> debug = new Property<>(false, "Debug");

    private long lastAttack = 0L;

    public AntiBot() {
        super("Anti Bot", new String[]{"antibot", "antibots"}, ModuleType.COMBAT);
        offerProperties(mode, remove, debug);

        listeners.add(new Listener<TickEvent>("antibot_tick_listener") {
            @Override
            public void call(TickEvent event) {

                setTag(mode.getFixedValue() + ", " + BOTS.size());

                for (EntityPlayer player : mc.world.playerEntities) {

                    if (mode.getValue().equals(Mode.NPC)) {
                        if (!isOnTab(player)) {
                            BOTS.add(player.getEntityId());
                        }

                        String displayName = player.getDisplayName().getUnformattedText();
                        if (displayName.contains("NPC") || displayName.equals("CIT-")) {
                            BOTS.add(player.getEntityId());
                        }
                    } else if (mode.getValue().equals(Mode.MATRIX)) {
                        List<EntityPlayer> players = mc.world.playerEntities
                                .stream()
                                .filter((p) -> isOnTab(p) && p.getGameProfile().getName().equals(player.getGameProfile().getName()))
                                .collect(Collectors.toList());

                        if (players.size() > 1) {
                            int id = players.get(1).getEntityId();
                            if (!BOTS.contains(id)) {
                                if (debug.getValue()) {
                                    Logger.getLogger().printToChat("[&cAntiBot&7] - Removed duplicate entity");
                                }

                                BOTS.add(id);
                            }
                        }
                    }
                }

                if (remove.getValue()) {
                    BOTS.forEach((id) -> {

                        try {
                            Entity entity = mc.world.getEntityByID(id);
                            if (entity != null) {
                                if (debug.getValue()) {
                                    Logger.getLogger().printToChat("[&cAntiBot&7] - Removed entity from world (&c" + id + "&7)");
                                }

                                mc.world.removeEntity(entity);
                                mc.world.removeEntityDangerously(entity);
                            }
                        } catch (Exception ignored) { // most likely concurrent

                        }
                    });
                }
            }
        });

        listeners.add(new Listener<PacketEvent>("antibot_packet_listener") {
            @Override
            public void call(PacketEvent event) {
                if (event.getPacket() instanceof CPacketUseEntity) {
                    CPacketUseEntity packet = event.getPacket();
                    if (packet.getAction().equals(CPacketUseEntity.Action.ATTACK)) {
                        if (mode.getValue().equals(Mode.ON_ATTACK) || mode.getValue().equals(Mode.MATRIX)) {
                            lastAttack = System.currentTimeMillis();
                        }
                    }
                } else if (event.getPacket() instanceof SPacketSpawnPlayer) {
                    SPacketSpawnPlayer packet = event.getPacket();

                    if (mode.getValue().equals(Mode.ON_ATTACK) || mode.getValue().equals(Mode.MATRIX)) {

                        if (System.currentTimeMillis() - lastAttack <= 3000L) {
                            double x = packet.getX() / 32.0;
                            double y = packet.getY() / 32.0;
                            double z = packet.getZ() / 32.0;

                            double distance = mc.player.getDistanceSq(x, y, z);
                            if (distance <= 5.0 * 5.0) {
                                int id = packet.getEntityID();
                                if (!BOTS.contains(id)) {
                                    if (debug.getValue()) {
                                        Logger.getLogger().printToChat("[&cAntiBot&7] - Removed entity recently spawned (&c" + (System.currentTimeMillis() - lastAttack) + "ms&7)");
                                    }

                                    BOTS.add(id);
                                }

                            }
                        }
                    }
                }
            }
        });
    }

    private boolean isOnTab(EntityPlayer player) {
        for (NetworkPlayerInfo info : mc.player.connection.getPlayerInfoMap()) {
            GameProfile profile = info.getGameProfile();
            if (profile.equals(player.getGameProfile())) {
                return true;
            }
        }

        return false;
    }

    public enum Mode {
        NPC, MATRIX, ON_ATTACK
    }
}
