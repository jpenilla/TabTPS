package xyz.jpenilla.tabtps;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import xyz.jpenilla.jmplib.BasePlugin;
import xyz.jpenilla.tabtps.api.NMS;
import xyz.jpenilla.tabtps.command.CommandTPS;
import xyz.jpenilla.tabtps.command.CommandTabTPS;
import xyz.jpenilla.tabtps.task.TaskManager;
import xyz.jpenilla.tabtps.util.TPSUtil;
import xyz.jpenilla.tabtps.util.UserPrefs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TabTPS extends BasePlugin {

    @Getter private NMS nmsHandler = null;
    @Getter private int majorMinecraftVersion;
    @Getter private TaskManager taskManager;
    @Getter private TPSUtil tpsUtil;
    @Getter private UserPrefs userPrefs;

    @Override
    public void onPluginEnable() {
        final String packageName = this.getServer().getClass().getPackage().getName();
        final String version = packageName.substring(packageName.lastIndexOf('.') + 1);
        majorMinecraftVersion = Integer.parseInt(version.split("_")[1]);

        if (majorMinecraftVersion > 15 && !isPaperServer()) {
            getLogger().info("You are not using Paper, and therefore NMS methods must be used to get TPS and MSPT.");
            getLogger().info("Please consider upgrading to Paper for better performance and compatibility at https://papermc.io/downloads");
        }

        if (majorMinecraftVersion < 16 || !isPaperServer()) {
            try {
                final Class<?> clazz = Class.forName("xyz.jpenilla.tabtps.nms." + version + ".NMSHandler");
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
            this.getLogger().info("Loaded NMS support for " + version);
        }

        this.tpsUtil = new TPSUtil(this);
        this.taskManager = new TaskManager(this);
        try {
            this.userPrefs = UserPrefs.deserialize(new File(getDataFolder() + File.separator + "user_preferences.json"));
        } catch (Exception e) {
            this.userPrefs = new UserPrefs();
            getLogger().warning("Failed to load user_preferences.json, creating a new one");
        }

        PaperCommandManager manager = new PaperCommandManager(this);
        manager.enableUnstableAPI("help");
        manager.registerCommand(new CommandTabTPS());
        manager.registerCommand(new CommandTPS());

        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);

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
}
