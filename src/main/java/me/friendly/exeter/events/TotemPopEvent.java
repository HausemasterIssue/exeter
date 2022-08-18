package me.friendly.exeter.events;

import me.friendly.api.event.Event;
import net.minecraft.entity.player.EntityPlayer;

public class TotemPopEvent extends Event {
    private final EntityPlayer player;
    private final int count;

    public TotemPopEvent(EntityPlayer player, int count) {
        this.player = player;
        this.count = count;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public int getCount() {
        return count;
    }
}
