package xyz.jpenilla.tabtps.api;

import org.bukkit.entity.Player;

public interface NMS {
    double[] getTps();

    double getMspt();

    void setHeaderFooter(Player player, String header, String footer);
}
