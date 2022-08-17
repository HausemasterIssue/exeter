package me.friendly.exeter.module.impl.world;

import com.mojang.authlib.GameProfile;
import me.friendly.api.event.Listener;
import me.friendly.api.properties.NumberProperty;
import me.friendly.api.properties.Property;
import me.friendly.exeter.events.*;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.world.World;

// a lil bit of kami-blue
public class FreeCam extends ToggleableModule {
    private final NumberProperty<Double> speed = new NumberProperty<>(0.5, 0.1, 2.0, "Speed", "flyspeed", "movespeed");
    private final Property<Boolean> noCaveCulling = new Property<>(false, "No Cave Culling", "nocaveculling", "noculling");

    private CameraGuy cameraGuy;

    public FreeCam() {
        super("Free Camera", new String[]{"freecam", "freecamera", "spectator"}, ModuleType.WORLD);
        offerProperties(speed, noCaveCulling);

        listeners.add(new Listener<CaveCullingEvent>("freecam_caveculling_listener") {
            @Override
            public void call(CaveCullingEvent event) {
                event.setCanceled(!noCaveCulling.getValue());
            }
        });

        listeners.add(new Listener<InputUpdateEvent>("freecam_inputupdate_listener") {
            @Override
            public void call(InputUpdateEvent event) {
                if (event.getMovementInput() instanceof MovementInputFromOptions) {

                    // reset states
                    event.getMovementInput().moveForward = 0.0f;
                    event.getMovementInput().moveStrafe = 0.0f;
                    event.getMovementInput().jump = false;

                    event.getMovementInput().forwardKeyDown = false;
                    event.getMovementInput().backKeyDown = false;
                    event.getMovementInput().leftKeyDown = false;
                    event.getMovementInput().rightKeyDown = false;

                    // fuck off!
                    event.setCanceled(cameraGuy != null);
                }
            }
        });

        listeners.add(new Listener<PlayerTurnEvent>("freecam_playerturn_listener") {
            @Override
            public void call(PlayerTurnEvent event) {
                if (cameraGuy != null) {
                    cameraGuy.setAngles(event.getYaw(), event.getPitch());
                    event.setCanceled(true);
                }
            }
        });

        listeners.add(new Listener<PacketEvent>("freecam_packet_listener") {
            @Override
            public void call(PacketEvent event) {
                if (event.getPacket() instanceof CPacketUseEntity) {
                    CPacketUseEntity packet = event.getPacket();
                    if (mc.player.equals(packet.getEntityFromWorld(mc.world))) {
                        event.setCanceled(true);
                    }
                }
            }
        });

        listeners.add(new Listener<TickEvent>("freecam_tick_listener") {
            @Override
            public void call(TickEvent event) {
                if (mc.world == null || mc.player == null) {
                    setRunning(false);
                    return;
                }

                if (cameraGuy == null) {

                    if (mc.player.ticksExisted > 5) {
                        cameraGuy = new CameraGuy(mc.world, mc.player.getGameProfile());
                        mc.world.spawnEntityInWorld(cameraGuy);
                        mc.setRenderViewEntity(cameraGuy);
                    }
                } else {

                    if (mc.player.isDead || mc.player.getHealth() <= 0.0f) {
                        if (cameraGuy != null) {
                            mc.world.removeEntity(cameraGuy);
                            mc.world.removeEntityDangerously(cameraGuy);

                            mc.setRenderViewEntity(mc.player);

                            cameraGuy = null;
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onDisable() {
        super.onDisable();

        if (cameraGuy != null) {
            mc.world.removeEntity(cameraGuy);
            mc.world.removeEntityDangerously(cameraGuy);

            mc.setRenderViewEntity(mc.player);
        }

        cameraGuy = null;
    }

    private class CameraGuy extends EntityOtherPlayerMP {
        private final Minecraft mc = Minecraft.getMinecraft();

        public CameraGuy(World worldIn, GameProfile gameProfileIn) {
            super(worldIn, gameProfileIn);
            copyLocationAndAnglesFrom(mc.player);

            capabilities.isFlying = true;
            capabilities.allowFlying = true;
        }

        @Override
        public void onLivingUpdate() {
            inventory.copyInventory(mc.player.inventory);
            updateEntityActionState();

            if (mc.gameSettings.keyBindForward.isKeyDown()) {
                moveForward = 1.0f;
            } else if (mc.gameSettings.keyBindBack.isKeyDown()) {
                moveForward = -1.0f;
            } else {
                moveForward = 0.0f;
            }

            if (mc.gameSettings.keyBindRight.isKeyDown()) {
                moveStrafing = -1.0f;
            } else if (mc.gameSettings.keyBindLeft.isKeyDown()) {
                moveStrafing = 1.0f;
            } else {
                moveStrafing = 0.0f;
            }

            if (!isMoving()) {
                motionX = 0.0;
                motionZ = 0.0;
            } else {

                double[] strafe = calcMotion(speed.getValue());

                motionX = strafe[0];
                motionZ = strafe[1];
            }

            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                motionY = speed.getValue();
            } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                motionY = -speed.getValue();
            } else {
                motionY = 0.0;
            }

            moveEntity(MoverType.SELF, motionX, motionY, motionZ);
        }

        private boolean isMoving() {
            return moveForward != 0.0f || moveStrafing != 0.0f;
        }

        private double[] calcMotion(double speed) {
            float f = getMovementYaw();
            double sin = -Math.sin(f);
            double cos = Math.cos(f);

            return new double[] { sin * speed, cos * speed };
        }

        private float getMovementYaw() {
            float yaw = rotationYaw;
            float n = 1.0f;

            if (moveForward < 0.0f) {
                yaw += 180.0f;
                n = -0.5f;
            } else if (moveForward > 0.0f) {
                n = 0.5f;
            }

            if (moveStrafing > 0.0f) {
                yaw -= 90.0f * n;
            }

            if (moveStrafing < 0.0f) {
                yaw += 90.0f * n;
            }

            return yaw * 0.017453292f;
        }

        @Override
        public float getEyeHeight() {
            return 1.65f;
        }

        @Override
        public boolean isSpectator() {
            return true;
        }

        @Override
        protected boolean isMovementBlocked() {
            return false;
        }

        @Override
        public boolean isInvisible() {
            return true;
        }

        @Override
        public boolean isInvisibleToPlayer(EntityPlayer player) {
            return true;
        }
    }
}
