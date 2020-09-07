package xyz.jpenilla.tabtps.module;

import org.bukkit.entity.Player;
import xyz.jpenilla.tabtps.TabTPS;

public class Ping extends Module {
    private final Player player;

    public Ping(Player player) {
        this.player = player;
    }

    @Override
    public String getLabel() {
        return "Ping";
    }

    @Override
    public String getData() {
        return getColoredPing(player) + "<gray>ms</gray>";
    }

    @Override
    public boolean needsPlayer() {
        return true;
    }

    public static int getPing(Player player) {
        return TabTPS.getInstance().getMajorMinecraftVersion() < 16 || !TabTPS.getInstance().isPaperServer() ? TabTPS.getInstance().getNmsHandler().getPing(player) : player.spigot().getPing();
    }

    public static String getColoredPing(Player player) {
        return getColoredPing(getPing(player));
    }

    public static String getColoredPing(int ping) {
        String color;
        if (ping < 100) {
            color = "green:dark_green";
        } else if (ping < 250) {
            color = "yellow:#FFC500";
        } else {
            color = "red:#FF4300";
        }
        return "<gradient:" + color + ">" + ping + "</gradient>";
    }
}
