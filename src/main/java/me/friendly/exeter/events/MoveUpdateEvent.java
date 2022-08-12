package me.friendly.exeter.events;

import me.friendly.api.event.Event;

public class MoveUpdateEvent extends Event {
    public final Era era;

    public double x, y, z;
    public float yaw, pitch;
    public boolean ground;

    public MoveUpdateEvent(double x, double y, double z, float yaw, float pitch, boolean ground) {
        era = Era.PRE;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.ground = ground;
    }

    public MoveUpdateEvent() {
        era = Era.POST;
    }

    public enum Era {
        PRE, POST
    }
}
