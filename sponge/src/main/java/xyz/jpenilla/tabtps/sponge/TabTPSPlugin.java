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
package xyz.jpenilla.tabtps.sponge;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import java.nio.file.Path;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.sponge.CloudInjectionModule;
import org.incendo.cloud.sponge.SpongeCommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.TabTPSPlatform;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.command.DelegateUser;
import xyz.jpenilla.tabtps.common.command.commands.TickInfoCommand;
import xyz.jpenilla.tabtps.common.service.TickTimeService;
import xyz.jpenilla.tabtps.common.service.UserService;
import xyz.jpenilla.tabtps.sponge.command.SpongeConsoleCommander;
import xyz.jpenilla.tabtps.sponge.command.SpongePingCommand;
import xyz.jpenilla.tabtps.sponge.command.SpongeTickInfoCommandFormatter;
import xyz.jpenilla.tabtps.sponge.service.SpongeUserService;

@Plugin("tabtps")
public final class TabTPSPlugin implements TabTPSPlatform<ServerPlayer, SpongeUser> {
  private final Injector injector;
  private final PluginContainer pluginContainer;
  private final Path dataDirectory;
  private final Logger logger = LoggerFactory.getLogger("TabTPS");
  private final SpongeCommandManager<Commander> commandManager;
  private final SpongeUserService userService;
  private final TabTPS tabTPS;
  private final Game game;

  @Inject
  public TabTPSPlugin(
    final @NonNull PluginContainer pluginContainer,
    @ConfigDir(sharedRoot = false) final @NonNull Path dataDirectory,
    final @NonNull Injector injector,
    final @NonNull Game game
  ) {
    final CloudInjectionModule<Commander> cloudModule = new CloudInjectionModule<>(
      Commander.class,
      ExecutionCoordinator.simpleCoordinator(),
      SenderMapper.create(
        commandCause -> {
          if (commandCause.subject() instanceof ServerPlayer) {
            final SpongeUser user = this.userService().user((ServerPlayer) commandCause.subject());
            return new DelegateUser<>(user, commandCause);
          }
          return new SpongeConsoleCommander(commandCause);
        },
        commander -> {
          if (commander instanceof SpongeConsoleCommander) {
            return ((SpongeConsoleCommander) commander).commandCause();
          } else if (commander instanceof DelegateUser) {
            return (CommandCause) ((DelegateUser<?, ?>) commander).c();
          }
          throw new IllegalArgumentException();
        }
      )
    );
    this.injector = injector.createChildInjector(cloudModule);
    this.game = game;
    this.pluginContainer = pluginContainer;
    this.dataDirectory = dataDirectory;
    this.userService = new SpongeUserService(this);
    this.commandManager = this.injector.getInstance(Key.get(new TypeLiteral<SpongeCommandManager<Commander>>() {
    }));
    this.commandManager.parserMapper().cloudNumberSuggestions(true);
    this.tabTPS = new TabTPS(this);
    TickInfoCommand.withFormatter(this.tabTPS(), this.tabTPS().commands(), new SpongeTickInfoCommandFormatter()).register();
    new SpongePingCommand(this, this.tabTPS.commands()).register();
    game.eventManager().registerListeners(this.pluginContainer, new UserListener(this));
    this.logger.info("Done initializing TabTPS.");
  }

  @Override
  public @NonNull UserService<ServerPlayer, SpongeUser> userService() {
    return this.userService;
  }

  @Override
  public @NonNull Path dataDirectory() {
    return this.dataDirectory;
  }

  @Override
  public @NonNull TabTPS tabTPS() {
    return this.tabTPS;
  }

  @Override
  public @NonNull TickTimeService tickTimeService() {
    return (TickTimeService) this.game.server();
  }

  @Override
  public int maxPlayers() {
    return this.game.server().maxPlayers();
  }

  @Override
  public void shutdown() {
  }

  @Override
  public void onReload() {
    this.game.server().onlinePlayers()
      .forEach(this.game.server().commandManager()::updateCommandTreeForPlayer);
  }

  @Override
  public @NonNull Logger logger() {
    return this.logger;
  }

  @Override
  public @NonNull CommandManager<Commander> commandManager() {
    return this.commandManager;
  }
}
