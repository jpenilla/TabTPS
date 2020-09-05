package xyz.jpenilla.tabtps.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.util.Constants;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@CommandAlias("memory|mem|ram")
public class CommandMemory extends BaseCommand {

    @Dependency
    private TabTPS tabTPS;

    @Default
    @CommandPermission(Constants.PERMISSION_COMMAND_TICKINFO)
    @Description("Displays the current memory pools of the server jvm. Output will vary greatly based on garbage collection settings.")
    public void onMemory(CommandSender sender) {
        final List<String> messages = new ArrayList<>();
        messages.add("");
        messages.add("<gradient:blue:aqua><strikethrough>----</strikethrough></gradient><aqua>[</aqua> <bold><gradient:red:gold>TabTPS RAM</gradient></bold> <gradient:aqua:blue>]<strikethrough>-----------------------</strikethrough>");
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool == null) {
                continue;
            }
            final MemoryUsage usage = pool.getUsage();
            if (usage.getMax() == -1) {
                continue;
            }
            final long init = usage.getInit() == -1 ? 0 : usage.getInit();
            final float initPercent = (float) init / usage.getMax();
            final float usedPercent = (float) usage.getUsed() / usage.getMax();
            final float committedPercent = (float) usage.getCommitted() / usage.getMax();
            final int barLength = 60;

            final StringBuilder memoryBar = new StringBuilder();
            final String hoverText = "<gradient:blue:aqua>" + usage.getUsed() / 1048576 + "</gradient><gray>M <white>Used</white>/<gradient:blue:aqua>" + usage.getCommitted() / 1048576 + "</gradient>M <white>Committed</white>\n" +
                    "<gradient:blue:aqua>" + usage.getMax() / 1048576 + "</gradient><gray>M <white>Max</white>, <gradient:blue:aqua>" + init / 1048576 + "</gradient>M <white>Init</white>";
            memoryBar.append("<hover:show_text:'").append(hoverText).append("'>");

            memoryBar.append("<gray>[");
            memoryBar.append(usedPercent < 0.8 ? "<gradient:green:dark_green>" : "<gradient:yellow:gold>");
            IntStream.range(0, (int) (barLength * (usedPercent))).forEach(i -> memoryBar.append("|"));
            memoryBar.append("</gradient>");

            memoryBar.append("<gradient:aqua:blue>");
            IntStream.range(0, (int) (barLength * (committedPercent - usedPercent))).forEach(i -> memoryBar.append("|"));
            memoryBar.append("</gradient>");

            memoryBar.append("<gradient:#929292:#5A5A5A>");
            IntStream.range(0, (int) (barLength * (1 - committedPercent))).forEach(i -> memoryBar.append("|"));
            while (getPipeCount(memoryBar.toString()) != barLength) {
                if (getPipeCount(memoryBar.toString()) > barLength) {
                    memoryBar.deleteCharAt(memoryBar.length() - 1);
                } else {
                    memoryBar.insert(memoryBar.indexOf("|"), "|");
                }
            }
            memoryBar.append("</gradient>");
            memoryBar.append("] ");

            final int initIndex = StringUtils.ordinalIndexOf(memoryBar.toString(), "|", Math.max(Math.round(barLength * initPercent), 1));
            memoryBar.deleteCharAt(initIndex);
            memoryBar.insert(initIndex, "<bold>|</bold>");

            memoryBar.append("<white><italic>").append(pool.getName());
            messages.add(memoryBar.toString());
        }
        tabTPS.getChat().send(sender, messages);
    }

    private int getPipeCount(String s) {
        int count = 0;
        while (s.contains("|")) {
            s = s.replaceFirst("\\|", "");
            count++;
        }
        return count;
    }
}
