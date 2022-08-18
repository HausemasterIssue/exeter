package viamcp.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import viamcp.ViaMCP;
import viamcp.protocols.ProtocolCollection;

public class AttackOrder
{
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static final int VER_1_8_ID = 47;

    public static void sendConditionalSwing(RayTraceResult ray, EnumHand enumHand)
    {
        if (ray != null && ray.typeOfHit != RayTraceResult.Type.ENTITY)
        {
            mc.player.swingArm(enumHand);
        }
    }

    public static void sendFixedAttack(EntityPlayer entityIn, Entity target, EnumHand enumHand, boolean packet)
    {
        // Using this instead of ViaMCP.PROTOCOL_VERSION so does not need to be changed between 1.8.x and 1.12.2 base
        // getVersion() can be null, but not in this case, as ID 47 exists, if not removed
        if(isBeforeCombatUpdate())
        {
            send1_8Attack(entityIn, target, enumHand, packet);
        }
        else
        {
            send1_9Attack(entityIn, target, enumHand, packet);
        }
    }

    private static void send1_8Attack(EntityPlayer entityIn, Entity target, EnumHand enumHand, boolean packet)
    {
        mc.player.swingArm(enumHand);

        if (packet) {
            mc.player.connection.sendPacket(new CPacketUseEntity(entityIn));
        } else {
            mc.playerController.attackEntity(entityIn, target);
        }
    }

    private static void send1_9Attack(EntityPlayer entityIn, Entity target, EnumHand enumHand, boolean packet)
    {
        if (packet) {
            mc.player.connection.sendPacket(new CPacketUseEntity(entityIn));
        } else {
            mc.playerController.attackEntity(entityIn, target);
        }

        mc.player.swingArm(enumHand);
    }

    public static boolean isBeforeCombatUpdate() {
        return ViaMCP.getInstance().getVersion() <= ProtocolCollection.getProtocolById(VER_1_8_ID).getVersion();
    }
}
