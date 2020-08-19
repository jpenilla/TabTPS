package xyz.jpenilla.tabtps.util;

public class MemoryUtil {
    public static int getUsedMemory() {
        return getTotalMemory() - (int) (Runtime.getRuntime().freeMemory() / 1048576);
    }

    public static int getTotalMemory() {
        return (int) (Runtime.getRuntime().totalMemory() / 1048576);
    }

    public static int getMaxMemory() {
        return (int) (Runtime.getRuntime().maxMemory() / 1048576);
    }
}
