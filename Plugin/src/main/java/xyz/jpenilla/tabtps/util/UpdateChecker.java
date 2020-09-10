package xyz.jpenilla.tabtps.util;

import com.google.common.base.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import xyz.jpenilla.tabtps.TabTPS;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UpdateChecker {
    private final TabTPS plugin;
    private final JsonParser parser = new JsonParser();
    private final String githubRepo;

    public UpdateChecker(TabTPS plugin, String githubRepo) {
        this.plugin = plugin;
        this.githubRepo = githubRepo;
    }

    public void checkVersion() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final JsonArray result;
            try {
                result = parser.parse(new InputStreamReader(new URL("https://api.github.com/repos/" + githubRepo + "/releases").openStream(), Charsets.UTF_8)).getAsJsonArray();
            } catch (IOException exception) {
                plugin.getLogger().info("Cannot look for updates: " + exception.getMessage());
                return;
            }

            final Map<String, String> versionMap = new LinkedHashMap<>();
            result.forEach(element -> versionMap.put(element.getAsJsonObject().get("tag_name").getAsString(), element.getAsJsonObject().get("html_url").getAsString()));
            final List<String> versionList = new LinkedList<>(versionMap.keySet());
            final String currentVersion = "v" + plugin.getDescription().getVersion();
            if (versionList.get(0).equals(currentVersion)) {
                plugin.getLogger().info("You are running the latest version of " + plugin.getName() + "! :)");
                return;
            }
            if (currentVersion.contains("SNAPSHOT")) {
                plugin.getLogger().info("You are running a development build of " + plugin.getName() + "! (" + currentVersion + ")");
                plugin.getLogger().info("The latest official release is " + versionList.get(0));
                return;
            }
            final int versionsBehind = versionList.indexOf(currentVersion);
            plugin.getLogger().info("There is an update available for " + plugin.getName() + "!");
            plugin.getLogger().info("You are running version " + currentVersion + ", which is " + (versionsBehind == -1 ? "many" : versionsBehind) + " versions outdated.");
            plugin.getLogger().info("Download the latest version, " + versionList.get(0) + " from GitHub at the link below:");
            plugin.getLogger().info(versionMap.get(versionList.get(0)));
        });
    }
}