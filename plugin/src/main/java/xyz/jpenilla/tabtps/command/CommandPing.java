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
import xyz.jpenilla.tabtps.module.ModuleRenderer;
import xyz.jpenilla.tabtps.module.PingModule;
import xyz.jpenilla.tabtps.util.PingUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CommandPing {
  private final TabTPS tabTPS;
  private final Pagination<String> pagination;

  public CommandPing(final @NonNull TabTPS tabTPS, final @NonNull CommandManager mgr) {
    this.tabTPS = tabTPS;
    this.pagination = Pagination.builder()
      .resultsPerPage(5)
      .width(38)
      .line(line -> {
        line.character('-');
        line.style(Style.style(TextColor.fromHexString("#47C8FF"), TextDecoration.STRIKETHROUGH));
      })
      .build(
        LinearComponents.linear(Constants.PREFIX, Component.text(" Player Pings")),
        (value, index) -> Collections.singleton(tabTPS.miniMessage().parse(Objects.requireNonNull(value))),
        page -> "/tabtps:pingall " + page
      );
  }

  @CommandDescription("Displays the senders ping to the server in milliseconds.")
  @CommandPermission(Constants.PERMISSION_COMMAND_PING)
  @CommandMethod("ping")
  public void onPingSelf(final @NonNull CommandSender sender) {
    if (!(sender instanceof Player)) {
      this.tabTPS.chat().send(sender, LinearComponents.linear(
        Constants.PREFIX,
        Component.space(),
        Component.text("Console must provide a player to check the ping of.", NamedTextColor.RED)
      ));
      return;
    }
    final Player player = (Player) sender;
    this.tabTPS.audiences().player(player).sendMessage(
      Component.text()
        .append(Constants.PREFIX)
        .append(Component.space())
        .append(Component.text("Your ", NamedTextColor.GRAY))
        .append(this.moduleRenderer(player).render())
        .build()
    );
  }

  @CommandDescription("Displays the targets ping to the server in milliseconds.")
  @CommandPermission(Constants.PERMISSION_COMMAND_PING_OTHERS)
  @CommandMethod("ping <target> [page]")
  public void onPing(
    final @NonNull CommandSender sender,
    @Argument(value = "target", description = "The player(s) to check the ping of.") final @NonNull MultiplePlayerSelector target,
    @Argument(value = "page", defaultValue = "1", description = "The page number of players to display, if applicable.") @Range(min = "1", max = "999") final int page
  ) {
    if (target.getPlayers().isEmpty()) {
      this.tabTPS.chat().send(sender, Constants.PREFIX.append(Component.text(String.format(" No players found for selector: '%s'", target.getSelector()), NamedTextColor.RED, TextDecoration.ITALIC)));
      return;
    }
    if (target.getPlayers().size() > 1) {
      this.pingMultiple(sender, target.getPlayers(), page);
      return;
    }
    final Player targetPlayer = target.getPlayers().get(0);
    this.tabTPS.audiences().sender(sender).sendMessage(
      Component.text()
        .append(Constants.PREFIX)
        .append(Component.space())
        .append(Component.text(targetPlayer.getName() + "'s", NamedTextColor.GRAY))
        .append(Component.space())
        .append(this.moduleRenderer(targetPlayer).render())
        .build()
    );
  }

  private ModuleRenderer moduleRenderer(final @NonNull Player player) {
    return ModuleRenderer.builder()
      .modules(new PingModule(this.tabTPS, player))
      .moduleRenderFunction(module -> Component.text()
        .append(Component.text(module.label().toLowerCase(Locale.ENGLISH), NamedTextColor.GRAY))
        .append(Component.text(":", NamedTextColor.WHITE))
        .append(Component.space())
        .append(module.display())
        .build()
      )
      .build();
  }

  @CommandDescription("Displays the pings of connected players with an average.")
  @CommandPermission(Constants.PERMISSION_COMMAND_PING_OTHERS)
  @CommandMethod("pingall [page]")
  public void onPingAll(
    final @NonNull CommandSender sender,
    @Argument(value = "page", defaultValue = "1", description = "The page number of players to display, if applicable.") @Range(min = "1", max = "999") final int page
  ) {
    this.pingMultiple(sender, ImmutableList.copyOf(Bukkit.getOnlinePlayers()), page);
  }

  private void pingMultiple(final @NonNull CommandSender sender, final @NonNull Collection<Player> targets, final int page) {
    final List<String> content = new ArrayList<>();
    final List<Integer> pings = new ArrayList<>();
    targets.stream().sorted(Comparator.comparing(player -> this.tabTPS.pingUtil().ping(player))).forEach(player -> {
      content.add(" <gray>-</gray> <white><italic>" + player.getName() + "</italic><gray>:</gray> " + this.tabTPS.pingUtil().coloredPing(player) + "<gray>ms");
      pings.add(this.tabTPS.pingUtil().ping(player));
    });
    final int avgPing = (int) Math.round(pings.stream().mapToInt(i -> i).average().orElse(0));
    final StringBuilder avg = new StringBuilder();
    avg.append("Average ping<gray>:</gray> ").append(PingUtil.coloredPing(avgPing)).append("<gray>ms <white>(</white><green>").append(Bukkit.getOnlinePlayers().size()).append("</green> player");
    if (targets.size() != 1) {
      avg.append("s");
    }
    avg.append("<white>)</white></gray>");
    final List<Component> messages = new ArrayList<>();
    messages.add(Component.text(""));
    messages.addAll(this.pagination.render(content, page));
    messages.add(Component.text(""));
    messages.add(this.tabTPS.miniMessage().parse(avg.toString()));
    this.tabTPS.chat().send(sender, messages);
  }
}
