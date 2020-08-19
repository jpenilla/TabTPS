package xyz.jpenilla.tabtps.module;

import xyz.jpenilla.tabtps.util.MemoryUtil;

public class Memory extends Module {
    @Override
    public String getLabel() {
        return "RAM";
    }

    @Override
    public String getData() {
        return "<gray><gradient:green:dark_green>" + MemoryUtil.getUsedMemory() + "</gradient>M<white>/</white><gradient:green:dark_green>" + MemoryUtil.getTotalMemory() + "</gradient>M <white>(</white>max <gradient:green:dark_green>" + MemoryUtil.getMaxMemory() + "</gradient>M</white>)</white></gray>";
    }
}
