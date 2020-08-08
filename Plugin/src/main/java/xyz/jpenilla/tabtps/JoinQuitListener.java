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
        if (tabTPS.getUserPrefs().getTabEnabled().contains(e.getPlayer().getUniqueId())) {
            tabTPS.getTaskManager().startTabTask(e.getPlayer());
        }
    }

    @EventHandler
    public void onJoin(PlayerQuitEvent e) {
        if (tabTPS.getUserPrefs().getTabEnabled().contains(e.getPlayer().getUniqueId())) {
            tabTPS.getTaskManager().stopTabTask(e.getPlayer());
        }
    }
}
