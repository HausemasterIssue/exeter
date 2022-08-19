package me.friendly.exeter.module;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

import me.friendly.api.interfaces.Toggleable;
import me.friendly.api.registry.ListRegistry;
import me.friendly.exeter.config.Config;
import me.friendly.exeter.core.Exeter;
import me.friendly.exeter.module.impl.combat.*;
import me.friendly.exeter.module.impl.exploits.*;
import me.friendly.exeter.module.impl.miscellaneous.*;
import me.friendly.exeter.module.impl.movement.*;
import me.friendly.exeter.module.impl.render.*;
import me.friendly.exeter.module.impl.world.*;
import org.lwjgl.input.Keyboard;

/**
 * Manages {@link Module}s for Exeter.
 */
public final class ModuleManager
    extends ListRegistry<Module> {

    public ModuleManager() {
        this.registry = new ArrayList();

        register(new Hud());
        register(new Fullbright());
        register(new ClickGui());
        register(new TabGui());
        register(new Colors());
        register(new Sprint());
        register(new Criticals());
        register(new WTap());
        register(new Velocity());
        register(new PacketFly());
        register(new GameSpeed());
        register(new FastPlace());
        register(new PacketUse());
        register(new PingSpoof());
        register(new NoSlow());
        register(new Speed());
        register(new AutoRespawn());
        register(new NoRotate());
        register(new Step());
        register(new BowRelease());
        register(new Aura());
        register(new Scaffold());
        register(new Fucker());
        register(new Blink());
        register(new NoFall());
        register(new InventoryWalk());
        register(new Flight());
        register(new SelfFill());
        register(new NoRender());
        register(new FastProjectile());
        register(new ChorusDelay());
        register(new NoWeather());
        register(new ExtraTab());
        register(new CameraClip());
        register(new FreeCam());
        register(new MiddleClick());
        register(new LongJump());
        register(new Notifications());
        register(new Trajectories());

        this.registry.sort(Comparator.comparing(Module::getLabel));

        Exeter.getInstance().getKeybindManager().getKeybindByLabel("Click Gui").setKey(Keyboard.KEY_RSHIFT);

        new Config("module_configurations.json"){

            @Override
            public void load(Object... source) {
                try {
                    if (!this.getFile().exists()) {
                        this.getFile().createNewFile();
                    }
                }
                catch (IOException exception) {
                    exception.printStackTrace();
                }
                File modDirectory = new File(Exeter.getInstance().getDirectory(), "modules");
                if (!modDirectory.exists()) {
                    modDirectory.mkdir();
                }
                Exeter.getInstance().getModuleManager().getRegistry().forEach(mod -> {
                    File file = new File(modDirectory, mod.getLabel().toLowerCase().replaceAll(" ", "") + ".json");
                    if (!file.exists()) {
                        return;
                    }
                    try (FileReader reader = new FileReader(file);){
                        JsonElement node = new JsonParser().parse((Reader)reader);
                        if (!node.isJsonObject()) {
                            return;
                        }
                        mod.loadConfig(node.getAsJsonObject());
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                this.loadConfig();
            }

            @Override
            public void save(Object... destination) {
                try {
                    if (!this.getFile().exists()) {
                        this.getFile().createNewFile();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                if (!this.getFile().exists()) {
                    return;
                }
                Exeter.getInstance().getModuleManager().getRegistry().forEach(Module::saveConfig);
                this.saveConfig();
            }

            /**
             * Loads module config from saved config.
             * Parses files in json format using Google Gson.
             * Called by {@link Config#load(Object...)}
             */
            private void loadConfig() {
                JsonElement root;
                File modsFile = new File(this.getFile().getAbsolutePath());
                if (!modsFile.exists()) {
                    return;
                }
                try (FileReader reader = new FileReader(modsFile);){
                    root = new JsonParser().parse((Reader)reader);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                if (!(root instanceof JsonArray)) {
                    return;
                }
                JsonArray mods = (JsonArray)root;
                mods.forEach(node -> {
                    if (!(node instanceof JsonObject)) {
                        return;
                    }
                    try {
                        JsonObject modNode = (JsonObject)node;
                        Exeter.getInstance().getModuleManager().getRegistry().forEach(mod -> {
                            if (mod.getLabel().equalsIgnoreCase(modNode.get("module-label").getAsString()) && mod instanceof Toggleable) {
                                ToggleableModule toggleableModule = (ToggleableModule)mod;
                                if (modNode.get("module-state").getAsBoolean()) {
                                    toggleableModule.setRunning(true);
                                }
                                toggleableModule.setDrawn(modNode.get("module-drawn").getAsBoolean());
                                Exeter.getInstance().getKeybindManager().getKeybindByLabel(toggleableModule.getLabel()).setKey(modNode.get("module-keybind").getAsInt());
                            }
                        });
                    }
                    catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
            }

            /**
             * Saves module config from saved config.
             * saves to file in json format using Google Gson.
             * Called by {@link Config#save(Object...)}
             */
            private void saveConfig() {
                File modsFile = new File(this.getFile().getAbsolutePath());
                if (modsFile.exists()) {
                    modsFile.delete();
                }
                if (Exeter.getInstance().getModuleManager().getRegistry().isEmpty()) {
                    return;
                }
                JsonArray mods = new JsonArray();
                Exeter.getInstance().getModuleManager().getRegistry().forEach(mod -> {
                    try {
                        JsonObject modObject = new JsonObject();
                        modObject.addProperty("module-label", mod.getLabel());
                        if (mod instanceof Toggleable) {
                            ToggleableModule toggleableModule = (ToggleableModule)mod;
                            modObject.addProperty("module-state", Boolean.valueOf(toggleableModule.isRunning()));
                            modObject.addProperty("module-drawn", Boolean.valueOf(toggleableModule.isDrawn()));
                            modObject.addProperty("module-keybind", (Number)Exeter.getInstance().getKeybindManager().getKeybindByLabel(toggleableModule.getLabel()).getKey());
                        }
                        mods.add((JsonElement)modObject);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                try (FileWriter writer = new FileWriter(modsFile);){
                    writer.write(new GsonBuilder().setPrettyPrinting().create().toJson((JsonElement)mods));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public Module getModuleByAlias(String alias) {
        for (Module module : registry) {
            for (String moduleAlias : module.getAliases()) {
                if (!alias.equalsIgnoreCase(moduleAlias)) continue;
                return module;
            }
        }
        return null;
    }
}

