package me.friendly.exeter.module.impl.world;

import me.friendly.api.event.Listener;
import me.friendly.api.minecraft.helper.RaycastHelper;
import me.friendly.api.minecraft.helper.RotationHelper;
import me.friendly.api.minecraft.helper.WorldHelper;
import me.friendly.api.properties.EnumProperty;
import me.friendly.api.properties.Property;
import me.friendly.api.stopwatch.Stopwatch;
import me.friendly.exeter.core.Exeter;
import me.friendly.exeter.events.MoveUpdateEvent;
import me.friendly.exeter.events.MoveUpdateEvent.Era;
import me.friendly.exeter.events.SafeWalkEvent;
import me.friendly.exeter.events.TickEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class Scaffold extends ToggleableModule {
    private final EnumProperty<Era> era = new EnumProperty<>(Era.POST, "Era", "placetiming", "timing");
    private final Property<Boolean> swing = new Property<>(true, "Swing", "swingarm");
    private final Property<Boolean> rotate = new Property<>(true, "Rotate", "rot", "face");
    private final EnumProperty<Raycast> raycast = new EnumProperty<>(Raycast.NONE, "Raycast", "raytrace", "legit");
    private final EnumProperty<Swap> swap = new EnumProperty<>(Swap.NORMAL, "Swap", "swapping", "blockhold");
    private final EnumProperty<BlockPicker> picker = new EnumProperty<>(BlockPicker.SEQUENTIAL, "Picker", "blockpicker", "pk");
    private final Property<Boolean> tower = new Property<>(true, "Tower", "fastup");
    private final Property<Boolean> eagle = new Property<>(false, "Eagle", "sneak");

    private final Stopwatch stopwatch = new Stopwatch();
    private PlaceInfo previous, current;

    private boolean eagling = false;

    public Scaffold() {
        super("Scaffold", new String[]{"scaffold", "blockfly", "bridge", "autobridge"}, ModuleType.WORLD);
        offerProperties(swing, rotate, raycast, swap, picker, tower, eagle);

        listeners.add(new Listener<TickEvent>("scaffold_tick_listener") {
            @Override
            public void call(TickEvent event) {
                if (eagle.getValue()) {
                    eagling = current == null || mc.world.isAirBlock(current.pos);
                    mc.gameSettings.keyBindSneak.pressed = eagling;
                } else {
                    if (eagling) {
                        mc.gameSettings.keyBindSneak.pressed = false;
                        eagling = false;
                    }
                }
            }
        });

        listeners.add(new Listener<MoveUpdateEvent>("scaffold_moveupdate_listener") {
            @Override
            public void call(MoveUpdateEvent event) {
                if (current != null) {
                    previous = current;
                }

                if (previous != null && rotate.getValue()) {
                    float[] angles = RotationHelper.calcAngleTo(previous.pos, previous.facing);
                    Exeter.getInstance().getRotationManager().setRotation(angles);
                }

                current = calc();
                if (current == null) {
                    return;
                }

                int slot = calcSlot();
                int oldSlot = mc.player.inventory.currentItem;

                if (slot == -1) {
                    return;
                } else {
                    if (swap.getValue().equals(Swap.NORMAL)) {
                        mc.player.inventory.currentItem = slot;
                    } else if (swap.getValue().equals(Swap.SPOOF)) {
                        if (Exeter.getInstance().getInventoryManager().slot != slot) {
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
                        }
                    }
                }

                if (event.era.equals(era.getValue())) {

                    boolean result;

                    switch (raycast.getValue()) {
                        case FULL: {
                            result = WorldHelper.placeAtRaycast(EnumHand.MAIN_HAND, swing.getValue());
                            break;
                        }

                        default:
                        case SEMI:
                        case NONE: {

                            if (raycast.getValue().equals(Raycast.NONE)) {
                                result = WorldHelper.placeAt(current.pos, current.facing, EnumHand.MAIN_HAND, swing.getValue());
                            } else if (RaycastHelper.canSeeBlockFace(current.pos, current.facing)) {
                                result = WorldHelper.placeAtRaycast(EnumHand.MAIN_HAND, swing.getValue());
                            } else {
                                result = false;
                            }
                            break;
                        }
                    }

                    if (result) {
                        if (mc.gameSettings.keyBindJump.isKeyDown() && tower.getValue()) {
                            mc.player.motionX *= 0.3;
                            mc.player.motionZ *= 0.3;
                            mc.player.jump();

                            if (stopwatch.hasCompleted(1200L)) {
                                stopwatch.reset();
                                mc.player.motionY = -0.28;
                            }
                        }

                        if (swap.getValue().equals(Swap.NORMAL)) {
                            mc.player.inventory.currentItem = oldSlot;
                        }
                    }
                }
            }
        });

        listeners.add(new Listener<SafeWalkEvent>("scaffold_safewalk_listener") {
            @Override
            public void call(SafeWalkEvent event) {
                event.setCanceled(true);
            }
        });
    }

    @Override
    protected void onDisable() {
        super.onDisable();

        if (eagling) {
            mc.gameSettings.keyBindSneak.pressed = false;
            eagling = false;
        }

        current = null;
        previous = null;

        Exeter.getInstance().getInventoryManager().sync();
    }

    private int calcSlot() {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!(stack.getItem() instanceof ItemBlock)) {
                continue;
            }

            if (slot == -1) {
                slot = i;
            } else {
                if (picker.getValue().equals(BlockPicker.HIGHEST) &&
                        mc.player.inventory.getStackInSlot(slot).stackSize < stack.stackSize) {
                    slot = i;
                }
            }
        }

        return slot;
    }

    private PlaceInfo calc() {
        BlockPos below = new BlockPos(mc.player.getPositionVector()).down();
        if (!isReplaceable(below)) {
            return null;
        }

        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos n = below.offset(facing);
            if (!isReplaceable(n)) {
                return new PlaceInfo(n, facing.getOpposite());
            }
        }

        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos n = below.offset(facing);
            if (isReplaceable(n)) {
                for (EnumFacing dir : EnumFacing.values()) {
                    BlockPos v = n.offset(dir);
                    if (!isReplaceable(v)) {
                        return new PlaceInfo(v, dir.getOpposite());
                    }
                }
            }
        }

        return null;
    }

    private boolean isReplaceable(BlockPos pos) {
        return mc.world.getBlockState(pos).getMaterial().isReplaceable();
    }

    public enum Swap {
        NORMAL, SPOOF
    }

    public enum BlockPicker {
        HIGHEST, SEQUENTIAL
    }

    public enum Raycast {
        NONE, SEMI, FULL
    }

    private static class PlaceInfo {
        private final BlockPos pos;
        private final EnumFacing facing;

        public PlaceInfo(BlockPos pos, EnumFacing facing) {
            this.pos = pos;
            this.facing = facing;
        }
    }
}
