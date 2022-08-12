package me.friendly.api.minecraft.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;

public class MovementHelper {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static double[] calcMotion(double speed) {
        float f = getMovementYaw();
        double sin = -Math.sin(f);
        double cos = Math.cos(f);

        return new double[] { sin * speed, cos * speed };
    }

    public static float getMovementYaw() {
        float rotationYaw = mc.player.rotationYaw;
        float n = 1.0f;

        if (mc.player.movementInput.moveForward < 0.0f) {
            rotationYaw += 180.0f;
            n = -0.5f;
        } else if (mc.player.movementInput.moveForward > 0.0f) {
            n = 0.5f;
        }

        if (mc.player.movementInput.moveStrafe > 0.0f) {
            rotationYaw -= 90.0f * n;
        }

        if (mc.player.movementInput.moveStrafe < 0.0f) {
            rotationYaw += 90.0f * n;
        }

        return rotationYaw * 0.017453292f;
    }

    public static boolean isMoving() {
        return mc.player.movementInput.moveForward != 0.0f || mc.player.movementInput.moveStrafe != 0.0f;
    }

    public static double baseNCPSpeed() {
        double base = 0.2873;

        if (mc.player != null) {
            if (mc.player.isPotionActive(MobEffects.SPEED)) {
                int amp = mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier();
                base *= 1.0 + 0.2 * (amp + 1);
            }
        }

        return base;
    }

    public static double getJumpheight(boolean low) {
        double motionY = low ? 0.3995 : 0.42;

        if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
            int amp = mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier();
            motionY += (amp + 1.0f) * 0.1f;
        }

        return motionY;
    }
}
