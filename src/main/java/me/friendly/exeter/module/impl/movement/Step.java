package me.friendly.exeter.module.impl.movement;

import me.friendly.api.event.Listener;
import me.friendly.exeter.events.StepEvent;
import me.friendly.exeter.events.TickEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.api.properties.EnumProperty;
import me.friendly.api.properties.NumberProperty;
import net.minecraft.network.play.client.CPacketPlayer.Position;

import java.util.HashMap;
import java.util.Map;

public class Step extends ToggleableModule {
    private static final Map<Double, double[]> HEIGHTS = new HashMap<Double, double[]>() {{
        put(0.75, new double[]{0.39, 0.753, 0.75});
        put(0.8125, new double[]{0.39, 0.7, 0.8125});
        put(0.875, new double[]{0.39, 0.7, 0.875});
        put(1.0, new double[]{0.42, 0.753, 1.0});
        put(1.5, new double[]{0.42, 0.75, 1.0, 1.16, 1.23, 1.2, 1.5});
        put(2.0, new double[]{0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43});
        put(2.5, new double[]{0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907});
    }};

    private final EnumProperty<Mode> mode = new EnumProperty<>(Mode.NCP, "Mode", "m");
    private final NumberProperty<Double> height = new NumberProperty<>(1.5, 0.7, 2.5, "Height", "h", "stepheight");

    private boolean timer = false;

    public Step() {
        super("Step", new String[]{"step"}, ModuleType.MOVEMENT);
        offerProperties(mode, height);

        listeners.add(new Listener<StepEvent>("step_step_listener") {
            @Override
            public void call(StepEvent event) {
                setTag(mode.getFixedValue());

                if (mode.getValue().equals(Mode.NCP)) {
                    double h = mc.player.getEntityBoundingBox().minY - mc.player.posY;

                    if (h <= 0.0 || h > height.getValue()) {
                        return;
                    }

                    double[] v = HEIGHTS.get(h);
                    if (v == null) {
                        return;
                    }

                    timer = true;
                    mc.timer.tickLength = 50.0f / (1.0f / (v.length + 1));

                    for (double o : v) {
                        mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY + o, mc.player.posZ, false));
                    }
                }
            }
        });

        listeners.add(new Listener<TickEvent>("step_tick_listener") {
            @Override
            public void call(TickEvent event) {
                if (timer && mc.player.onGround) {
                    mc.timer.tickLength = 50.0f;
                    timer = false;
                }

                mc.player.stepHeight = height.getValue().floatValue();
            }
        });
    }

    @Override
    protected void onDisable() {
        super.onDisable();

        mc.timer.tickLength = 50.0f;
        timer = false;
        mc.player.stepHeight = 0.6f;
    }

    public enum Mode {
        NCP, VANILLA
    }
}
