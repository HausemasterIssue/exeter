package me.friendly.exeter.module.impl.movement;

import me.friendly.api.event.Listener;
import me.friendly.api.minecraft.helper.MovementHelper;
import me.friendly.exeter.events.MoveEvent;
import me.friendly.exeter.events.MoveUpdateEvent;
import me.friendly.exeter.events.PacketEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.api.properties.EnumProperty;
import me.friendly.api.properties.Property;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class Speed extends ToggleableModule {
    private final EnumProperty<Mode> mode = new EnumProperty<>(Mode.NCP_HOP, "Mode", "m");
    private final Property<Boolean> timer = new Property<>(true, "Use Timer", "timer", "timerboost");

    private int stage = 4;
    private double distanceTraveled = 0.0;
    private double moveSpeed = 0.0;
    private int lag = 0;
    private boolean boost = false;

    public Speed() {
        super("Speed", new String[]{"speed", "vroomvroom", "fastwalk"}, ModuleType.MOVEMENT);
        offerProperties(mode, timer);

        listeners.add(new Listener<MoveEvent>("speed_move_listener") {
            @Override
            public void call(MoveEvent event) {
                switch (mode.getValue()) {
                    case STRICT_HOP: {
                        setTag("Strict Strafe");
                        break;
                    }

                    case NCP_HOP: {
                        setTag("Strafe");
                        break;
                    }

                    case YPORT: {
                        setTag("YPort");
                        break;
                    }

                    case ONGROUND:
                        setTag("On Ground");
                        break;
                }

                --lag;
                if (lag > 0) {
                    return;
                }

                if (mode.getValue().equals(Mode.NCP_HOP) || mode.getValue().equals(Mode.STRICT_HOP)) {

                    if (mc.player.onGround && MovementHelper.isMoving()) {
                        stage = 2;
                    }

                    if (stage == 1 && MovementHelper.isMoving()) {
                        stage = 2;

                        double multiplier = mode.getValue().equals(Mode.NCP_HOP) ? 1.37 : 1.35;
                        moveSpeed = multiplier * MovementHelper.baseNCPSpeed() - 0.01;
                    } else if (stage == 2) {
                        stage = 3;

                        if (mc.player.onGround && MovementHelper.isMoving()) {
                            double motionY = MovementHelper.getJumpheight(true);

                            mc.player.motionY = motionY;
                            event.y = motionY;

                            mc.timer.tickLength = 50.0f;

                            if (timer.getValue()) {
                                float speed = 1.075f;

                                // todo: make ncp strafe more reliable
                                if (mode.getValue().equals(Mode.NCP_HOP)) {
                                    speed = boost ? 1.094f : 1.075f;
                                } else if (mode.getValue().equals(Mode.STRICT_HOP)) {
                                    speed = boost ? 1.089f : 1.075f;
                                }

                                mc.timer.tickLength = 50.0f / speed;
                            } else {
                                mc.timer.tickLength = 50.0f;
                            }

                            if (mode.getValue().equals(Mode.NCP_HOP)) {
                                moveSpeed *= boost ? 1.621 : 1.5885;
                            } else if (mode.getValue().equals(Mode.STRICT_HOP)) {
                                moveSpeed *= boost ? 1.424 : 1.355;
                            }
                        }
                    } else if (stage == 3) {
                        stage = 4;

                        double d = 0.66;
                        if (mode.getValue().equals(Mode.NCP_HOP)) {
                            d = boost ? 0.66 : 0.72;
                        }

                        double adjusted = d * (distanceTraveled - MovementHelper.baseNCPSpeed());
                        moveSpeed = distanceTraveled - adjusted;

                        boost = !boost;
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
            }
        });

        listeners.add(new Listener<MoveUpdateEvent>("speed_moveupdate_listener") {
            @Override
            public void call(MoveUpdateEvent event) {
                double x = mc.player.posX - mc.player.prevPosX;
                double z = mc.player.posZ - mc.player.prevPosZ;

                distanceTraveled = Math.sqrt(x * x + z * z);
            }
        });

        listeners.add(new Listener<PacketEvent>("speed_packet_listener") {
            @Override
            public void call(PacketEvent event) {
                if (event.getPacket() instanceof SPacketPlayerPosLook) {
                    lag = 15;
                    stage = 4;
                    moveSpeed = 0.0;
                    distanceTraveled = 0.0;
                    boost = false;

                    mc.timer.tickLength = 50.0f;
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
        distanceTraveled = 0.0;
        lag = 0;
        boost = false;
    }

    public enum Mode {
        NCP_HOP, STRICT_HOP, YPORT, ONGROUND
    }
}
