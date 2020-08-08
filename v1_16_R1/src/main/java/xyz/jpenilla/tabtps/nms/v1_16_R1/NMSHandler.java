package xyz.jpenilla.tabtps.nms.v1_16_R1;

import net.minecraft.server.v1_16_R1.MathHelper;
import net.minecraft.server.v1_16_R1.MinecraftServer;
import xyz.jpenilla.tabtps.api.NMS;

public class NMSHandler extends NMS {

    @Override
    public double[] getTps() {
        return MinecraftServer.getServer().recentTps;
    }

    @Override
    public double getMspt() {
        return MathHelper.a(MinecraftServer.getServer().h) * 1.0E-6D;
    }
}
