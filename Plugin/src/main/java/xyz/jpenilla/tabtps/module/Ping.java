package xyz.jpenilla.tabtps.module;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import xyz.jpenilla.tabtps.TabTPS;

@AllArgsConstructor
public class Ping extends Module {
    private final TabTPS tabTPS;
    private final Player player;

    @Override
    public String getLabel() {
        return "Ping";
    }

    @Override
    public String getData() {
        return tabTPS.getPingUtil().getColoredPing(player) + "<gray>ms</gray>";
    }

    @Override
    public boolean needsPlayer() {
        return true;
    }
}
