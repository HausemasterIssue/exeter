package me.friendly.exeter.module.impl.combat;

import io.netty.util.internal.ConcurrentSet;
import me.friendly.api.event.Listener;
import me.friendly.api.minecraft.helper.RotationHelper;
import me.friendly.api.minecraft.helper.WorldHelper;
import me.friendly.api.properties.EnumProperty;
import me.friendly.api.properties.NumberProperty;
import me.friendly.api.properties.Property;
import me.friendly.api.stopwatch.Stopwatch;
import me.friendly.exeter.core.Exeter;
import me.friendly.exeter.events.PacketEvent;
import me.friendly.exeter.events.TickEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FeetTrap extends ToggleableModule {
    private final EnumProperty<Timing> timing = new EnumProperty<>(Timing.VANILLA, "Timing", "order");
    private final Property<Boolean> rotate = new Property<>(true, "Rotate", "rot", "face");
    private final Property<Boolean> ncpStrict = new Property<>(true, "NCP Strict", "ncpstrict");
    private final NumberProperty<Integer> blocks = new NumberProperty<>(4, 1, 8, "Blocks", "placements");
    private final NumberProperty<Integer> delay = new NumberProperty<>(1, 0, 4, "Delay", "dl");
    private final Property<Boolean> reactive = new Property<>(true, "Reactive", "attack");

    private final Stopwatch stopwatch = new Stopwatch();
    private BlockSearchThread searchThread;
    private int placed = 0;

    private final Queue<BlockPos> nextTick = new ConcurrentLinkedQueue<>();

    public FeetTrap() {
        super("Feet Trap", new String[]{"feettrap", "surround", "autoobsidian"}, ModuleType.COMBAT);
        offerProperties(timing, rotate, ncpStrict, blocks, delay, reactive);

        listeners.add(new Listener<PacketEvent>("feettrap_packet_listener") {
            @Override
            public void call(PacketEvent event) {
                if (event.getPacket() instanceof SPacketBlockChange) {
                    SPacketBlockChange packet = event.getPacket();
                    if (WorldHelper.isReplaceable(packet.getBlockPosition()) && !isEntityBlocking(packet.getBlockPosition())) {

                        if (searchThread.placements.contains(packet.getBlockPosition())) {
                            place(packet.getBlockPosition(), false);
                        }
                    }
                } else if (event.getPacket() instanceof SPacketSpawnObject) {
                    SPacketSpawnObject packet = event.getPacket();
                    if (packet.getType() == 51) {

                        BlockPos base = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
                        AxisAlignedBB bb = new AxisAlignedBB(base, base.add(2.0, 2.0, 2.0));

                        ArrayList<BlockPos> positions = new ArrayList<>(searchThread.replacements);
                        positions.addAll(nextTick);

                        for (BlockPos pos : positions) {

                            if (pos.equals(base.up()) && bb.intersectsWith(new AxisAlignedBB(pos))) {
                                List<Entity> entities = mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos));
                                if (!entities.isEmpty()) {
                                    for (Entity entity : entities) {
                                        if (entity instanceof EntityEnderCrystal && !entity.isDead && reactive.getValue()) {
                                            breakCrystal(packet.getEntityID());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        });

        listeners.add(new Listener<TickEvent>("feettrap_tick_listener") {
            @Override
            public void call(TickEvent event) {
                placed = 0;

                if (stopwatch.hasCompleted(delay.getValue() * 50L)) {

                    if (!nextTick.isEmpty()) {

                        while (placed < blocks.getValue()) {
                            BlockPos next = nextTick.poll();
                            if (next == null) {
                                break;
                            }

                            place(next, true);
                        }
                    }

                    if (!searchThread.replacements.isEmpty()) {
                        for (BlockPos next : searchThread.replacements) {
                            if (placed >= blocks.getValue()) {
                                break;
                            }
                            place(next, true);
                        }
                    }

                    if (placed >= blocks.getValue()) {
                        stopwatch.reset();
                        //Exeter.getInstance().getInventoryManager().sync();
                    }

                    //Exeter.getInstance().getInventoryManager().sync();
                }
            }
        });
    }

    @Override
    protected void onEnable() {
        super.onEnable();

        searchThread = new BlockSearchThread();
        searchThread.start();
    }

    @Override
    protected void onDisable() {
        super.onDisable();

        searchThread.interrupt();
        searchThread = null;

        nextTick.clear();
        placed = 0;

        Exeter.getInstance().getInventoryManager().sync();
    }

    private void place(BlockPos pos, boolean ignoreEntities) {
        if (placed >= blocks.getValue()) {
            stopwatch.reset();
            nextTick.add(pos);
            return;
        }

        if (!WorldHelper.isReplaceable(pos)) {
            return;
        }

        if (!ignoreEntities) {

            for (Entity entity : new ArrayList<>(mc.world.loadedEntityList)) {
                if (entity instanceof EntityEnderCrystal && !entity.isDead) {

                    if (new AxisAlignedBB(pos).intersectsWith(entity.getEntityBoundingBox()) && reactive.getValue()) {

                        if (rotate.getValue()) {
                            float[] angles = RotationHelper.calcAngleTo(entity);

                            Exeter.getInstance().getRotationManager().setRotation(angles);
                            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(angles[0], angles[1], mc.player.onGround));
                        }

                        breakCrystal(entity.getEntityId());

                        if (timing.getValue().equals(Timing.SEQUENTIAL)) {
                            nextTick.add(pos);
                            return;
                        }
                    }
                }
            }
        }

        WorldHelper.PlaceInfo result = WorldHelper.getPlacement(pos, false);
        if (result != null) {

            if (searchThread.slot != -1 && Exeter.getInstance().getInventoryManager().getSlot() != searchThread.slot) {
                mc.player.connection.sendPacket(new CPacketHeldItemChange(searchThread.slot));
            }

            ++placed;

            if (rotate.getValue()) {
                float[] angles = RotationHelper.calcAngleTo(result.pos, result.facing);

                Exeter.getInstance().getRotationManager().setRotation(angles);
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(angles[0], angles[1], mc.player.onGround));
            }

            if (ncpStrict.getValue()) {
                WorldHelper.placeAtRaycast(EnumHand.MAIN_HAND, false);
            } else {
                WorldHelper.placeAt(result.pos, result.facing, EnumHand.MAIN_HAND, false);
            }

            //Exeter.getInstance().getInventoryManager().sync();
        }
    }

    private void breakCrystal(int id) {
        CPacketUseEntity packet = new CPacketUseEntity();
        packet.entityId = id;
        packet.action = CPacketUseEntity.Action.ATTACK;

        mc.player.connection.sendPacket(packet);
    }

    private boolean isEntityBlocking(BlockPos pos) {
        return !mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos), (s) -> !s.isDead).isEmpty();
    }

    public enum Timing {
        VANILLA, SEQUENTIAL
    }

    private class BlockSearchThread extends Thread {

        public final Set<BlockPos> placements = new ConcurrentSet<>();
        public final Set<BlockPos> replacements = new ConcurrentSet<>();

        public int slot = -1;

        public BlockSearchThread() {
            super("Block-Search-Thread");
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {

                if (slot != -1) {
                    ItemStack stack = mc.player.inventory.getStackInSlot(slot);
                    if (stack.func_190926_b() || !(stack.getItem() instanceof ItemBlock)) {
                        continue;
                    }

                    Block block = ((ItemBlock) stack.getItem()).getBlock();
                    if (!(block.equals(Blocks.OBSIDIAN) || block.equals(Blocks.ENDER_CHEST))) {
                        slot = -1;
                    }
                }

                for (int i = 0; i < 9; ++i) {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);
                    if (!(stack.getItem() instanceof ItemBlock)) {
                        continue;
                    }

                    Block block = ((ItemBlock) stack.getItem()).getBlock();
                    if (block.equals(Blocks.OBSIDIAN) || block.equals(Blocks.ENDER_CHEST)) {
                        slot = i;
                    }
                }

                BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
                Set<BlockPos> lookup = new HashSet<>();

                for (EnumFacing facing : EnumFacing.VALUES) {
                    if (facing.equals(EnumFacing.UP)) {
                        continue;
                    }

                    BlockPos n = pos.offset(facing);
                    if (isEntityBlocking(n)) {
                        lookup.add(n);
                    } else {
                        if (WorldHelper.isReplaceable(n)) {
                            replacements.add(n);
                        }

                        placements.add(n);
                    }
                }

                if (!lookup.isEmpty()) {
                    for (BlockPos next : lookup) {
                        for (EnumFacing facing : EnumFacing.VALUES) {
                            if (facing.equals(EnumFacing.UP)) {
                                continue;
                            }

                            BlockPos n = next.offset(facing);
                            if (WorldHelper.isReplaceable(n)) {
                                if (isEntityBlocking(n)) {
                                    for (EnumFacing dir : EnumFacing.VALUES) {
                                        if (dir.equals(EnumFacing.UP)) {
                                            continue;
                                        }

                                        BlockPos nn = n.offset(dir);
                                        if (!isEntityBlocking(nn)) {
                                            if (WorldHelper.isReplaceable(nn)) {
                                                replacements.add(nn);
                                            }

                                            placements.add(nn);
                                        }
                                    }
                                } else {
                                    replacements.add(n);
                                    placements.add(n);
                                }
                            }
                        }
                    }
                }

                replacements.removeIf((b) -> !WorldHelper.isReplaceable(b));
            }
        }
    }
}
