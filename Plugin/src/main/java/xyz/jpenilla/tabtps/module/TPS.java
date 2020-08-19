package xyz.jpenilla.tabtps.module;

import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.util.TPSUtil;

public class TPS extends Module {
    @Override
    public String getLabel() {
        return "TPS";
    }

    @Override
    public String getData() {
        return TPSUtil.getColoredTps(TabTPS.getInstance().getTpsUtil().getTps()[0]);
    }
}
