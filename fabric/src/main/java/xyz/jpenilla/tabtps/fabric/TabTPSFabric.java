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
package xyz.jpenilla.tabtps.fabric;

import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.fabric.FabricServerCommandManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.minecraft.commands.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.TabTPSPlatform;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.command.commands.TickInfoCommand;
import xyz.jpenilla.tabtps.common.service.TickTimeService;
import xyz.jpenilla.tabtps.common.service.UserService;
import xyz.jpenilla.tabtps.common.util.UpdateChecker;
import xyz.jpenilla.tabtps.fabric.access.CommandSouceStackAccess;
import xyz.jpenilla.tabtps.fabric.command.FabricConsoleCommander;
import xyz.jpenilla.tabtps.fabric.command.FabricTickInfoCommandFormatter;
import xyz.jpenilla.tabtps.fabric.service.FabricTickTimeService;
import xyz.jpenilla.tabtps.fabric.service.FabricUserService;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class TabTPSFabric implements ModInitializer, TabTPSPlatform<ServerPlayer, FabricUser> {
  private static TabTPSFabric instance = null;
  private final Path dataDirectory = new File("").getAbsoluteFile().toPath().resolve("config/TabTPS");
  private final Logger logger = LoggerFactory.getLogger("TabTPS");
  private final FabricUserService userService;
  private final TabTPS tabTPS;
  private final CommandManager<Commander> commandManager;
  private final TickTimeService tickTimeService;
  private FabricServerAudiences serverAudiences;
  private MinecraftServer server;

  public TabTPSFabric() {
    if (instance != null) {
      throw new IllegalStateException("Cannot create a second instance of " + this.getClass().getName());
    }
    instance = this;

    this.tickTimeService = new FabricTickTimeService(this);
    this.userService = new FabricUserService(this); // todo store in level container?

    this.commandManager = new FabricServerCommandManager<>(
      AsynchronousCommandExecutionCoordinator.<Commander>newBuilder().build(),
      commandSourceStack -> {
        final CommandSource commandSource = ((CommandSouceStackAccess) commandSourceStack).tabtps$commandSource();
        if (commandSource instanceof ServerPlayer) {
          return this.userService().user((ServerPlayer) commandSource);
        }
        return new FabricConsoleCommander(this, commandSourceStack);
      },
      commander -> {
        if (commander instanceof FabricConsoleCommander) {
          return ((FabricConsoleCommander) commander).commandSourceStack();
        } else if (commander instanceof FabricUser) {
          return ((FabricUser) commander).base().createCommandSourceStack();
        }
        throw new IllegalArgumentException();
      }
    );
    ((FabricServerCommandManager<Commander>) this.commandManager).brigadierManager().setNativeNumberSuggestions(false);

    this.tabTPS = new TabTPS(this);

    TickInfoCommand.withFormatter(this.tabTPS, this.tabTPS.commands(), new FabricTickInfoCommandFormatter(this)).register();
    // todo ping targets command
  }

  public static @NonNull TabTPSFabric get() {
    return instance;
  }

  @Override
  public void onInitialize() {
    ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
      this.server = minecraftServer;
      this.serverAudiences = FabricServerAudiences.of(minecraftServer);

      CompletableFuture.runAsync(() -> UpdateChecker.checkVersion("{version}").forEach(this.logger::info));
    });

    ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> {
      if (this.tabTPS != null) {
        if (minecraftServer.isDedicatedServer()) {
          this.tabTPS.shutdown();
        } else {
          this.userService.flush();
        }
      }
      this.server = null;
      this.serverAudiences = null;
    });

    /* Seems to trigger too early for permission checks with LP (NPE)
    ServerPlayConnectionEvents.JOIN.register((serverGamePacketListener, packetSender, minecraftServer) ->
      this.userService.handleJoin(serverGamePacketListener.player));

    ServerPlayConnectionEvents.DISCONNECT.register((serverGamePacketListener, minecraftServer) ->
      this.userService.handleQuit(serverGamePacketListener.player));
    */
  }

  public @NonNull FabricServerAudiences serverAudiences() {
    return this.serverAudiences;
  }

  public @NonNull MinecraftServer server() {
    return this.server;
  }

  @Override
  public @NonNull UserService<ServerPlayer, FabricUser> userService() {
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
    return this.tickTimeService;
  }

  @Override
  public int maxPlayers() {
    return this.server.getMaxPlayers();
  }

  @Override
  public void shutdown() {
  }

  @Override
  public void onReload() {
    this.server().getPlayerList().getPlayers().forEach(player ->
      this.server.getCommands().sendCommands(player));
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
