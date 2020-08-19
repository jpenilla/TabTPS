package xyz.jpenilla.tabtps.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.bukkit.command.CommandSender;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.module.ModuleRenderer;
import xyz.jpenilla.tabtps.util.Constants;
import xyz.jpenilla.tabtps.util.MemoryUtil;
import xyz.jpenilla.tabtps.util.TPSUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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
            moduleRenderer = new ModuleRenderer(tabTPS).separator(" ").moduleRenderFunction(module -> "<gray>" + module.getLabel() + "</gray><white>:</white> " + module.getData());
        }

        final StringBuilder tpsBuilder = new StringBuilder();
        final double[] tps = tabTPS.getTpsUtil().getTps();
        tpsBuilder.append("<gray><hover:show_text:'Ticks per second<gray>.</gray> <green>20</green> is optimal<gray>.</gray>'>TPS<white>:</white> ");
        tpsBuilder.append(TPSUtil.getColoredTps(tps[0]));
        tpsBuilder.append(" <italic>(1m)</italic><white>,</white> ");
        tpsBuilder.append(TPSUtil.getColoredTps(tps[1]));
        tpsBuilder.append(" <italic>(5m)</italic><white>,</white> ");
        tpsBuilder.append(TPSUtil.getColoredTps(tps[2]));
        tpsBuilder.append(" <italic>(15m)</italic></hover>");

        StringBuilder memoryBar = new StringBuilder("<hover:show_text:'Megabytes of Memory/RAM<gray>.</gray> Used<gray>/</gray>Allocated <white>(<gray>Maximum</gray>)</white>'>" + "<gray>[");
        float usedPercent = (float) MemoryUtil.getUsedMemory() / MemoryUtil.getMaxMemory();
        float allocatedPercent = (float) MemoryUtil.getTotalMemory() / MemoryUtil.getMaxMemory();
        int barLength = 93;
        if (usedPercent < 0.8) {
            memoryBar.append("<gradient:green:dark_green>");
        } else {
            memoryBar.append("<gradient:yellow:gold>");
        }
        IntStream.range(0, (int) (barLength * usedPercent)).forEach(i -> memoryBar.append("|"));
        memoryBar.append("</gradient>");
        memoryBar.append("<gradient:aqua:blue>");
        IntStream.range(0, (int) (barLength * (allocatedPercent - usedPercent))).forEach(i -> memoryBar.append("|"));
        memoryBar.append("</gradient>");
        memoryBar.append("<gradient:#929292:#5A5A5A>");
        IntStream.range(0, (int) (barLength * (1 - allocatedPercent))).forEach(i -> memoryBar.append("|"));
        memoryBar.append("</gradient>");
        memoryBar.append("]");

        List<String> messages = new ArrayList<>();
        messages.add("");
        messages.add("<gradient:blue:aqua><strikethrough>-----------</strikethrough></gradient><aqua>[</aqua> <bold><gradient:red:gold>TabTPS</gradient></bold> <gradient:aqua:blue>]<strikethrough>-----------</strikethrough>");
        messages.add(tpsBuilder.toString());
        messages.add("<hover:show_text:'Milliseconds per tick<gray>.</gray> MSPT <gray><</gray> 50 <gray>-></gray> <green>20 TPS</green>'>" + moduleRenderer.render("mspt"));
        messages.add("<hover:show_text:'Megabytes of Memory/RAM<gray>.</gray> Used<gray>/</gray>Allocated <white>(<gray>Maximum</gray>)</white>'>" + moduleRenderer.render("memory"));
        messages.add(memoryBar.toString());

        tabTPS.getChat().send(sender, messages);
    }
}
