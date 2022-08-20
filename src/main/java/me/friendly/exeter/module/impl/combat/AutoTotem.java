package me.friendly.exeter.module.impl.combat;

import me.friendly.api.event.Listener;
import me.friendly.api.properties.EnumProperty;
import me.friendly.api.properties.NumberProperty;
import me.friendly.api.properties.Property;
import me.friendly.api.stopwatch.Stopwatch;
import me.friendly.exeter.events.TickEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AutoTotem extends ToggleableModule {
    private final EnumProperty<Mode> mode = new EnumProperty<>(Mode.CRYSTAL, "Mode", "m", "item", "type");
    private final NumberProperty<Float> health = new NumberProperty<>(14.0f, 1.0f, 18.0f, "Health", "minhealth");
    private final Property<Boolean> ncpStrict = new Property<>(false, "NCP Strict", "ncpstrict");
    private final NumberProperty<Integer> delay = new NumberProperty<>(1, 0, 8, "Delay", "dl");
    //private final Property<Boolean> hotbar = new Property<>(true, "Hotbar Swap", "hotbar");

    private final Queue<Integer> queue = new ConcurrentLinkedQueue<>();
    private final Stopwatch stopwatch = new Stopwatch();
    private Item item = Items.field_190929_cY;
    private boolean opened = false;

    public AutoTotem() {
        super("Auto Totem", new String[]{"autototem", "offhand", "autooffhand"}, ModuleType.COMBAT);
        offerProperties(mode, health, ncpStrict, delay);

        listeners.add(new Listener<TickEvent>("autototem_tick_listener") {
            @Override
            public void call(TickEvent event) {
                if (willDieFromFall() || health.getValue() > mc.player.getHealth()) {
                    item = Items.field_190929_cY;
                } else {

                    if (mc.player.getHeldItemMainhand().getItem() instanceof ItemSword && mc.gameSettings.keyBindUseItem.isKeyDown()) {
                        item = Items.GOLDEN_APPLE;
                    } else {
                        item = mode.getValue().item;
                    }
                }

                if (queue.isEmpty() && !mc.player.getHeldItemOffhand().getItem().equals(item)) {

                    //queue.clear();

                    int slot = -1;
                    for (int i = 0; i < 36; ++i) {
                        ItemStack stack = mc.player.inventory.getStackInSlot(i);
                        if (stack.getItem().equals(item)) {
                            slot = i;
                            break;
                        }
                    }

                    if (slot == -1) {
                        return;
                    }

                    // stopwatch.reset();

                    queue.add(slot < 9 ? slot + 36 : slot);
                    queue.add(45);
                    queue.add(slot < 9 ? slot + 36 : slot);
                } else {

                    int windowId = mc.player.openContainer.windowId;

                    if (stopwatch.hasCompleted(delay.getValue() * 50L)) {
                        Integer next = queue.poll();
                        if (next == null) {
                            if (opened) {
                                mc.player.connection.sendPacket(new CPacketCloseWindow(windowId));
                                opened = false;
                            }

                            return;
                        }

                        if (!opened && ncpStrict.getValue()) {
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.OPEN_INVENTORY));
                            opened = true;
                        }

                        if (ncpStrict.getValue() && mc.player.isHandActive()) {
                            mc.player.stopActiveHand();
                            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        }

                        mc.playerController.windowClick(windowId, next, 0, ClickType.PICKUP, mc.player);
                        stopwatch.reset();
                    }

                }
            }
        });
    }

    @Override
    protected void onDisable() {
        super.onDisable();

        queue.clear();
        item = Items.field_190929_cY;

        if (opened) {
            mc.player.connection.sendPacket(new CPacketCloseWindow(mc.player.openContainer.windowId));
            opened = false;
        }
    }

    private boolean willDieFromFall() {
        return ((3.0f - mc.player.fallDistance) / 2.0f) + 3.5f >= mc.player.getHealth();
    }

    public enum Mode {
        TOTEM(Items.field_190929_cY),
        CRYSTAL(Items.END_CRYSTAL),
        GAPPLE(Items.GOLDEN_APPLE);

        private final Item item;

        Mode(Item item) {
            this.item = item;
        }
    }
}
