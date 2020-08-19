package xyz.jpenilla.tabtps.util;

public class Constants {
    private static final String DOT = ".";
    private static final String PERMISSION_ROOT = "tabtps";

    private static final String PERMISSION_TOGGLE_ROOT = PERMISSION_ROOT + DOT + "toggle";
    public static final String PERMISSION_TOGGLE_TAB = PERMISSION_TOGGLE_ROOT + DOT + "tab";
    public static final String PERMISSION_TOGGLE_ACTIONBAR = PERMISSION_TOGGLE_ROOT + DOT + "actionbar";

    public static final String PERMISSION_COMMAND_TICKINFO = PERMISSION_ROOT + DOT + "tps";
    public static final String PERMISSION_COMMAND_TOGGLE = PERMISSION_TOGGLE_ROOT;
    public static final String PERMISSION_COMMAND_RELOAD = PERMISSION_ROOT + DOT + "reload";
}
