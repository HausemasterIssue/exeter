package me.friendly.exeter.module.impl.movement;

import me.friendly.api.event.Listener;
import me.friendly.exeter.events.MoveUpdateEvent;
import me.friendly.exeter.events.MoveUpdateEvent.Era;
import me.friendly.exeter.events.SlowDownEvent;
import me.friendly.exeter.events.TickEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.api.properties.EnumProperty;
import net.minecraft.item.ItemShield;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class NoSlow extends ToggleableModule {
    private final EnumProperty<Mode> mode = new EnumProperty<>(Mode.VANILLA, "Mode", "m");

    public NoSlow() {
        super("No Slow", new String[]{"noslow", "noslowdown", "antislow"}, ModuleType.MOVEMENT);
        offerProperties(mode);

        listeners.add(new Listener<SlowDownEvent>("noslow_slowdown_listener") {
            @Override
            public void call(SlowDownEvent event) {
                if (!mc.player.isRiding() && mc.player.isHandActive()) {
                    event.input.moveForward *= 5.0f;
                    event.input.moveStrafe *= 5.0f;
                }
            }
        });

        listeners.add(new Listener<TickEvent>("noslow_tick_listener") {
            @Override
            public void call(TickEvent event) {
                if (!mc.player.isRiding() && mc.player.isHandActive()) {

                    if (mode.getValue().equals(Mode.STRICT_NCP)) {
                        mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                    }
                }
            }
        });

        listeners.add(new Listener<MoveUpdateEvent>("noslow_moveupdate_listener") {
            @Override
            public void call(MoveUpdateEvent event) {
                if (!mc.player.isRiding() && mc.player.isHandActive()) {
                    if (mode.getValue().equals(Mode.NCP) || mode.getValue().equals(Mode.STRICT_NCP)) {
                        if (mc.player.getActiveItemStack().getItem() instanceof ItemShield) {
                            if (event.era.equals(Era.PRE)) {
                                mc.player.connection.sendPacket(
                                        new CPacketPlayerDigging(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                            } else {
                                mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(mc.player.getActiveHand()));
                            }
                        }
                    }
                }
            }
        });
    }

    public enum Mode {
        VANILLA, NCP, STRICT_NCP
    }
}
