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

import cloud.commandframework.CommandHelpHandler;
import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.context.CommandContext;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.jpenilla.tabtps.Constants;
import xyz.jpenilla.tabtps.TabTPS;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CommandTabTPS {
  private final TabTPS tabTPS;
  private final CommandManager mgr;

  public CommandTabTPS(final @NonNull TabTPS tabTPS, final @NonNull CommandManager mgr) {
    this.tabTPS = tabTPS;
    this.mgr = mgr;
  }

  @Suggestions("help_query")
  public List<String> helpSuggestions(final @NonNull CommandContext<CommandSender> context, final @NonNull String input) {
    return ((CommandHelpHandler.IndexHelpTopic<CommandSender>) this.mgr.getCommandHelpHandler().queryHelp(context.getSender(), ""))
      .getEntries().stream().map(CommandHelpHandler.VerboseHelpEntry::getSyntaxString).collect(Collectors.toList());
  }

  @CommandDescription("Shows help for the TabTPS commands.")
  @CommandMethod("tabtps help [query]")
  public void help(
    final @NonNull CommandSender sender,
    @Greedy @Argument(value = "query", description = "Help Query", suggestions = "help_query") final @Nullable String query
  ) {
    this.tabTPS.commandManager().help().queryCommands(query == null ? "" : query, sender);
  }

  @CommandDescription("Shows info about the TabTPS plugin.")
  @CommandMethod("tabtps about")
  public void about(final @NonNull CommandSender sender) {
    final String header = this.tabTPS.chat().getCenteredMessage("<strikethrough><gradient:white:black:white>----------------------------------");
    this.tabTPS.chat().sendParsed(sender, ImmutableList.of(
      header,
      this.tabTPS.chat().getCenteredMessage("<hover:show_text:'<rainbow>click me!'><click:open_url:" + this.tabTPS.getDescription().getWebsite() + ">" + this.tabTPS.getName() + " <gradient:blue:aqua>" + this.tabTPS.getDescription().getVersion()),
      this.tabTPS.chat().getCenteredMessage("<gray>By <gradient:gold:yellow>jmp"),
      header
    ));
  }

  @CommandDescription("Reloads the TabTPS config files.")
  @CommandPermission(Constants.PERMISSION_COMMAND_RELOAD)
  @CommandMethod("tabtps reload")
  public void reload(final @NonNull CommandSender sender) {
    Bukkit.getScheduler().runTask(this.tabTPS, () -> {
      try {
        this.tabTPS.configManager().load();
      } catch (final Exception e) {
        this.tabTPS.getLogger().log(Level.WARNING, "Failed to reload config, is there an error in the config files?", e);
        this.tabTPS.chat().send(sender, Constants.PREFIX_MINIMESSAGE + "<italic><red> Something went wrong reloading the configs, check console for more info.");
        return;
      }
      ImmutableList.copyOf(Bukkit.getOnlinePlayers()).forEach(player -> {
        this.tabTPS.permissionManager().attach(player);
        if (this.tabTPS.taskManager().hasActionBarTask(player)) {
          this.tabTPS.taskManager().startActionBarTask(player);
        }
        if (this.tabTPS.taskManager().hasTabTask(player)) {
          this.tabTPS.taskManager().startTabTask(player);
        }
        if (this.tabTPS.taskManager().hasBossTask(player)) {
          this.tabTPS.taskManager().startBossTask(player);
        }
      });
      this.tabTPS.chat().send(sender, Constants.PREFIX_MINIMESSAGE + "<italic> <gradient:green:dark_green>Reload complete</gradient><gray>.");
    });
  }

  @CommandDescription("Toggles showing information in the tab menu.")
  @CommandPermission(Constants.PERMISSION_TOGGLE_TAB)
  @CommandMethod(value = "tabtps toggle tab", requiredSender = Player.class)
  public void toggleTab(final @NonNull CommandSender sender) {
    Bukkit.getScheduler().runTask(this.tabTPS, () -> {
      final Player player = (Player) sender;
      if (this.tabTPS.taskManager().hasTabTask(player)) {
        this.tabTPS.taskManager().stopTabTask(player);
        this.tabTPS.userPreferences().tabEnabled().remove(player.getUniqueId());
        this.tabTPS.chat().send(player, Constants.PREFIX_MINIMESSAGE + "<italic><hover:show_text:'<green>Click to toggle'><click:run_command:/tabtps toggle tab> <gradient:red:gold>Not showing TPS and MSPT in tab menu any more</gradient><gray>.");
      } else {
        this.tabTPS.taskManager().startTabTask(player);
        this.tabTPS.userPreferences().tabEnabled().add(player.getUniqueId());
        this.tabTPS.chat().send(player, Constants.PREFIX_MINIMESSAGE + "<italic><hover:show_text:'<green>Click to toggle'><click:run_command:/tabtps toggle tab> <gradient:green:yellow>Showing TPS and MSPT in tab menu</gradient><gray>.");
      }
    });
  }

  @CommandDescription("Toggles showing information in the action bar.")
  @CommandPermission(Constants.PERMISSION_TOGGLE_ACTIONBAR)
  @CommandMethod(value = "tabtps toggle actionbar", requiredSender = Player.class)
  public void toggleActionBar(final @NonNull CommandSender sender) {
    Bukkit.getScheduler().runTask(this.tabTPS, () -> {
      final Player player = (Player) sender;
      if (this.tabTPS.taskManager().hasActionBarTask(player)) {
        this.tabTPS.taskManager().stopActionBarTask(player);
        this.tabTPS.userPreferences().actionBarEnabled().remove(player.getUniqueId());
        this.tabTPS.chat().send(player, Constants.PREFIX_MINIMESSAGE + "<italic><hover:show_text:'<green>Click to toggle'><click:run_command:/tabtps toggle actionbar> <gradient:red:gold>Not showing TPS and MSPT in action bar any more</gradient><gray>.");
      } else {
        this.tabTPS.taskManager().startActionBarTask(player);
        this.tabTPS.userPreferences().actionBarEnabled().add(player.getUniqueId());
        this.tabTPS.chat().send(player, Constants.PREFIX_MINIMESSAGE + "<italic><hover:show_text:'<green>Click to toggle'><click:run_command:/tabtps toggle actionbar> <gradient:green:yellow>Showing TPS and MSPT in action bar</gradient><gray>.");
      }
    });
  }

  @CommandDescription("Toggles showing information in a boss bar.")
  @CommandPermission(Constants.PERMISSION_TOGGLE_BOSSBAR)
  @CommandMethod(value = "tabtps toggle bossbar", requiredSender = Player.class)
  public void toggleBossBar(final @NonNull CommandSender sender) {
    Bukkit.getScheduler().runTask(this.tabTPS, () -> {
      final Player player = (Player) sender;
      if (this.tabTPS.taskManager().hasBossTask(player)) {
        this.tabTPS.taskManager().stopBossTask(player);
        this.tabTPS.userPreferences().bossBarEnabled().remove(player.getUniqueId());
        this.tabTPS.chat().send(player, Constants.PREFIX_MINIMESSAGE + "<italic><hover:show_text:'<green>Click to toggle'><click:run_command:/tabtps toggle bossbar> <gradient:red:gold>Not showing TPS and MSPT in boss bar any more</gradient><gray>.");
      } else {
        this.tabTPS.taskManager().startBossTask(player);
        this.tabTPS.userPreferences().bossBarEnabled().add(player.getUniqueId());
        this.tabTPS.chat().send(player, Constants.PREFIX_MINIMESSAGE + "<italic><hover:show_text:'<green>Click to toggle'><click:run_command:/tabtps toggle bossbar> <gradient:green:yellow>Showing TPS and MSPT in boss bar</gradient><gray>.");
      }
    });
  }
}
