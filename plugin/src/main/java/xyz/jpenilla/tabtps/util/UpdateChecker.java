/*
 * This file is part of TabTPS, licensed under the MIT License.
 *
 * Copyright (c) 2020-2021 Jason Penilla
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.jpenilla.tabtps.util;

import com.google.common.base.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
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

  public UpdateChecker(final @NonNull TabTPS plugin, final @NonNull String githubRepo) {
    this.plugin = plugin;
    this.githubRepo = githubRepo;
  }

  public void checkVersion() {
    Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
      final JsonArray result;
      try {
        result = this.parser.parse(new InputStreamReader(new URL("https://api.github.com/repos/" + this.githubRepo + "/releases").openStream(), Charsets.UTF_8)).getAsJsonArray();
      } catch (final IOException exception) {
        this.plugin.getLogger().info("Cannot look for updates: " + exception.getMessage());
        return;
      }

      final Map<String, String> versionMap = new LinkedHashMap<>();
      result.forEach(element -> versionMap.put(element.getAsJsonObject().get("tag_name").getAsString(), element.getAsJsonObject().get("html_url").getAsString()));
      final List<String> versionList = new LinkedList<>(versionMap.keySet());
      final String currentVersion = "v" + this.plugin.getDescription().getVersion();
      if (versionList.get(0).equals(currentVersion)) {
        this.plugin.getLogger().info("You are running the latest version of " + this.plugin.getName() + "! :)");
        return;
      }
      if (currentVersion.contains("SNAPSHOT")) {
        this.plugin.getLogger().info("You are running a development build of " + this.plugin.getName() + "! (" + currentVersion + ")");
        this.plugin.getLogger().info("The latest official release is " + versionList.get(0));
        return;
      }
      final int versionsBehind = versionList.indexOf(currentVersion);
      this.plugin.getLogger().info("There is an update available for " + this.plugin.getName() + "!");
      this.plugin.getLogger().info("You are running version " + currentVersion + ", which is " + (versionsBehind == -1 ? "many" : versionsBehind) + " versions outdated.");
      this.plugin.getLogger().info("Download the latest version, " + versionList.get(0) + " from GitHub at the link below:");
      this.plugin.getLogger().info(versionMap.get(versionList.get(0)));
    });
  }
}
