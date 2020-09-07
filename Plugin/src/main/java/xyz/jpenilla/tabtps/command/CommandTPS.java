package xyz.jpenilla.tabtps.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.module.ModuleRenderer;
import xyz.jpenilla.tabtps.util.Constants;
import xyz.jpenilla.tabtps.util.MemoryUtil;
import xyz.jpenilla.tabtps.util.TPSUtil;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

@CommandAlias("tickinfo|mspt")
public class CommandTPS extends BaseCommand {
    private ModuleRenderer moduleRenderer = null;

    @Dependency
    private TabTPS tabTPS;

    @Default
    @CommandPermission(Constants.PERMISSION_COMMAND_TICKINFO)
    @Description("Displays the current TPS and MSPT of the server.")
    public void onTPS(CommandSender sender) {
        if (moduleRenderer == null) {
            Player player = null;
            if (sender instanceof Player) player = (Player) sender;
            moduleRenderer = new ModuleRenderer(player).separator("<gray>,</gray> ").moduleRenderFunction(module -> "<gray>" + module.getLabel() + "</gray><white>:</white> " + module.getData());
        }

        final double[] tps = tabTPS.getTpsUtil().getTps();
        final String tpsString = "<hover:show_text:'Ticks per second<gray>.</gray> <green>20</green> is optimal<gray>.</gray>'>" +
                "<gray>TPS<white>:</white> " +
                TPSUtil.getColoredTps(tps[0]) +
                " <italic>(1m)</italic><white>,</white> " +
                TPSUtil.getColoredTps(tps[1]) +
                " <italic>(5m)</italic><white>,</white> " +
                TPSUtil.getColoredTps(tps[2]) +
                " <italic>(15m)</italic></hover>";

        final List<String> messages = new ArrayList<>();
        messages.add("");
        messages.add("<gradient:blue:aqua><strikethrough>-----------</strikethrough></gradient><aqua>[</aqua> <bold><gradient:red:gold>TabTPS</gradient></bold> <gradient:aqua:blue>]<strikethrough>-----------</strikethrough>");
        messages.add(tpsString);
        messages.add("<hover:show_text:'Milliseconds per tick<gray>.</gray> MSPT <gray><</gray> 50 <gray>-></gray> <green>20 TPS</green>'>" + moduleRenderer.render("mspt"));
        messages.add("<hover:show_text:'CPU usage for the Minecraft server process as well as the system CPU usage.'>" + moduleRenderer.render("cpu"));
        messages.add("<hover:show_text:'Megabytes of Memory/RAM<gray>.</gray> Used<gray>/</gray>Allocated <white>(<gray>Maximum</gray>)</white>'>" + moduleRenderer.render("memory"));
        tabTPS.getChat().send(sender, messages);
        tabTPS.getChat().send(sender, MemoryUtil.renderBar(null, ManagementFactory.getMemoryMXBean().getHeapMemoryUsage(), 91));
    }
}
