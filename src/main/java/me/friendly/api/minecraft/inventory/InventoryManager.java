package me.friendly.api.minecraft.inventory;

import me.friendly.api.event.Listener;
import me.friendly.exeter.core.Exeter;
import me.friendly.exeter.events.PacketEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketHeldItemChange;

public class InventoryManager {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public int slot = -1;

    public InventoryManager() {
        Exeter.getInstance().getEventManager().register(new Listener<PacketEvent>("invmanager_packet_listener") {
            @Override
            public void call(PacketEvent event) {
                if (event.getPacket() instanceof CPacketHeldItemChange) {
                    slot = ((CPacketHeldItemChange) event.getPacket()).slotId;
                } else if (event.getPacket() instanceof SPacketHeldItemChange) {
                    slot = ((SPacketHeldItemChange) event.getPacket()).getHeldItemHotbarIndex();
                }
            }
        });
    }

    public void sync() {
        if (mc.player.inventory.currentItem != slot) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
        }
    }

    public ItemStack getStack() {
        if (slot == -1) {
            slot = mc.player.inventory.currentItem;
            return mc.player.getHeldItemMainhand();
        }

        return mc.player.inventory.getStackInSlot(slot);
    }

    public int getSlot() {
        return slot;
    }
}
