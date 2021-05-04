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

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.User;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.command.Commands;
import xyz.jpenilla.tabtps.common.command.TabTPSCommand;
import xyz.jpenilla.tabtps.common.config.DisplayConfig;
import xyz.jpenilla.tabtps.common.util.Constants;

import java.util.function.Function;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class ToggleDisplayCommands extends TabTPSCommand {
  public ToggleDisplayCommands(final @NonNull TabTPS tabTPS, final @NonNull Commands commands) {
    super(tabTPS, commands);
  }

  @Override
  public void register() {
    final Command.Builder<Commander> toggle = this.commands.rootBuilder().literal("toggle");

    this.commands.register(toggle.literal("tab")
      .senderType(User.class)
      .permission(commander -> this.togglePermission(commander, DisplayConfig::tabSettings))
      .meta(MinecraftExtrasMetaKeys.DESCRIPTION, translatable("tabtps.command.toggle_tab.description"))
      .handler(this::toggleTab));

    this.commands.register(toggle.literal("actionbar")
      .senderType(User.class)
      .permission(commander -> this.togglePermission(commander, DisplayConfig::actionBarSettings))
      .meta(MinecraftExtrasMetaKeys.DESCRIPTION, translatable("tabtps.command.toggle_actionbar.description"))
      .handler(this::toggleActionBar));

    this.commands.register(toggle.literal("bossbar")
      .senderType(User.class)
      .permission(commander -> this.togglePermission(commander, DisplayConfig::bossBarSettings))
      .meta(MinecraftExtrasMetaKeys.DESCRIPTION, translatable("tabtps.command.toggle_bossbar.description"))
      .handler(this::toggleBossBar));
  }

  private boolean togglePermission(final @NonNull Commander commander, final @NonNull Function<DisplayConfig, DisplayConfig.DisplaySettings> function) {
    if (!(commander instanceof User)) return true; // todo ?
    final User<?> user = (User<?>) commander;
    return this.tabTPS.findDisplayConfig(user)
      .map(config -> function.apply(config).allow())
      .orElse(false);
  }

  private void toggleTab(final @NonNull CommandContext<Commander> context) {
    final User<?> user = (User<?>) context.getSender();
    if (user.tab().enabled()) {
      user.tab().stopDisplay();
      user.tab().enabled(false);
      user.sendMessage(feedbackMessage("/tabtps toggle tab", "tabtps.command.toggle.tab.disabled", RED));
    } else {
      user.tab().enabled(true);
      user.tab().startDisplay();
      user.sendMessage(feedbackMessage("/tabtps toggle tab", "tabtps.command.toggle.tab.enabled", GREEN));
    }
  }

  private void toggleActionBar(final @NonNull CommandContext<Commander> context) {
    final User<?> user = (User<?>) context.getSender();
    if (user.actionBar().enabled()) {
      user.actionBar().stopDisplay();
      user.actionBar().enabled(false);
      user.sendMessage(feedbackMessage("/tabtps toggle actionbar", "tabtps.command.toggle.actionbar.disabled", RED));
    } else {
      user.actionBar().enabled(true);
      user.actionBar().startDisplay();
      user.sendMessage(feedbackMessage("/tabtps toggle actionbar", "tabtps.command.toggle.actionbar.enabled", GREEN));
    }
  }

  private void toggleBossBar(final @NonNull CommandContext<Commander> context) {
    final User<?> user = (User<?>) context.getSender();
    if (user.bossBar().enabled()) {
      user.bossBar().stopDisplay();
      user.bossBar().enabled(false);
      user.sendMessage(feedbackMessage("/tabtps toggle bossbar", "tabtps.command.toggle.bossbar.disabled", RED));
    } else {
      user.bossBar().enabled(true);
      user.bossBar().startDisplay();
      user.sendMessage(feedbackMessage("/tabtps toggle bossbar", "tabtps.command.toggle.bossbar.enabled", GREEN));
    }
  }

  private static @NonNull Component feedbackMessage(final @NonNull String command, final @NonNull String key, final @NonNull TextColor color) {
    return text()
      .append(Constants.PREFIX)
      .append(space())
      .append(translatable()
        .key(key)
        .color(color)
        .decorate(TextDecoration.ITALIC)
        .hoverEvent(translatable("tabtps.misc.text.click_to_toggle", GREEN))
        .clickEvent(runCommand(command)))
      .build();
  }
}
