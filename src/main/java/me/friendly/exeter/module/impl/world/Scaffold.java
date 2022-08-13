package me.friendly.exeter.module.impl.world;

import me.friendly.api.event.Listener;
import me.friendly.api.minecraft.helper.RotationHelper;
import me.friendly.api.minecraft.helper.WorldHelper;
import me.friendly.api.stopwatch.Stopwatch;
import me.friendly.exeter.core.Exeter;
import me.friendly.exeter.events.MoveUpdateEvent;
import me.friendly.exeter.events.MoveUpdateEvent.Era;
import me.friendly.exeter.events.PacketEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.api.properties.EnumProperty;
import me.friendly.api.properties.Property;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class Scaffold extends ToggleableModule {
    private final Property<Boolean> swing = new Property<>(true, "Swing", "swingarm");
    private final Property<Boolean> rotate = new Property<>(true, "Rotate", "rot", "face");
    private final EnumProperty<Swap> swap = new EnumProperty<>(Swap.NORMAL, "Swap", "swapping", "blockhold");
    private final EnumProperty<BlockPicker> picker = new EnumProperty<>(BlockPicker.SEQUENTIAL, "Picker", "blockpicker", "pk");
    private final Property<Boolean> tower = new Property<>(true, "Tower", "fastup");

    private final Stopwatch stopwatch = new Stopwatch();
    private PlaceInfo previous, current;
    private float[] rotations;
    private int s;

    public Scaffold() {
        super("Scaffold", new String[]{"scaffold", "blockfly", "bridge", "autobridge"}, ModuleType.WORLD);
        offerProperties(swing, rotate, swap, picker, tower);

        listeners.add(new Listener<MoveUpdateEvent>("scaffold_moveupdate_listener") {
            @Override
            public void call(MoveUpdateEvent event) {
                if (current != null) {
                    previous = current;
                }

                if (previous != null && rotate.getValue()) {
                    float[] angles = RotationHelper.calcAngleTo(previous.pos, previous.facing);
                    Exeter.getInstance().getRotationManager().setRotation(angles);
                    rotations = angles;
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
                        s = slot;
                        mc.player.inventory.currentItem = slot;
                    } else if (swap.getValue().equals(Swap.SPOOF)) {
                        if (Exeter.getInstance().getInventoryManager().slot != slot) {
                            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
                            s = slot;
                        }
                    }
                }

                if (event.era.equals(Era.POST)) {

                    if (WorldHelper.placeAt(current.pos, current.facing, EnumHand.MAIN_HAND, swing.getValue())) {
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

//                    EnumActionResult result = mc.playerController.processRightClickBlock(
//                            mc.player,
//                            mc.world,
//                            current.pos,
//                            current.facing,
//                            new Vec3d(current.pos).addVector(0.5, 0.5, 0.5),
//                            EnumHand.MAIN_HAND
//                    );
//
//                    if (result.equals(EnumActionResult.SUCCESS)) {
//                        if (swing.getValue()) {
//                            mc.player.swingArm(EnumHand.MAIN_HAND);
//                        } else {
//                            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
//                        }
//
//                        if (mc.gameSettings.keyBindJump.isKeyDown() && tower.getValue()) {
//                            mc.player.motionX *= 0.3;
//                            mc.player.motionZ *= 0.3;
//                            mc.player.jump();
//
//                            if (stopwatch.hasCompleted(1200L)) {
//                                stopwatch.reset();
//                                mc.player.motionY = -0.28;
//                            }
//                        }
//
//                        if (swap.getValue().equals(Swap.NORMAL)) {
//                            mc.player.inventory.currentItem = oldSlot;
//                        }
//                    }
                }
            }
        });

        listeners.add(new Listener<PacketEvent>("scaffold_packet_listener") {
            @Override
            public void call(PacketEvent event) {
//                if (event.getPacket() instanceof CPacketHeldItemChange) {
//                    CPacketHeldItemChange packet = event.getPacket();
//                    if (packet.getSlotId() != s) {
//                        packet.slotId = s;
//                    }
//                }
            }
        });
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

    private static class PlaceInfo {
        private final BlockPos pos;
        private final EnumFacing facing;

        public PlaceInfo(BlockPos pos, EnumFacing facing) {
            this.pos = pos;
            this.facing = facing;
        }
    }
}
