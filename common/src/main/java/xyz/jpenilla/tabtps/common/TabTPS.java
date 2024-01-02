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
package xyz.jpenilla.tabtps.common;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.command.Commands;
import xyz.jpenilla.tabtps.common.command.TabTPSCommand;
import xyz.jpenilla.tabtps.common.command.commands.AboutCommand;
import xyz.jpenilla.tabtps.common.command.commands.HelpCommand;
import xyz.jpenilla.tabtps.common.command.commands.MemoryCommand;
import xyz.jpenilla.tabtps.common.command.commands.PingCommand;
import xyz.jpenilla.tabtps.common.command.commands.ReloadCommand;
import xyz.jpenilla.tabtps.common.command.commands.ToggleDisplayCommands;
import xyz.jpenilla.tabtps.common.config.ConfigManager;
import xyz.jpenilla.tabtps.common.config.DisplayConfig;
import xyz.jpenilla.tabtps.common.util.CPUMonitor;

public final class TabTPS {
  private final TabTPSPlatform<?, ?> platform;
  private final CPUMonitor cpuMonitor;
  private final ConfigManager configManager;
  private final ScheduledExecutorService executor;
  private final Commands commands;

  public TabTPS(final @NonNull TabTPSPlatform<?, ?> platform) {
    this.platform = platform;
    try {
      Messages.load();
      this.configManager = new ConfigManager(platform.dataDirectory());
      this.configManager.load();
      final ScheduledThreadPoolExecutor ex = new ScheduledThreadPoolExecutor(4);
      ex.setRemoveOnCancelPolicy(true);
      this.executor = Executors.unconfigurableScheduledExecutorService(ex);
      this.commands = new Commands(this, platform.commandManager());
      this.registerCommands();
      this.cpuMonitor = new CPUMonitor();
    } catch (final IOException e) {
      this.platform.shutdown();
      this.shutdown();
      throw initializationFailed(e);
    }
  }

  public void shutdown() {
    if (this.cpuMonitor != null) {
      this.cpuMonitor.shutdown();
    }
    if (this.executor != null) {
      this.executor.shutdown();
    }
    this.platform.userService().flush();
  }

  public synchronized void reload() {
    try {
      this.configManager().load();
    } catch (final IOException e) {
      throw new IllegalStateException("Failed to reload configs", e);
    }
    this.platform.userService().reload();
    this.platform().onReload();
  }

  private void registerCommands() {
    Stream.of(
      new HelpCommand(this, this.commands),
      new ReloadCommand(this, this.commands),
      new ToggleDisplayCommands(this, this.commands),
      new AboutCommand(this, this.commands),
      new PingCommand(this, this.commands),
      new MemoryCommand(this, this.commands)
    ).forEach(TabTPSCommand::register);
  }

  public @NonNull TabTPSPlatform<?, ?> platform() {
    return this.platform;
  }

  public @NonNull ConfigManager configManager() {
    return this.configManager;
  }

  public @NonNull Optional<DisplayConfig> findDisplayConfig(final @NonNull User<?> player) {
    for (final String permission : this.configManager.pluginSettings().permissionPriorities()) {
      if (player.hasPermission(permission) || permission.isEmpty()) {
        return Optional.of(this.configManager.displayConfigsByPermission().get(permission));
      }
    }
    return Optional.empty();
  }

  public @NonNull ScheduledExecutorService executor() {
    return this.executor;
  }

  public @NonNull CPUMonitor cpuMonitor() {
    return this.cpuMonitor;
  }

  public @NonNull Commands commands() {
    return this.commands;
  }

  private static @NonNull IllegalStateException initializationFailed(final Throwable cause) {
    return new IllegalStateException("Failed to initialize TabTPS", cause);
  }
}
