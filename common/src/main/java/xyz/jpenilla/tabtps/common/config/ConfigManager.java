/*
 * This file is part of TabTPS, licensed under the MIT License.
 *
 * Copyright (c) 2020-2024 Jason Penilla
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
package xyz.jpenilla.tabtps.common.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.ConfigurateException;

public final class ConfigManager {
  private final Path dataDirectory;

  private final ConfigLoader<PluginSettings> pluginSettingsLoader;
  private PluginSettings pluginSettings;

  private final Path displayConfigsPath;
  private final Map<DisplayConfig, ConfigLoader<DisplayConfig>> displayConfigs = new HashMap<>();
  private final Map<String, DisplayConfig> displayConfigsByPermission = new HashMap<>();

  private final Path themePath;
  private final Map<Theme, ConfigLoader<Theme>> themes = new HashMap<>();
  private final Map<String, Theme> themesByName = new HashMap<>();

  public ConfigManager(final @NonNull Path dataDirectory) {
    this.dataDirectory = dataDirectory;

    this.pluginSettingsLoader = new ConfigLoader<>(
      PluginSettings.class,
      this.dataDirectory.resolve("main.conf"),
      options -> options.header("TabTPS main plugin settings")
    );

    this.displayConfigsPath = this.dataDirectory.resolve("display-configs");
    this.themePath = this.dataDirectory.resolve("themes");
  }

  public void load() throws ConfigurateException {
    tryCreateDirectoryIfNeeded(this.dataDirectory);
    this.pluginSettings = this.pluginSettingsLoader.load();

    tryCreateDirectoryIfNeeded(this.themePath);
    try {
      this.themes.clear();
      this.themesByName.clear();
      final List<Path> existingFiles = Files.list(this.themePath).collect(Collectors.toList());
      final List<Path> paths = existingFiles.size() != 0 ? existingFiles : Collections.singletonList(this.themePath.resolve("default.conf"));
      for (final Path path : paths) {
        if (path.toString().endsWith(".conf")) {
          final ConfigLoader<Theme> loader = new ConfigLoader<>(
            Theme.class,
            path,
            options -> options.header("Theme")
          );
          final Theme config = loader.load();
          this.themes.put(config, loader);
          this.themesByName.put(path.getFileName().toString().split("\\.")[0], config);
        }
      }
    } catch (final IOException e) {
      throw new ConfigurateException("Failed to load themes", e);
    }

    tryCreateDirectoryIfNeeded(this.displayConfigsPath);
    try {
      this.displayConfigs.clear();
      this.displayConfigsByPermission.clear();
      final List<Path> existingFiles = Files.list(this.displayConfigsPath).collect(Collectors.toList());
      final List<Path> paths = existingFiles.size() != 0 ? existingFiles : Collections.singletonList(this.displayConfigsPath.resolve("default.conf"));
      final Set<String> usedPermissions = new HashSet<>();
      for (final Path path : paths) {
        if (path.toString().endsWith(".conf")) {
          final ConfigLoader<DisplayConfig> loader = new ConfigLoader<>(
            DisplayConfig.class,
            path,
            options -> options.header(
              "TabTPS display configuration\n"
                + "\n"
                + "   Available modules: [tps, mspt, memory, ping, cpu, players]\n"
                + "   Modules are configured in comma separated format, i.e. \"cpu,tps,mspt\", \"ping\", or \"\" (no modules)"
            )
          );
          final DisplayConfig config = loader.load();
          if (usedPermissions.contains(config.permission())) {
            throw new ConfigurateException(String.format(
              "Cannot load config with duplicate permission '%s': %s",
              config.permission(),
              path
            ));
          }
          usedPermissions.add(config.permission());
          this.displayConfigs.put(config, loader);
          this.displayConfigsByPermission.put(config.permission(), config);
        }
      }
    } catch (final IOException e) {
      throw new ConfigurateException("Failed to load display configs", e);
    }

    final Set<String> permissions = this.displayConfigs.keySet().stream()
      .map(DisplayConfig::permission).collect(Collectors.toSet());
    this.pluginSettings.permissionPriorities().addAll(permissions);
    this.pluginSettings.permissionPriorities().removeIf(p -> !permissions.contains(p));

    this.save();
  }

  public void save() throws ConfigurateException {
    this.pluginSettingsLoader.save(this.pluginSettings);

    for (final Map.Entry<DisplayConfig, ConfigLoader<DisplayConfig>> entry : this.displayConfigs.entrySet()) {
      entry.getValue().save(entry.getKey());
    }
    for (final Map.Entry<Theme, ConfigLoader<Theme>> entry : this.themes.entrySet()) {
      entry.getValue().save(entry.getKey());
    }
  }

  public @NonNull PluginSettings pluginSettings() {
    return this.pluginSettings;
  }

  public @NonNull Collection<DisplayConfig> displayConfigs() {
    return Collections.unmodifiableSet(this.displayConfigs.keySet());
  }

  public @NonNull Theme theme(final @NonNull String name) {
    final Theme theme = this.themesByName.get(name);
    if (theme == null) {
      throw new IllegalArgumentException(String.format(
        "Uh oh! No theme found with the name '%s'. Available themes: [%s]",
        name,
        String.join(", ", this.themesByName.keySet())
      ));
    }
    return theme;
  }

  public @NonNull Map<String, DisplayConfig> displayConfigsByPermission() {
    return this.displayConfigsByPermission;
  }

  private static void tryCreateDirectoryIfNeeded(final @NonNull Path directory) {
    if (Files.exists(directory)) {
      return;
    }
    try {
      Files.createDirectory(directory);
    } catch (final IOException e) {
      throw new IllegalStateException("Failed to create directory: " + directory, e);
    }
  }
}
