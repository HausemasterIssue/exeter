package me.friendly.exeter.module.impl.render;

import me.friendly.api.event.Listener;
import me.friendly.api.minecraft.render.RenderMethods;
import me.friendly.exeter.core.Exeter;
import me.friendly.exeter.events.RenderWorldEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Trajectories extends ToggleableModule {
    public Trajectories() {
        super("Trajectories", new String[]{"trajectories"}, ModuleType.RENDER);

        listeners.add(new Listener<RenderWorldEvent>("trajectories_renderworld_lisstener") {
            @Override
            public void call(RenderWorldEvent event) {

                // this is a shit fix
                if (mc.player.ticksExisted < 30) {
                    return;
                }

                ItemStack stack = mc.player.getHeldItemMainhand();
                if (!(stack.getItem() instanceof ItemBow || (stack.getItem() instanceof ItemEnderPearl) || stack.getItem() instanceof ItemSnowball || stack.getItem() instanceof ItemEgg || stack.getItem() instanceof ItemExpBottle || stack.getItem() instanceof ItemSplashPotion || stack.getItem() instanceof ItemLingeringPotion)) {
                    return;
                }

                // EntityThrowable

                float[] server = Exeter.getInstance().getRotationManager().getServerRotation();
                if (server == null) {
                    server = new float[] { mc.player.rotationYaw, mc.player.rotationPitch };
                }

                float yaw = server[0];
                float pitch = server[1];

                double x = mc.player.posX;
                double y = mc.player.posY + mc.player.getEyeHeight() - 0.10000000149011612;
                double z = mc.player.posZ;

                float velocity = 0.0f;
                float inaccuracy = 0.0f;
                float pitchOffset = 0.0f;

                if (stack.getItem() instanceof ItemEnderPearl || stack.getItem() instanceof ItemEgg || stack.getItem() instanceof ItemSnowball) {
                    velocity = 1.5f;
                    inaccuracy = 1.0f;
                } else if (stack.getItem() instanceof ItemExpBottle) {
                    velocity = 0.7f;
                    inaccuracy = 1.0f;
                    pitchOffset = -20.0f;
                } else if (stack.getItem() instanceof ItemLingeringPotion || stack.getItem() instanceof ItemSplashPotion) {
                    velocity = 0.5f;
                    inaccuracy = 1.0f;
                    pitchOffset = -20.0f;
                } else if (stack.getItem() instanceof ItemBow) {
                    int charge = stack.getMaxItemUseDuration() - mc.player.getItemInUseCount();
                    velocity = ItemBow.getArrowVelocity(charge) * 3.0f;
                    inaccuracy = 1.0f;
                }

                //double motionX, motionY, motionZ;
                float magicNumber = 0.017453292f;

                double motionX = -MathHelper.sin(yaw * magicNumber) * MathHelper.cos(pitch * magicNumber);
                double motionY = -MathHelper.sin((pitch + pitchOffset) * magicNumber);
                double motionZ = MathHelper.cos(yaw * magicNumber) * MathHelper.cos(pitch * magicNumber);

                double distance = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);

                motionX /= distance;
                motionY /= distance;
                motionZ /= distance;

                // todo: normal MC uses pseudo random num generation, predict?
                motionX += 0.007499999832361937 * inaccuracy;
                motionY += 0.007499999832361937 * inaccuracy;
                motionZ += 0.007499999832361937 * inaccuracy;

                motionX *= velocity;
                motionY *= velocity;
                motionZ *= velocity;

                boolean landed = false;

                RayTraceResult finalResult = null;
                Entity landedEntity = null;

                double size = 0.25;
                double d0 = 0.0;

                while (!landed) {
                    Vec3d pos = new Vec3d(x, y, z);
                    Vec3d motion = new Vec3d(x + motionX, y + motionY, z + motionZ);

                    RayTraceResult result = mc.world.rayTraceBlocks(pos, motion);
                    if (result != null && !result.typeOfHit.equals(RayTraceResult.Type.MISS)) {
                        landed = true;
                        finalResult = result;
                    }

                    AxisAlignedBB bb = new AxisAlignedBB(
                            x - size, y - size, z - size,
                            x + size, y + size, z + size)
                            .addCoord(motionX, motionY, motionZ)
                            .expandXyz(1.0);

                    List<Entity> entitiesColliding = mc.world.getEntitiesWithinAABB(Entity.class, bb);
                    if (!entitiesColliding.isEmpty()) {
                        for (Entity entity : entitiesColliding) {
                            AxisAlignedBB box = entity.getEntityBoundingBox();
                            if (!entity.canBeCollidedWith() || entity.equals(mc.player)) {
                                continue;
                            }

                            box = box.expandXyz(0.30000001192092896);

                            RayTraceResult r = box.calculateIntercept(pos, motion);
                            if (r != null) {
                                double d = pos.distanceTo(r.hitVec);
                                if (d < d0 || d0 == 0.0) {
                                    d0 = d;

                                    landedEntity = entity;
                                    landed = true;
                                    finalResult = r;
                                }
                            }
                        }
                    }

                    x += motionX;
                    y += motionY;
                    z += motionZ;

                    motionX *= 0.99;
                    motionY *= 0.99;
                    motionZ *= 0.99;

                    if (!mc.player.hasNoGravity()) {
                        if (stack.getItem() instanceof ItemExpBottle) {
                            motionY -= 0.07;
                        } else {
                            motionY -= 0.03;
                        }
                    }
                }

                double renderX = -mc.getRenderManager().renderPosX;
                double renderY = -mc.getRenderManager().renderPosY;
                double renderZ = -mc.getRenderManager().renderPosZ;

                glPushMatrix();
                RenderMethods.enableGL3D();

                glTranslated(renderX, renderY, renderZ);
                RenderMethods.glColor(Colors.getClientColorCustomAlpha(80));

                if (landedEntity != null) {
                    RenderMethods.drawBox(landedEntity.getEntityBoundingBox());
                } else {
                    RenderMethods.drawBox(new AxisAlignedBB(
                            x + 0.5, y, z + 0.5,
                            x - 0.5, y + 0.5, z - 0.5
                    ));
                }

                RenderMethods.disableGL3D();
                glPopMatrix();
            }
        });
    }
}
