package xyz.jpenilla.tabtps;

import lombok.Getter;
import org.bstats.bukkit.Metrics;
import xyz.jpenilla.jmplib.BasePlugin;
import xyz.jpenilla.tabtps.api.NMS;
import xyz.jpenilla.tabtps.command.CommandHelper;
import xyz.jpenilla.tabtps.task.TaskManager;
import xyz.jpenilla.tabtps.util.CPUUtil;
import xyz.jpenilla.tabtps.util.TPSUtil;
import xyz.jpenilla.tabtps.util.UpdateChecker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TabTPS extends BasePlugin {
    @Getter private static TabTPS instance;
    @Getter private NMS nmsHandler = null;
    @Getter private TaskManager taskManager;
    @Getter private TPSUtil tpsUtil;
    @Getter private CPUUtil cpuUtil;
    @Getter private UserPrefs userPrefs;
    @Getter private PluginSettings pluginSettings;
    @Getter private CommandHelper commandHelper;

    @Override
    public void onPluginEnable() {
        instance = this;
        setupNMS();

        this.pluginSettings = new PluginSettings(this);
        this.pluginSettings.load();
        this.tpsUtil = new TPSUtil(this);
        this.cpuUtil = new CPUUtil();
        this.taskManager = new TaskManager(this);
        this.taskManager.startRecordCpuTask();
        try {
            this.userPrefs = UserPrefs.deserialize(new File(getDataFolder() + File.separator + "user_preferences.json"));
        } catch (Exception e) {
            this.userPrefs = new UserPrefs();
            getLogger().warning("Failed to load user_preferences.json, creating a new one");
        }
        this.commandHelper = new CommandHelper(this);

        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);

        new UpdateChecker(this, 82528).checkVersion();
        Metrics metrics = new Metrics(this, 8458);
    }

    @Override
    public void onDisable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        try {
            if (this.userPrefs != null) {
                this.userPrefs.serialize(new FileWriter(new File(getDataFolder() + File.separator + "user_preferences.json")));
            }
        } catch (IOException e) {
            getLogger().warning("Failed to save user_preferences.json");
            e.printStackTrace();
        }
    }

    private void setupNMS() {
        if (getMajorMinecraftVersion() > 15 && !isPaperServer()) {
            getLogger().info("You are not using Paper, and therefore NMS methods must be used to get TPS and MSPT.");
            getLogger().info("Please consider upgrading to Paper for better performance and compatibility at https://papermc.io/downloads");
        }

        if (getMajorMinecraftVersion() < 16 || !isPaperServer()) {
            try {
                final Class<?> clazz = Class.forName("xyz.jpenilla.tabtps.nms." + getServerApiVersion() + ".NMSHandler");
                if (NMS.class.isAssignableFrom(clazz)) {
                    this.nmsHandler = (NMS) clazz.getConstructor().newInstance();
                }
            } catch (final Exception e) {
                e.printStackTrace();
                this.getLogger().severe("Could not find support for this Minecraft version.");
                this.getLogger().info("Check for updates at " + getDescription().getWebsite());
                this.setEnabled(false);
                return;
            }
            this.getLogger().info("Loaded NMS support for " + getServerApiVersion());
        }
    }
}
