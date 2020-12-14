package xyz.jpenilla.tabtps.nms.api;

import org.bukkit.entity.Player;

public interface NMS {
    double[] getTps();

    double getMspt();

    int getPing(Player player);
}
