package me.friendly.exeter.module.impl.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.friendly.api.event.Listener;
import me.friendly.api.minecraft.render.font.FontUtil;
import me.friendly.exeter.core.Exeter;
import me.friendly.exeter.events.MoveUpdateEvent;
import me.friendly.exeter.events.RenderGameOverlayEvent;
import me.friendly.exeter.events.RenderGameOverlayEvent.Type;
import me.friendly.exeter.module.Module;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.api.properties.EnumProperty;
import me.friendly.api.properties.Property;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public final class Hud extends Module {
    public static final Property<Boolean> customFont = new Property<Boolean>(false, "CustomFont", "cf", "font");
    private final Property<Boolean> watermark = new Property<Boolean>(true, "Watermark", "wm", "water");
    private final Property<Boolean> direction = new Property<Boolean>(true, "Direction", "facing", "d");
    private final Property<Boolean> armor = new Property<Boolean>(true, "Armor", "a");
    private final Property<Boolean> potions = new Property<Boolean>(true, "Potions", "pots");
    private final Property<Boolean> fps = new Property<>(true, "FPS", "frames");
    private final Property<Boolean> tps = new Property<>(true, "TPS", "ticks", "performance");
    private final Property<Boolean> speed = new Property<>(true, "Speed", "zoom");
    private final Property<Boolean> ping = new Property<>(true, "Ping", "latency");
    private final Property<Boolean> time = new Property<Boolean>(true, "Time", "t");
    private final Property<Boolean> coords = new Property<Boolean>(true, "Coords", "coord", "c", "cord");
    private final Property<Boolean> arraylist = new Property<Boolean>(true, "ArrayList", "array", "al");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
    private final EnumProperty<Organize> organize = new EnumProperty<Organize>(Organize.LENGTH, "Organize", "o");
    private final EnumProperty<Look> look = new EnumProperty<Look>(Look.DEFAULT, "Casing", "c");

    private double moveSpeed = 0.0;

    public Hud() {
        super("HUD", new String[]{"textgui", "hud", "overlay"});
        this.offerProperties(this.customFont, this.look, this.watermark, this.organize, this.potions, this.armor, this.time, this.direction, this.arraylist, this.coords);

        Exeter.getInstance().getEventManager().register(
                new Listener<RenderGameOverlayEvent>("text_gui_render_game_overlay_listener") {

            @Override
            public void call(RenderGameOverlayEvent event) {
                if (mc.gameSettings.showDebugInfo || !event.getType().equals(Type.IN_GAME)) {
                    return;
                }

                ScaledResolution res = event.getScaledResolution();

                if (watermark.getValue()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    FontUtil.drawString(Exeter.TITLE + " v" + Exeter.VERSION + "+" + Exeter.BUILD, 2.0f, 2.0f, -1);
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                }

                if (arraylist.getValue()) {
                    List<Module> modules = Exeter.getInstance().getModuleManager().getRegistry();
                    switch (organize.getValue()) {
                        case ABC:
                            modules.sort(Comparator.comparing(Module::getTag));
                            break;

                        case LENGTH:
                            modules.sort(Comparator.comparingInt((s) -> -FontUtil.getStringWidth(s.getTag())));
                            break;
                    }

                    int rc = 0;

                    int y = 2;
                    for (Module module : modules) {
                        if (module instanceof ToggleableModule) {
                            ToggleableModule t = (ToggleableModule) module;
                            if (t.isDrawn() && t.isRunning()) {
                                int len = FontUtil.getStringWidth(t.getTag());

                                int color = Colors.getClientColor();
                                if (Colors.hudRainbow.getValue()) {
                                    color = Colors.getRainbow(Colors.rainbowSpeed.getValue().intValue(), rc++ * 50, Colors.saturation.getValue(), Colors.lightness.getValue());
                                }

                                FontUtil.drawString(t.getTag(), res.getScaledWidth() - len - 2, y, color);
                                y += FontUtil.getHeight() + 1;
                            }
                        }
                    }
                }

                if (armor.getValue()) {
                    int x = 15;
                    int y = mc.player.isInsideOfMaterial(Material.WATER) && !mc.player.capabilities.isCreativeMode ? 65 : (mc.player.capabilities.isCreativeMode ? 38 : 55);

                    GlStateManager.pushMatrix();
                    RenderHelper.enableGUIStandardItemLighting();

                    for (int i = 3; i >= 0; --i) {
                        ItemStack stack = mc.player.inventory.armorInventory.get(i);

                        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, res.getScaledWidth() / 2 + x, res.getScaledHeight() - y);
                        mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, res.getScaledWidth() / 2 + x, res.getScaledHeight() - y);

                        x += 18;
                    }

                    RenderHelper.disableStandardItemLighting();
                    GlStateManager.popMatrix();
                }

                // bottom right
                int y = res.getScaledHeight() - (mc.currentScreen instanceof GuiChat ? 24 : 10);

                if (potions.getValue()) {
                    Collection<PotionEffect> effects = mc.player.getActivePotionEffects();
                    if (!effects.isEmpty()) {
                        for (PotionEffect effect : effects) {
                            Potion potion = effect.getPotion();

                            String name = I18n.format(potion.getName());
                            name += String.format(" \u00a77%s : %s", effect.getAmplifier() + 1, Potion.getPotionDurationString(effect, 1));

                            FontUtil.drawString(name, res.getScaledWidth() - FontUtil.getStringWidth(name) - 2, y, potion.getLiquidColor());

                            y -= FontUtil.getHeight() + 1;
                        }
                    }
                }

                if (speed.getValue()) {
                    String time = ChatFormatting.GRAY + "Speed: " + ChatFormatting.RESET + String.format("%.2f", moveSpeed) + "km/h";
                    FontUtil.drawString(time, res.getScaledWidth() - FontUtil.getStringWidth(time) - 2, y, -1);
                    y -= FontUtil.getHeight() + 1;
                }

                if (time.getValue()) {
                    String time = ChatFormatting.GRAY + "Time: " + ChatFormatting.RESET + dateFormat.format(new Date());
                    FontUtil.drawString(time, res.getScaledWidth() - FontUtil.getStringWidth(time) - 2, y, -1);
                    y -= FontUtil.getHeight() + 1;
                }

                if (ping.getValue()) {
                    int l = 0;
                    for (NetworkPlayerInfo info : mc.player.connection.getPlayerInfoMap()) {
                        if (info.getGameProfile().getId().equals(mc.player.getGameProfile().getId())) {
                            l = info.getResponseTime();
                            break;
                        }
                    }

                    String time = ChatFormatting.GRAY + "Ping: " + ChatFormatting.RESET + l + "ms";
                    FontUtil.drawString(time, res.getScaledWidth() - FontUtil.getStringWidth(time) - 2, y, -1);
                    y -= FontUtil.getHeight() + 1;
                }

                if (fps.getValue()) {
                    String time = ChatFormatting.GRAY + "FPS: " + ChatFormatting.RESET + Minecraft.getDebugFPS();
                    FontUtil.drawString(time, res.getScaledWidth() - FontUtil.getStringWidth(time) - 2, y, -1);
                    y -= FontUtil.getHeight() + 1;
                }

                if (tps.getValue()) {
                    // todo: calc TPS
                    String time = ChatFormatting.GRAY + "TPS: " + ChatFormatting.RESET + "20.0";
                    FontUtil.drawString(time, res.getScaledWidth() - FontUtil.getStringWidth(time) - 2, y, -1);
                    y -= FontUtil.getHeight() + 1;
                }

//                if (coords.getValue()) {
//                    String coordinatesFormat = String.format("\u00a7f%s, %s, %s \u00a77XYZ", (int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ);
//                    FontUtil.drawString(coordinatesFormat, 2, y -= 9, -1);
//                }
//
//                if (direction.getValue()) {
//                    String direction = String.format("\u00a77%s", PlayerHelper.getFacingWithProperCapitals());
//                    FontUtil.drawString(direction, 2, y -= 10, -1);
//                }
//
//                y = scaledResolution.getScaledHeight() - (mc.currentScreen instanceof GuiChat ? 24 : 2);
//
//                if (time.getValue()) {
//                    String time = String.format("\u00a77%s", dateFormat.format(new Date()));
//                    FontUtil.drawString(time, scaledResolution.getScaledWidth() - FontUtil.getStringWidth(time) - 2, y -= 9, -1);
//                }
            }
        });

        Exeter.getInstance().getEventManager().register(new Listener<MoveUpdateEvent>("hud_move_update_listener") {
            @Override
            public void call(MoveUpdateEvent event) {
                double x = mc.player.posX - mc.player.lastTickPosX;
                double z = mc.player.posZ - mc.player.lastTickPosZ;

                moveSpeed = Math.sqrt(x * x + z * z) / 1000.0 / (0.05 / 3600) * (50.0f / mc.timer.tickLength);
            }
        });
    }

    private enum Look {
        DEFAULT,
        LOWER,
        UPPER,
        CUB;

    }

    private enum Organize {
        ABC,
        LENGTH;

    }
}

