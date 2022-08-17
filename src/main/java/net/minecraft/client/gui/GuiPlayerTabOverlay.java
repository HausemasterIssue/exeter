package net.minecraft.client.gui;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;

import me.friendly.exeter.core.Exeter;
import me.friendly.exeter.events.TabOverlayEvent;
import me.friendly.exeter.module.impl.miscellaneous.ExtraTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;

public class GuiPlayerTabOverlay extends Gui
{
    private static final Ordering<NetworkPlayerInfo> ENTRY_ORDERING = Ordering.from(new GuiPlayerTabOverlay.PlayerComparator());
    private static final Ordering<NetworkPlayerInfo> PING_ORDERING = Ordering.from(new ExtraTab.PingSorting());

    private final Minecraft mc;
    private final GuiIngame guiIngame;
    private ITextComponent footer;
    private ITextComponent header;

    /**
     * The last time the playerlist was opened (went from not being renderd, to being rendered)
     */
    private long lastTimeOpened;

    /** Weither or not the playerlist is currently being rendered */
    private boolean isBeingRendered;

    public GuiPlayerTabOverlay(Minecraft mcIn, GuiIngame guiIngameIn)
    {
        this.mc = mcIn;
        this.guiIngame = guiIngameIn;
    }

    /**
     * Returns the name that should be renderd for the player supplied
     */
    public String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn)
    {
        String raw = "";

        TabOverlayEvent event = new TabOverlayEvent(TabOverlayEvent.Type.TEXT);
        event.setPlayerText(networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getUnformattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName()));
        Exeter.getInstance().getEventManager().dispatch(event);

        if (event.isCanceled()) {
            raw = event.getPlayerText();
        } else {
            raw = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
        }

        return raw;

        //return networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
    }

    /**
     * Called by GuiIngame to update the information stored in the playerlist, does not actually render the list,
     * however.
     */
    public void updatePlayerList(boolean willBeRendered)
    {
        if (willBeRendered && !this.isBeingRendered)
        {
            this.lastTimeOpened = Minecraft.getSystemTime();
        }

        this.isBeingRendered = willBeRendered;
    }

    /**
     * Renders the playerlist, its background, headers and footers.
     */
    public void renderPlayerlist(int width, Scoreboard scoreboardIn, @Nullable ScoreObjective scoreObjectiveIn)
    {
        NetHandlerPlayClient nethandlerplayclient = this.mc.player.connection;

        Ordering<NetworkPlayerInfo> sorter = ENTRY_ORDERING;

        TabOverlayEvent event = new TabOverlayEvent(TabOverlayEvent.Type.SORT);
        Exeter.getInstance().getEventManager().dispatch(event);

        switch (event.getSorting()) {
            case DEFAULT:
                sorter = ENTRY_ORDERING;
                break;

            case PING:
                sorter = PING_ORDERING;
                break;
        }

        List<NetworkPlayerInfo> list = sorter.sortedCopy(nethandlerplayclient.getPlayerInfoMap());
        int i = 0;
        int j = 0;

        for (NetworkPlayerInfo networkplayerinfo : list)
        {
            int k = this.mc.fontRendererObj.getStringWidth(this.getPlayerName(networkplayerinfo));
            i = Math.max(i, k);

            if (scoreObjectiveIn != null && scoreObjectiveIn.getRenderType() != IScoreCriteria.EnumRenderType.HEARTS)
            {
                k = this.mc.fontRendererObj.getStringWidth(" " + scoreboardIn.getOrCreateScore(networkplayerinfo.getGameProfile().getName(), scoreObjectiveIn).getScorePoints());
                j = Math.max(j, k);
            }
        }

        event = new TabOverlayEvent(TabOverlayEvent.Type.LENGTH);
        Exeter.getInstance().getEventManager().dispatch(event);

        int count = event.getLength(); // default: 80

        list = list.subList(0, Math.min(list.size(), count));
        int l3 = list.size();
        int i4 = l3;
        int j4;

        for (j4 = 1; i4 > 20; i4 = (l3 + j4 - 1) / j4)
        {
            ++j4;
        }

        boolean flag = this.mc.isIntegratedServerRunning() || this.mc.getConnection().getNetworkManager().isEncrypted();

        event = new TabOverlayEvent(TabOverlayEvent.Type.PICTURE);
        Exeter.getInstance().getEventManager().dispatch(event);
        if (event.isCanceled()) {
            flag = event.isRenderPicture();
        }

        int l;

        if (scoreObjectiveIn != null)
        {
            if (scoreObjectiveIn.getRenderType() == IScoreCriteria.EnumRenderType.HEARTS)
            {
                l = 90;
            }
            else
            {
                l = j;
            }
        }
        else
        {
            l = 0;
        }

        int i1 = Math.min(j4 * ((flag ? 9 : 0) + i + l + 13), width - 50) / j4;
        int j1 = width / 2 - (i1 * j4 + (j4 - 1) * 5) / 2;
        int k1 = 10;
        int l1 = i1 * j4 + (j4 - 1) * 5;
        List<String> list1 = null;

        if (this.header != null)
        {
            list1 = this.mc.fontRendererObj.listFormattedStringToWidth(this.header.getFormattedText(), width - 50);

            for (String s : list1)
            {
                l1 = Math.max(l1, this.mc.fontRendererObj.getStringWidth(s));
            }
        }

        List<String> list2 = null;

        if (this.footer != null)
        {
            list2 = this.mc.fontRendererObj.listFormattedStringToWidth(this.footer.getFormattedText(), width - 50);

            for (String s1 : list2)
            {
                l1 = Math.max(l1, this.mc.fontRendererObj.getStringWidth(s1));
            }
        }

        if (list1 != null)
        {
            drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + list1.size() * this.mc.fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);

            for (String s2 : list1)
            {
                int i2 = this.mc.fontRendererObj.getStringWidth(s2);
                this.mc.fontRendererObj.drawStringWithShadow(s2, (float)(width / 2 - i2 / 2), (float)k1, -1);
                k1 += this.mc.fontRendererObj.FONT_HEIGHT;
            }

            ++k1;
        }

        drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + i4 * 9, Integer.MIN_VALUE);

        for (int k4 = 0; k4 < l3; ++k4)
        {
            int l4 = k4 / i4;
            int i5 = k4 % i4;
            int j2 = j1 + l4 * i1 + l4 * 5;
            int k2 = k1 + i5 * 9;
            drawRect(j2, k2, j2 + i1, k2 + 8, 553648127);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            if (k4 < list.size())
            {
                NetworkPlayerInfo networkplayerinfo1 = list.get(k4);
                GameProfile gameprofile = networkplayerinfo1.getGameProfile();

                if (flag)
                {
                    EntityPlayer entityplayer = this.mc.world.getPlayerEntityByUUID(gameprofile.getId());
                    boolean flag1 = entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.CAPE) && ("Dinnerbone".equals(gameprofile.getName()) || "Grumm".equals(gameprofile.getName()));
                    this.mc.getTextureManager().bindTexture(networkplayerinfo1.getLocationSkin());
                    int l2 = 8 + (flag1 ? 8 : 0);
                    int i3 = 8 * (flag1 ? -1 : 1);
                    Gui.drawScaledCustomSizeModalRect(j2, k2, 8.0F, (float)l2, 8, i3, 8, 8, 64.0F, 64.0F);

                    if (entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.HAT))
                    {
                        int j3 = 8 + (flag1 ? 8 : 0);
                        int k3 = 8 * (flag1 ? -1 : 1);
                        Gui.drawScaledCustomSizeModalRect(j2, k2, 40.0F, (float)j3, 8, k3, 8, 8, 64.0F, 64.0F);
                    }

                    j2 += 9;
                }

                String s4 = this.getPlayerName(networkplayerinfo1);

                if (networkplayerinfo1.getGameType() == GameType.SPECTATOR)
                {
                    this.mc.fontRendererObj.drawStringWithShadow(TextFormatting.ITALIC + s4, (float)j2, (float)k2, -1862270977);
                }
                else
                {
                    this.mc.fontRendererObj.drawStringWithShadow(s4, (float)j2, (float)k2, -1);
                }

                if (scoreObjectiveIn != null && networkplayerinfo1.getGameType() != GameType.SPECTATOR)
                {
                    int k5 = j2 + i + 1;
                    int l5 = k5 + l;

                    if (l5 - k5 > 5)
                    {
                        this.drawScoreboardValues(scoreObjectiveIn, k2, gameprofile.getName(), k5, l5, networkplayerinfo1);
                    }
                }

                this.drawPing(i1, j2 - (flag ? 9 : 0), k2, networkplayerinfo1);
            }
        }

        if (list2 != null)
        {
            k1 = k1 + i4 * 9 + 1;
            drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + list2.size() * this.mc.fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);

            for (String s3 : list2)
            {
                int j5 = this.mc.fontRendererObj.getStringWidth(s3);
                this.mc.fontRendererObj.drawStringWithShadow(s3, (float)(width / 2 - j5 / 2), (float)k1, -1);
                k1 += this.mc.fontRendererObj.FONT_HEIGHT;
            }
        }
    }

    protected void drawPing(int p_175245_1_, int p_175245_2_, int p_175245_3_, NetworkPlayerInfo networkPlayerInfoIn)
    {
        int latency = networkPlayerInfoIn.getResponseTime();

        TabOverlayEvent event = new TabOverlayEvent(TabOverlayEvent.Type.PING);
        Exeter.getInstance().getEventManager().dispatch(event);

        int j = 4;
        if (latency < 0) {
            j = 5;
        } else if (latency < 150) {
            j = 0;
        } else if (latency < 300) {
            j = 1;
        } else if (latency < 600) {
            j = 2;
        } else if (latency < 1000) {
            j = 3;
        }

        switch (event.getPingDisplay()) {
            case DEFAULT: {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.mc.getTextureManager().bindTexture(ICONS);

                this.zLevel += 100.0F;
                this.drawTexturedModalRect(p_175245_2_ + p_175245_1_ - 11, p_175245_3_, 0, 176 + j * 8, 10, 8);
                this.zLevel -= 100.0F;

                break;
            }

            case TEXT: {
                TextFormatting formatting = TextFormatting.GREEN;
                switch (j) {
                    case 1:
                        formatting = TextFormatting.YELLOW;
                        break;

                    case 2:
                        formatting = TextFormatting.RED;
                        break;

                    case 3:
                    case 4:
                    case 5:
                        formatting = TextFormatting.DARK_RED;
                        break;
                }

                mc.fontRendererObj.drawStringWithShadow(formatting.toString() + latency + "ms", p_175245_2_ + p_175245_1_ - 11, p_175245_3_, -1);
                break;
            }

            case NONE:
                break;
        }
    }

    private void drawScoreboardValues(ScoreObjective objective, int p_175247_2_, String name, int p_175247_4_, int p_175247_5_, NetworkPlayerInfo info)
    {
        int i = objective.getScoreboard().getOrCreateScore(name, objective).getScorePoints();

        if (objective.getRenderType() == IScoreCriteria.EnumRenderType.HEARTS)
        {
            this.mc.getTextureManager().bindTexture(ICONS);

            if (this.lastTimeOpened == info.getRenderVisibilityId())
            {
                if (i < info.getLastHealth())
                {
                    info.setLastHealthTime(Minecraft.getSystemTime());
                    info.setHealthBlinkTime((long)(this.guiIngame.getUpdateCounter() + 20));
                }
                else if (i > info.getLastHealth())
                {
                    info.setLastHealthTime(Minecraft.getSystemTime());
                    info.setHealthBlinkTime((long)(this.guiIngame.getUpdateCounter() + 10));
                }
            }

            if (Minecraft.getSystemTime() - info.getLastHealthTime() > 1000L || this.lastTimeOpened != info.getRenderVisibilityId())
            {
                info.setLastHealth(i);
                info.setDisplayHealth(i);
                info.setLastHealthTime(Minecraft.getSystemTime());
            }

            info.setRenderVisibilityId(this.lastTimeOpened);
            info.setLastHealth(i);
            int j = MathHelper.ceil((float)Math.max(i, info.getDisplayHealth()) / 2.0F);
            int k = Math.max(MathHelper.ceil((float)(i / 2)), Math.max(MathHelper.ceil((float)(info.getDisplayHealth() / 2)), 10));
            boolean flag = info.getHealthBlinkTime() > (long)this.guiIngame.getUpdateCounter() && (info.getHealthBlinkTime() - (long)this.guiIngame.getUpdateCounter()) / 3L % 2L == 1L;

            if (j > 0)
            {
                float f = Math.min((float)(p_175247_5_ - p_175247_4_ - 4) / (float)k, 9.0F);

                if (f > 3.0F)
                {
                    for (int l = j; l < k; ++l)
                    {
                        this.drawTexturedModalRect((float)p_175247_4_ + (float)l * f, (float)p_175247_2_, flag ? 25 : 16, 0, 9, 9);
                    }

                    for (int j1 = 0; j1 < j; ++j1)
                    {
                        this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, flag ? 25 : 16, 0, 9, 9);

                        if (flag)
                        {
                            if (j1 * 2 + 1 < info.getDisplayHealth())
                            {
                                this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, 70, 0, 9, 9);
                            }

                            if (j1 * 2 + 1 == info.getDisplayHealth())
                            {
                                this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, 79, 0, 9, 9);
                            }
                        }

                        if (j1 * 2 + 1 < i)
                        {
                            this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, j1 >= 10 ? 160 : 52, 0, 9, 9);
                        }

                        if (j1 * 2 + 1 == i)
                        {
                            this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, j1 >= 10 ? 169 : 61, 0, 9, 9);
                        }
                    }
                }
                else
                {
                    float f1 = MathHelper.clamp((float)i / 20.0F, 0.0F, 1.0F);
                    int i1 = (int)((1.0F - f1) * 255.0F) << 16 | (int)(f1 * 255.0F) << 8;
                    String s = "" + (float)i / 2.0F;

                    if (p_175247_5_ - this.mc.fontRendererObj.getStringWidth(s + "hp") >= p_175247_4_)
                    {
                        s = s + "hp";
                    }

                    this.mc.fontRendererObj.drawStringWithShadow(s, (float)((p_175247_5_ + p_175247_4_) / 2 - this.mc.fontRendererObj.getStringWidth(s) / 2), (float)p_175247_2_, i1);
                }
            }
        }
        else
        {
            String s1 = TextFormatting.YELLOW + "" + i;
            this.mc.fontRendererObj.drawStringWithShadow(s1, (float)(p_175247_5_ - this.mc.fontRendererObj.getStringWidth(s1)), (float)p_175247_2_, 16777215);
        }
    }

    public void setFooter(@Nullable ITextComponent footerIn)
    {
        this.footer = footerIn;
    }

    public void setHeader(@Nullable ITextComponent headerIn)
    {
        this.header = headerIn;
    }

    public void resetFooterHeader()
    {
        this.header = null;
        this.footer = null;
    }

    static class PlayerComparator implements Comparator<NetworkPlayerInfo>
    {
        private PlayerComparator()
        {
        }

        public int compare(NetworkPlayerInfo p_compare_1_, NetworkPlayerInfo p_compare_2_)
        {
            ScorePlayerTeam scoreplayerteam = p_compare_1_.getPlayerTeam();
            ScorePlayerTeam scoreplayerteam1 = p_compare_2_.getPlayerTeam();
            return ComparisonChain.start().compareTrueFirst(p_compare_1_.getGameType() != GameType.SPECTATOR, p_compare_2_.getGameType() != GameType.SPECTATOR).compare(scoreplayerteam != null ? scoreplayerteam.getRegisteredName() : "", scoreplayerteam1 != null ? scoreplayerteam1.getRegisteredName() : "").compare(p_compare_1_.getGameProfile().getName(), p_compare_2_.getGameProfile().getName()).result();
        }
    }
}
