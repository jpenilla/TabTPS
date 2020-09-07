package xyz.jpenilla.tabtps.module;

import org.bukkit.entity.Player;

public abstract class Module {
    public abstract String getLabel();
    public abstract String getData();
    public abstract boolean needsPlayer();

    public static Module from(Player player, String name) {
        switch (name.toLowerCase()) {
            case "tps":
                return new TPS();
            case "mspt":
                return new MSPT();
            case "memory":
                return new Memory();
            case "ping":
                return new Ping(player);
            case "cpu" :
                return new CPU();
            default:
                throw new IllegalArgumentException("No such module: " + name.toLowerCase());
        }
    }
}
