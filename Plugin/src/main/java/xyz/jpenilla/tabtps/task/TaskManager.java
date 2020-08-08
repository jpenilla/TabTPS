package xyz.jpenilla.tabtps.task;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.jpenilla.tabtps.TabTPS;

import java.util.HashMap;
import java.util.UUID;

public class TaskManager {

    private final TabTPS tabTPS;
    private final HashMap<UUID, Integer> tpsTabTaskIds = new HashMap<>();

    public TaskManager(TabTPS tabTPS) {
        this.tabTPS = tabTPS;
    }

    public boolean hasTabTask(Player player) {
        return tpsTabTaskIds.containsKey(player.getUniqueId());
    }

    public void startTabTask(Player player) {
        stopTabTask(player);
        final int taskId = new TPSTabTask(tabTPS, player).runTaskTimerAsynchronously(tabTPS, 0L, 1L).getTaskId();
        tpsTabTaskIds.put(player.getUniqueId(), taskId);
    }

    public void stopTabTask(Player player) {
        if (hasTabTask(player)) {
            Bukkit.getScheduler().cancelTask(tpsTabTaskIds.get(player.getUniqueId()));
            if (player.isOnline()) {
                if (tabTPS.getMajorMinecraftVersion() < 16) {
                    tabTPS.getNmsHandler().setHeaderFooter(player, null, null);
                } else {
                    player.setPlayerListHeaderFooter((String) null, null);
                }
            }
            tpsTabTaskIds.remove(player.getUniqueId());
        }
    }
}
