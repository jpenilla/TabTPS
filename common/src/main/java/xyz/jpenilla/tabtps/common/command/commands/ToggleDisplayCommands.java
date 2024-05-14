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

import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.permission.PredicatePermission;
import xyz.jpenilla.tabtps.common.Messages;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.User;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.command.Commands;
import xyz.jpenilla.tabtps.common.command.TabTPSCommand;
import xyz.jpenilla.tabtps.common.config.DisplayConfig;
import xyz.jpenilla.tabtps.common.util.Constants;
import xyz.jpenilla.tabtps.common.util.TranslatableProvider;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;
import static org.incendo.cloud.minecraft.extras.RichDescription.richDescription;

public final class ToggleDisplayCommands extends TabTPSCommand {
  public ToggleDisplayCommands(final @NonNull TabTPS tabTPS, final @NonNull Commands commands) {
    super(tabTPS, commands);
  }

  @Override
  public void register() {
    final Command.Builder<Commander> toggle = this.commands.rootBuilder().literal("toggle");

    this.commands.register(toggle.literal("tab")
      .senderType(User.TYPE)
      .permission(PredicatePermission.of(user -> this.togglePermission(user, DisplayConfig::tabSettings)))
      .commandDescription(richDescription(Messages.COMMAND_TOGGLE_TAB_DESCRIPTION.plain()))
      .handler(this::toggleTab));

    this.commands.register(toggle.literal("actionbar")
      .senderType(User.TYPE)
      .permission(PredicatePermission.of(user -> this.togglePermission(user, DisplayConfig::actionBarSettings)))
      .commandDescription(richDescription(Messages.COMMAND_TOGGLE_ACTIONBAR_DESCRIPTION.plain()))
      .handler(this::toggleActionBar));

    this.commands.register(toggle.literal("bossbar")
      .senderType(User.TYPE)
      .permission(PredicatePermission.of(user -> this.togglePermission(user, DisplayConfig::bossBarSettings)))
      .commandDescription(richDescription(Messages.COMMAND_TOGGLE_BOSSBAR_DESCRIPTION.plain()))
      .handler(this::toggleBossBar));
  }

  private boolean togglePermission(final @NonNull User<?> sender, final @NonNull Function<DisplayConfig, DisplayConfig.DisplaySettings> function) {
    return this.tabTPS.findDisplayConfig(sender)
      .map(config -> function.apply(config).allow())
      .orElse(false);
  }

  private void toggleTab(final @NonNull CommandContext<User<?>> context) {
    final User<?> user = context.sender();
    if (user.tab().enabled()) {
      user.tab().stopDisplay();
      user.tab().enabled(false);
      user.sendMessage(feedbackMessage("/tabtps toggle tab", Messages.COMMAND_TOGGLE_TAB_DISABLED, RED));
    } else {
      user.tab().enabled(true);
      user.tab().startDisplay();
      user.sendMessage(feedbackMessage("/tabtps toggle tab", Messages.COMMAND_TOGGLE_TAB_ENABLED, GREEN));
    }
    user.markDirty();
  }

  private void toggleActionBar(final @NonNull CommandContext<User<?>> context) {
    final User<?> user = context.sender();
    if (user.actionBar().enabled()) {
      user.actionBar().stopDisplay();
      user.actionBar().enabled(false);
      user.sendMessage(feedbackMessage("/tabtps toggle actionbar", Messages.COMMAND_TOGGLE_ACTIONBAR_DISABLED, RED));
    } else {
      user.actionBar().enabled(true);
      user.actionBar().startDisplay();
      user.sendMessage(feedbackMessage("/tabtps toggle actionbar", Messages.COMMAND_TOGGLE_ACTIONBAR_ENABLED, GREEN));
    }
    user.markDirty();
  }

  private void toggleBossBar(final @NonNull CommandContext<User<?>> context) {
    final User<?> user = context.sender();
    if (user.bossBar().enabled()) {
      user.bossBar().stopDisplay();
      user.bossBar().enabled(false);
      user.sendMessage(feedbackMessage("/tabtps toggle bossbar", Messages.COMMAND_TOGGLE_BOSSBAR_DISABLED, RED));
    } else {
      user.bossBar().enabled(true);
      user.bossBar().startDisplay();
      user.sendMessage(feedbackMessage("/tabtps toggle bossbar", Messages.COMMAND_TOGGLE_BOSSBAR_ENABLED, GREEN));
    }
    user.markDirty();
  }

  private static @NonNull Component feedbackMessage(final @NonNull String command, final @NonNull TranslatableProvider translatable, final @NonNull TextColor color) {
    return text()
      .append(Constants.PREFIX)
      .append(space())
      .append(translatable.build(tr -> {
        tr.color(color);
        tr.decorate(ITALIC);
        tr.hoverEvent(Messages.MISC_TEXT_CLICK_TO_TOGGLE.styled(GREEN));
        tr.clickEvent(runCommand(command));
      }))
      .build();
  }
}
