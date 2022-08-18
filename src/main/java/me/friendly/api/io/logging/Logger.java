package me.friendly.api.io.logging;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public final class Logger {
    /** Logger instance */
    private static Logger INSTANCE = null;

    /**
     * Appends message param to client tag,
     * and prints it to the log.
     *
     * @param message to be printed
     */
    public void print(String message) {
        System.out.printf("[%s] %s%n", "Exeter", message);
    }

    /**
     * Appends message param to client tag,
     * and prints it to the chat.
     *
     * @param message to be printed
     */
    public void printToChat(String message) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(String.format("§c[%s] §7%s", "Exeter", message.replace("&", "§"))).setStyle(new Style().setColor(TextFormatting.GRAY)));
    }

    /**
     * Appends message param to client tag,
     * and prints it to the chat.
     *
     * @param message to be printed
     * @param id the id of the editable message
     */
    public void printToChat(String message, int id) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(
                new TextComponentString(String.format(
                        "§c[%s] §7%s", "Exeter",
                        message.replace("&", "§")))
                        .setStyle(new Style().setColor(TextFormatting.GRAY)),
                id);
    }

    public static Logger getLogger() {
        return INSTANCE == null ? (INSTANCE = new Logger()) : INSTANCE;
    }
}

