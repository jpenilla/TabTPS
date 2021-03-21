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

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.keys.SimpleCloudKey;
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import cloud.commandframework.permission.PredicatePermission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.User;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.command.Commands;
import xyz.jpenilla.tabtps.common.command.TabTPSCommand;
import xyz.jpenilla.tabtps.common.util.Constants;
import xyz.jpenilla.tabtps.common.util.Serializers;

import java.util.concurrent.atomic.AtomicBoolean;

public final class ToggleDisplayCommands extends TabTPSCommand {
  public ToggleDisplayCommands(final @NonNull TabTPS tabTPS, final @NonNull Commands commands) {
    super(tabTPS, commands);

    this.commands.registerPermissionPredicate(
      Constants.PERMISSION_TOGGLE_ACTIONBAR,
      PredicatePermission.of(
        SimpleCloudKey.of(Constants.PERMISSION_TOGGLE_ACTIONBAR),
        sender -> {
          if (!(sender instanceof User)) return true; // todo ?
          final User<?> player = (User<?>) sender;
          final AtomicBoolean passed = new AtomicBoolean(false);
          this.tabTPS.findDisplayConfig(player).ifPresent(config -> passed.set(config.actionBarSettings().allow()));
          return passed.get();
        }
      )
    );

    this.commands.registerPermissionPredicate(
      Constants.PERMISSION_TOGGLE_BOSSBAR,
      PredicatePermission.of(
        SimpleCloudKey.of(Constants.PERMISSION_TOGGLE_BOSSBAR),
        sender -> {
          if (!(sender instanceof User)) return true; // todo ?
          final User<?> player = (User<?>) sender;
          final AtomicBoolean passed = new AtomicBoolean(false);
          this.tabTPS.findDisplayConfig(player).ifPresent(config -> passed.set(config.bossBarSettings().allow()));
          return passed.get();
        }
      )
    );

    this.commands.registerPermissionPredicate(
      Constants.PERMISSION_TOGGLE_TAB,
      PredicatePermission.of(
        SimpleCloudKey.of(Constants.PERMISSION_TOGGLE_TAB),
        sender -> {
          if (!(sender instanceof User)) return true; // todo ?
          final User<?> player = (User<?>) sender;
          final AtomicBoolean passed = new AtomicBoolean(false);
          this.tabTPS.findDisplayConfig(player).ifPresent(config -> passed.set(config.tabSettings().allow()));
          return passed.get();
        }
      )
    );
  }

  @Override
  public void register() {
    this.commands.registerSubcommand(builder ->
      builder.literal("toggle")
        .literal("tab")
        .senderType(User.class)
        .permission(this.commands.permissionPredicate(Constants.PERMISSION_TOGGLE_TAB))
        .meta(MinecraftExtrasMetaKeys.DESCRIPTION, Component.translatable("tabtps.command.toggle_tab.description"))
        .handler(this::toggleTab)
    );

    this.commands.registerSubcommand(builder ->
      builder.literal("toggle")
        .literal("actionbar")
        .senderType(User.class)
        .permission(this.commands.permissionPredicate(Constants.PERMISSION_TOGGLE_ACTIONBAR))
        .meta(MinecraftExtrasMetaKeys.DESCRIPTION, Component.translatable("tabtps.command.toggle_actionbar.description"))
        .handler(this::toggleActionBar)
    );

    this.commands.registerSubcommand(builder ->
      builder.literal("toggle")
        .literal("bossbar")
        .senderType(User.class)
        .permission(this.commands.permissionPredicate(Constants.PERMISSION_TOGGLE_BOSSBAR))
        .meta(MinecraftExtrasMetaKeys.DESCRIPTION, Component.translatable("tabtps.command.toggle_bossbar.description"))
        .handler(this::toggleBossBar)
    );
  }

  private void toggleTab(final @NonNull CommandContext<Commander> context) {
    final User<?> user = (User<?>) context.getSender();
    if (user.tab().enabled()) {
      user.tab().stopDisplay();
      user.tab().enabled(false);
      user.sendMessage(TextComponent.ofChildren(
        Constants.PREFIX,
        Component.space(),
        Serializers.MINIMESSAGE.parse("<italic><gradient:red:gold>Not showing TPS and MSPT in tab menu any more</gradient><gray>.")
          .hoverEvent(Component.translatable("tabtps.misc.text.click_to_toggle", NamedTextColor.GREEN))
          .clickEvent(ClickEvent.runCommand("/tabtps toggle tab"))
      ));
    } else {
      user.tab().enabled(true);
      user.tab().startDisplay();
      user.sendMessage(TextComponent.ofChildren(
        Constants.PREFIX,
        Component.space(),
        Serializers.MINIMESSAGE.parse("<italic><gradient:green:yellow>Showing TPS and MSPT in tab menu</gradient><gray>.")
          .hoverEvent(Component.translatable("tabtps.misc.text.click_to_toggle", NamedTextColor.GREEN))
          .clickEvent(ClickEvent.runCommand("/tabtps toggle tab"))
      ));
    }
  }

  private void toggleActionBar(final @NonNull CommandContext<Commander> context) {
    final User<?> user = (User<?>) context.getSender();
    if (user.actionBar().enabled()) {
      user.actionBar().stopDisplay();
      user.actionBar().enabled(false);
      user.sendMessage(TextComponent.ofChildren(
        Constants.PREFIX,
        Component.space(),
        Serializers.MINIMESSAGE.parse("<italic><gradient:red:gold>Not showing TPS and MSPT in action bar any more</gradient><gray>.")
          .hoverEvent(Component.translatable("tabtps.misc.text.click_to_toggle", NamedTextColor.GREEN))
          .clickEvent(ClickEvent.runCommand("/tabtps toggle actionbar"))
      ));
    } else {
      user.actionBar().enabled(true);
      user.actionBar().startDisplay();
      user.sendMessage(TextComponent.ofChildren(
        Constants.PREFIX,
        Component.space(),
        Serializers.MINIMESSAGE.parse("<italic><gradient:green:yellow>Showing TPS and MSPT in action bar</gradient><gray>.")
          .hoverEvent(Component.translatable("tabtps.misc.text.click_to_toggle", NamedTextColor.GREEN))
          .clickEvent(ClickEvent.runCommand("/tabtps toggle actionbar"))
      ));
    }
  }

  private void toggleBossBar(final @NonNull CommandContext<Commander> context) {
    final User<?> user = (User<?>) context.getSender();
    if (user.bossBar().enabled()) {
      user.bossBar().stopDisplay();
      user.bossBar().enabled(false);
      user.sendMessage(TextComponent.ofChildren(
        Constants.PREFIX,
        Component.space(),
        Serializers.MINIMESSAGE.parse("<italic><gradient:red:gold>Not showing TPS and MSPT in boss bar any more</gradient><gray>.")
          .hoverEvent(Component.translatable("tabtps.misc.text.click_to_toggle", NamedTextColor.GREEN))
          .clickEvent(ClickEvent.runCommand("/tabtps toggle bossbar"))
      ));
    } else {
      user.bossBar().enabled(true);
      user.bossBar().startDisplay();
      user.sendMessage(TextComponent.ofChildren(
        Constants.PREFIX,
        Component.space(),
        Serializers.MINIMESSAGE.parse("<italic><gradient:green:yellow>Showing TPS and MSPT in boss bar</gradient><gray>.")
          .hoverEvent(Component.translatable("tabtps.misc.text.click_to_toggle", NamedTextColor.GREEN))
          .clickEvent(ClickEvent.runCommand("/tabtps toggle bossbar"))
      ));
    }
  }
}
