package xyz.jpenilla.tabtps.task;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.jpenilla.tabtps.Constants;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.module.ModuleRenderer;

public class ActionBarTPSTask extends BukkitRunnable {
    private final Player player;
    private final TabTPS tabTPS;
    private final ModuleRenderer renderer;
    private boolean firstTick = true;

    public ActionBarTPSTask(TabTPS tabTPS, Player player) {
        this.renderer = ModuleRenderer.builder()
                .modules(tabTPS, player, tabTPS.getPluginSettings().getModules().getActionBar()).separator(" <white>|</white> ")
                .moduleRenderFunction(module -> "<bold><gradient:blue:aqua>" + module.getLabel() + "</gradient><white>:</white></bold> " + module.getData())
                .build();
        this.player = player;
        this.tabTPS = tabTPS;
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
        return renderer.render();
    }
}
