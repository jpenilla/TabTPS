package xyz.jpenilla.tabtps.task;

import net.kyori.adventure.text.serializer.bungeecord.BungeeCordComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.module.ModuleRenderer;
import xyz.jpenilla.tabtps.util.Constants;

public class TabTPSTask extends BukkitRunnable {
    private static final GsonComponentSerializer legacyGsonComponentSerializer = GsonComponentSerializer.builder().downsampleColors().build();
    private static final LegacyComponentSerializer legacyComponentSerializer = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().build();
    private static final BungeeCordComponentSerializer bungeeComponentSerializer = BungeeCordComponentSerializer.get();
    private final ModuleRenderer renderer;
    private final Player player;
    private final TabTPS tabTPS;
    private boolean firstTick = true;

    public TabTPSTask(TabTPS tabTPS, Player player) {
        this.renderer = new ModuleRenderer(tabTPS).separator(" ").moduleRenderFunction(module -> "<gray>" + module.getLabel() + "</gray><white>:</white> " + module.getData());
        this.player = player;
        this.tabTPS = tabTPS;
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
        if (tabTPS.getMajorMinecraftVersion() < 16) {
            tabTPS.getNmsHandler().setHeaderFooter(player, null, legacyGsonComponentSerializer.serialize(tabTPS.getMiniMessage().parse(getFooter())));
        } else {
            if (tabTPS.isPaperServer()) {
                player.setPlayerListHeaderFooter(null, bungeeComponentSerializer.serialize(tabTPS.getMiniMessage().parse(getFooter())));
            } else {
                player.setPlayerListHeaderFooter(null, legacyComponentSerializer.serialize(tabTPS.getMiniMessage().parse(getFooter())));
            }
        }
    }

    private String getFooter() {
        return renderer.render();
    }
}
