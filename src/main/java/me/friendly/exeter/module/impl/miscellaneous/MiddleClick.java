package me.friendly.exeter.module.impl.miscellaneous;

import me.friendly.api.event.Event;
import me.friendly.api.event.Listener;
import me.friendly.api.properties.Property;
import me.friendly.exeter.core.Exeter;
import me.friendly.exeter.events.TickEvent;
import me.friendly.exeter.friend.Friend;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Mouse;

public class MiddleClick extends ToggleableModule {
    private final Property<Boolean> friend = new Property<>(true, "Friend", "fren", "addfriend", "removefriend");
    private final Property<Boolean> pearl = new Property<>(true, "Pearl", "enderpearl", "keypearl");

    public MiddleClick() {
        super("Middle Click", new String[]{"middleclick", "midclick", "mcp", "mcf"}, ModuleType.MISCELLANEOUS);
        offerProperties(friend, pearl);

        listeners.add(new Listener<TickEvent>("middleclick_tick_listener") {
            @Override
            public void call(TickEvent event) {
                if (Mouse.isButtonDown(2) && !Mouse.getEventButtonState()) {
                    RayTraceResult result = mc.objectMouseOver;
                    if (result != null && !result.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {
                        if (result.typeOfHit.equals(RayTraceResult.Type.ENTITY)) {
                            if (friend.getValue()) {

                                Entity entity = result.entityHit;
                                if (entity instanceof EntityPlayer) {

                                    Friend f = Exeter.getInstance()
                                            .getFriendManager()
                                            .getFriendByAliasOrLabel(entity.getName());

                                    if (f != null) {
                                        Exeter.getInstance().getFriendManager().unregister(f);
                                    } else {
                                        Exeter.getInstance().getFriendManager().unregister(new Friend(entity.getName(), entity.getName()));
                                    }
                                }
                            }
                        } else if (result.typeOfHit.equals(RayTraceResult.Type.MISS)) {
                            if (pearl.getValue()) {

                                int slot = -1;
                                EnumHand hand = EnumHand.MAIN_HAND;

                                if (mc.player.getHeldItemOffhand().getItem().equals(Items.ENDER_PEARL)) {
                                    slot = 45;
                                    hand = EnumHand.OFF_HAND;
                                } else {

                                    for (int i = 0; i < 9; ++i) {
                                        ItemStack stack = mc.player.inventory.getStackInSlot(i);
                                        if (stack.getItem().equals(Items.ENDER_PEARL)) {
                                            slot = i;
                                            break;
                                        }
                                    }
                                }

                                if (slot == -1) {
                                    return;
                                }

                                // todo: check for pearl cooldown instead of automatically swapping

                                if (Exeter.getInstance().getInventoryManager().getSlot() != slot) {
                                    mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
                                }

                                mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(hand));
                                mc.player.connection.sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                            }
                        }
                    }
                }
            }
        });
    }
}
