package xyz.jpenilla.tabtps.task;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeCordComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.jpenilla.tabtps.Constants;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.module.Module;
import xyz.jpenilla.tabtps.module.ModuleRenderer;

public class TabTPSTask extends BukkitRunnable {
    private static final GsonComponentSerializer legacyGsonComponentSerializer = GsonComponentSerializer.builder().downsampleColors().build();
    private static final LegacyComponentSerializer legacyComponentSerializer = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().build();
    private static final BungeeCordComponentSerializer bungeeComponentSerializer = BungeeCordComponentSerializer.get();
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
        if (tabTPS.getMajorMinecraftVersion() < 16) {
            tabTPS.getNmsHandler().setHeaderFooter(player,
                    legacyGsonComponentSerializer.serialize(getHeader()),
                    legacyGsonComponentSerializer.serialize(getFooter()));
        } else {
            if (tabTPS.isPaperServer()) {
                player.setPlayerListHeaderFooter(
                        bungeeComponentSerializer.serialize(getHeader()),
                        bungeeComponentSerializer.serialize(getFooter()));
            } else {
                player.setPlayerListHeaderFooter(
                        legacyComponentSerializer.serialize(getHeader()),
                        legacyComponentSerializer.serialize(getFooter()));
            }
        }
    }

    private Component getHeader() {
        return tabTPS.getMiniMessage().parse(headerRenderer.render());
    }

    private Component getFooter() {
        return tabTPS.getMiniMessage().parse(footerRenderer.render());
    }
}
