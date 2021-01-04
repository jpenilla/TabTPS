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
package xyz.jpenilla.tabtps.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Range;
import cloud.commandframework.bukkit.arguments.selector.MultiplePlayerSelector;
import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.feature.pagination.Pagination;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.Constants;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.config.Theme;
import xyz.jpenilla.tabtps.module.ModuleRenderer;
import xyz.jpenilla.tabtps.module.PingModule;
import xyz.jpenilla.tabtps.util.PingUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

final class CommandPing {
  private final TabTPS tabTPS;

  CommandPing(final @NonNull TabTPS tabTPS, final @NonNull CommandManager mgr) {
    this.tabTPS = tabTPS;
  }

  private @NonNull Pagination<Component> pagination(final @NonNull String prefix) {
    return Pagination.builder()
      .resultsPerPage(5)
      .width(38)
      .line(line -> {
        line.character('-');
        line.style(Style.style(TextColor.fromHexString("#47C8FF"), TextDecoration.STRIKETHROUGH));
      })
      .build(
        LinearComponents.linear(
          Constants.PREFIX,
          Component.space(),
          Component.translatable("tabtps.command.ping.text.player_pings")
        ),
        (value, index) -> Collections.singleton(value),
        page -> String.format("/%s %d", prefix, page)
      );
  }

  @CommandDescription("tabtps.command.ping_self.description")
  @CommandPermission(Constants.PERMISSION_COMMAND_PING)
  @CommandMethod("ping")
  public void onPingSelf(final @NonNull CommandSender sender) {
    if (!(sender instanceof Player)) {
      this.tabTPS.chat().send(sender, LinearComponents.linear(
        Constants.PREFIX,
        Component.space(),
        Component.translatable("tabtps.command.ping.text.console_must_provide_player", NamedTextColor.RED)
      ));
      return;
    }
    final Player player = (Player) sender;
    this.tabTPS.audiences().player(player).sendMessage(
      Component.text()
        .append(Constants.PREFIX)
        .append(Component.space())
        .append(
          Component.translatable(
            "tabtps.command.ping_self.text.your_ping",
            NamedTextColor.GRAY,
            LinearComponents.linear(
              this.tabTPS.pingUtil().coloredPing(player, Theme.DEFAULT.colorScheme()),
              Component.translatable(
                "tabtps.label.milliseconds_short",
                Theme.DEFAULT.colorScheme().textSecondary()
              )
            )
          )
        )
        .build()
    );
  }

  @CommandDescription("tabtps.command.ping_target.description")
  @CommandPermission(Constants.PERMISSION_COMMAND_PING_OTHERS)
  @CommandMethod("ping <target> [page]")
  public void onPing(
    final @NonNull CommandSender sender,
    @Argument(value = "target", description = "tabtps.command.ping_target.arguments.target") final @NonNull MultiplePlayerSelector target,
    @Argument(value = "page", defaultValue = "1", description = "tabtps.command.ping.arguments.page") @Range(min = "1", max = "999") final int page
  ) {
    if (target.getPlayers().isEmpty()) {
      final Component component = LinearComponents.linear(
        Constants.PREFIX,
        Component.space(),
        Component.translatable(
          "tabtps.misc.command.text.no_players_found",
          NamedTextColor.RED,
          Component.text(target.getSelector())
        )
      );
      this.tabTPS.chat().send(sender, component);
      return;
    }
    if (target.getPlayers().size() > 1) {
      this.pingMultiple(sender, target.getPlayers(), page, String.format("tabtps:ping %s", target.getSelector()));
      return;
    }
    final Player targetPlayer = target.getPlayers().get(0);
    this.tabTPS.audiences().sender(sender).sendMessage(
      Component.text()
        .append(Constants.PREFIX)
        .append(Component.space())
        .append(Component.translatable(
          "tabtps.command.ping_target.text.targets_ping",
          NamedTextColor.GRAY,
          Component.text(targetPlayer.getName(), NamedTextColor.GRAY),
          LinearComponents.linear(
            this.tabTPS.pingUtil().coloredPing(targetPlayer, Theme.DEFAULT.colorScheme()),
            Component.translatable(
              "tabtps.label.milliseconds_short",
              Theme.DEFAULT.colorScheme().textSecondary()
            )
          )
        ))
        .build()
    );
  }

  private @NonNull ModuleRenderer moduleRenderer(final @NonNull Player player) {
    return ModuleRenderer.builder()
      .modules(new PingModule(this.tabTPS, Theme.DEFAULT, player))
      .moduleRenderFunction(ModuleRenderer.standardRenderFunction(Theme.DEFAULT))
      .build();
  }

  @CommandDescription("tabtps.command.ping_all.description")
  @CommandPermission(Constants.PERMISSION_COMMAND_PING_OTHERS)
  @CommandMethod("pingall [page]")
  public void onPingAll(
    final @NonNull CommandSender sender,
    @Argument(value = "page", defaultValue = "1", description = "tabtps.command.ping.arguments.page") @Range(min = "1", max = "999") final int page
  ) {
    this.pingMultiple(sender, ImmutableList.copyOf(Bukkit.getOnlinePlayers()), page, "tabtps:pingall");
  }

  private void pingMultiple(
    final @NonNull CommandSender sender,
    final @NonNull Collection<Player> targets,
    final int page,
    final @NonNull String commandPrefix
  ) {
    final List<Component> content = new ArrayList<>();
    final List<Integer> pings = new ArrayList<>();
    targets.stream().sorted(Comparator.comparing(player -> this.tabTPS.pingUtil().ping(player))).forEach(player -> {
      content.add(LinearComponents.linear(
        Component.space(),
        Component.text("-", NamedTextColor.GRAY),
        Component.space(),
        Component.text(player.getName(), NamedTextColor.WHITE, TextDecoration.ITALIC),
        Component.text(":", NamedTextColor.GRAY),
        Component.space(),
        this.tabTPS.pingUtil().coloredPing(player, Theme.DEFAULT.colorScheme()),
        Component.translatable("tabtps.label.milliseconds_short", NamedTextColor.GRAY)
      ));
      pings.add(this.tabTPS.pingUtil().ping(player));
    });
    final int avgPing = (int) Math.round(pings.stream().mapToInt(i -> i).average().orElse(0));
    final Component playerAmount = LinearComponents.linear(
      Component.text("(", NamedTextColor.WHITE),
      Component.translatable(
        targets.size() == 1 ? "tabtps.command.ping.text.amount_players_singular" : "tabtps.command.ping.text.amount_players",
        NamedTextColor.GRAY,
        Component.text(Bukkit.getOnlinePlayers().size(), NamedTextColor.GREEN)
      ),
      Component.text(")", NamedTextColor.WHITE)
    );
    final Component summary = LinearComponents.linear(
      Component.translatable("tabtps.command.ping.text.average_ping", NamedTextColor.WHITE),
      Component.text(": ", NamedTextColor.GRAY),
      PingUtil.coloredPing(avgPing, Theme.DEFAULT.colorScheme()),
      Component.translatable("tabtps.label.milliseconds_short", NamedTextColor.GRAY),
      Component.space(),
      playerAmount
    );
    final List<Component> messages = new ArrayList<>();
    messages.add(Component.empty());
    messages.addAll(this.pagination(commandPrefix).render(content, page));
    messages.add(Component.empty());
    messages.add(summary);
    this.tabTPS.chat().send(sender, messages);
  }
}
