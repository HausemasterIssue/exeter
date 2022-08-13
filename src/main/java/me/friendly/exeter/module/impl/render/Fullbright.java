package me.friendly.exeter.module.impl.render;

import me.friendly.api.event.Listener;
import me.friendly.exeter.events.TickEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.api.properties.EnumProperty;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class Fullbright extends ToggleableModule {
    private final EnumProperty<Mode> mode = new EnumProperty<>(Mode.POTION, "Mode", "m");

    private float oldGamma = -1.0f;
    private boolean gavePotion = false;
    private float[] lightMap = null;

    public Fullbright() {
        super("Fullbright", new String[]{"fullbright", "bright", "brightness", "fb"}, ModuleType.RENDER);
        offerProperties(mode);

        listeners.add(new Listener<TickEvent>("fullbright_tick_listener") {
            @Override
            public void call(TickEvent event) {
                switch (mode.getValue()) {
                    case GAMMA:
                        resetLightmap();
                        removePotions();

                        if (oldGamma == -1.0f) {
                            oldGamma = mc.gameSettings.gammaSetting;
                        }

                        mc.gameSettings.gammaSetting = 100.0f;
                        break;

                    case POTION:
                        restoreGamma();
                        resetLightmap();

                        if (mc.player != null) {
                            if (!mc.player.isPotionActive(MobEffects.NIGHT_VISION)) {
                                gavePotion = true;
                                mc.player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 9999, 1));
                            }
                        }
                        break;

                    case LIGHTMAP:

                        if (mc.world != null) {
                            restoreGamma();
                            removePotions();

                            if (lightMap == null) {
                                lightMap = mc.world.provider.lightBrightnessTable;
                            }

                            for (int i = 0; i < 16; ++i) {
                                mc.world.provider.lightBrightnessTable[i] = 1.0f;
                            }
                        }
                        break;
                }
            }
        });
    }

    @Override
    protected void onDisable() {
        super.onDisable();

        restoreGamma();
        removePotions();
        resetLightmap();
    }

    private void resetLightmap() {
        if (lightMap != null) {
            mc.world.provider.lightBrightnessTable = lightMap;
            lightMap = null;
        }
    }

    private void removePotions() {
        if (gavePotion && mc.player != null) {
            mc.player.removeActivePotionEffect(MobEffects.NIGHT_VISION);
        }

        gavePotion = false;
    }

    private void restoreGamma() {
        if (oldGamma != -1.0f) {
            mc.gameSettings.gammaSetting = oldGamma;
            oldGamma = -1.0f;
        }
    }

    public enum Mode {
        GAMMA, LIGHTMAP, POTION
    }
}

