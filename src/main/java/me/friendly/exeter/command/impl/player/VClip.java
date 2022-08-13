package me.friendly.exeter.command.impl.player;

import me.friendly.exeter.command.Argument;
import me.friendly.exeter.command.Command;

public final class VClip extends Command {
    public VClip() {
        super(new String[]{"vclip", "vc", "v"}, new Argument("blocks"));
    }

    @Override
    public String dispatch() {
        double blocks = Double.parseDouble(this.getArgument("blocks").getValue());
        minecraft.player.setPosition(minecraft.player.posX, minecraft.player.posY + blocks, minecraft.player.posZ);
        return String.format("Teleported %s &e%s&7 block(s).", blocks < 0.0 ? "down" : "up", blocks);
    }
}

