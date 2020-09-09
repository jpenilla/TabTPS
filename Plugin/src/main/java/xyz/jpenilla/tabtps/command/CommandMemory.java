package xyz.jpenilla.tabtps.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;
import xyz.jpenilla.tabtps.Constants;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.util.MemoryUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@CommandAlias("memory|mem|ram")
public class CommandMemory extends BaseCommand {

    @Dependency
    private TabTPS tabTPS;

    @Default
    @CommandPermission(Constants.PERMISSION_COMMAND_TICKINFO)
    @Description("Displays the current memory pools of the server jvm. Output will vary greatly based on garbage collection settings.")
    public void onMemory(CommandSender sender) {
        final List<Component> messages = new ArrayList<>();
        messages.add(TextComponent.of(""));
        messages.add(tabTPS.getMiniMessage().parse("<gradient:blue:aqua><strikethrough>----</strikethrough></gradient><aqua>[</aqua> <bold><gradient:red:gold>TabTPS RAM</gradient></bold> <gradient:aqua:blue>]<strikethrough>-----------------------</strikethrough>"));
        if (!tabTPS.getPluginSettings().getIgnoredMemoryPools().contains("Heap Memory Usage")) {
            messages.add(MemoryUtil.renderBar("Heap Memory Usage", ManagementFactory.getMemoryMXBean().getHeapMemoryUsage(), 60));
        }
        if (!tabTPS.getPluginSettings().getIgnoredMemoryPools().contains("Non-Heap Memory Usage")) {
            messages.add(MemoryUtil.renderBar("Non-Heap Memory Usage", ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage(), 60));
        }
        ManagementFactory.getMemoryPoolMXBeans().stream()
                .filter(bean -> bean != null && !tabTPS.getPluginSettings().getIgnoredMemoryPools().contains(bean.getName()))
                .sorted(Comparator.comparing(MemoryPoolMXBean::getName))
                .map(bean -> MemoryUtil.renderBar(bean.getName(), bean.getUsage(), 60))
                .forEach(messages::add);
        tabTPS.getChat().send(sender, messages);
    }
}
