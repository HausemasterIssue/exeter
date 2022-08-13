package me.friendly.exeter.module.impl.world;

import me.friendly.api.event.Listener;
import me.friendly.api.minecraft.helper.RotationHelper;
import me.friendly.exeter.events.MoveUpdateEvent;
import me.friendly.exeter.events.TickEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.api.properties.NumberProperty;
import me.friendly.api.properties.Property;
import net.minecraft.block.BlockBed;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class Fucker extends ToggleableModule {
    private final NumberProperty<Double> range = new NumberProperty<>(4.0, 1.0, 6.0, "Range", "distance", "dist");
    private final Property<Boolean> swing = new Property<>(true, "Swing", "swingarm");
    private final Property<Boolean> rotate = new Property<>(true, "Rotate", "rot", "face");

    private float[] rotations;

    public Fucker() {
        super("Fucker", new String[]{"fucker", "bednuker", "bedfucker"}, ModuleType.WORLD);
        offerProperties(range, swing, rotate);

        listeners.add(new Listener<TickEvent>("fucker_tick_listener") {
            @Override
            public void call(TickEvent event) {

                if (mc.world == null || mc.player == null) {
                    return;
                }

                BlockPos bedPos = getBedPos();
                if (bedPos != null) {

                    if (rotate.getValue()) {
                        rotations = RotationHelper.calcAngleTo(bedPos, EnumFacing.UP);
                    } else {
                        rotations = null;
                    }

                    if (mc.playerController.onPlayerDamageBlock(bedPos, EnumFacing.UP)) {
                        if (swing.getValue()) {
                            mc.player.swingArm(EnumHand.MAIN_HAND);
                        } else {
                            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                        }
                    }
                } else {
                    rotations = null;
                }
            }
        });

        listeners.add(new Listener<MoveUpdateEvent>("fucker_moveupdate_listener") {
            @Override
            public void call(MoveUpdateEvent event) {
                if (rotations != null) {
                    event.yaw = rotations[0];
                    event.pitch = rotations[1];

                    mc.player.rotationYawHead = rotations[0];
                    mc.player.renderYawOffset = rotations[0];
                }
            }
        });
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        rotations = null;
    }

    private BlockPos getBedPos() {
        for (double x = -range.getValue(); x <= range.getValue(); ++x) {
            for (double y = -range.getValue(); y <= range.getValue(); ++y) {
                for (double z = -range.getValue(); z <= range.getValue(); ++z) {
                    BlockPos pos = new BlockPos(mc.player.getPositionVector()).add(x, y, z);

                    if (mc.player.getDistance(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5) < range.getValue()) {
                        if (mc.world.getBlockState(pos).getBlock() instanceof BlockBed) {
                            return pos;
                        }
                    }
                }
            }
        }

        return null;
    }
}
