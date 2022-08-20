package me.friendly.exeter.events;

import me.friendly.api.event.Event;
import net.minecraft.block.Block;

public class LightValueEvent extends Event {
    private final Block block;
    public int light;

    public LightValueEvent(Block block, int light) {
        this.block = block;
        this.light = light;
    }

    public Block getBlock() {
        return block;
    }
}
