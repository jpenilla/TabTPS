package xyz.jpenilla.tabtps.util;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import xyz.jpenilla.tabtps.TabTPS;

@AllArgsConstructor
public class PingUtil {
    private final TabTPS tabTPS;

    public int getPing(Player player) {
        return tabTPS.getMajorMinecraftVersion() < 16 || !tabTPS.isPaperServer() ? tabTPS.getNmsHandler().getPing(player) : player.spigot().getPing();
    }

    public String getColoredPing(Player player) {
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
