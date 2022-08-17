package me.friendly.exeter.events;

import me.friendly.api.event.Event;
import net.minecraft.client.network.NetworkPlayerInfo;

public class TabOverlayEvent extends Event {
    private final Type type;

    private PingDisplay pingDisplay = PingDisplay.DEFAULT;
    private int length = 80;
    private boolean renderPicture = false;

    private String playerText = null;
    private Sorting sorting = Sorting.DEFAULT;

    public TabOverlayEvent(Type type) {
        this.type = type;
    }

    public void setPingDisplay(PingDisplay pingDisplay) {
        this.pingDisplay = pingDisplay;
    }

    public PingDisplay getPingDisplay() {
        return pingDisplay;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    public void setRenderPicture(boolean renderPicture) {
        this.renderPicture = renderPicture;
    }

    public boolean isRenderPicture() {
        return renderPicture;
    }

    public void setPlayerText(String playerText) {
        this.playerText = playerText;
    }

    public String getPlayerText() {
        return playerText;
    }

    public void setSorting(Sorting sorting) {
        this.sorting = sorting;
    }

    public Sorting getSorting() {
        return sorting;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        TEXT,
        PICTURE,
        PING,
        LENGTH,
        SORT
    }

    public enum PingDisplay {
        DEFAULT, NONE, TEXT
    }

    public enum Sorting {
        DEFAULT, PING
    }
}
