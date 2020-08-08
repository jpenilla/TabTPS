package xyz.jpenilla.tabtps.nms.v1_8_R3;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import xyz.jpenilla.tabtps.api.NMS;

import java.lang.reflect.Field;

public class NMSHandler extends NMS {

    private static final ChatComponentText EMPTY = new ChatComponentText("");

    @Override
    public double[] getTps() {
        return MinecraftServer.getServer().recentTps;
    }

    @Override
    public double getMspt() {
        return MathHelper.a(MinecraftServer.getServer().h) * 1.0E-6D;
    }

    @Override
    public void setHeaderFooter(Player player, String header, String footer) {
        IChatBaseComponent h = header != null ? IChatBaseComponent.ChatSerializer.a(header) : EMPTY;
        IChatBaseComponent f = footer != null ? IChatBaseComponent.ChatSerializer.a(footer) : EMPTY;
        CraftPlayer craftplayer = (CraftPlayer) player;
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
        try {
            Field headerField = packet.getClass().getDeclaredField("a");
            headerField.setAccessible(true);
            headerField.set(packet, h);
            headerField.setAccessible(!headerField.isAccessible());

            Field footerField = packet.getClass().getDeclaredField("b");
            footerField.setAccessible(true);
            footerField.set(packet, f);
            footerField.setAccessible(!footerField.isAccessible());
        } catch (Exception e) {
            e.printStackTrace();
        }
        craftplayer.getHandle().playerConnection.sendPacket(packet);
    }
}
