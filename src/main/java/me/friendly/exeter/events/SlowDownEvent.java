package me.friendly.exeter.events;

import me.friendly.api.event.Event;
import net.minecraft.util.MovementInput;

public class SlowDownEvent extends Event {
    public final MovementInput input;

    public SlowDownEvent(MovementInput input) {
        this.input = input;
    }
}
