package xyz.jpenilla.tabtps.task;

import com.google.common.collect.ImmutableList;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.module.ModuleRenderer;
import xyz.jpenilla.tabtps.util.Constants;

import java.util.List;

public class ActionBarTPSTask extends BukkitRunnable {
    private final Player player;
    private final TabTPS tabTPS;
    private final ModuleRenderer renderer;
    private final List<String> modules;
    private boolean firstTick = true;

    public ActionBarTPSTask(TabTPS tabTPS, Player player) {
        this.renderer = new ModuleRenderer().separator(" <white>|</white> ").moduleRenderFunction(module -> "<bold><gradient:blue:aqua>" + module.getLabel() + "</gradient><white>:</white></bold> " + module.getData());
        this.player = player;
        this.tabTPS = tabTPS;
        this.modules = ImmutableList.copyOf(tabTPS.getPluginSettings().getModules().getActionBar().split(","));
    }

    @Override
    public void run() {
        if (firstTick) {
            firstTick = false;
            if (!player.hasPermission(Constants.PERMISSION_TOGGLE_ACTIONBAR)) {
                tabTPS.getTaskManager().stopActionBarTask(player);
                tabTPS.getUserPrefs().getActionBarEnabled().remove(player.getUniqueId());
                return;
            }
        }
        if (!player.isOnline()) {
            tabTPS.getTaskManager().stopActionBarTask(player);
        }
        tabTPS.getChat().sendActionBar(player, getText());
    }

    private String getText() {
        return renderer.render(modules);
    }
}
