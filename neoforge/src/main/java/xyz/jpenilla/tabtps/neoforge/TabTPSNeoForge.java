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
package xyz.jpenilla.tabtps.neoforge;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContext;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.neoforge.NeoForgeServerCommandManager;
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
import xyz.jpenilla.tabtps.common.util.TranslatableProvider;
import xyz.jpenilla.tabtps.common.util.UpdateChecker;
import xyz.jpenilla.tabtps.neoforge.command.NeoForgeConsoleCommander;
import xyz.jpenilla.tabtps.neoforge.command.NeoForgePingCommand;
import xyz.jpenilla.tabtps.neoforge.command.NeoForgeTickInfoCommandFormatter;
import xyz.jpenilla.tabtps.neoforge.service.NeoForgeUserService;

@Mod("tabtps")
public final class TabTPSNeoForge implements TabTPSPlatform<ServerPlayer, NeoForgeUser> {
  private static TabTPSNeoForge instance = null;
  private final Path configDirectory = Path.of("config", "tabtps");
  private final Logger logger = LoggerFactory.getLogger("TabTPS");
  private final NeoForgeUserService userService;
  private final TabTPS tabTPS;
  private final NeoForgeServerCommandManager<Commander> commandManager;
  private MinecraftServer server;

  public TabTPSNeoForge(final ModContainer modContainer) {
    if (instance != null) {
      throw new IllegalStateException("Cannot create a second instance of " + this.getClass().getName());
    }
    instance = this;

    TranslatableProvider.MOD_JAR_OVERRIDE = modContainer.getModInfo().getOwningFile().getFile().getFilePath();

    this.userService = new NeoForgeUserService(this); // todo store in level container?

    this.commandManager = new NeoForgeServerCommandManager<>(
      ExecutionCoordinator.simpleCoordinator(),
      SenderMapper.create(
        commandSourceStack -> {
          final Entity entity = commandSourceStack.getEntity();
          if (entity instanceof ServerPlayer player) {
            final NeoForgeUser user = this.userService().user(player);
            return new DelegateUser<>(user, commandSourceStack);
          }
          return new NeoForgeConsoleCommander(commandSourceStack);
        },
        commander -> {
          if (commander instanceof NeoForgeConsoleCommander consoleCommander) {
            return consoleCommander.commandSourceStack();
          } else if (commander instanceof DelegateUser<?, ?> user) {
            return (CommandSourceStack) user.c();
          }
          throw new IllegalArgumentException();
        }
      )
    );

    this.tabTPS = new TabTPS(this);

    TickInfoCommand.withFormatter(this.tabTPS, this.tabTPS.commands(), new NeoForgeTickInfoCommandFormatter(this)).register();
    new NeoForgePingCommand(this, this.tabTPS.commands()).register();

    NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> {
      this.server = event.getServer();

      if (this.tabTPS.configManager().pluginSettings().updateChecker()) {
        CompletableFuture.runAsync(() -> UpdateChecker.checkVersion(Constants.TABTPS_VERSION).forEach(this.logger::info));
      }
    });

    NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> {
      if (event.getServer().isDedicatedServer()) {
        this.tabTPS.shutdown();
      } else {
        this.userService.flush();
      }
      this.server = null;
    });

    NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
      if (event.getEntity() instanceof ServerPlayer player) {
        this.userService.handleJoin(player);
      }
    });
    NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
      if (event.getEntity() instanceof ServerPlayer player) {
        this.userService.handleQuit(player);
      }
    });
    NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) -> {
      if (event.getEntity() instanceof ServerPlayer player) {
        this.userService.replacePlayer(player);
      }
    });

    NeoForge.EVENT_BUS.addListener((PermissionGatherEvent.Nodes event) -> {
      final List<PermissionNode<?>> permissions = new ArrayList<>(List.of(
        node(Constants.PERMISSION_COMMAND_ERROR_HOVER_STACKTRACE)
      ));
      for (final String permission : this.tabTPS.configManager().displayConfigsByPermission().keySet()) {
        if (!permission.isBlank()) {
          permissions.add(node(permission));
        }
      }
      event.addNodes(permissions);
    });

    this.logger.info("Done initializing TabTPS.");
  }

  private static PermissionNode<Boolean> node(final String permission) {
    final int i = permission.indexOf(".");
    return new PermissionNode<>(
      permission.substring(0, i),
      permission.substring(i + 1),
      PermissionTypes.BOOLEAN,
      TabTPSNeoForge::defaultPermissionHandler
    );
  }

  private static Boolean defaultPermissionHandler(
    final @Nullable ServerPlayer player,
    final UUID uuid,
    final PermissionDynamicContext<?>... contexts
  ) {
    return player != null && player.permissions().hasPermission(new Permission.HasCommandLevel(player.level().getServer().operatorUserPermissions().level()));
  }

  public static @NonNull TabTPSNeoForge get() {
    return instance;
  }

  public @NonNull MinecraftServer server() {
    return Objects.requireNonNull(this.server, "server is null");
  }

  @Override
  public @NonNull UserService<ServerPlayer, NeoForgeUser> userService() {
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
  public @NonNull NeoForgeServerCommandManager<Commander> commandManager() {
    return this.commandManager;
  }

  @Override
  public Throwable asComponentMessageThrowable(final Throwable thr) {
    if (thr instanceof CommandSyntaxException e) {
      return (Throwable) MinecraftServerAudiences.of(this.server()).asComponentThrowable(e);
    }
    return thr;
  }
}
