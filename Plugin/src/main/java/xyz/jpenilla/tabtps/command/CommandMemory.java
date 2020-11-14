package xyz.jpenilla.tabtps.command;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import xyz.jpenilla.tabtps.Constants;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.util.MemoryUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CommandMemory {
    private final TabTPS tabTPS;

    public CommandMemory(TabTPS tabTPS, CommandManager mgr) {
        this.tabTPS = tabTPS;
    }

    @CommandDescription("Displays the current memory pools of the server jvm. Output will vary greatly based on garbage collection settings.")
    @CommandPermission(Constants.PERMISSION_COMMAND_TICKINFO)
    @CommandMethod("memory|mem|ram")
    public void onMemory(CommandSender sender) {
        final List<Component> messages = new ArrayList<>();
        messages.add(Component.text(""));
        final Component header = TabTPS.getInstance().getPrefixComponent()
                .append(Component.space())
                .append(Component.text(
                        "Memory Usage",
                        NamedTextColor.GRAY,
                        TextDecoration.ITALIC
                ));
        messages.add(header);
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
