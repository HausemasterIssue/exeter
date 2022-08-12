package me.friendly.exeter.account.gui;

import me.friendly.exeter.account.Account;
import me.friendly.exeter.account.AccountException;
import me.friendly.exeter.core.Exeter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;

public class GuiAccountSlot extends GuiSlot {
    private final GuiAccountScreen guiAccountScreen;
    public int selected;

    public GuiAccountSlot(GuiAccountScreen aList) {
        super(Minecraft.getMinecraft(), aList.width, aList.height, 32, aList.height - 60, 27);

        this.guiAccountScreen = aList;
        this.selected = 0;
    }

    @Override
    protected int getContentHeight() {
        return getSize() * 40;
    }

    @Override
    protected int getSize() {
        return Exeter.getInstance().getAccountManager().getRegistry().size();
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        selected = slotIndex;

        if (isDoubleClick) {
            Account account = Exeter.getInstance().getAccountManager().getRegistry().get(slotIndex);
            if (account != null) {
                try {
                    Exeter.getInstance().getAccountManager().login(account.getLabel(), account.getPassword());
                } catch (AccountException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return selected == slotIndex;
    }

    protected int getSelected() {
        return selected;
    }

    @Override
    protected void drawBackground() {
        guiAccountScreen.drawDefaultBackground();
    }

    @Override
    protected void func_192637_a(int p_192637_1_, int p_192637_2_, int p_192637_3_, int p_192637_4_, int p_192637_5_, int p_192637_6_, float p_192637_7_) {
        try {
            Account account = Exeter.getInstance().getAccountManager().getRegistry().get(p_192637_1_);

            mc.fontRendererObj.drawCenteredString(account.getLabel(), guiAccountScreen.width / 2, p_192637_3_ + 2, -5592406);
            mc.fontRendererObj.drawCenteredString(account.isPremium() ? account.getPassword().replaceAll("(?s).", "*") : "Not Available", guiAccountScreen.width / 2, p_192637_3_ + 15, -5592406);
        } catch (AccountException exception) {
            exception.printStackTrace();
        }
    }
}

