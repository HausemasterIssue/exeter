package me.friendly.exeter.core;

import me.friendly.api.event.basic.BasicEventManager;
import me.friendly.api.minecraft.inventory.InventoryManager;
import me.friendly.exeter.command.CommandManager;
import me.friendly.exeter.config.Config;
import me.friendly.exeter.config.ConfigManager;
import me.friendly.exeter.friend.FriendManager;
import me.friendly.exeter.account.AccountManager;
import me.friendly.exeter.keybind.KeybindManager;
import me.friendly.api.io.logging.Logger;
import me.friendly.exeter.module.ModuleManager;
import me.friendly.exeter.rotate.RotationManager;
import org.lwjgl.opengl.Display;

import java.io.File;

/**
 * Exeter client for MCP 1.12.2
 *
 * Exeter client. A client created by Friendly,
 * for Minecraft version 1.8. It has been released
 * or leaked on that version. Gopro336 has obtained
 * that version, and here, has reconstructed the
 * original source code. In this process, Gopro has
 * also ported the client to his preferred version
 * and platform, Minecraft 1.12.2 forge. Furthermore,
 * Gopro has done work to clean up the decompiled code,
 * and javadoc it.
 *
 * @author Friendly
 * @author Gopro336
 * @version b23
 */
public final class Exeter {
    private static Exeter INSTANCE = null;

    private static final File DIRECTORY = new File(System.getProperty("user.home"), "exeter");
    public static final long START_TIME = System.nanoTime() / 1000000L;

    public static final String TITLE = "Exeter";
    public static final String VERSION = "1.0.0";
    public static final int BUILD = 1;

    private final BasicEventManager eventManager;
    private final KeybindManager keybindManager;
    private final ModuleManager moduleManager;
    private final CommandManager commandManager;
    private final FriendManager friendManager;
    private final ConfigManager configManager;
    private final AccountManager accountManager;
    private final RotationManager rotationManager;
    private final InventoryManager inventoryManager;

    public Exeter() {

        Logger.getLogger().print("Initializing...");
        INSTANCE = this;

        // In exeter 1.8, the config file is named clarinet for whatever reason. I changed that to be exeter
        if (!DIRECTORY.exists()) {
            Logger.getLogger().print(String.format("%s client directory.", DIRECTORY.mkdir() ? "Created" : "Failed to create"));
        }

        eventManager = new BasicEventManager();
        configManager = new ConfigManager();
        friendManager = new FriendManager();
        keybindManager = new KeybindManager();
        commandManager = new CommandManager();
        moduleManager = new ModuleManager();
        accountManager = new AccountManager();
        rotationManager = new RotationManager();
        inventoryManager = new InventoryManager();

        getConfigManager().getRegistry().forEach(Config::load);

        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook Thread") {
            @Override
            public void run() {
                Logger.getLogger().print("Shutting down...");
                getConfigManager().getRegistry().forEach(Config::save);
                Logger.getLogger().print("Shutdown.");
            }
        });

        Display.setTitle(TITLE + " v" + VERSION + "+" + BUILD);
        Logger.getLogger().print(String.format("Initialized, took %s milliseconds.", System.nanoTime() / 1000000L - START_TIME));
    }

    public static Exeter getInstance() {
        return INSTANCE;
    }

    public ModuleManager getModuleManager() {
        return this.moduleManager;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public KeybindManager getKeybindManager() {
        return this.keybindManager;
    }

    public FriendManager getFriendManager() {
        return this.friendManager;
    }

    public BasicEventManager getEventManager() {
        return this.eventManager;
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public AccountManager getAccountManager() {
        return this.accountManager;
    }

    public RotationManager getRotationManager() {
        return rotationManager;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public File getDirectory() {
        return DIRECTORY;
    }
}

