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
package xyz.jpenilla.tabtps.common.command.commands;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionHandler;
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import cloud.commandframework.minecraft.extras.RichDescription;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.feature.pagination.Pagination;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.User;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.command.Commands;
import xyz.jpenilla.tabtps.common.command.TabTPSCommand;
import xyz.jpenilla.tabtps.common.command.exception.CommandCompletedException;
import xyz.jpenilla.tabtps.common.config.Theme;
import xyz.jpenilla.tabtps.common.util.Constants;
import xyz.jpenilla.tabtps.common.util.PingUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PingCommand extends TabTPSCommand {
  public PingCommand(final @NonNull TabTPS tabTPS, final @NonNull Commands commands) {
    super(tabTPS, commands);
  }

  @Override
  public void register() {
    this.commandManager.command(
      this.commandManager.commandBuilder("ping")
        .permission(Constants.PERMISSION_COMMAND_PING)
        .meta(MinecraftExtrasMetaKeys.DESCRIPTION, Component.translatable("tabtps.command.ping_self.description"))
        .handler(this::onPingSelf)
    );

    this.commandManager.command(
      this.commandManager.commandBuilder("pingall")
        .argument(
          IntegerArgument.<Commander>newBuilder("page")
            .withMin(1)
            .withMax(999)
            .asOptionalWithDefault("1"),
          RichDescription.translatable("tabtps.command.ping.arguments.page")
        )
        .permission(Constants.PERMISSION_COMMAND_PING_OTHERS)
        .meta(MinecraftExtrasMetaKeys.DESCRIPTION, Component.translatable("tabtps.command.ping_all.description"))
        .handler(this::onPingAll)
    );
  }

  protected <T> void registerPingTargetsCommand(
    final @NonNull CommandArgument<Commander, T> targetsArgument,
    final @NonNull CommandExecutionHandler<Commander> handler
  ) {
    this.commandManager.command(
      this.commandManager.commandBuilder("ping")
        .argument(targetsArgument, RichDescription.translatable("tabtps.command.ping_target.arguments.target"))
        .argument(
          IntegerArgument.<Commander>newBuilder("page")
            .withMin(1)
            .withMax(999)
            .asOptionalWithDefault("1"),
          RichDescription.translatable("tabtps.command.ping.arguments.page")
        )
        .permission(Constants.PERMISSION_COMMAND_PING_OTHERS)
        .meta(MinecraftExtrasMetaKeys.DESCRIPTION, Component.translatable("tabtps.command.ping_target.description"))
        .handler(handler)
    );
  }

  private void onPingAll(final @NonNull CommandContext<Commander> context) {
    final Commander sender = context.getSender();
    final int page = context.get("page");
    this.pingMultiple(sender, Collections.unmodifiableCollection(this.tabTPS.platform().userService().onlineUsers()), page, "pingall");
  }

  private void onPingSelf(final @NonNull CommandContext<Commander> context) {
    final Commander sender = context.getSender();
    if (!(sender instanceof User)) {
      throw CommandCompletedException.withMessage(
        TextComponent.ofChildren(
          Constants.PREFIX,
          Component.space(),
          Component.translatable("tabtps.command.ping.text.console_must_provide_player", NamedTextColor.RED)
        )
      );
    }
    final User<?> player = (User<?>) sender;
    player.sendMessage(
      Component.text()
        .append(Constants.PREFIX)
        .append(Component.space())
        .append(
          Component.translatable(
            "tabtps.command.ping_self.text.your_ping",
            NamedTextColor.GRAY,
            TextComponent.ofChildren(
              PingUtil.coloredPing(player, Theme.DEFAULT.colorScheme()),
              Component.translatable(
                "tabtps.label.milliseconds_short",
                Theme.DEFAULT.colorScheme().textSecondary()
              )
            )
          )
        )
    );
  }

  protected final void pingTargets(
    final @NonNull Commander commander,
    final @NonNull List<User<?>> targets,
    final @NonNull String inputString,
    final int page
  ) {
    if (targets.isEmpty()) {
      throw CommandCompletedException.withMessage(
        TextComponent.ofChildren(
          Constants.PREFIX,
          Component.space(),
          Component.translatable(
            "tabtps.misc.command.text.no_players_found",
            NamedTextColor.RED,
            Component.text(inputString)
          )
        )
      );
    }
    if (targets.size() > 1) {
      this.pingMultiple(commander, targets, page, String.format("ping %s", inputString));
      return;
    }
    final User<?> targetPlayer = targets.get(0);
    commander.sendMessage(
      Component.text()
        .append(Constants.PREFIX)
        .append(Component.space())
        .append(Component.translatable(
          "tabtps.command.ping_target.text.targets_ping",
          NamedTextColor.GRAY,
          targetPlayer.displayName(),
          TextComponent.ofChildren(
            PingUtil.coloredPing(targetPlayer, Theme.DEFAULT.colorScheme()),
            Component.translatable(
              "tabtps.label.milliseconds_short",
              Theme.DEFAULT.colorScheme().textSecondary()
            )
          )
        ))
        .build()
    );
  }

  private void pingMultiple(
    final @NonNull Commander sender,
    final @NonNull Collection<User<?>> targets,
    final int page,
    final @NonNull String commandPrefix
  ) {
    final List<Component> content = new ArrayList<>();
    final List<Integer> pings = new ArrayList<>();
    targets.stream().sorted(Comparator.comparing(User::ping)).forEach(player -> {
      content.add(TextComponent.ofChildren(
        Component.space(),
        Component.text("-", NamedTextColor.GRAY),
        Component.space(),
        player.displayName(),
        Component.text(":", NamedTextColor.GRAY),
        Component.space(),
        PingUtil.coloredPing(player, Theme.DEFAULT.colorScheme()),
        Component.translatable("tabtps.label.milliseconds_short", NamedTextColor.GRAY)
      ));
      pings.add(player.ping());
    });
    final int avgPing = (int) Math.round(pings.stream().mapToInt(i -> i).average().orElse(0));
    final Component playerAmount = TextComponent.ofChildren(
      Component.text("(", NamedTextColor.WHITE),
      Component.translatable(
        targets.size() == 1 ? "tabtps.command.ping.text.amount_players_singular" : "tabtps.command.ping.text.amount_players",
        NamedTextColor.GRAY,
        Component.text(this.tabTPS.platform().userService().onlinePlayers(), NamedTextColor.GREEN),
        Component.empty()
      ),
      Component.text(")", NamedTextColor.WHITE)
    );
    final Component summary = TextComponent.ofChildren(
      Component.translatable("tabtps.command.ping.text.average_ping", NamedTextColor.WHITE),
      Component.text(": ", NamedTextColor.GRAY),
      PingUtil.coloredPing(avgPing, Theme.DEFAULT.colorScheme()),
      Component.translatable("tabtps.label.milliseconds_short", NamedTextColor.GRAY),
      Component.space(),
      playerAmount
    );
    final List<Component> messages = new ArrayList<>();
    messages.add(Component.empty());
    messages.addAll(pagination(commandPrefix).render(content, page));
    messages.add(Component.empty());
    messages.add(summary);
    messages.forEach(sender::sendMessage);
  }

  private static @NonNull Pagination<Component> pagination(final @NonNull String prefix) {
    return Pagination.builder()
      .resultsPerPage(10)
      .width(38)
      .line(line -> {
        line.character('-');
        line.style(Style.style(TextColor.fromHexString("#47C8FF"), TextDecoration.STRIKETHROUGH));
      })
      .build(
        TextComponent.ofChildren(
          Constants.PREFIX,
          Component.space(),
          Component.translatable("tabtps.command.ping.text.player_pings")
        ),
        (value, index) -> Collections.singleton(value),
        page -> String.format("/%s %d", prefix, page)
      );
  }
}
