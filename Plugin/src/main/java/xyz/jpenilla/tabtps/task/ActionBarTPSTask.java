package xyz.jpenilla.tabtps.task;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.util.TPSUtil;

public class ActionBarTPSTask extends BukkitRunnable {
    private final Player player;
    private final TabTPS tabTPS;
    private boolean firstTick = true;

    public ActionBarTPSTask(TabTPS tabTPS, Player player) {
        this.player = player;
        this.tabTPS = tabTPS;
    }

    @Override
    public void run() {
        if (firstTick) {
            firstTick = false;
            if (!player.hasPermission("tabtps.toggleactionbar")) {
                tabTPS.getTaskManager().stopActionBarTask(player);
                tabTPS.getUserPrefs().getActionBarEnabled().remove(player.getUniqueId());
            }
        }
        if (!player.isOnline()) {
            tabTPS.getTaskManager().stopActionBarTask(player);
        }
        tabTPS.getChat().sendActionBar(player, getText());
    }

    private String getText() {
        final StringBuilder text = new StringBuilder();
        text.append("<bold><gradient:blue:aqua>TPS</gradient><white>:</white> ");
        text.append(TPSUtil.getColoredTps(tabTPS.getTpsUtil().getTps()[0]));
        text.append("  </bold>-<bold>  ");
        text.append("<gradient:blue:aqua>MSPT</gradient><white>:</white> ");
        text.append(TPSUtil.getColoredMspt(tabTPS.getTpsUtil().getMspt()));
        return text.toString();
    }
}
