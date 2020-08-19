package xyz.jpenilla.tabtps.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.util.Constants;

import java.util.ArrayList;

@CommandAlias("tabtps|ttps")
public class CommandTabTPS extends BaseCommand {
    private final String prefix = "<white>[<gradient:blue:aqua>TabTPS</gradient>]</white><italic>";

    @Dependency
    private TabTPS tabTPS;

    @HelpCommand
    @Description("Shows help for the '/tabtps' command.")
    public void onHelp(CommandSender sender) {
        showCommandHelp();
    }

    @Subcommand("about")
    @Description("Shows info about the TabTPS plugin.")
    public void onAbout(CommandSender sender) {
        ArrayList<String> list = new ArrayList<>();
        final String header = tabTPS.getChat().getCenteredMessage("<strikethrough><gradient:white:black:white>----------------------------------");
        list.add(header);
        list.add(tabTPS.getChat().getCenteredMessage("<hover:show_text:'<rainbow>click me!'><click:open_url:" + tabTPS.getDescription().getWebsite() + ">" + tabTPS.getName() + " <gradient:blue:aqua>" + tabTPS.getDescription().getVersion()));
        list.add(tabTPS.getChat().getCenteredMessage("<gray>By <gradient:gold:yellow>jmp"));
        list.add(header);
        tabTPS.getChat().sendParsed(sender, list);
    }

    @Subcommand("reload")
    @CommandPermission(Constants.PERMISSION_COMMAND_RELOAD)
    @Description("Reloads the TabTPS config files.")
    public void onReload(CommandSender sender) {
        tabTPS.getPluginSettings().load();
        tabTPS.getChat().send(sender, prefix + " <gradient:green:dark_green>Reload complete</gradient><gray>.");
    }

    @Subcommand("toggle|t")
    @CommandPermission(Constants.PERMISSION_COMMAND_TOGGLE)
    @Description("Toggles information displays.")
    @CommandCompletion("*")
    public void onToggle(Player player, CommandHelper.Toggle toggle) {
        switch (toggle) {
            case TAB:
                if (tabTPS.getTaskManager().hasTabTask(player)) {
                    tabTPS.getTaskManager().stopTabTask(player);
                    tabTPS.getUserPrefs().getTabEnabled().remove(player.getUniqueId());
                    tabTPS.getChat().send(player, prefix + "<hover:show_text:'<green>Click to toggle'><click:run_command:/tabtps toggle tab> <gradient:red:gold>Not showing TPS and MSPT in tab menu any more</gradient><gray>.");
                } else {
                    tabTPS.getTaskManager().startTabTask(player);
                    tabTPS.getUserPrefs().getTabEnabled().add(player.getUniqueId());
                    tabTPS.getChat().send(player, prefix + "<hover:show_text:'<green>Click to toggle'><click:run_command:/tabtps toggle tab> <gradient:green:yellow>Showing TPS and MSPT in tab menu</gradient><gray>.");
                }
                break;
            case ACTION_BAR:
                if (tabTPS.getTaskManager().hasActionBarTask(player)) {
                    tabTPS.getTaskManager().stopActionBarTask(player);
                    tabTPS.getUserPrefs().getActionBarEnabled().remove(player.getUniqueId());
                    tabTPS.getChat().send(player, prefix + "<hover:show_text:'<green>Click to toggle'><click:run_command:/tabtps toggle actionbar> <gradient:red:gold>Not showing TPS and MSPT in action bar any more</gradient><gray>.");
                } else {
                    tabTPS.getTaskManager().startActionBarTask(player);
                    tabTPS.getUserPrefs().getActionBarEnabled().add(player.getUniqueId());
                    tabTPS.getChat().send(player, prefix + "<hover:show_text:'<green>Click to toggle'><click:run_command:/tabtps toggle actionbar> <gradient:green:yellow>Showing TPS and MSPT in action bar</gradient><gray>.");
                }
                break;
        }
    }
}
