package me.friendly.exeter.module.impl.movement;

import me.friendly.api.event.Event;
import me.friendly.api.event.Listener;
import me.friendly.api.minecraft.helper.MovementHelper;
import me.friendly.api.properties.NumberProperty;
import me.friendly.api.properties.Property;
import me.friendly.exeter.events.MoveEvent;
import me.friendly.exeter.events.MoveUpdateEvent;
import me.friendly.exeter.events.PacketEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class LongJump extends ToggleableModule {
    private final NumberProperty<Double> boost = new NumberProperty<>(4.5, 1.0, 10.0, "Boost", "speed", "b");
    private final Property<Boolean> timer = new Property<>(true, "Timer", "slow", "slowdown");
    private final Property<Boolean> accelerate = new Property<>(true, "Accelerate", "accel", "boostup");
    private final Property<Boolean> flagDisable = new Property<>(true, "Flag Disable", "autodisable");

    private double moveSpeed = 0.0;
    private double distance = 0.0;
    private int stage = 4;

    public LongJump() {
        super("Long Jump", new String[]{"longjump", "lj", "farjump"}, ModuleType.MOVEMENT);
        offerProperties(boost, timer, accelerate, flagDisable);

        listeners.add(new Listener<MoveEvent>("longjump_move_listener") {
            @Override
            public void call(MoveEvent event) {
                if (timer.getValue()) {
                    mc.timer.tickLength = 50.0f / 0.8f;
                } else {
                    mc.timer.tickLength = 50.0f;
                }

                if (stage == 1 && MovementHelper.isMoving()) {
                    stage = 2;
                    moveSpeed = boost.getValue() * MovementHelper.baseNCPSpeed() - 0.01;
                } else if (stage == 2) {
                    stage = 3;

                    if (mc.player.onGround && MovementHelper.isMoving()) {
                        mc.player.motionY = 0.42;
                        event.y = 0.42;

                        if (accelerate.getValue()) {
                            moveSpeed *= 2.149;
                        }
                    }
                } else if (stage == 3) {
                    stage = 4;

                    double adjusted = 0.66 * (distance - MovementHelper.baseNCPSpeed());
                    moveSpeed = distance - adjusted;
                } else {
                    List<AxisAlignedBB> boxes = mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0));
                    if ((boxes.size() > 0 || mc.player.isCollidedVertically) && stage > 0) {
                        stage = 1;
                    }

                    moveSpeed -= moveSpeed / 159.0;
                }

                moveSpeed = Math.max(moveSpeed, MovementHelper.baseNCPSpeed());

                if (MovementHelper.isMoving()) {
                    double[] motion = MovementHelper.calcMotion(moveSpeed);

                    event.x = motion[0];
                    event.z = motion[1];
                } else {
                    event.x = 0.0;
                    event.z = 0.0;
                }
            }
        });

        listeners.add(new Listener<MoveUpdateEvent>("longjump_moveupdate_listener") {
            @Override
            public void call(MoveUpdateEvent event) {
                double x = mc.player.posX - mc.player.prevPosX;
                double z = mc.player.posZ - mc.player.prevPosZ;

                distance = Math.sqrt(x * x + z * z);
            }
        });

        listeners.add(new Listener<PacketEvent>("longjump_packet_listener") {
            @Override
            public void call(PacketEvent event) {
                if (event.getPacket() instanceof SPacketPlayerPosLook) {
                    if (flagDisable.getValue()) {
                        setRunning(false);
                    } else {
                        distance = 0.0;
                        moveSpeed = 0.0;
                        stage = 4;

                        mc.timer.tickLength = 50.0f;
                    }
                }
            }
        });
    }

    @Override
    protected void onDisable() {
        super.onDisable();

        mc.timer.tickLength = 50.0f;
        stage = 4;
        moveSpeed = 0.0;
        distance = 0.0;
    }
}
