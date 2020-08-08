package xyz.jpenilla.tabtps.nms.v1_16_R1;

import net.minecraft.server.v1_16_R1.MathHelper;
import net.minecraft.server.v1_16_R1.MinecraftServer;
import org.bukkit.entity.Player;
import xyz.jpenilla.tabtps.api.NMS;

public class NMSHandler implements NMS {

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
        // not needed anymore for > 1.16
    }
}
