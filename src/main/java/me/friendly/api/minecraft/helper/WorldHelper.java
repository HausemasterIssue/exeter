package me.friendly.api.minecraft.helper;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class WorldHelper {
    private static Minecraft mc = Minecraft.getMinecraft();

    public static BlockPos getSpawnPoint() {
        return WorldHelper.mc.world.getSpawnPoint();
    }

    public static Block getBlock(double x, double y, double z) {
        return WorldHelper.mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public static boolean placeAt(BlockPos pos, EnumHand hand, boolean swing) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos n = pos.offset(facing);
            if (!isReplaceable(n)) {
                boolean res = placeAt(n, facing.getOpposite(), hand, swing);
                if (res) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean placeAt(BlockPos pos, EnumFacing facing, EnumHand hand, boolean swing) {
        EnumActionResult result = mc.playerController.processRightClick(mc.player, mc.world, pos, facing, getHitVec(pos), hand);
        // Logger.getLogger().printToChat(result.name());
        if (result.equals(EnumActionResult.SUCCESS)) {
            if (swing) {
                mc.player.swingArm(hand);
            } else {
                mc.player.connection.sendPacket(new CPacketAnimation(hand));
            }
        }

        return result.equals(EnumActionResult.SUCCESS);
    }

    public static Vec3d getHitVec(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static boolean isReplaceable(BlockPos at) {
        return mc.world.getBlockState(at).getMaterial().isReplaceable();
    }
}

