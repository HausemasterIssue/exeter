package me.friendly.api.minecraft.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationHelper {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static float[] calcAngleTo(Entity entity) {
        Vec3d diff = entity.getPositionEyes(1.0f).subtract(mc.player.getPositionEyes(1.0f));
        float dist = MathHelper.sqrt(diff.xCoord * diff.xCoord + diff.zCoord * diff.zCoord);

        float yaw = (float) (Math.toDegrees(Math.atan2(diff.zCoord, diff.xCoord)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(diff.yCoord, dist)));

        yaw = MathHelper.wrapDegrees(yaw);

        return new float[] { yaw, pitch };
    }

    public static float[] calcAngleTo(BlockPos pos, EnumFacing facing) {

        double x = interpolate((float) mc.player.posX, (float) mc.player.lastTickPosX);
        double y = interpolate((float) mc.player.posY, (float) mc.player.lastTickPosY);
        double z = interpolate((float) mc.player.posZ, (float) mc.player.lastTickPosZ);

        Vec3d v = new Vec3d(pos.getX() + 0.5 - x + facing.getFrontOffsetX() / 2.0,
                pos.getY() + 0.5,
                pos.getZ() + 0.5 - z + facing.getFrontOffsetZ() / 2.0);

        float dist = MathHelper.sqrt(v.xCoord * v.xCoord + v.zCoord * v.zCoord);

        float yaw = (float) (Math.atan2(v.zCoord, v.xCoord) * 180.0 / Math.PI - 90.0);
        float pitch = (float) (Math.atan2(y + mc.player.getEyeHeight() - v.yCoord, dist) * 180.0 / Math.PI);

        if (yaw < 0.0f) {
            yaw += 360.0f;
        }

        return new float[] { yaw, pitch };
    }

    public static float interpolate(float start, float end) {
        float partialTicks = mc.timer.elapsedTicks;
        return partialTicks == 1.0f ? start : end + (start - end) * partialTicks;
    }
}
