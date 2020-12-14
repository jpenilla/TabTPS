package xyz.jpenilla.tabtps.task;

import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.jpenilla.tabtps.Constants;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.module.Module;
import xyz.jpenilla.tabtps.module.ModuleRenderer;

public class TabTPSTask extends BukkitRunnable {
    private final ModuleRenderer headerRenderer;
    private final ModuleRenderer footerRenderer;
    private final Player player;
    private final TabTPS tabTPS;
    private boolean firstTick = true;

    public TabTPSTask(TabTPS tabTPS, Player player) {
        this.headerRenderer = ModuleRenderer.builder().modules(tabTPS, player, tabTPS.getPluginSettings().getModules().getTabHeader()).separator(" ").moduleRenderFunction(TabTPSTask::renderModule).build();
        this.footerRenderer = ModuleRenderer.builder().modules(tabTPS, player, tabTPS.getPluginSettings().getModules().getTabFooter()).separator(" ").moduleRenderFunction(TabTPSTask::renderModule).build();
        this.player = player;
        this.tabTPS = tabTPS;
    }

    private static String renderModule(Module module) {
        return "<gray>" + module.getLabel() + "</gray><white>:</white> " + module.getData();
    }

    @Override
    public void run() {
        if (firstTick) {
            firstTick = false;
            if (!player.hasPermission(Constants.PERMISSION_TOGGLE_TAB)) {
                tabTPS.getTaskManager().stopTabTask(player);
                tabTPS.getUserPrefs().getTabEnabled().remove(player.getUniqueId());
                return;
            }
        }
        if (!player.isOnline()) {
            tabTPS.getTaskManager().stopTabTask(player);
        }
        final Audience player = tabTPS.getAudience().player(this.player);
        if (this.headerRenderer.moduleCount() > 0) {
            player.sendPlayerListHeader(tabTPS.getMiniMessage().parse(headerRenderer.render()));
        }
        if (this.footerRenderer.moduleCount() > 0) {
            player.sendPlayerListFooter(tabTPS.getMiniMessage().parse(footerRenderer.render()));
        }
    }
}
