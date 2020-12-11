package xyz.jpenilla.tabtps.command;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.annotations.specifier.Range;
import cloud.commandframework.bukkit.arguments.selector.MultiplePlayerSelector;
import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.feature.pagination.Pagination;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.jpenilla.tabtps.Constants;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.module.ModuleRenderer;
import xyz.jpenilla.tabtps.util.PingUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CommandPing {
    private final TabTPS tabTPS;
    private final Pagination<String> pagination;

    public CommandPing(TabTPS tabTPS, CommandManager mgr) {
        this.tabTPS = tabTPS;
        this.pagination = Pagination.builder()
                .resultsPerPage(5)
                .width(38)
                .line(line -> {
                    line.character('-');
                    line.style(Style.style(TextColor.fromHexString("#47C8FF"), TextDecoration.STRIKETHROUGH));
                })
                .build(
                        Component.text().append(tabTPS.getPrefixComponent()).append(Component.text(" Player Pings")).build(),
                        (value, index) -> Collections.singleton(tabTPS.getMiniMessage().parse(Objects.requireNonNull(value))),
                        page -> "/tabtps:ping all " + page
                );
    }

    @CommandDescription("Displays the senders ping to the server in milliseconds.")
    @CommandPermission(Constants.PERMISSION_COMMAND_PING)
    @CommandMethod("ping")
    public void onPingSelf(CommandSender sender) {
        if (!(sender instanceof Player)) {
            tabTPS.getChat().send(sender, tabTPS.getPrefixComponent() + "<reset><red> Console must provide a player to check the ping of.");
            return;
        }
        tabTPS.getChat().send(sender, tabTPS.getPrefix() + "<gray> Your " + getModuleRenderer((Player) sender).render());
    }

    @CommandDescription("Displays the targets ping to the server in milliseconds.")
    @CommandPermission(Constants.PERMISSION_COMMAND_PING_OTHERS)
    @CommandMethod("ping <target> [page]")
    public void onPing(CommandSender sender,
                       @Argument(value = "target", description = "The player(s) to check the ping of.") MultiplePlayerSelector target,
                       @Argument(value = "page", defaultValue = "1", description = "The page number of players to display, if applicable.") @Range(min = "1", max = "999") int page) {
        if (target.getPlayers().isEmpty()) {
            tabTPS.getChat().send(sender, tabTPS.getPrefixComponent().append(Component.text(String.format(" No players found for selector: '%s'", target.getSelector()), NamedTextColor.RED, TextDecoration.ITALIC)));
            return;
        }
        if (target.getPlayers().size() > 1) {
            pingMultiple(sender, target.getPlayers(), page);
            return;
        }
        tabTPS.getChat().send(sender, tabTPS.getPrefix() + "<gray> " + target.getPlayers().get(0).getName() + "'s " + getModuleRenderer(target.getPlayers().get(0)).render());
    }

    private ModuleRenderer getModuleRenderer(Player player) {
        return ModuleRenderer.builder().modules(tabTPS, player, "ping").moduleRenderFunction(module -> "<gray>" + module.getLabel() + "</gray><white>:</white> " + module.getData()).build();
    }

    @CommandDescription("Displays the pings of connected players with an average.")
    @CommandPermission(Constants.PERMISSION_COMMAND_PING_OTHERS)
    @CommandMethod("pingall [page]")
    public void onPingAll(CommandSender sender,
                          @Argument(value = "page", defaultValue = "1", description = "The page number of players to display, if applicable.") @Range(min = "1", max = "999") int page) {
        pingMultiple(sender, ImmutableList.copyOf(Bukkit.getOnlinePlayers()), page);
    }

    private void pingMultiple(CommandSender sender, Collection<Player> targets, int page) {
        final List<String> content = new ArrayList<>();
        final List<Integer> pings = new ArrayList<>();
        targets.stream().sorted(Comparator.comparing(player -> tabTPS.getPingUtil().getPing(player))).forEach(player -> {
            content.add(" <gray>-</gray> <white><italic>" + player.getName() + "</italic><gray>:</gray> " + tabTPS.getPingUtil().getColoredPing(player) + "<gray>ms");
            pings.add(tabTPS.getPingUtil().getPing(player));
        });
        final int avgPing = (int) Math.round(pings.stream().mapToInt(i -> i).average().orElse(0));
        final StringBuilder avg = new StringBuilder();
        avg.append("Average ping<gray>:</gray> ").append(PingUtil.getColoredPing(avgPing)).append("<gray>ms <white>(</white><green>").append(Bukkit.getOnlinePlayers().size()).append("</green> player");
        if (targets.size() != 1) {
            avg.append("s");
        }
        avg.append("<white>)</white></gray>");
        final List<Component> messages = new ArrayList<>();
        messages.add(Component.text(""));
        messages.addAll(pagination.render(content, page));
        messages.add(Component.text(""));
        messages.add(tabTPS.getMiniMessage().parse(avg.toString()));
        tabTPS.getChat().send(sender, messages);
    }
}
