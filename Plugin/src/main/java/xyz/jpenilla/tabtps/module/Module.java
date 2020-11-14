package xyz.jpenilla.tabtps.module;

import org.bukkit.entity.Player;
import xyz.jpenilla.tabtps.TabTPS;

public abstract class Module {
    public abstract String getLabel();
    public abstract String getData();
    public abstract boolean needsPlayer();

    public static Module from(TabTPS tabTPS, Player player, String name) {
        switch (name.toLowerCase()) {
            case "tps":
                return new TPS(tabTPS);
            case "mspt":
                return new MSPT(tabTPS);
            case "memory":
                return new Memory();
            case "ping":
                return new Ping(tabTPS, player);
            case "cpu":
                return new CPU(tabTPS);
            default:
                throw new IllegalArgumentException(String.format("No such module: '%s'", name.toLowerCase()));
        }
    }
}
