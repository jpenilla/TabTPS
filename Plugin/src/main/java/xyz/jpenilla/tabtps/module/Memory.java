package xyz.jpenilla.tabtps.module;

public class Memory extends Module {
    @Override
    public String getLabel() {
        return "RAM";
    }

    @Override
    public String getData() {
        final int totalMem = (int) (Runtime.getRuntime().totalMemory() / 1048576);
        final int freeMem = (int) (Runtime.getRuntime().freeMemory() / 1048576);
        final int maxMem = (int) (Runtime.getRuntime().maxMemory() / 1048576);
        final int usedMem = totalMem - freeMem;
        return "<gray><gradient:green:dark_green>" + usedMem + "</gradient>M<white>/</white><gradient:green:dark_green>" + totalMem + "</gradient>M <white>(</white>max <gradient:green:dark_green>" + maxMem + "</gradient>M</white>)</white></gray>";
    }
}
