package me.friendly.exeter.module.impl.movement;

import me.friendly.api.event.Listener;
import me.friendly.exeter.events.KeybindingIsPressedEvent;
import me.friendly.exeter.events.PacketEvent;
import me.friendly.exeter.events.PlayerUpdateEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.api.properties.EnumProperty;
import me.friendly.api.properties.Property;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;

public class InventoryWalk extends ToggleableModule {
    private final KeyBinding[] BINDS = { mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindSprint, mc.gameSettings.keyBindSneak, mc.gameSettings.keyBindJump  };

    private final EnumProperty<Mode> mode = new EnumProperty<>(Mode.VANILLA, "Mode", "m");
    private final Property<Boolean> rotate = new Property<>(true, "Rotate", "arrowrotate", "rot", "face");

    private boolean clicked = false;

    public InventoryWalk() {
        super("Inventory Walk", new String[]{"inventorywalk", "inventorymove", "invwalk", "invmove"}, ModuleType.MOVEMENT);
        offerProperties(mode);

        listeners.add(new Listener<PlayerUpdateEvent>("inventorywalk_update_listener") {
            @Override
            public void call(PlayerUpdateEvent event) {
                if (inValidGui()) {
                    mc.currentScreen.allowUserInput = true;

                    for (KeyBinding binding : BINDS) {
                        try {
                            KeyBinding.setKeyBindState(binding.getKeyCode(), Keyboard.isKeyDown(binding.getKeyCode()));
                        } catch (Exception ignored) {
                        }
                    }

                    if (rotate.getValue()) {
                        if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                            mc.player.rotationPitch -= 5.0f;
                        } else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                            mc.player.rotationPitch += 5.0f;
                        } else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                            mc.player.rotationYaw -= 5.0f;
                        } else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                            mc.player.rotationYaw += 5.0f;
                        }

                        mc.player.rotationPitch = MathHelper.clamp(mc.player.rotationPitch, -90.0f, 90.0f);
                    }
                }
            }
        });

        listeners.add(new Listener<KeybindingIsPressedEvent>("inventorywalk_bindingpressed_listener") {
            @Override
            public void call(KeybindingIsPressedEvent event) {
                if (inValidGui()) {
                    try {
                        event.pressed = Keyboard.isKeyDown(event.key);
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        listeners.add(new Listener<PacketEvent>("inventorywalk_packet_listener") {
            @Override
            public void call(PacketEvent event) {

                if (mode.getValue().equals(Mode.NCP_STRICT)) {
                    if (event.getPacket() instanceof CPacketClickWindow) {

                        // https://github.com/Updated-NoCheatPlus/NoCheatPlus/blob/master/NCPCore/src/main/java/fr/neatmonster/nocheatplus/checks/inventory/InventoryMove.java#L112-L115
                        if (mc.player.isHandActive()) {
                            mc.player.connection.sendPacketSilent(new CPacketPlayerDigging(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                            mc.player.stopActiveHand();
                        }

                        clicked = true;

                        // https://github.com/Updated-NoCheatPlus/NoCheatPlus/blob/master/NCPCore/src/main/java/fr/neatmonster/nocheatplus/checks/inventory/InventoryMove.java#L81-L83
                        // https://github.com/Updated-NoCheatPlus/NoCheatPlus/blob/master/NCPCore/src/main/java/fr/neatmonster/nocheatplus/checks/inventory/InventoryMove.java#L129-L145
                        if (mc.player.isSprinting()) {
                            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                        }
                    } else if (event.getPacket() instanceof SPacketConfirmTransaction) {
                        if (clicked) {
                            clicked = false;

                            if (mc.player.isSprinting()) {
                                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onDisable() {
        super.onDisable();

        if (clicked) {
            clicked = false;

            if (mc.player.isSprinting()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            }
        }
    }

    private boolean inValidGui() {
        return mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat) && !(mc.currentScreen instanceof GuiRepair);
    }

    public enum Mode {
        VANILLA, NCP_STRICT
    }
}
