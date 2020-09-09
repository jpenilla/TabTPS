package xyz.jpenilla.tabtps.task;

import org.bukkit.scheduler.BukkitRunnable;
import xyz.jpenilla.tabtps.TabTPS;

public class RecordCPUTask extends BukkitRunnable {
    private final TabTPS tabTPS;

    public RecordCPUTask(TabTPS tabTPS) {
        this.tabTPS = tabTPS;
    }

    @Override
    public void run() {
        tabTPS.getCpuUtil().recordUsage();
    }
}
