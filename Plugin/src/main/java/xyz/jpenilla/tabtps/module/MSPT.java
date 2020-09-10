package xyz.jpenilla.tabtps.module;

import lombok.AllArgsConstructor;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.util.TPSUtil;

@AllArgsConstructor
public class MSPT extends Module {
    private final TabTPS tabTPS;

    @Override
    public String getLabel() {
        return "MSPT";
    }

    @Override
    public String getData() {
        return TPSUtil.getColoredMspt(tabTPS.getTpsUtil().getMspt());
    }

    @Override
    public boolean needsPlayer() {
        return false;
    }
}
