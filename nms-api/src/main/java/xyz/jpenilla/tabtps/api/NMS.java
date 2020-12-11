package xyz.jpenilla.tabtps.api;

import org.bukkit.entity.Player;

public interface NMS {
    double[] getTps();

    double getMspt();

    int getPing(Player player);

    default void setHeaderFooter(Player player, String header, String footer) {
        /* This method only needs to be implemented up to 1.15 */
    }
}
