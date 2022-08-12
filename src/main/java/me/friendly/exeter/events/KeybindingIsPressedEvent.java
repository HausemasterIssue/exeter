package me.friendly.exeter.events;

import me.friendly.api.event.Event;

public class KeybindingIsPressedEvent extends Event {
    public boolean pressed;
    public final int key;

    public KeybindingIsPressedEvent(boolean pressed, int key) {
        this.pressed = pressed;
        this.key = key;
    }
}
