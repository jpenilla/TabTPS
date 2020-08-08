package xyz.jpenilla.tabtps.api;

import org.bukkit.entity.Player;

public abstract class NMS {
    public abstract double[] getTps();

    public abstract double getMspt();

    public void setHeaderFooter(Player player, String header, String footer) {
        // This method is only required to be implemented up to 1.15
    }
}
