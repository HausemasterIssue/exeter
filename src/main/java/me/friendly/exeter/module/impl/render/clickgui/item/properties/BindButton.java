package me.friendly.exeter.module.impl.render.clickgui.item.properties;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.friendly.api.minecraft.render.RenderMethods;
import me.friendly.api.minecraft.render.font.FontUtil;
import me.friendly.exeter.keybind.Keybind;
import me.friendly.exeter.module.impl.render.Colors;
import me.friendly.exeter.module.impl.render.clickgui.item.Button;
import me.friendly.exeter.module.impl.render.clickgui.item.Item;
import org.lwjgl.input.Keyboard;

public class BindButton extends Button {
    private final Keybind keybind;
    private boolean listening = false;

    public BindButton(Keybind keybind) {
        super("Bind");
        this.keybind = keybind;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        //super.drawScreen(mouseX, mouseY, partialTicks);

        if (isHovering(mouseX, mouseY)) {
            RenderMethods.drawGradientRect(this.x, this.y, this.x + (float)this.width, this.y + (float)this.height, 0x77AAAAAB, 0x66AAAAAB);
        }

        String text = getLabel() + ": " + ChatFormatting.GRAY + Keyboard.getKeyName(keybind.getKey());
        if (listening) {
            text = "Listening...";
        }

        FontUtil.drawString(text, this.x + 2.0f, this.y + 4.0f, -1);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        //super.mouseClicked(mouseX, mouseY, mouseButton);

        if (isHovering(mouseX, mouseY) && mouseButton == 0) {
            Item.playClickSound();
            listening = !listening;
        }
    }

    @Override
    public void keyTyped(char keyTyped, int keyCode) {
        super.keyTyped(keyTyped, keyCode);

        if (listening) {
            listening = false;
            keybind.setKey(keyCode == Keyboard.KEY_ESCAPE ? Keyboard.KEY_NONE : keyCode);
        }
    }
}
