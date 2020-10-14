package xyz.jpenilla.tabtps;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class PluginSettings {
    private final TabTPS tabTPS;

    @Getter private UserPrefsDefaults userPrefsDefaults;
    @Getter private Modules modules;
    @Getter private final List<String> ignoredMemoryPools = new ArrayList<>();

    public PluginSettings(TabTPS tabTPS) {
        this.tabTPS = tabTPS;
    }

    public void load() {
        tabTPS.saveDefaultConfig();
        tabTPS.reloadConfig();
        final FileConfiguration config = tabTPS.getConfig();

        userPrefsDefaults = new UserPrefsDefaults(
                config.getBoolean("defaults.tab_tps", false),
                config.getBoolean("defaults.action_bar_tps", false)
        );

        modules = new Modules(
                config.getString("modules.tab_header", ""),
                config.getString("modules.tab_footer", "tps,mspt"),
                config.getString("modules.action_bar", "tps,mspt,ping")
        );

        ignoredMemoryPools.clear();
        ignoredMemoryPools.addAll(config.getStringList("ignored_memory_pools"));
    }

    public void save() {
        final FileConfiguration config = tabTPS.getConfig();

        config.set("defaults.tab_tps", userPrefsDefaults.isTab());
        config.set("defaults.action_bar_tps", userPrefsDefaults.isActionBar());

        config.set("modules.tab_header", modules.getTabHeader());
        config.set("modules.tab_footer", modules.getTabFooter());
        config.set("modules.action_bar", modules.getActionBar());

        config.set("ignored_memory_pools", ignoredMemoryPools);

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
        private String tabHeader;

        @Getter @Setter
        private String tabFooter;

        @Getter @Setter
        private String actionBar;
    }
}
