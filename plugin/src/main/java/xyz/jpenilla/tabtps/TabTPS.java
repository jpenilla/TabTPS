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
package xyz.jpenilla.tabtps;

import kr.entree.spigradle.annotations.PluginMain;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.jpenilla.jmplib.BasePlugin;
import xyz.jpenilla.jmplib.Environment;
import xyz.jpenilla.tabtps.command.CommandManager;
import xyz.jpenilla.tabtps.config.ConfigManager;
import xyz.jpenilla.tabtps.config.PluginSettings;
import xyz.jpenilla.tabtps.nms.api.NMS;
import xyz.jpenilla.tabtps.task.TaskManager;
import xyz.jpenilla.tabtps.util.CPUUtil;
import xyz.jpenilla.tabtps.util.PermissionManager;
import xyz.jpenilla.tabtps.util.PingUtil;
import xyz.jpenilla.tabtps.util.TPSUtil;
import xyz.jpenilla.tabtps.util.UpdateChecker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

@PluginMain
public class TabTPS extends BasePlugin {
  private NMS nmsHandler = null;
  private TaskManager taskManager;
  private TPSUtil tpsUtil;
  private CPUUtil cpuUtil;
  private PingUtil pingUtil;
  private UserPreferences userPreferences;
  private PermissionManager permissionManager;
  private ConfigManager configManager;
  private CommandManager commandManager;

  @Override
  public void onPluginEnable() {
    this.setupNMS();

    final TranslationRegistry registry = TranslationRegistry.create(Key.key(this.getName().toLowerCase(Locale.ENGLISH), "translations"));
    final String prefix = "tabtps_";
    final String suffix = ".properties";
    final Set<Locale> locales = new HashSet<>();
    try {
      final Enumeration<URL> urls = this.getClassLoader().getResources("META-INF");
      while (urls.hasMoreElements()) {
        final URL url = urls.nextElement();
        final JarURLConnection connection = (JarURLConnection) (url.openConnection());
        try (final JarFile jar = connection.getJarFile()) {
          locales.addAll(
            Collections.list(jar.entries()).stream()
              .map(ZipEntry::toString)
              .filter(path -> path.startsWith(prefix) && path.endsWith(suffix))
              .map(path -> path.replaceFirst(prefix, "").replaceFirst(suffix, ""))
              .map(name -> name.split("_"))
              .map(locale -> new Locale(locale[0], locale[1]))
              .collect(Collectors.toSet())
          );
        }
      }
    } catch (final IOException e) {
      this.getLogger().log(Level.SEVERE, "Failed to load translations", e);
      this.setEnabled(false);
      return;
    }
    locales.forEach(locale -> registry.registerAll(locale, PropertyResourceBundle.getBundle("tabtps", locale), true));
    GlobalTranslator.get().addSource(registry);

    this.permissionManager = new PermissionManager(this);

    try {
      this.configManager = new ConfigManager(this);
      this.configManager.load();
    } catch (final Exception e) {
      this.getLogger().log(Level.SEVERE, "Failed to load config", e);
      this.setEnabled(false);
      return;
    }

    try {
      this.commandManager = new CommandManager(this);
    } catch (final Exception e) {
      this.getLogger().log(Level.SEVERE, "Failed to initialize command manager.", e);
      this.setEnabled(false);
      return;
    }

    try {
      this.userPreferences = UserPreferences.deserialize(new File(getDataFolder() + File.separator + "user_preferences.json"));
    } catch (final Exception e) {
      this.userPreferences = new UserPreferences();
      this.getLogger().warning("Failed to load user_preferences.json, creating a new one");
    }

    this.tpsUtil = new TPSUtil(this);
    this.cpuUtil = new CPUUtil(this);
    this.pingUtil = new PingUtil(this);
    this.taskManager = new TaskManager(this);
    this.cpuUtil.startRecordingUsage();

    Bukkit.getPluginManager().registerEvents(new JoinQuitListener(this), this);

    if (this.pluginSettings().updateChecker()) {
      UpdateChecker.checkVersion(
        this,
        "jmanpenilla/TabTPS",
        messages -> messages.forEach(this.getLogger()::info)
      );
    }
    final Metrics metrics = new Metrics(this, 8458);
  }

  @Override
  public void onDisable() {
    this.cpuUtil.stopRecordingUsage();
    Bukkit.getScheduler().cancelTasks(this);

    if (!this.getDataFolder().exists()) {
      this.getDataFolder().mkdirs();
    }
    try {
      if (this.userPreferences != null) {
        this.userPreferences.serialize(new FileWriter(new File(getDataFolder() + File.separator + "user_preferences.json")));
      }
    } catch (final IOException e) {
      this.getLogger().log(Level.WARNING, "Failed to save user_preferences.json", e);
    }
  }

  public @NonNull PermissionManager permissionManager() {
    return this.permissionManager;
  }

  public @NonNull ConfigManager configManager() {
    return this.configManager;
  }

  public @NonNull PluginSettings pluginSettings() {
    return this.configManager.pluginSettings();
  }

  public @Nullable NMS nmsHandler() {
    return this.nmsHandler;
  }

  public @NonNull TaskManager taskManager() {
    return this.taskManager;
  }

  public @NonNull TPSUtil tpsUtil() {
    return this.tpsUtil;
  }

  public @NonNull CPUUtil cpuUtil() {
    return this.cpuUtil;
  }

  public @NonNull PingUtil pingUtil() {
    return this.pingUtil;
  }

  public @NonNull UserPreferences userPreferences() {
    return this.userPreferences;
  }

  public @NonNull CommandManager commandManager() {
    return this.commandManager;
  }

  private void setupNMS() {
    if (Environment.majorMinecraftVersion() > 15 && !Environment.paper()) {
      this.getLogger().info("# ");
      this.getLogger().info("# You are not using Paper, and therefore NMS methods must be used to get TPS and MSPT.");
      this.getLogger().info("# Please consider upgrading to Paper for better performance and compatibility at https://papermc.io/downloads");
      this.getLogger().info("# ");
    }

    if (Environment.majorMinecraftVersion() < 16 || !Environment.paper()) {
      try {
        final Class<?> clazz = Class.forName("xyz.jpenilla.tabtps.nms." + Environment.serverApiVersion() + ".NMSHandler");
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
      this.getLogger().info("Loaded NMS support for " + Environment.serverApiVersion());
    }
  }
}
