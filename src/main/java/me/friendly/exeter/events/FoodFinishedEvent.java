package me.friendly.exeter.events;

import me.friendly.api.event.Event;
import net.minecraft.item.ItemStack;

public class FoodFinishedEvent extends Event {
    private final ItemStack stack;

    public FoodFinishedEvent(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }
}
