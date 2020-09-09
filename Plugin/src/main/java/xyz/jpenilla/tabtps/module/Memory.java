package xyz.jpenilla.tabtps.module;

import xyz.jpenilla.tabtps.util.MemoryUtil;

public class Memory extends Module {

    private final boolean alwaysShowMax;

    public Memory() {
        this.alwaysShowMax = false;
    }

    public Memory(boolean alwaysShowMax) {
        this.alwaysShowMax = alwaysShowMax;
    }

    @Override
    public String getLabel() {
        return "RAM";
    }

    @Override
    public String getData() {
        final StringBuilder builder = new StringBuilder("<gray><gradient:green:dark_green>" + MemoryUtil.getUsedMemory() + "</gradient>M<white>/</white><gradient:green:dark_green>" + MemoryUtil.getCommittedMemory() + "</gradient>M");
        if (alwaysShowMax || MemoryUtil.getCommittedMemory() != MemoryUtil.getMaxMemory()) {
            builder.append(" <white>(</white>max <gradient:green:dark_green>").append(MemoryUtil.getMaxMemory()).append("</gradient>M<white>)</white>");
        }
        builder.append("</gray>");
        return builder.toString();
    }

    @Override
    public boolean needsPlayer() {
        return false;
    }
}
