package me.friendly.exeter.module.impl.miscellaneous;

import me.friendly.api.event.Listener;
import me.friendly.api.io.logging.Logger;
import me.friendly.api.properties.Property;
import me.friendly.exeter.core.Exeter;
import me.friendly.exeter.events.DeathEvent;
import me.friendly.exeter.events.TotemPopEvent;
import me.friendly.exeter.module.Module;

public class Notifications extends Module {
    private static final int GENERAL_NOTIFICATION_ID = -694201337;

    private final Property<Boolean> chat = new Property<>(true, "Chat", "chatdisplay");
    private final Property<Boolean> pops = new Property<>(true, "Pops", "totempops");

    public Notifications() {
        super("Notifications", new String[]{"notifications"});
        offerProperties(chat, pops);

        Exeter.getInstance().getEventManager().register(new Listener<TotemPopEvent>("notifications_pop_listener") {
            @Override
            public void call(TotemPopEvent event) {
                if (pops.getValue()) {
                    log(event.getPlayer().getDisplayName().getUnformattedText()
                                    + " has popped &c"
                                    + event.getCount()
                                    + "&7 totem"
                                    + (event.getCount() > 1 ? "s." : "."),
                            event.getPlayer().hashCode());
                }
            }
        });

        Exeter.getInstance().getEventManager().register(new Listener<DeathEvent>("notifications_death_listener") {
            @Override
            public void call(DeathEvent event) {
                if (pops.getValue()) {
                    int count = Exeter.getInstance().getPopManager().getPops(event.getPlayer().getEntityId());
                    if (count > 0) {
                        log(event.getPlayer().getDisplayName().getUnformattedText()
                                        + " has died after popping &c"
                                        + count
                                        + "&7 totem"
                                        + (count > 1 ? "s." : "."),
                                event.getPlayer().hashCode());
                    }
                }
            }
        });
    }

    private void log(String text, int id) {
        if (chat.getValue()) {
            Logger.getLogger().printToChat(text, id);
        } else {
            // todo: notification tray
        }
    }
}
