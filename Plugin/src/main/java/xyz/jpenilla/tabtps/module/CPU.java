package xyz.jpenilla.tabtps.module;

import lombok.AllArgsConstructor;
import xyz.jpenilla.tabtps.TabTPS;

@AllArgsConstructor
public class CPU extends Module {
    private final TabTPS tabTPS;

    @Override
    public String getLabel() {
        return "CPU";
    }

    @Override
    public String getData() {
        return "<gradient:green:dark_green>" + tabTPS.getCpuUtil().getRecentSystemCpuLoadSnapshot() + "</gradient><gray>%<white>, </white><gradient:green:dark_green>" + tabTPS.getCpuUtil().getRecentProcessCpuLoadSnapshot() + "</gradient>%</gray> <white>(<gray>sys.</gray>, <gray>proc.</gray>)</white>";
    }

    @Override
    public boolean needsPlayer() {
        return false;
    }
}
