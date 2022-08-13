package me.friendly.api.minecraft.helper;

import me.friendly.exeter.core.Exeter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;

public class RaycastHelper {
    public static final Minecraft mc = Minecraft.getMinecraft();

    public static RayTraceResult raycast(double distance) {
        Vec3d rotation = getRotationVec();
        Vec3d eyes = mc.player.getPositionEyes(1.0f);

        return mc.world.rayTraceBlocks(
                eyes, eyes.addVector(rotation.xCoord * distance, rotation.yCoord * distance, rotation.zCoord * distance),
                false, false, true
        );
    }

    public static boolean canSeeBlockFace(RayTraceResult result, BlockPos pos, EnumFacing facing) {
        if (result == null || !result.typeOfHit.equals(Type.BLOCK)) {
            return false;
        }

        return pos.equals(result.getBlockPos()) && facing.equals(result.sideHit);
    }

    public static boolean canSeeBlockFace(BlockPos pos, EnumFacing facing) {
        RayTraceResult result = raycast(mc.playerController.getBlockReachDistance());
        return canSeeBlockFace(result, pos, facing);
    }

    public static Vec3d getRotationVec() {
        float[] serverRotations = Exeter.getInstance().getRotationManager().getServerRotation();
        return mc.player.getVectorForRotation(serverRotations[1], serverRotations[0]);
    }
}
