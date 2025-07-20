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
package xyz.jpenilla.tabtps.fabric;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.FabricServerCommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.TabTPSPlatform;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.command.DelegateUser;
import xyz.jpenilla.tabtps.common.command.commands.TickInfoCommand;
import xyz.jpenilla.tabtps.common.service.TickTimeService;
import xyz.jpenilla.tabtps.common.service.UserService;
import xyz.jpenilla.tabtps.common.util.Constants;
import xyz.jpenilla.tabtps.common.util.UpdateChecker;
import xyz.jpenilla.tabtps.fabric.command.FabricConsoleCommander;
import xyz.jpenilla.tabtps.fabric.command.FabricPingCommand;
import xyz.jpenilla.tabtps.fabric.command.FabricTickInfoCommandFormatter;
import xyz.jpenilla.tabtps.fabric.service.FabricUserService;

public final class TabTPSFabric implements ModInitializer, TabTPSPlatform<ServerPlayer, FabricUser> {
  private static TabTPSFabric instance = null;
  private final Path configDirectory = FabricLoader.getInstance().getConfigDir().resolve("TabTPS");
  private final Logger logger = LoggerFactory.getLogger("TabTPS");
  private final FabricUserService userService;
  private final TabTPS tabTPS;
  private final FabricServerCommandManager<Commander> commandManager;
  private MinecraftServer server;

  public TabTPSFabric() {
    if (instance != null) {
      throw new IllegalStateException("Cannot create a second instance of " + this.getClass().getName());
    }
    instance = this;

    this.userService = new FabricUserService(this); // todo store in level container?

    this.commandManager = new FabricServerCommandManager<>(
      ExecutionCoordinator.simpleCoordinator(),
      SenderMapper.create(
        commandSourceStack -> {
          final Entity entity = commandSourceStack.getEntity();
          if (entity instanceof ServerPlayer player) {
            final FabricUser user = this.userService().user(player);
            return new DelegateUser<>(user, commandSourceStack);
          }
          return new FabricConsoleCommander(commandSourceStack);
        },
        commander -> {
          if (commander instanceof FabricConsoleCommander consoleCommander) {
            return consoleCommander.commandSourceStack();
          } else if (commander instanceof DelegateUser<?, ?> user) {
            return (CommandSourceStack) user.c();
          }
          throw new IllegalArgumentException();
        }
      )
    );

    this.tabTPS = new TabTPS(this);

    TickInfoCommand.withFormatter(this.tabTPS, this.tabTPS.commands(), new FabricTickInfoCommandFormatter(this)).register();
    new FabricPingCommand(this, this.tabTPS.commands()).register();
    this.logger.info("Done initializing TabTPS.");
  }

  public static @NonNull TabTPSFabric get() {
    return instance;
  }

  @Override
  public void onInitialize() {
    ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
      this.server = minecraftServer;

      if (this.tabTPS.configManager().pluginSettings().updateChecker()) {
        CompletableFuture.runAsync(() -> UpdateChecker.checkVersion(Constants.TABTPS_VERSION).forEach(this.logger::info));
      }
    });

    ServerLifecycleEvents.SERVER_STOPPED.register(minecraftServer -> {
      if (this.tabTPS != null) {
        if (minecraftServer.isDedicatedServer()) {
          this.tabTPS.shutdown();
        } else {
          this.userService.flush();
        }
      }
      this.server = null;
    });

    /* Seems to trigger too early for permission checks with LP (NPE)
    ServerPlayConnectionEvents.JOIN.register((serverGamePacketListener, packetSender, minecraftServer) ->
      this.userService.handleJoin(serverGamePacketListener.player));

    ServerPlayConnectionEvents.DISCONNECT.register((serverGamePacketListener, minecraftServer) ->
      this.userService.handleQuit(serverGamePacketListener.player));
    */
  }

  public @NonNull MinecraftServer server() {
    return Objects.requireNonNull(this.server, "server is null");
  }

  @Override
  public @NonNull UserService<ServerPlayer, FabricUser> userService() {
    return this.userService;
  }

  @Override
  public @NonNull Path dataDirectory() {
    return this.configDirectory;
  }

  @Override
  public @NonNull TabTPS tabTPS() {
    return this.tabTPS;
  }

  @Override
  public @NonNull TickTimeService tickTimeService() {
    return (TickTimeService) this.server;
  }

  @Override
  public int maxPlayers() {
    return this.server().getMaxPlayers();
  }

  @Override
  public void shutdown() {
  }

  @Override
  public void onReload() {
    this.server().getPlayerList().getPlayers().forEach(player ->
      this.server().getCommands().sendCommands(player));
  }

  @Override
  public @NonNull Logger logger() {
    return this.logger;
  }

  @Override
  public @NonNull FabricServerCommandManager<Commander> commandManager() {
    return this.commandManager;
  }
}
