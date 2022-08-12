package me.friendly.exeter.module.impl.combat;

import me.friendly.api.event.Listener;
import me.friendly.exeter.events.TickEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.exeter.properties.NumberProperty;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class BowRelease extends ToggleableModule {
    private final NumberProperty<Integer> ticks = new NumberProperty<>(3, 3, 20, "Ticks", "time");

    public BowRelease() {
        super("Bow Release", new String[]{"bowrelease", "autorelease", "fastbow"}, ModuleType.COMBAT);
        offerProperties(ticks);

        listeners.add(new Listener<TickEvent>("bowrelease_tick_listener") {
            @Override
            public void call(TickEvent event) {
                if (mc.player.getActiveItemStack().getItem() instanceof ItemBow && mc.player.getItemInUseMaxCount() >= ticks.getValue()) {
                    mc.player.stopActiveHand();
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                }
            }
        });
    }
}
