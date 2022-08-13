package me.friendly.exeter.module.impl.combat;

import me.friendly.api.event.Listener;
import me.friendly.api.minecraft.helper.WorldHelper;
import me.friendly.api.minecraft.helper.WorldHelper.PlaceInfo;
import me.friendly.api.properties.NumberProperty;
import me.friendly.api.properties.Property;
import me.friendly.exeter.core.Exeter;
import me.friendly.exeter.events.MoveUpdateEvent;
import me.friendly.exeter.events.MoveUpdateEvent.Era;
import me.friendly.exeter.module.ModuleType;
import me.friendly.exeter.module.ToggleableModule;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class SelfFill extends ToggleableModule {
    private final Property<Boolean> rotate = new Property<>(true, "Rotate", "rot", "face");
    private final Property<Boolean> strict = new Property<>(false, "Strict", "ncpstrict", "2b2t");
    private final NumberProperty<Double> flag = new NumberProperty<>(5.0, -100.0, 100.0, "Flag", "offset", "up");

    public SelfFill() {
        super("Self Fill", new String[]{"selffill", "burrow", "blocklag"}, ModuleType.COMBAT);
        offerProperties(rotate, strict, flag);

        listeners.add(new Listener<MoveUpdateEvent>("selffill_moveupdate_listener") {
            @Override
            public void call(MoveUpdateEvent event) {
                int slot = -1;
                for (int i = 0; i < 9; ++i) {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);

                    // func_190926_b = isEmpty() - thanks MCP mappings
                    if (stack.func_190926_b() || !(stack.getItem() instanceof ItemBlock)) {
                        continue;
                    }

                    Block block = ((ItemBlock) stack.getItem()).getBlock();
                    if (block.equals(Blocks.OBSIDIAN) || block.equals(Blocks.ENDER_CHEST)) {
                        slot = i;
                        break;
                    }
                }

                if (slot == -1) {
                    setRunning(false);
                    return;
                }

                // check if we are inside a block already, or we're not on ground
                if (mc.player.isEntityInsideOpaqueBlock() || !mc.player.onGround) {
                    setRunning(false);
                    return;
                }

                BlockPos playerPos = new BlockPos(mc.player.getPositionVector());

                // no headspace available
                if (!WorldHelper.isReplaceable(playerPos.add(0.0, mc.player.height + 0.25, 0.0))) {
                    setRunning(false);
                    return;
                }

                if (rotate.getValue()) {
                    Exeter.getInstance().getRotationManager().setRotation(mc.player.rotationYaw, 90.0f);

                    event.yaw = mc.player.rotationYaw;
                    event.pitch = 90.0f;
                }

                PlaceInfo info = WorldHelper.getPlacement(playerPos, strict.getValue());
                if (info == null) {
                    setRunning(false);
                    return;
                }

                if (event.era.equals(Era.POST)) {

                    mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));

                    mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY + 0.42, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY + 0.75, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY + 1.01, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY + 1.16, mc.player.posZ, false));

                    mc.player.connection.sendPacket(new Rotation(event.yaw, event.pitch, false));

                    float facingX = (float) (info.hitVec.xCoord - (double) info.pos.getX());
                    float facingY = (float) (info.hitVec.yCoord - (double) info.pos.getY());
                    float facingZ = (float) (info.hitVec.zCoord - (double) info.pos.getZ());

                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(info.pos, info.facing, EnumHand.MAIN_HAND, facingX, facingY, facingZ));
                    mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

                    mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY + flag.getValue(), mc.player.posZ, false));

                    setRunning(false);
                }
            }
        });
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        Exeter.getInstance().getInventoryManager().sync();
    }
}
