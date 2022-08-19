package me.friendly.exeter.module.impl.render;

import me.friendly.api.event.Event;
import me.friendly.api.event.Listener;
import me.friendly.exeter.events.ParticleRenderEvent;
import me.friendly.exeter.events.RenderGameOverlayEvent;
import me.friendly.exeter.events.SpawnExplosionParticleEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import me.friendly.api.properties.Property;

public class NoRender extends ToggleableModule {
    private final Property<Boolean> hurtCam = new Property<>(true, "Hurtcam", "hurtcamera");
    private final Property<Boolean> fire = new Property<>(true, "Fire", "fireoverlay");
    private final Property<Boolean> pumpkin = new Property<>(true, "Pumpkin", "pumpkins", "pumpkinoverlay");
    private final Property<Boolean> pops = new Property<>(false, "Totem Pops", "totempops", "pops");
    private final Property<Boolean> particles = new Property<>(false, "Particles");
    private final Property<Boolean> explosions = new Property<>(true, "Explosions");

    public NoRender() {
        super("No Render", new String[]{"norender", "antirender", "rendertweaks"}, ModuleType.RENDER);
        offerProperties(hurtCam, fire, pumpkin, pops, particles, explosions);

        listeners.add(new Listener<RenderGameOverlayEvent>("norender_rendergameoverlay_listener") {
            @Override
            public void call(RenderGameOverlayEvent event) {
                switch (event.getType()) {
                    case FIRE:
                        event.setRenderFire(!fire.getValue());
                        break;

                    case ITEM:
                        event.setRenderItems(!pops.getValue());
                        break;

                    case HURTCAM:
                        event.setRenderHurtcam(!hurtCam.getValue());
                        break;

                    case PUMPKIN:
                        event.setRenderHurtcam(!pumpkin.getValue());
                        break;
                }
            }
        });

        listeners.add(new Listener<ParticleRenderEvent>("norender_particle_render_listener") {
            @Override
            public void call(ParticleRenderEvent event) {
                event.setCanceled(particles.getValue());
            }
        });

        listeners.add(new Listener<SpawnExplosionParticleEvent>("norender_sep_listener") {
            @Override
            public void call(SpawnExplosionParticleEvent event) {
                event.setCanceled(explosions.getValue());
            }
        });
    }
}
