package me.friendly.api.minecraft.helper;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;

public final class WorldHelper {
    private static Minecraft mc = Minecraft.getMinecraft();

    public static BlockPos getSpawnPoint() {
        return WorldHelper.mc.world.getSpawnPoint();
    }

    public static Block getBlock(double x, double y, double z) {
        return WorldHelper.mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public static PlaceInfo getPlacement(BlockPos pos, boolean raytrace) {

        if (raytrace) {
            RayTraceResult result = RaycastHelper.raycast(mc.playerController.getBlockReachDistance());
            if (result != null && result.typeOfHit.equals(Type.BLOCK)) {
                return new PlaceInfo(result.getBlockPos(), result.sideHit, result.hitVec);
            }
        } else {
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos n = pos.offset(facing);
                if (!isReplaceable(n)) {
                    return new PlaceInfo(n, facing.getOpposite(), new Vec3d(n).addVector(0.5, 0.5, 0.5));
                }
            }
        }

        return null;
    }

    public static boolean placeAt(BlockPos pos, EnumHand hand, boolean swing) {
        PlaceInfo info = getPlacement(pos, false);
        return info != null && placeAt(info.pos, info.facing, hand, swing);
    }

    public static boolean placeAt(BlockPos pos, EnumFacing facing, EnumHand hand, boolean swing) {
        EnumActionResult result = mc.playerController.processRightClick(mc.player, mc.world, pos, facing, getHitVec(pos), hand);
        if (result.equals(EnumActionResult.SUCCESS)) {
            if (swing) {
                mc.player.swingArm(hand);
            } else {
                mc.player.connection.sendPacket(new CPacketAnimation(hand));
            }
        }

        return result.equals(EnumActionResult.SUCCESS);
    }

    public static boolean placeAtRaycast(EnumHand hand, boolean swing) {
        RayTraceResult result = RaycastHelper.raycast(mc.playerController.getBlockReachDistance());
        if (result != null && result.typeOfHit.equals(Type.BLOCK)) {
            EnumActionResult action = mc.playerController.processRightClick(
                    mc.player, mc.world,
                    result.getBlockPos(),
                    result.sideHit,
                    result.hitVec,
                    hand);

            if (action.equals(EnumActionResult.SUCCESS)) {
                if (swing) {
                    mc.player.swingArm(hand);
                } else {
                    mc.player.connection.sendPacket(new CPacketAnimation(hand));
                }
            }

            return action.equals(EnumActionResult.SUCCESS);
        }

        return false;
    }

    public static Vec3d getHitVec(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static boolean isReplaceable(BlockPos at) {
        return mc.world.getBlockState(at).getMaterial().isReplaceable();
    }

    public static class PlaceInfo {
        public final BlockPos pos;
        public final EnumFacing facing;
        public final Vec3d hitVec;

        public PlaceInfo(BlockPos pos, EnumFacing facing, Vec3d hitVec) {
            this.pos = pos;
            this.facing = facing;
            this.hitVec = hitVec;
        }
    }
}

