package xyz.jpenilla.tabtps.nms.v1_11_R1;

import net.minecraft.server.v1_11_R1.ChatComponentText;
import net.minecraft.server.v1_11_R1.IChatBaseComponent;
import net.minecraft.server.v1_11_R1.MathHelper;
import net.minecraft.server.v1_11_R1.MinecraftServer;
import net.minecraft.server.v1_11_R1.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import xyz.jpenilla.tabtps.api.NMS;

import java.lang.reflect.Field;

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
        return MathHelper.a(MinecraftServer.getServer().h) * 1.0E-6D;
    }

    @Override
    public int getPing(Player player) {
        return ((CraftPlayer) player).getHandle().ping;
    }

    @Override
    public void setHeaderFooter(Player player, String header, String footer) {
        IChatBaseComponent h = header != null ? IChatBaseComponent.ChatSerializer.a(header) : EMPTY;
        IChatBaseComponent f = footer != null ? IChatBaseComponent.ChatSerializer.a(footer) : EMPTY;
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
        try {
            Field headerField = packet.getClass().getDeclaredField("a");
            headerField.setAccessible(true);
            headerField.set(packet, h);

            Field footerField = packet.getClass().getDeclaredField("b");
            footerField.setAccessible(true);
            footerField.set(packet, f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
