package xyz.jpenilla.tabtps.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.feature.pagination.Pagination;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.module.ModuleRenderer;
import xyz.jpenilla.tabtps.module.Ping;
import xyz.jpenilla.tabtps.util.Constants;

import java.util.*;

@CommandAlias("ping")
public class CommandPing extends BaseCommand {

    @Dependency
    private TabTPS tabTPS;

    @Default
    @CommandPermission(Constants.PERMISSION_COMMAND_PING)
    @Description("Displays the senders ping to the server in milliseconds.")
    public void onPing(Player player) {
        tabTPS.getChat().send(player, CommandTabTPS.prefix + "<reset><gray> Your " + getModuleRenderer(player).render());
    }

    @Default
    @CommandPermission(Constants.PERMISSION_COMMAND_PING_OTHERS)
    @Description("Displays the targets ping to the server in milliseconds.")
    @CommandCompletion("*")
    public void onPingOther(CommandSender sender, OnlinePlayer target) {
        tabTPS.getChat().send(sender, CommandTabTPS.prefix + "<reset><gray> " + target.getPlayer().getName() + "'s " + getModuleRenderer(target.getPlayer()).render());
    }

    private ModuleRenderer getModuleRenderer(Player player) {
        return new ModuleRenderer(player).modules("ping").moduleRenderFunction(module -> "<gray>" + module.getLabel() + "</gray><white>:</white> " + module.getData());
    }

    private static final Pagination<String> pagination = Pagination.builder()
            .resultsPerPage(5)
            .width(38)
            .line(line -> {
                line.character('-');
                line.style(Style.of(TextColor.fromHexString("#47C8FF"), TextDecoration.STRIKETHROUGH));
            })
            .build(
                    TextComponent.builder().append(CommandTabTPS.prefixComponent).append(TextComponent.of(" Player Pings")).build(),
                    (value, index) -> Collections.singleton(TabTPS.getInstance().getMiniMessage().parse(Objects.requireNonNull(value))),
                    page -> "/tabtps:ping all " + page
            );

    @Subcommand("all")
    @CommandPermission(Constants.PERMISSION_COMMAND_PING_OTHERS)
    @Description("Displays the pings of connected players with an average.")
    @CommandCompletion("*")
    public void onPingAll(CommandSender sender, @Optional Integer page) {
        final List<String> content = new ArrayList<>();
        final List<Integer> pings = new ArrayList<>();
        ImmutableList.copyOf(Bukkit.getOnlinePlayers()).stream().sorted(Comparator.comparing(Ping::getPing).reversed()).forEach(player -> {
            content.add(" <gray>-</gray> <white><italic>" + player.getName() + "</italic><gray>:</gray> " + Ping.getColoredPing(player) + "<gray>ms");
            pings.add(Ping.getPing(player));
        });
        float pingAvg = 0;
        for (Integer ping : pings) {
            pingAvg += (float) ping / pings.size();
        }
        final StringBuilder avg = new StringBuilder();
        avg.append("Average ping<gray>:</gray> ").append(Ping.getColoredPing(Math.round(pingAvg))).append("<gray>ms <white>(</white><green>").append(Bukkit.getOnlinePlayers().size()).append("</green> player");
        if (Bukkit.getOnlinePlayers().size() != 1) {
            avg.append("s");
        }
        avg.append("<white>)</white></gray>");
        final List<Component> messages = new ArrayList<>();
        messages.add(TextComponent.of(""));
        messages.addAll(pagination.render(content, page == null ? 1 : page));
        messages.add(TextComponent.of(""));
        messages.add(tabTPS.getMiniMessage().parse(avg.toString()));
        tabTPS.getChat().send(sender, messages);
    }
}
