package me.friendly.exeter.events;

import me.friendly.api.event.Event;

public class PutColorModifierEvent extends Event {
    public int j;

    public PutColorModifierEvent(int j) {
        this.j = j;
    }
}
