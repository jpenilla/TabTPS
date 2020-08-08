package xyz.jpenilla.tabtps.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.entity.Player;
import xyz.jpenilla.tabtps.TabTPS;

@CommandAlias("tabtps|ttps")
public class CommandTabTPS extends BaseCommand {
    @Dependency
    private TabTPS tabTPS;

    @Default
    @CommandPermission("tabtps.toggletab")
    @Description("Toggles showing the current TPS and MSPT of the server in your tab menu.")
    public void onToggleTPS(Player player) {
        if (tabTPS.getTaskManager().hasTabTask(player)) {
            tabTPS.getTaskManager().stopTabTask(player);
            tabTPS.getUserPrefs().getTabEnabled().remove(player.getUniqueId());
            tabTPS.getChat().send(player, "<gradient:red:gold>Not showing TPS and MSPT in tab menu any more");
        } else {
            tabTPS.getTaskManager().startTabTask(player);
            tabTPS.getUserPrefs().getTabEnabled().add(player.getUniqueId());
            tabTPS.getChat().send(player, "<gradient:green:yellow>Showing TPS and MSPT in tab menu");
        }
    }
}
