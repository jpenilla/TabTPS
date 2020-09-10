package xyz.jpenilla.tabtps.command;

import co.aikar.commands.*;
import org.bukkit.entity.Player;
import xyz.jpenilla.tabtps.Constants;
import xyz.jpenilla.tabtps.TabTPS;

import java.util.ArrayList;
import java.util.List;

public class CommandHelper {
    private final TabTPS tabTPS;
    private final PaperCommandManager manager;

    public CommandHelper(TabTPS tabTPS) {
        this.tabTPS = tabTPS;

        manager = new PaperCommandManager(tabTPS);
        manager.enableUnstableAPI("help");

        registerContexts();
        registerCompletions();

        manager.registerCommand(new CommandTabTPS());
        manager.registerCommand(new CommandTPS(tabTPS));
        manager.registerCommand(new CommandMemory());
        manager.registerCommand(new CommandPing(tabTPS));
    }

    private void registerCompletions() {
        CommandCompletions<BukkitCommandCompletionContext> completions = manager.getCommandCompletions();

        completions.registerCompletion("available_display_toggles", completion -> {
            List<String> c = new ArrayList<>();

            if (completion.getPlayer().hasPermission(Constants.PERMISSION_TOGGLE_TAB)) {
                c.add("tab");
            }
            if (completion.getPlayer().hasPermission(Constants.PERMISSION_TOGGLE_ACTIONBAR)) {
                c.add("actionbar");
            }

            return c;
        });
        completions.setDefaultCompletion("available_display_toggles", Toggle.class);
    }

    private void registerContexts() {
        CommandContexts<BukkitCommandExecutionContext> contexts = manager.getCommandContexts();

        contexts.registerContext(Toggle.class, context -> {
            Player player = context.getPlayer();
            String firstArg = context.popFirstArg();

            if (firstArg.equalsIgnoreCase("tab")) {
                if (player.hasPermission(Constants.PERMISSION_TOGGLE_TAB)) {
                    return Toggle.TAB;
                }
                throw new InvalidCommandArgument("No permission");
            }
            if (firstArg.equalsIgnoreCase("actionbar")) {
                if (player.hasPermission(Constants.PERMISSION_TOGGLE_ACTIONBAR)) {
                    return Toggle.ACTION_BAR;
                }
                throw new InvalidCommandArgument("No permission");
            }

            List<String> valid = new ArrayList<>();
            if (player.hasPermission(Constants.PERMISSION_TOGGLE_TAB)) {
                valid.add("tab");
            }
            if (player.hasPermission(Constants.PERMISSION_TOGGLE_ACTIONBAR)) {
                valid.add("actionbar");
            }
            throw new InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}", ACFUtil.join(valid, ", "));
        });
    }

    public enum Toggle {
        TAB, ACTION_BAR
    }
}
