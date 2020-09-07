package xyz.jpenilla.tabtps.nms.v1_14_R1;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_14_R1.MathHelper;
import net.minecraft.server.v1_14_R1.MinecraftServer;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import xyz.jpenilla.tabtps.api.NMS;

public class NMSHandler extends NMS {

    @Override
    public double[] getTps() {
        return MinecraftServer.getServer().recentTps;
    }

    @Override
    public double getMspt() {
        return MathHelper.a(MinecraftServer.getServer().f) * 1.0E-6D;
    }

    @Override
    public int getPing(Player player) {
        return ((CraftPlayer) player).getHandle().ping;
    }

    @Override
    public void setHeaderFooter(Player player, String header, String footer) {
        final String h = header != null ? BaseComponent.toLegacyText(ComponentSerializer.parse(header)) : null;
        final String f = footer != null ? BaseComponent.toLegacyText(ComponentSerializer.parse(footer)) : null;
        player.setPlayerListHeaderFooter(h, f);
    }
}
