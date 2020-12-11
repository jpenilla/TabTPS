package xyz.jpenilla.tabtps.nms.v1_13_R2;

import net.minecraft.server.v1_13_R2.ChatComponentText;
import net.minecraft.server.v1_13_R2.IChatBaseComponent;
import net.minecraft.server.v1_13_R2.MathHelper;
import net.minecraft.server.v1_13_R2.MinecraftServer;
import net.minecraft.server.v1_13_R2.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import xyz.jpenilla.tabtps.api.NMS;

public class NMSHandler implements NMS {

    private static final ChatComponentText EMPTY = new ChatComponentText("");

    @SuppressWarnings("deprecation")
    @Override
    public double[] getTps() {
        return MinecraftServer.getServer().recentTps;
    }

    @SuppressWarnings("deprecation")
    @Override
    public double getMspt() {
        return MathHelper.a(MinecraftServer.getServer().d) * 1.0E-6D;
    }

    @Override
    public int getPing(Player player) {
        return ((CraftPlayer) player).getHandle().ping;
    }

    @Override
    public void setHeaderFooter(Player player, String header, String footer) {
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
        packet.header = header != null ? IChatBaseComponent.ChatSerializer.a(header) : EMPTY;
        packet.footer = footer != null ? IChatBaseComponent.ChatSerializer.a(footer) : EMPTY;
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
