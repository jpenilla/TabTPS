package xyz.jpenilla.tabtps;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {
    private final TabTPS tabTPS;

    public JoinQuitListener(TabTPS tabTPS) {
        this.tabTPS = tabTPS;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (tabTPS.getPluginSettings().getUserPrefsDefaults().isTab()) {
            if (!tabTPS.getUserPrefs().getTabEnabled().contains(e.getPlayer().getUniqueId())
                    && e.getPlayer().hasPermission(Constants.PERMISSION_TOGGLE_TAB)) {
                tabTPS.getUserPrefs().getTabEnabled().add(e.getPlayer().getUniqueId());
            }
        }
        if (tabTPS.getPluginSettings().getUserPrefsDefaults().isActionBar()) {
            if (!tabTPS.getUserPrefs().getActionBarEnabled().contains(e.getPlayer().getUniqueId())
                    && e.getPlayer().hasPermission(Constants.PERMISSION_TOGGLE_ACTIONBAR)) {
                tabTPS.getUserPrefs().getActionBarEnabled().add(e.getPlayer().getUniqueId());
            }
        }

        if (tabTPS.getUserPrefs().getTabEnabled().contains(e.getPlayer().getUniqueId())) {
            tabTPS.getTaskManager().startTabTask(e.getPlayer());
        }
        if (tabTPS.getUserPrefs().getActionBarEnabled().contains(e.getPlayer().getUniqueId())) {
            tabTPS.getTaskManager().startActionBarTask(e.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (tabTPS.getUserPrefs().getTabEnabled().contains(e.getPlayer().getUniqueId())) {
            tabTPS.getTaskManager().stopTabTask(e.getPlayer());
        }
        if (tabTPS.getUserPrefs().getActionBarEnabled().contains(e.getPlayer().getUniqueId())) {
            tabTPS.getTaskManager().stopActionBarTask(e.getPlayer());
        }
    }
}
