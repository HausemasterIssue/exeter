package me.friendly.exeter.module.impl.render;

import me.friendly.api.event.Listener;
import me.friendly.api.properties.NumberProperty;
import me.friendly.exeter.events.LightValueEvent;
import me.friendly.exeter.events.PutColorModifierEvent;
import me.friendly.exeter.events.RenderBlockLayerEvent;
import me.friendly.exeter.events.ShouldRenderSideEvent;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;

import java.util.HashSet;
import java.util.Set;

public class Xray extends ToggleableModule {
    public static Xray INSTANCE;
    public static final Set<Block> BLOCKS = new HashSet<>();

    private final NumberProperty<Integer> opacity = new NumberProperty<>(120, 0, 255, "Opacity", "alpha");

    public Xray() {
        super("Xray", new String[]{"xray", "wallhack"}, ModuleType.RENDER);

        BLOCKS.add(Blocks.OBSIDIAN);
        BLOCKS.add(Blocks.PORTAL);
        BLOCKS.add(Blocks.END_PORTAL_FRAME);
        BLOCKS.add(Blocks.END_PORTAL);
        BLOCKS.add(Blocks.COAL_ORE);
        BLOCKS.add(Blocks.IRON_ORE);
        BLOCKS.add(Blocks.REDSTONE_ORE);
        BLOCKS.add(Blocks.EMERALD_ORE);
        BLOCKS.add(Blocks.QUARTZ_ORE);
        BLOCKS.add(Blocks.LAPIS_ORE);
        BLOCKS.add(Blocks.DIAMOND_ORE);
        BLOCKS.add(Blocks.BEACON);
        BLOCKS.add(Blocks.FURNACE);
        BLOCKS.add(Blocks.LIT_FURNACE);
        BLOCKS.add(Blocks.CRAFTING_TABLE);
        BLOCKS.add(Blocks.CHEST);
        BLOCKS.add(Blocks.TRAPPED_CHEST);
        BLOCKS.add(Blocks.ENDER_CHEST);
        BLOCKS.add(Blocks.COMMAND_BLOCK);
        BLOCKS.add(Blocks.CHAIN_COMMAND_BLOCK);
        BLOCKS.add(Blocks.REPEATING_COMMAND_BLOCK);

        INSTANCE = this;

        listeners.add(new Listener<PutColorModifierEvent>("xray_putcolormod_listener") {
            @Override
            public void call(PutColorModifierEvent event) {
                event.j = event.j & 0x00ffffff | opacity.getValue() << 24;
                event.setCanceled(true);
            }
        });

        listeners.add(new Listener<RenderBlockLayerEvent>("xray_renderblocklayer_listener") {
            @Override
            public void call(RenderBlockLayerEvent event) {
                event.setLayer(BLOCKS.contains(event.getBlock()) ? BlockRenderLayer.SOLID : BlockRenderLayer.TRANSLUCENT);
                event.setCanceled(true);
            }
        });

        listeners.add(new Listener<ShouldRenderSideEvent>("xray_renderside_listener") {
            @Override
            public void call(ShouldRenderSideEvent event) {
                if (BLOCKS.contains(event.getBlock())) {
                    event.setCanceled(true);
                }
            }
        });

        listeners.add(new Listener<LightValueEvent>("xray_lightvalue_listener") {
            @Override
            public void call(LightValueEvent event) {
                event.light = 100;
                event.setCanceled(true);
            }
        });
    }

    @Override
    protected void onEnable() {
        super.onEnable();

        if (mc.world != null) {
            mc.renderGlobal.loadRenderers();
        } else {
            setRunning(false);
        }
    }

    @Override
    protected void onDisable() {
        super.onDisable();

        if (mc.world != null) {
            mc.renderGlobal.loadRenderers();
        }
    }
}
