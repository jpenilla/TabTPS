package xyz.jpenilla.tabtps.api;

import org.bukkit.entity.Player;

public abstract class NMS {
    public abstract double[] getTps();

    public abstract double getMspt();

    public abstract int getPing(Player player);

    public void setHeaderFooter(Player player, String header, String footer) {
        /* This method only needs to be implemented up to 1.15 */
    }
}
