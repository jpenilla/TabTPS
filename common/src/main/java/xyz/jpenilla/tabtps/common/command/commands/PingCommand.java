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
package xyz.jpenilla.tabtps.common.command.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.feature.pagination.Pagination;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.execution.CommandExecutionHandler;
import org.incendo.cloud.parser.ParserDescriptor;
import xyz.jpenilla.tabtps.common.Messages;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.User;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.command.Commands;
import xyz.jpenilla.tabtps.common.command.TabTPSCommand;
import xyz.jpenilla.tabtps.common.command.exception.CommandCompletedException;
import xyz.jpenilla.tabtps.common.config.Theme;
import xyz.jpenilla.tabtps.common.util.Components;
import xyz.jpenilla.tabtps.common.util.Constants;
import xyz.jpenilla.tabtps.common.util.PingUtil;
import xyz.jpenilla.tabtps.common.util.TranslatableProvider;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.Style.style;
import static net.kyori.adventure.text.format.TextColor.color;
import static net.kyori.adventure.text.format.TextDecoration.STRIKETHROUGH;
import static org.incendo.cloud.minecraft.extras.RichDescription.richDescription;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;

public class PingCommand extends TabTPSCommand {
  public PingCommand(final @NonNull TabTPS tabTPS, final @NonNull Commands commands) {
    super(tabTPS, commands);
  }

  @Override
  public void register() {
    this.commands.register(this.commandManager.commandBuilder("ping")
      .permission(Constants.PERMISSION_COMMAND_PING)
      .commandDescription(richDescription(Messages.COMMAND_PING_SELF_DESCRIPTION.plain()))
      .handler(this::onPingSelf));

    this.commands.register(this.commandManager.commandBuilder("pingall")
      .optional("page", integerParser(1, 999), DefaultValue.constant(1), richDescription(Messages.COMMAND_PING_ARGUMENTS_PAGE))
      .permission(Constants.PERMISSION_COMMAND_PING_OTHERS)
      .commandDescription(richDescription(Messages.COMMAND_PING_ALL_DESCRIPTION.plain()))
      .handler(this::onPingAll));
  }

  protected <T> void registerPingTargetsCommand(
    final @NonNull ParserDescriptor<Commander, T> targetsArgument,
    final @NonNull CommandExecutionHandler<Commander> handler
  ) {
    this.commands.register(this.commandManager.commandBuilder("ping")
      .required("target", targetsArgument, richDescription(Messages.COMMAND_PING_TARGET_ARGUMENTS_TARGET))
      .optional("page", integerParser(1, 999), DefaultValue.constant(1), richDescription(Messages.COMMAND_PING_ARGUMENTS_PAGE))
      .permission(Constants.PERMISSION_COMMAND_PING_OTHERS)
      .commandDescription(richDescription(Messages.COMMAND_PING_TARGET_DESCRIPTION.plain()))
      .handler(handler));
  }

  private void onPingAll(final @NonNull CommandContext<Commander> context) {
    final Commander sender = context.sender();
    final int page = context.get("page");
    this.pingMultiple(sender, Collections.unmodifiableCollection(this.tabTPS.platform().userService().onlineUsers()), page, "pingall");
  }

  private void onPingSelf(final @NonNull CommandContext<Commander> context) {
    final Commander sender = context.sender();
    if (!(sender instanceof User)) {
      throw CommandCompletedException.withMessage(Components.ofChildren(
        Constants.PREFIX,
        space(),
        Messages.COMMAND_PING_TEXT_CONSOLE_MUST_PROVIDE_PLAYER.styled(RED)
      ));
    }
    final User<?> player = (User<?>) sender;
    player.sendMessage(text()
      .append(Constants.PREFIX)
      .append(space())
      .append(Messages.COMMAND_PING_SELF_TEXT_YOUR_PING.styled(
        GRAY,
        Components.ofChildren(
          PingUtil.coloredPing(player, Theme.DEFAULT.colorScheme()),
          Messages.LABEL_MILLISECONDS_SHORT.styled(Theme.DEFAULT.colorScheme().textSecondary())
        )
      )));
  }

  protected final void pingTargets(
    final @NonNull Commander commander,
    final @NonNull List<User<?>> targets,
    final @NonNull String inputString,
    final int page
  ) {
    if (targets.isEmpty()) {
      throw CommandCompletedException.withMessage(Components.ofChildren(
        Constants.PREFIX,
        space(),
        Messages.MISC_COMMAND_TEXT_NO_PLAYERS_FOUND.styled(RED, text(inputString))
      ));
    }
    if (targets.size() > 1) {
      this.pingMultiple(commander, targets, page, String.format("ping %s", inputString));
      return;
    }
    final User<?> targetPlayer = targets.get(0);
    commander.sendMessage(text()
      .append(Constants.PREFIX)
      .append(space())
      .append(Messages.COMMAND_PING_TARGET_TEXT_TARGETS_PING.styled(
        GRAY,
        targetPlayer.displayName(),
        Components.ofChildren(
          PingUtil.coloredPing(targetPlayer, Theme.DEFAULT.colorScheme()),
          Messages.LABEL_MILLISECONDS_SHORT.styled(Theme.DEFAULT.colorScheme().textSecondary())
        )
      )));
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
      content.add(Components.ofChildren(
        space(),
        text("-", GRAY),
        space(),
        player.displayName(),
        text(":", GRAY),
        space(),
        PingUtil.coloredPing(player, Theme.DEFAULT.colorScheme()),
        Messages.LABEL_MILLISECONDS_SHORT.styled(GRAY)
      ));
      pings.add(player.ping());
    });
    final int avgPing = (int) Math.round(pings.stream().mapToInt(i -> i).average().orElse(0));
    final TranslatableProvider playerAmountTranslatable = targets.size() == 1
      ? Messages.COMMAND_PING_TEXT_AMOUNT_PLAYERS_SINGULAR
      : Messages.COMMAND_PING_TEXT_AMOUNT_PLAYERS;
    final Component playerAmount = Components.ofChildren(
      text('(', WHITE),
      playerAmountTranslatable.styled(GRAY, text(this.tabTPS.platform().userService().onlinePlayers(), GREEN)),
      text(')', WHITE)
    );
    final Component summary = Components.ofChildren(
      Messages.COMMAND_PING_TEXT_AVERAGE_PING.styled(WHITE),
      text(": ", GRAY),
      PingUtil.coloredPing(avgPing, Theme.DEFAULT.colorScheme()),
      Messages.LABEL_MILLISECONDS_SHORT.styled(GRAY),
      space(),
      playerAmount
    );
    final List<Component> messages = new ArrayList<>();
    messages.add(empty());
    messages.addAll(pagination(commandPrefix).render(content, page));
    messages.add(empty());
    messages.add(summary);
    messages.forEach(sender::sendMessage);
  }

  private static @NonNull Pagination<Component> pagination(final @NonNull String prefix) {
    return Pagination.builder()
      .resultsPerPage(10)
      .width(38)
      .line(line -> line.character('-').style(style(color(0x47C8FF), STRIKETHROUGH)))
      .build(
        Components.ofChildren(
          Constants.PREFIX,
          space(),
          Messages.COMMAND_PING_TEXT_PLAYER_PINGS
        ),
        (value, index) -> Collections.singleton(value),
        page -> String.format("/%s %d", prefix, page)
      );
  }
}
