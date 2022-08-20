package me.friendly.exeter.events;

import me.friendly.api.event.Event;
import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class ShouldRenderSideEvent extends Event {
    private final Block block;
    private final BlockPos pos;
    private final EnumFacing facing;

    public ShouldRenderSideEvent(Block block, BlockPos pos, EnumFacing facing) {
        this.block = block;
        this.pos = pos;
        this.facing = facing;
    }

    public Block getBlock() {
        return block;
    }

    public BlockPos getPos() {
        return pos;
    }

    public EnumFacing getFacing() {
        return facing;
    }
}
