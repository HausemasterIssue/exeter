package me.friendly.exeter.events;

import me.friendly.api.event.Event;
import net.minecraft.entity.player.EntityPlayer;

public class DeathEvent extends Event {
    private final EntityPlayer player;

    public DeathEvent(EntityPlayer player) {
        this.player = player;
    }

    public EntityPlayer getPlayer() {
        return player;
    }
}
