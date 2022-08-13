package me.friendly.exeter.module.impl.movement;

import me.friendly.api.event.Listener;
import me.friendly.exeter.events.BlockPushEvent;
import me.friendly.exeter.events.PacketEvent;
import me.friendly.exeter.events.TickEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.api.properties.Property;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;

public class Velocity extends ToggleableModule {
    private final Property<Boolean> knockback = new Property<>(true, "Knockback", "kb");
    private final Property<Boolean> explosions = new Property<>(true, "Explosions", "ex");
    private final Property<Boolean> entities = new Property<>(true, "Entities", "things", "players", "e");
    private final Property<Boolean> blocks = new Property<>(true, "Blocks", "cubeshitters");

    private float entityReduction = 0.0f;

    public Velocity() {
        super("Velocity", new String[]{"velocity", "vel", "antikb", "antiknockback"}, ModuleType.MOVEMENT);
        offerProperties(knockback, explosions, entities, blocks);

        listeners.add(new Listener<PacketEvent>("velocity_packet_listener") {
            @Override
            public void call(PacketEvent event) {
                if (event.getPacket() instanceof SPacketEntityVelocity) {
                    SPacketEntityVelocity packet = event.getPacket();
                    if (packet.entityID == mc.player.getEntityId() && knockback.getValue()) {
                        packet.motionX = 0;
                        packet.motionY = 0;
                        packet.motionZ = 0;
                    }
                } else if (event.getPacket() instanceof SPacketExplosion) {
                    SPacketExplosion packet = event.getPacket();
                    if (explosions.getValue()) {
                        packet.motionX = 0.0f;
                        packet.motionY = 0.0f;
                        packet.motionZ = 0.0f;
                    }
                }
            }
        });

        listeners.add(new Listener<BlockPushEvent>("velocity_block_push_listener") {
            @Override
            public void call(BlockPushEvent event) {
                event.setCanceled(blocks.getValue());
            }
        });

        listeners.add(new Listener<TickEvent>("velocity_tick_listener") {
            @Override
            public void call(TickEvent event) {
                if (entities.getValue()) {
                    entityReduction = 0.0f;
                    mc.player.entityCollisionReduction = 1.0f;
                } else {
                    mc.player.entityCollisionReduction = entityReduction;
                }
            }
        });
    }
}
