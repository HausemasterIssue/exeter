package me.friendly.exeter.module.impl.combat;

import me.friendly.api.event.Listener;
import me.friendly.api.minecraft.helper.RotationHelper;
import me.friendly.api.stopwatch.Stopwatch;
import me.friendly.exeter.core.Exeter;
import me.friendly.exeter.events.MoveUpdateEvent;
import me.friendly.exeter.events.MoveUpdateEvent.Era;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.api.properties.EnumProperty;
import me.friendly.api.properties.NumberProperty;
import me.friendly.api.properties.Property;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;

import java.util.Comparator;
import java.util.Random;

public class Aura extends ToggleableModule {
    private static final Random RNG = new Random();

    private final EnumProperty<Mode> mode = new EnumProperty<>(Mode.SWITCH, "Mode", "m");
    private final EnumProperty<Priority> priority = new EnumProperty<>(Priority.DISTANCE, "Priority", "sort", "order");
    private final EnumProperty<Era> era = new EnumProperty<>(Era.PRE, "Era", "attacktiming");

    private final Property<Boolean> rotate = new Property<>(true, "Rotate", "rot", "face");
    private final Property<Boolean> raytrace = new Property<>(false, "Raytrace", "raycast", "legit");

    private final NumberProperty<Double> range = new NumberProperty<>(4.5, 3.0, 6.0, "Range", "hitrange", "distance");
    private final NumberProperty<Double> wallRange = new NumberProperty<>(3.0, 3.0, 6.0, "Wall Range", "wallhitrange", "walldistance");

    private final Property<Boolean> hitDelay = new Property<>(true, "Hit Delay", "1.9", "hitdelay");
    private final Property<Boolean> fullCharge = new Property<>(true, "Charged Attack", "chargedattack", "extradamage", "wait");
    private final Property<Boolean> keepSprint = new Property<>(false, "Keep Sprint", "keepsprint", "sprintattack");

    private final NumberProperty<Integer> min = new NumberProperty<>(8, 1, 20, "Min", "minaps", "mincps");
    private final NumberProperty<Integer> max = new NumberProperty<>(12, 2, 20, "Max", "maxaps", "maxcps");
    private final NumberProperty<Integer> deviation = new NumberProperty<>(8, 0, 12, "Deviation", "random");

    private final Stopwatch stopwatch = new Stopwatch();
    private EntityLivingBase target = null;

    public Aura() {
        super("Aura", new String[]{"aura", "killaura", "forcefield", "ka"}, ModuleType.COMBAT);
        offerProperties(mode, priority, era, rotate, raytrace, range, wallRange, hitDelay, fullCharge, keepSprint, min, max, deviation);

        listeners.add(new Listener<MoveUpdateEvent>("aura_moveupdate_listener") {
            @Override
            public void call(MoveUpdateEvent event) {
                if (!isValidTarget(target) || mode.getValue().equals(Mode.SWITCH)) {
                    target = (EntityLivingBase) mc.world.loadedEntityList
                            .stream()
                            .filter((e) -> e instanceof EntityLivingBase && isValidTarget((EntityLivingBase) e))
                            .min(Comparator.comparingDouble((e) -> {
                                double val = 0.0;

                                switch (priority.getValue()) {
                                    case HEALTH:
                                        val = ((EntityLivingBase) e).getHealth();
                                        break;

                                    case DISTANCE:
                                        val = mc.player.getDistanceSqToEntity(e);
                                        break;
                                }

                                return val;
                            })).orElse(null);
                }

                if (target == null) {
                    return;
                }

                if (rotate.getValue()) {
                    Exeter.getInstance().getRotationManager().setRotation(RotationHelper.calcAngleTo(target));
                }

                if (event.era.equals(era.getValue())) {
                    if (hitDelay.getValue()) {

                        boolean attack = false;

                        float str = mc.player.getCooledAttackStrength(0.5f);
                        if (fullCharge.getValue()) {
                            // todo: doesn't work how i wanted it (i want it to attack when the + is under the attack indicator)
                            attack = str >= 1.0f && mc.player.getCooldownPeriod() > 5.0f;
                        } else {
                            attack = str >= 1.0f;
                        }

                        attack &= target.isEntityAlive();

                        if (attack) {
                            attack();
                        }
                    } else {

                        if (min.getValue() > max.getValue()) {
                            min.setValue(max.getValue() - 1);
                        }

                        int cps = random(min.getValue(), max.getValue());
                        if (deviation.getValue() != 0) {
                            cps = cps + random(0, deviation.getValue()) - random(0, deviation.getValue());
                        }

                        cps = Math.max(cps, 1);

                        if (stopwatch.hasCompleted(1000L / cps)) {
                            stopwatch.reset();
                            attack();
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        target = null;
        stopwatch.reset();
    }

    private int random(int min, int max) {
        return RNG.nextInt((max + 1) - min) + min;
    }

    private void attack() {
        if (target != null) {

            if (keepSprint.getValue()) {
                mc.player.connection.sendPacket(new CPacketUseEntity(target));
            } else {
                mc.playerController.attackEntity(mc.player, target);
            }

            mc.player.swingArm(EnumHand.MAIN_HAND);
        }
    }

    private boolean isValidTarget(EntityLivingBase base) {
        if (base == null || base.isDead || base.equals(mc.player) || Exeter.getInstance().getFriendManager().isFriend(base.getName())) {
            return false;
        }

        if (base instanceof EntityArmorStand) {
            return false;
        }

        double r = mc.player.canEntityBeSeen(base) ? range.getValue() * range.getValue() : wallRange.getValue() * wallRange.getValue();
        if (r < mc.player.getDistanceSqToEntity(base)) {
            return false;
        }

        // todo: raytracing
        if (raytrace.getValue()) {

        }

        return true;
    }

    public enum Mode {
        SINGLE, SWITCH
    }

    public enum Priority {
        DISTANCE, HEALTH
    }
}
