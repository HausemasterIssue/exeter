package me.friendly.exeter.gui.screens.accountmanager;

import me.friendly.exeter.core.Exeter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;

public class GuiAccountSlot
extends GuiSlot {
    private GuiAccountScreen guiAccountScreen;
    int selected;

    public GuiAccountSlot(GuiAccountScreen aList) {
        super(Minecraft.getMinecraft(), aList.width, aList.height, 32, aList.height - 60, 27);
        this.guiAccountScreen = aList;
        this.selected = 0;
    }

    @Override
    protected int getContentHeight() {
        return this.getSize() * 40;
    }

    @Override
    protected int getSize() {
        return Exeter.getInstance().getAccountManager().getRegistry().size();
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        this.selected = slotIndex;
        if (isDoubleClick) {
            Account account = (Account)Exeter.getInstance().getAccountManager().getRegistry().get(slotIndex);
//            try {
//                //Minecraft.getMinecraft().processLogin(account.getLabel(), account.getPassword());
//            }
//            catch (AccountException exception) {
//                exception.printStackTrace();
//            }
        }
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return this.selected == slotIndex;
    }

    protected int getSelected() {
        return this.selected;
    }

    @Override
    protected void drawBackground() {
        this.guiAccountScreen.drawDefaultBackground();
    }

    @Override
    protected void func_192637_a(int p_192637_1_, int p_192637_2_, int p_192637_3_, int p_192637_4_, int p_192637_5_, int p_192637_6_, float p_192637_7_) {
        try {
            Account account = (Account)Exeter.getInstance().getAccountManager().getRegistry().get(p_192637_1_);

            this.mc.fontRendererObj.drawCenteredString(((Account)Exeter.getInstance().getAccountManager().getRegistry().get(p_192637_1_)).getLabel(), this.mc.displayWidth / 2, p_192637_3_ + 2, -5592406);
            this.mc.fontRendererObj.drawCenteredString(account.isPremium() ? account.getPassword().replaceAll("(?s).", "*") : "Not Available", this.mc.displayWidth / 2, p_192637_3_ + 15, -5592406);
        }
        catch (AccountException exception) {
            exception.printStackTrace();
        }
    }

//    @Override
//    protected void drawSlot(int selectedIndex, int x, int y, int var5, int var6, int var7) {
//        try {
//            Account account = (Account)Exeter.getInstance().getAccountManager().getRegistry().get(selectedIndex);
//            this.mc.fontRenderer.drawCenteredString(((Account)Exeter.getInstance().getAccountManager().getRegistry().get(selectedIndex)).getLabel(), this.mc.displayWidth / 2, y + 2, -5592406, true);
//            this.mc.fontRenderer.drawCenteredString(account.isPremium() ? account.getPassword().replaceAll("(?s).", "*") : "Not Available", this.mc.displayWidth / 2, y + 15, -5592406, true);
//        }
//        catch (AccountException exception) {
//            exception.printStackTrace();
//        }
//    }
}

