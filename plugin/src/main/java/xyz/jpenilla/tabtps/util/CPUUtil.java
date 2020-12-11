package xyz.jpenilla.tabtps.util;

import com.sun.management.OperatingSystemMXBean;
import lombok.Getter;
import org.bukkit.Bukkit;
import xyz.jpenilla.tabtps.TabTPS;

import java.lang.management.ManagementFactory;
import java.util.DoubleSummaryStatistics;

public class CPUUtil {
    private final TabTPS tabTPS;
    @Getter private double recentProcessCpuLoadSnapshot = 0;
    @Getter private double recentSystemCpuLoadSnapshot = 0;
    private final double[] recentSystemUsage = new double[20];
    private final double[] recentProcessUsage = new double[20];
    private int index = 0;
    private int monitorTaskId;

    public CPUUtil(TabTPS tabTPS) {
        this.tabTPS = tabTPS;
    }

    public void startRecordingUsage() {
        stopRecordingUsage();
        monitorTaskId = Bukkit.getScheduler()
                .runTaskTimerAsynchronously(tabTPS, this::recordUsage, 0L, 10L)
                .getTaskId();
    }

    public void stopRecordingUsage() {
        if (monitorTaskId != 0) {
            Bukkit.getScheduler().cancelTask(monitorTaskId);
        }
    }

    private void nextIndex() {
        index++;
        if (index == 20) {
            index = 0;
        }
    }

    private void recordUsage() {
        recentProcessUsage[index] = getCurrentProcessCpuLoad();
        recentSystemUsage[index] = getCurrentSystemCpuLoad();
        recentProcessCpuLoadSnapshot = getRecentProcessCpuLoad();
        recentSystemCpuLoadSnapshot = getRecentSystemCpuLoad();
        nextIndex();
    }

    private double getRecentProcessCpuLoad() {
        return round(getAverage(recentProcessUsage));
    }

    private double getRecentSystemCpuLoad() {
        return round(getAverage(recentSystemUsage));
    }

    private static double getAverage(double[] values) {
        final DoubleSummaryStatistics statistics = new DoubleSummaryStatistics();
        for (final double d : values.clone()) {
            if (d != 0 && !Double.isNaN(d)) {
                statistics.accept(d);
            }
        }
        return statistics.getAverage();
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static double getCurrentProcessCpuLoad() {
        return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getProcessCpuLoad() * 100;
    }

    private static double getCurrentSystemCpuLoad() {
        return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getSystemCpuLoad() * 100;
    }
}
