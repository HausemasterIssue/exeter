package me.friendly.exeter.events;

import me.friendly.api.event.Event;

public class RenderRotationsEvent extends Event {
    public float yaw, pitch;

    public RenderRotationsEvent(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
