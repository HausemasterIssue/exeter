package me.friendly.exeter.events;

import me.friendly.api.event.Event;

public class PlayerTurnEvent extends Event {
    private final float yaw, pitch;

    public PlayerTurnEvent(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
