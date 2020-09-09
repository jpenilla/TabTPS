package xyz.jpenilla.tabtps.module;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CPU extends Module {
    public static Double round(Double val) {
        return new BigDecimal(val.toString()).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static double getProcessCpuLoad() {
        return round(((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getProcessCpuLoad() * 100);
    }

    public static double getSystemCpuLoad() {
        return round(((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getSystemCpuLoad() * 100);
    }

    @Override
    public String getLabel() {
        return "CPU";
    }

    @Override
    public String getData() {
        //return "<gradient:green:dark_green>" + getSystemCpuLoad() + "</gradient><white>% (<gray>sys.</gray>)</white><white>,</white> <gradient:green:dark_green>" + getProcessCpuLoad() + "</gradient><white>% (<gray>proc.</gray>)</white>";
        return "<gradient:green:dark_green>" + getSystemCpuLoad() + "</gradient><gray>%<white>, </white><gradient:green:dark_green>" + getProcessCpuLoad() + "</gradient>%</gray> <white>(<gray>sys.</gray>, <gray>proc.</gray>)</white>";
    }

    @Override
    public boolean needsPlayer() {
        return false;
    }
}
