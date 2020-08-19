package xyz.jpenilla.tabtps.util;

import org.bukkit.scheduler.BukkitRunnable;
import xyz.jpenilla.tabtps.TabTPS;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class UpdateChecker {

    private final TabTPS plugin;
    private final int resourceId;

    public UpdateChecker(TabTPS plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void checkVersion() {
        class CheckVersionTask extends BukkitRunnable {
            @Override
            public void run() {
                try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId).openStream(); Scanner scanner = new Scanner(inputStream)) {
                    if (scanner.hasNext()) {
                        String latest = scanner.next();
                        if (plugin.getDescription().getVersion().equalsIgnoreCase(latest)) {
                            plugin.getLogger().info("You are running the latest version of " + plugin.getName() + "! :)");
                        } else if (plugin.getDescription().getVersion().contains("SNAPSHOT")) {
                            plugin.getLogger().info("[!] You are running a development build of " + plugin.getName() + " (" + plugin.getDescription().getVersion() + ") [!]");
                        } else {
                            plugin.getLogger().info("[!] You are running an outdated version of " + plugin.getName() + " (" + plugin.getDescription().getVersion() + ") [!]");
                            plugin.getLogger().info("Version " + latest + " is available at https://www.spigotmc.org/resources/tabtps.82528/");
                        }
                    }
                } catch (IOException exception) {
                    plugin.getLogger().info("Cannot look for updates: " + exception.getMessage());
                }
            }
        }
        new CheckVersionTask().runTaskAsynchronously(plugin);
    }
}