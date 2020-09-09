package xyz.jpenilla.tabtps.module;

import xyz.jpenilla.tabtps.TabTPS;

public class CPU extends Module {

    @Override
    public String getLabel() {
        return "CPU";
    }

    @Override
    public String getData() {
        return "<gradient:green:dark_green>" + TabTPS.getInstance().getCpuUtil().getRecentSystemCpuLoadSnapshot() + "</gradient><gray>%<white>, </white><gradient:green:dark_green>" + TabTPS.getInstance().getCpuUtil().getRecentProcessCpuLoadSnapshot() + "</gradient>%</gray> <white>(<gray>sys.</gray>, <gray>proc.</gray>)</white>";
    }

    @Override
    public boolean needsPlayer() {
        return false;
    }
}
