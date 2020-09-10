package xyz.jpenilla.tabtps.module;

import lombok.AllArgsConstructor;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.util.TPSUtil;

@AllArgsConstructor
public class TPS extends Module {
    private final TabTPS tabTPS;

    @Override
    public String getLabel() {
        return "TPS";
    }

    @Override
    public String getData() {
        return TPSUtil.getColoredTps(tabTPS.getTpsUtil().getTps()[0]);
    }

    @Override
    public boolean needsPlayer() {
        return false;
    }
}
