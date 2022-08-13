package me.friendly.api.registry;

import java.util.ArrayList;

/**
 * Register
 *
 * @param <T> the type of Object registered.
 */
public class ListRegistry<T> {

    protected java.util.List<T> registry = new ArrayList<>();

    public void register(T element) {
        this.registry.add(element);
    }

    public void unregister(T element) {
        this.registry.remove(element);
    }

    public void clear() {
        this.registry.clear();
    }

    public java.util.List<T> getRegistry() {
        return this.registry;
    }
}

