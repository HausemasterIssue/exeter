package me.friendly.exeter.module.impl.movement;

import me.friendly.api.event.Listener;
import me.friendly.exeter.events.KeybindingIsPressedEvent;
import me.friendly.exeter.events.PlayerUpdateEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.api.properties.EnumProperty;
import me.friendly.api.properties.Property;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;

public class InventoryWalk extends ToggleableModule {
    private final KeyBinding[] BINDS = { mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindSprint, mc.gameSettings.keyBindSneak, mc.gameSettings.keyBindJump  };

    private final EnumProperty<Mode> mode = new EnumProperty<>(Mode.VANILLA, "Mode", "m");
    private final Property<Boolean> rotate = new Property<>(true, "Rotate", "arrowrotate", "rot", "face");

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
    }

    private boolean inValidGui() {
        return mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat) && !(mc.currentScreen instanceof GuiRepair);
    }

    public enum Mode {
        VANILLA, NCP_STRICT
    }
}
