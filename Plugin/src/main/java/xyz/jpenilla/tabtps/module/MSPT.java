package xyz.jpenilla.tabtps.module;

import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.util.TPSUtil;

public class MSPT extends Module {
    @Override
    public String getLabel() {
        return "MSPT";
    }

    @Override
    public String getData() {
        return TPSUtil.getColoredMspt(TabTPS.getInstance().getTpsUtil().getMspt());
    }

    @Override
    public boolean needsPlayer() {
        return false;
    }
}
