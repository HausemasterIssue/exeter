package me.friendly.exeter.events;

import me.friendly.api.event.Event;
import net.minecraft.block.Block;
import net.minecraft.util.BlockRenderLayer;

public class RenderBlockLayerEvent extends Event {
    private BlockRenderLayer layer;
    private final Block block;

    public RenderBlockLayerEvent(BlockRenderLayer layer, Block block) {
        this.layer = layer;
        this.block = block;
    }

    public BlockRenderLayer getLayer() {
        return layer;
    }

    public void setLayer(BlockRenderLayer layer) {
        this.layer = layer;
    }

    public Block getBlock() {
        return block;
    }
}