package me.friendly.exeter.account.gui;

import me.friendly.exeter.account.Account;
import me.friendly.exeter.account.AccountException;
import me.friendly.exeter.core.Exeter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.Session;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class GuiAccountScreen extends GuiScreen implements GuiYesNoCallback {
    private static final Random RNG = new Random();
    private final Minecraft mc = Minecraft.getMinecraft();

    private String dispErrorString = "";
    private boolean deleteMenuOpen = false;

    private GuiAccountSlot accountSlot;
    private int timer = 0;

    private GuiButton random;

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        accountSlot.handleMouseInput();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(1, width / 2 - 154, height - 48, 73, 20, "Add"));
        buttonList.add(new GuiButton(2, width / 2 - 76, height - 48, 73, 20, "Login"));
        buttonList.add(new GuiButton(3, width / 2 + 78, height - 48, 73, 20, "Remove"));
        buttonList.add(new GuiButton(4, width / 2 - 76, height - 26, 149, 20, "Back"));
        buttonList.add(new GuiButton(5, width / 2, height - 48, 73, 20, "Direct Login"));
        buttonList.add(random = new GuiButton(6, width / 2 - 154, height - 26, 73, 20, "Random"));
        buttonList.add(new GuiButton(7, width / 2 + 78, height - 26, 73, 20, "Import"));

        accountSlot = new GuiAccountSlot(this);
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        super.confirmClicked(result, id);

        if (deleteMenuOpen) {
            deleteMenuOpen = false;

            if (result) {
                Exeter.getInstance().getAccountManager().getRegistry().remove(id);
            }

            mc.displayGuiScreen(this);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 200) {
            --accountSlot.selected;
        } else if (keyCode == 208) {
            ++accountSlot.selected;
        } else if (keyCode == 28) {
            Account account = Exeter.getInstance().getAccountManager().getRegistry().get(accountSlot.selected);

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
    public void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button.id == 1) {
            mc.displayGuiScreen(new GuiAccountAdd());
        } else if (button.id == 2) {
            Account account = Exeter.getInstance().getAccountManager().getRegistry().get(accountSlot.selected);
            if (account != null) {
                if (account.isPremium()) {
                    try {
                        Exeter.getInstance().getAccountManager().login(account.getLabel(), account.getPassword());
                    } catch (AccountException e) {
                        e.printStackTrace();
                    }
                } else {
                    mc.session = new Session(account.getLabel(), "", "", "legacy");
                }
            }
        } else if (button.id == 3) {
            Account account = Exeter.getInstance().getAccountManager().getRegistry().get(accountSlot.selected);

            if (account != null) {
                deleteMenuOpen = true;
                mc.displayGuiScreen(new GuiYesNo(this,
                        "Are you sure you want to delete \"" + account.getLabel() + "\"?",
                        "", "Confirm", "Cancel",
                        accountSlot.selected));
            }
        } else if (button.id == 4) {
            mc.displayGuiScreen(new GuiMainMenu());
        } else if (button.id == 5) {
            mc.displayGuiScreen(new GuiDirectLogin(this));
        } else if (button.id == 6) {
            if (!random.enabled) {
                return;
            }

            int randomIndex = RNG.nextInt(Exeter.getInstance().getAccountManager().getRegistry().size());
            Account account = Exeter.getInstance().getAccountManager().getRegistry().get(randomIndex);

            if (account != null) {
                if (account.isPremium()) {
                    try {
                        Exeter.getInstance().getAccountManager().login(account.getLabel(), account.getPassword());
                    } catch (AccountException e) {
                        e.printStackTrace();
                    }
                } else {
                    mc.session = new Session(account.getLabel(), "", "", "legacy");
                }
            }
        } else if (button.id == 7) {
            importAlts();
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        random.enabled = !Exeter.getInstance().getAccountManager().getRegistry().isEmpty();
    }

    private void importAlts() {
        JFrame frame = new JFrame("Select a file");

        JFileChooser chooser = new JFileChooser();
        chooser.setVisible(true);
        chooser.setSize(500, 400);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("File", "txt"));

        chooser.addActionListener(e -> {
            if (e.getActionCommand().equals("ApproveSelection") && chooser.getSelectedFile() != null) {
                try {
                    Scanner scanner = new Scanner(new FileReader(chooser.getSelectedFile()));
                    scanner.useDelimiter("\n");

                    while (scanner.hasNext()) {
                        String[] split = scanner.next().trim().split(":");
                        Exeter.getInstance().getAccountManager().getRegistry().add(new Account(split[0], split[1]));
                    }

                    scanner.close();
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }

                try {
                    StringBuilder data = new StringBuilder();
                    for (Account alt : Exeter.getInstance().getAccountManager().getRegistry()) {
                        data.append(alt.getFileLine()).append("\n");
                    }

                    BufferedWriter writer = new BufferedWriter(new FileWriter(Exeter.getInstance().getDirectory() + "/accounts.txt"));
                    writer.write(data.toString());
                    writer.close();
                } catch (Exception localException) {
                    localException.printStackTrace();
                }

                frame.setVisible(false);
                frame.dispose();
            }

            if (e.getActionCommand().equals("CancelSelection")) {
                frame.setVisible(false);
                frame.dispose();
            }
        });

        frame.setAlwaysOnTop(true);
        frame.add(chooser);
        frame.setVisible(true);
        frame.setSize(750, 600);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        accountSlot.drawScreen(mouseX, mouseY, partialTicks);

        mc.fontRendererObj.drawStringWithShadow(mc.session.getUsername(), width - mc.fontRendererObj.getStringWidth(Minecraft.getMinecraft().getSession().getUsername()) - 2, 2.0f, 0xA0A0A0);
        mc.fontRendererObj.drawStringWithShadow("Accounts: " + Exeter.getInstance().getAccountManager().getRegistry().size(), 2.0f, 2.0f, 0xA0A0A0);

        if (dispErrorString.length() > 1) {
            ++timer;
            if (timer > 100) {
                dispErrorString = "";
                timer = 0;
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}

