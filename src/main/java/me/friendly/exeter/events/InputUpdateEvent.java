package me.friendly.exeter.events;

import me.friendly.api.event.Event;
import net.minecraft.util.MovementInput;

public class InputUpdateEvent extends Event {
    private final MovementInput movementInput;

    public InputUpdateEvent(MovementInput movementInput) {
        this.movementInput = movementInput;
    }

    public MovementInput getMovementInput() {
        return movementInput;
    }
}
