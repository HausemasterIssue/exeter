package me.friendly.exeter.module.impl.render;

import me.friendly.api.event.Listener;
import me.friendly.api.properties.Property;
import me.friendly.exeter.events.CaveCullingEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;

public class CameraClip extends ToggleableModule {
    public static CameraClip INSTANCE;

    private final Property<Boolean> noCaveCulling = new Property<>(false, "No Cave Culling", "nocaveculling", "noculling");

    public CameraClip() {
        super("Camera Clip", new String[]{"cameraclip", "viewclip"}, ModuleType.RENDER);
        offerProperties(noCaveCulling);

        listeners.add(new Listener<CaveCullingEvent>("cameraclip_caveculling_listener") {
            @Override
            public void call(CaveCullingEvent event) {
                event.setCanceled(noCaveCulling.getValue());
            }
        });

        INSTANCE = this;
    }
}
