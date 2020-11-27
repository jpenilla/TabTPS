package xyz.jpenilla.tabtps.task;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.jpenilla.tabtps.TabTPS;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TaskManager {
    private final TabTPS tabTPS;
    private final Map<UUID, Integer> tabTpsTaskIds = new HashMap<>();
    private final Map<UUID, Integer> actionBarTpsTaskIds = new HashMap<>();
    private int recordCpuTaskId = 0;

    public TaskManager(TabTPS tabTPS) {
        this.tabTPS = tabTPS;
    }

    public void startRecordCpuTask() {
        stopRecordCpuTask();
        recordCpuTaskId = new RecordCPUTask(tabTPS)
                .runTaskTimerAsynchronously(tabTPS, 0L, 10L)
                .getTaskId();
    }

    public void stopRecordCpuTask() {
        if (recordCpuTaskId != 0) {
            Bukkit.getScheduler().cancelTask(recordCpuTaskId);
            recordCpuTaskId = 0;
        }
    }

    public boolean hasTabTask(Player player) {
        return tabTpsTaskIds.containsKey(player.getUniqueId());
    }

    public void startTabTask(Player player) {
        stopTabTask(player);
        final int taskId = new TabTPSTask(tabTPS, player)
                .runTaskTimerAsynchronously(tabTPS, 0L, tabTPS.getPluginSettings().getTabUpdateRate())
                .getTaskId();
        tabTpsTaskIds.put(player.getUniqueId(), taskId);
    }

    public void stopTabTask(Player player) {
        if (hasTabTask(player)) {
            Bukkit.getScheduler().cancelTask(tabTpsTaskIds.get(player.getUniqueId()));
            if (player.isOnline()) {
                if (tabTPS.getMajorMinecraftVersion() < 16) {
                    tabTPS.getNmsHandler().setHeaderFooter(player, null, null);
                } else {
                    player.setPlayerListHeaderFooter((String) null, null);
                }
            }
            tabTpsTaskIds.remove(player.getUniqueId());
        }
    }

    public boolean hasActionBarTask(Player player) {
        return actionBarTpsTaskIds.containsKey(player.getUniqueId());
    }

    public void startActionBarTask(Player player) {
        stopActionBarTask(player);
        final int taskId = new ActionBarTPSTask(tabTPS, player)
                .runTaskTimerAsynchronously(tabTPS, 0L, tabTPS.getPluginSettings().getActionBarUpdateRate())
                .getTaskId();
        actionBarTpsTaskIds.put(player.getUniqueId(), taskId);
    }

    public void stopActionBarTask(Player player) {
        if (hasActionBarTask(player)) {
            Bukkit.getScheduler().cancelTask(actionBarTpsTaskIds.get(player.getUniqueId()));
            actionBarTpsTaskIds.remove(player.getUniqueId());
        }
    }
}
