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
import xyz.jpenilla.tabtps.Constants;
import xyz.jpenilla.tabtps.TabTPS;

import java.util.List;
import java.util.stream.Collectors;

public class CommandTabTPS {
    private final TabTPS tabTPS;
    private final CommandManager mgr;

    public CommandTabTPS(TabTPS tabTPS, CommandManager mgr) {
        this.tabTPS = tabTPS;
        this.mgr = mgr;
    }

    @Suggestions("help_query")
    public List<String> helpSuggestions(CommandContext<CommandSender> context, String input) {
        return ((CommandHelpHandler.IndexHelpTopic<CommandSender>) mgr.getCommandHelpHandler().queryHelp(context.getSender(), ""))
                .getEntries().stream().map(CommandHelpHandler.VerboseHelpEntry::getSyntaxString).collect(Collectors.toList());
    }

    @CommandDescription("Shows help for the TabTPS commands.")
    @CommandMethod("tabtps help [query]")
    public void onHelp(
            CommandSender sender,
            @Greedy @Argument(value = "query", description = "Help Query", suggestions = "help_query") String query
    ) {
        tabTPS.getCommandManager().getHelp().queryCommands(query == null ? "" : query, sender);
    }

    @CommandDescription("Shows info about the TabTPS plugin.")
    @CommandMethod("tabtps about")
    public void onAbout(CommandSender sender) {
        final String header = tabTPS.getChat().getCenteredMessage("<strikethrough><gradient:white:black:white>----------------------------------");
        tabTPS.getChat().sendParsed(sender, ImmutableList.of(
                header,
                tabTPS.getChat().getCenteredMessage("<hover:show_text:'<rainbow>click me!'><click:open_url:" + tabTPS.getDescription().getWebsite() + ">" + tabTPS.getName() + " <gradient:blue:aqua>" + tabTPS.getDescription().getVersion()),
                tabTPS.getChat().getCenteredMessage("<gray>By <gradient:gold:yellow>jmp"),
                header
        ));
    }

    @CommandDescription("Reloads the TabTPS config files.")
    @CommandPermission(Constants.PERMISSION_COMMAND_RELOAD)
    @CommandMethod("tabtps reload")
    public void onReload(CommandSender sender) {
        tabTPS.getPluginSettings().load();
        ImmutableList.copyOf(Bukkit.getOnlinePlayers()).forEach(player -> {
            if (tabTPS.getTaskManager().hasActionBarTask(player)) {
                tabTPS.getTaskManager().startActionBarTask(player);
            }
            if (tabTPS.getTaskManager().hasTabTask(player)) {
                tabTPS.getTaskManager().startTabTask(player);
            }
        });
        tabTPS.getChat().send(sender, tabTPS.getPrefix() + "<italic> <gradient:green:dark_green>Reload complete</gradient><gray>.");
    }

    @CommandDescription("Toggles showing information in the tab menu.")
    @CommandPermission(Constants.PERMISSION_TOGGLE_TAB)
    @CommandMethod(value = "tabtps toggle tab", requiredSender = Player.class)
    public void onToggleTab(CommandSender sender) {
        Player player = (Player) sender;
        if (tabTPS.getTaskManager().hasTabTask(player)) {
            tabTPS.getTaskManager().stopTabTask(player);
            tabTPS.getUserPrefs().getTabEnabled().remove(player.getUniqueId());
            tabTPS.getChat().send(player, tabTPS.getPrefix() + "<italic><hover:show_text:'<green>Click to toggle'><click:run_command:/tabtps toggle tab> <gradient:red:gold>Not showing TPS and MSPT in tab menu any more</gradient><gray>.");
        } else {
            tabTPS.getTaskManager().startTabTask(player);
            tabTPS.getUserPrefs().getTabEnabled().add(player.getUniqueId());
            tabTPS.getChat().send(player, tabTPS.getPrefix() + "<italic><hover:show_text:'<green>Click to toggle'><click:run_command:/tabtps toggle tab> <gradient:green:yellow>Showing TPS and MSPT in tab menu</gradient><gray>.");
        }
    }

    @CommandDescription("Toggles showing information in the action bar.")
    @CommandPermission(Constants.PERMISSION_TOGGLE_ACTIONBAR)
    @CommandMethod(value = "tabtps toggle actionbar", requiredSender = Player.class)
    public void onToggleActionBar(CommandSender sender) {
        Player player = (Player) sender;
        if (tabTPS.getTaskManager().hasActionBarTask(player)) {
            tabTPS.getTaskManager().stopActionBarTask(player);
            tabTPS.getUserPrefs().getActionBarEnabled().remove(player.getUniqueId());
            tabTPS.getChat().send(player, tabTPS.getPrefix() + "<italic><hover:show_text:'<green>Click to toggle'><click:run_command:/tabtps toggle actionbar> <gradient:red:gold>Not showing TPS and MSPT in action bar any more</gradient><gray>.");
        } else {
            tabTPS.getTaskManager().startActionBarTask(player);
            tabTPS.getUserPrefs().getActionBarEnabled().add(player.getUniqueId());
            tabTPS.getChat().send(player, tabTPS.getPrefix() + "<italic><hover:show_text:'<green>Click to toggle'><click:run_command:/tabtps toggle actionbar> <gradient:green:yellow>Showing TPS and MSPT in action bar</gradient><gray>.");
        }
    }
}
