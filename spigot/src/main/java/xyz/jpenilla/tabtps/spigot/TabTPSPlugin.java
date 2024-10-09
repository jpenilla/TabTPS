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
package xyz.jpenilla.tabtps.spigot;

import io.papermc.lib.PaperLib;
import java.nio.file.Path;
import java.util.logging.Level;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.jpenilla.pluginbase.legacy.PluginBase;
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

public final class TabTPSPlugin extends PluginBase implements TabTPSPlatform<Player, BukkitUser> {
  private TabTPS tabTPS;
  private LegacyPaperCommandManager<Commander> commandManager;
  private UserService<Player, BukkitUser> userService;
  private TickTimeService tickTimeService;
  private Logger logger;

  @Override
  public void enable() {
    PaperLib.suggestPaper(this, Level.WARNING);
    this.logger = LoggerFactory.getLogger(this.getLogger().getName());
    if (this.craftBukkit()) {
      this.getServer().getPluginManager().disablePlugin(this);
      return;
    }
    if (PaperLib.getMinecraftVersion() < 16 || !PaperLib.isPaper()) {
      this.tickTimeService = new SpigotTickTimeService();
    } else {
      this.tickTimeService = new PaperTickTimeService();
    }

    this.setupCommandManager();
    this.userService = new BukkitUserService(this);

    this.tabTPS = new TabTPS(this);
    this.registerCommands();

    this.getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);

    if (this.tabTPS.configManager().pluginSettings().updateChecker()) {
      this.getServer().getScheduler().runTaskAsynchronously(this, () ->
        UpdateChecker.checkVersion(this.getDescription().getVersion()).forEach(this.logger::info)
      );
    }
    final Metrics metrics = new Metrics(this, 8458);
  }

  @Override
  public void disable() {
    if (this.tabTPS != null) { // don't shutdown if we have an exception before init completes
      this.tabTPS.shutdown();
    }
  }

  @Override
  public void shutdown() {
    this.getServer().getPluginManager().disablePlugin(this);
  }

  @Override
  public void onReload() {
    if (PaperLib.getMinecraftVersion() >= 13) {
      this.getServer().getOnlinePlayers().forEach(Player::updateCommands);
    }
  }

  private void setupCommandManager() {
    this.commandManager = new LegacyPaperCommandManager<>(
      this,
      ExecutionCoordinator.simpleCoordinator(),
      SenderMapper.create(
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
      )
    );

    if (this.commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
      this.commandManager.registerBrigadier();
    } else if (this.commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
      this.commandManager.registerAsynchronousCompletions();
    }
  }

  private void registerCommands() {
    if (PaperLib.getMinecraftVersion() >= 15 && PaperLib.isPaper()) {
      TickInfoCommand.withFormatter(this.tabTPS, this.tabTPS.commands(), new PaperTickInfoCommandFormatter(this)).register();
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
    return this.getServer().getMaxPlayers();
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
