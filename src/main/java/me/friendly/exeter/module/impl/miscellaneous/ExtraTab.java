package me.friendly.exeter.module.impl.miscellaneous;

import me.friendly.api.event.Listener;
import me.friendly.api.properties.EnumProperty;
import me.friendly.api.properties.NumberProperty;
import me.friendly.api.properties.Property;
import me.friendly.exeter.core.Exeter;
import me.friendly.exeter.events.TabOverlayEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.text.TextFormatting;

import java.util.Comparator;

public class ExtraTab extends ToggleableModule {
    public final NumberProperty<Integer> count = new NumberProperty<>(20, -1, 1000, "Count", "length", "len");
    public final Property<Boolean> noPicture = new Property<>(false, "No Picture", "1.7");
    public final EnumProperty<TabOverlayEvent.Sorting> sorting = new EnumProperty<>(TabOverlayEvent.Sorting.DEFAULT, "Sorting", "sort");
    public final Property<Boolean> friendHighlight = new Property<>(true, "Friend Highlight", "highlightfriends", "bluefriends");
    public final EnumProperty<TabOverlayEvent.PingDisplay> display = new EnumProperty<>(TabOverlayEvent.PingDisplay.DEFAULT, "Display", "disp");

    public ExtraTab() {
        super("Extra Tab", new String[]{"extratab", "extendedtab"}, ModuleType.MISCELLANEOUS);
        offerProperties(count, noPicture, sorting, friendHighlight, display);

        listeners.add(new Listener<TabOverlayEvent>("extratab_taboverlay_listener") {
            @Override
            public void call(TabOverlayEvent event) {
                switch (event.getType()) {
                    case PICTURE: {
                        event.setRenderPicture(!noPicture.getValue());
                        event.setCanceled(true);
                        break;
                    }

                    case TEXT: {
                        if (friendHighlight.getValue() && (Exeter.getInstance().getFriendManager().isFriend(event.getPlayerText()) || mc.player.getGameProfile().getName().equals(event.getPlayerText()))) {
                            event.setPlayerText(TextFormatting.AQUA + event.getPlayerText());
                            event.setCanceled(true);
                        }
                        break;
                    }

                    case PING: {
                        event.setPingDisplay(display.getValue());
                        break;
                    }

                    case LENGTH: {
                        if (count.getValue() >= 1) {
                            event.setLength(count.getValue());
                        }
                        break;
                    }

                    case SORT: {
                        event.setSorting(sorting.getValue());
                        break;
                    }
                }
            }
        });
    }

    public static class PingSorting implements Comparator<NetworkPlayerInfo> {
        @Override
        public int compare(NetworkPlayerInfo o1, NetworkPlayerInfo o2) {
            return o1.getResponseTime() - o2.getResponseTime();
        }
    }
}
