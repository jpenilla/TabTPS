package xyz.jpenilla.tabtps.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;
import xyz.jpenilla.tabtps.TabTPS;

public class PluginSettings {
    private final TabTPS tabTPS;

    @Getter private UserPrefsDefaults userPrefsDefaults;
    @Getter private Modules modules;
    @Getter private String moduleOrder;

    public PluginSettings(TabTPS tabTPS) {
        this.tabTPS = tabTPS;
    }

    public void load() {
        tabTPS.saveDefaultConfig();
        final FileConfiguration config = tabTPS.getConfig();

        moduleOrder = config.getString("module_order");

        userPrefsDefaults = new UserPrefsDefaults(
                config.getBoolean("defaults.tab_tps", false),
                config.getBoolean("defaults.action_bar_tps", false)
        );

        modules = new Modules(
                config.getBoolean("modules.tps", true),
                config.getBoolean("modules.mspt", true),
                config.getBoolean("modules.memory", false)
        );
    }

    public void save() {
        final FileConfiguration config = tabTPS.getConfig();

        config.set("module_order", moduleOrder);

        config.set("defaults.tab_tps", userPrefsDefaults.isTab());
        config.set("defaults.action_bar_tps", userPrefsDefaults.isActionBar());

        config.set("modules.tps", modules.isTps());
        config.set("modules.mspt", modules.isMspt());
        config.set("modules.memory", modules.isMemory());

        tabTPS.saveConfig();
    }

    @AllArgsConstructor
    public static class UserPrefsDefaults {
        @Getter @Setter
        private boolean tab;

        @Getter @Setter
        private boolean actionBar;
    }

    @AllArgsConstructor
    public static class Modules {
        @Getter @Setter
        private boolean tps;

        @Getter @Setter
        private boolean mspt;

        @Getter @Setter
        private boolean memory;
    }
}
