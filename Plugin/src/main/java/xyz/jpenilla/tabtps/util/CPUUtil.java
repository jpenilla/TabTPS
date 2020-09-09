package xyz.jpenilla.tabtps.util;

import com.sun.management.OperatingSystemMXBean;
import lombok.Getter;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

public class CPUUtil {
    @Getter private double recentProcessCpuLoadSnapshot = 0;
    @Getter private double recentSystemCpuLoadSnapshot = 0;
    private final double[] recentSystemUsage = new double[8];
    private final double[] recentProcessUsage = new double[8];
    private int index = 0;

    private void nextIndex() {
        index++;
        if (index > 7) {
            index = 0;
        }
    }

    public void recordUsage() {
        recentProcessUsage[index] = getCurrentProcessCpuLoad();
        recentSystemUsage[index] = getCurrentSystemCpuLoad();
        recentProcessCpuLoadSnapshot = getRecentProcessCpuLoad();
        recentSystemCpuLoadSnapshot = getRecentSystemCpuLoad();
        nextIndex();
    }

    public double getRecentProcessCpuLoad() {
        return round(getAverage(recentProcessUsage));
    }

    public double getRecentSystemCpuLoad() {
        return round(getAverage(recentSystemUsage));
    }

    public static double getAverage(double[] values) {
        return Arrays.stream(values.clone()).filter(d -> d != 0).summaryStatistics().getAverage();
    }

    public static double round(double val) {
        return new BigDecimal(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static double getCurrentProcessCpuLoad() {
        return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getProcessCpuLoad() * 100;
    }

    public static double getCurrentSystemCpuLoad() {
        return ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getSystemCpuLoad() * 100;
    }
}
