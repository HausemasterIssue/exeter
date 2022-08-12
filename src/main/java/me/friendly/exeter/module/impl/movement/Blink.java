package me.friendly.exeter.module.impl.movement;

import me.friendly.api.event.Listener;
import me.friendly.api.minecraft.helper.MovementHelper;
import me.friendly.api.minecraft.render.RenderMethods;
import me.friendly.exeter.events.PacketEvent;
import me.friendly.exeter.events.RenderWorldEvent;
import me.friendly.exeter.events.TickEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.exeter.module.impl.render.Colors;
import me.friendly.exeter.properties.Property;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.lwjgl.opengl.GL11.*;

public class Blink extends ToggleableModule {
    private final Property<Boolean> render = new Property<>(true, "Render", "draw", "lines", "trails");

    private final Queue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    private final List<Vec3d> movements = new CopyOnWriteArrayList<>();

    public Blink() {
        super("Blink", new String[]{"blink", "fakelag", "packetholder"}, ModuleType.MISCELLANEOUS);
        offerProperties(render);

        listeners.add(new Listener<PacketEvent>("blink_packet_listener") {
            @Override
            public void call(PacketEvent event) {
                if (event.getPacket() instanceof CPacketPlayer) {
                    packets.add(event.getPacket());
                    event.setCanceled(true);
                }
            }
        });

        listeners.add(new Listener<TickEvent>("blink_tick_listener") {
            @Override
            public void call(TickEvent event) {
                if (render.getValue()) {
                    movements.add(new Vec3d(mc.player.prevPosX, mc.player.prevPosY, mc.player.prevPosZ));
                    movements.add(mc.player.getPositionVector());
                } else {
                    movements.clear();
                }
            }
        });

        listeners.add(new Listener<RenderWorldEvent>("blink_renderworld_listener") {
            @Override
            public void call(RenderWorldEvent event) {

                if (render.getValue()) {
                    glPushMatrix();

                    glEnable(GL_BLEND);
                    glBlendFunc(770, 771);
                    GlStateManager.disableLighting();
                    GlStateManager.disableDepth();

                    glTranslated(-mc.getRenderManager().renderPosX, -mc.getRenderManager().renderPosY, -mc.getRenderManager().renderPosZ);

                    RenderMethods.glColor(Colors.getClientColor());
                    GlStateManager.glLineWidth(1.5f);

                    glBegin(GL_LINES);
                    movements.forEach((m) -> glVertex3d(m.xCoord, m.yCoord, m.zCoord));
                    glEnd();

                    RenderMethods.glColor(0xFFFFFFFF);

                    GlStateManager.enableDepth();
                    GlStateManager.enableLighting();
                    glEnable(GL_TEXTURE_2D);
                    glDisable(GL_BLEND);
                    glPopMatrix();
                }
            }
        });
    }

    @Override
    protected void onDisable() {
        super.onDisable();

        while (!packets.isEmpty()) {
            mc.player.connection.sendPacketSilent(packets.poll());
        }
    }
}
