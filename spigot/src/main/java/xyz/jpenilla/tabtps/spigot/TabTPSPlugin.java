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
package xyz.jpenilla.tabtps.spigot;

import cloud.commandframework.CommandManager;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.common.collect.ImmutableList;
import io.papermc.lib.PaperLib;
import java.nio.file.Path;
import java.util.logging.Level;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.jpenilla.jmplib.BasePlugin;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.TabTPSPlatform;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.command.commands.TickInfoCommand;
import xyz.jpenilla.tabtps.common.service.TickTimeService;
import xyz.jpenilla.tabtps.common.service.UserService;
import xyz.jpenilla.tabtps.common.util.UpdateChecker;
import xyz.jpenilla.tabtps.spigot.command.BukkitConsoleCommander;
import xyz.jpenilla.tabtps.spigot.command.BukkitPingCommand;
import xyz.jpenilla.tabtps.spigot.command.PaperTickInfoCommandFormatter;
import xyz.jpenilla.tabtps.spigot.service.BukkitUserService;
import xyz.jpenilla.tabtps.spigot.service.PaperTickTimeService;
import xyz.jpenilla.tabtps.spigot.service.SpigotTickTimeService;

public final class TabTPSPlugin extends BasePlugin implements TabTPSPlatform<Player, BukkitUser> {
  private TabTPS tabTPS;
  private PaperCommandManager<Commander> commandManager;
  private UserService<Player, BukkitUser> userService;
  private TickTimeService tickTimeService;
  private Logger logger;

  @Override
  public void onPluginEnable() {
    PaperLib.suggestPaper(this, Level.WARNING);
    this.logger = LoggerFactory.getLogger(this.getLogger().getName());
    if (this.craftBukkit()) {
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }
    if (PaperLib.getMinecraftVersion() < 16 || !PaperLib.isPaper()) {
      this.tickTimeService = new SpigotTickTimeService();
    } else {
      this.tickTimeService = new PaperTickTimeService();
    }

    try {
      this.setupCommandManager();
    } catch (final Exception e) {
      throw new IllegalStateException("Failed to initialize command manager", e);
    }
    this.userService = new BukkitUserService(this);

    this.tabTPS = new TabTPS(this);
    this.registerCommands();

    Bukkit.getPluginManager().registerEvents(new JoinQuitListener(this), this);

    if (this.tabTPS.configManager().pluginSettings().updateChecker()) {
      Bukkit.getScheduler().runTaskAsynchronously(this, () ->
        UpdateChecker.checkVersion(this.getDescription().getVersion()).forEach(this.logger::info)
      );
    }
    final Metrics metrics = new Metrics(this, 8458);
  }

  @Override
  public void onDisable() {
    if (this.tabTPS != null) { // don't shutdown if we have an exception before init completes
      this.tabTPS.shutdown();
    }
  }

  @Override
  public void shutdown() {
    Bukkit.getPluginManager().disablePlugin(this);
  }

  @Override
  public void onReload() {
    if (PaperLib.getMinecraftVersion() >= 13) {
      Bukkit.getScheduler().runTask(this, () -> ImmutableList.copyOf(Bukkit.getOnlinePlayers()).forEach(Player::updateCommands));
    }
  }

  private void setupCommandManager() throws Exception {
    this.commandManager = new PaperCommandManager<>(
      this,
      AsynchronousCommandExecutionCoordinator
        .<Commander>newBuilder().build(),
      commandSender -> {
        if (commandSender instanceof Player) {
          return this.userService().user((Player) commandSender);
        }
        return BukkitConsoleCommander.from(this.audiences(), commandSender);
      },
      commander -> {
        if (commander instanceof BukkitConsoleCommander) {
          return ((BukkitConsoleCommander) commander).commandSender();
        } else if (commander instanceof BukkitUser) {
          return ((BukkitUser) commander).base();
        }
        throw new IllegalArgumentException();
      }
    );

    /* Register Brigadier */
    if (this.commandManager.queryCapability(CloudBukkitCapabilities.BRIGADIER)) {
      this.commandManager.registerBrigadier();
      final CloudBrigadierManager<Commander, ?> brigadierManager = this.commandManager.brigadierManager();
      if (brigadierManager != null) {
        brigadierManager.setNativeNumberSuggestions(false);
      }
      this.logger().info("Successfully registered Mojang Brigadier support for commands.");
    }

    /* Register Asynchronous Completion Listener */
    if (this.commandManager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
      this.commandManager.registerAsynchronousCompletions();
      this.logger.info("Successfully registered asynchronous command completion listener.");
    }
  }

  private void registerCommands() {
    if (PaperLib.getMinecraftVersion() >= 15 && PaperLib.isPaper()) {
      TickInfoCommand.withFormatter(this.tabTPS, this.tabTPS.commands(), new PaperTickInfoCommandFormatter()).register();
    } else {
      TickInfoCommand.defaultFormatter(this.tabTPS, this.tabTPS.commands()).register();
    }
    new BukkitPingCommand(this, this.tabTPS.commands()).register();
  }

  @Override
  public @NonNull CommandManager<Commander> commandManager() {
    return this.commandManager;
  }

  @Override
  public @NonNull UserService<Player, BukkitUser> userService() {
    return this.userService;
  }

  @Override
  public @NonNull Path dataDirectory() {
    return this.getDataFolder().toPath();
  }

  @Override
  public @NonNull TabTPS tabTPS() {
    return this.tabTPS;
  }

  @Override
  public @NonNull TickTimeService tickTimeService() {
    return this.tickTimeService;
  }

  @Override
  public int maxPlayers() {
    return Bukkit.getMaxPlayers();
  }

  @Override
  public @NonNull Logger logger() {
    return this.logger;
  }

  private boolean craftBukkit() {
    if (!PaperLib.isSpigot()) {
      this.logger.error("==========================================");
      this.logger.error("TabTPS is not compatible with CraftBukkit.");
      this.logger.error("You must use either Spigot or later forks");
      this.logger.error("such as Paper in order to use TabTPS.");
      this.logger.error("==========================================");
      return true;
    }
    return false;
  }
}
