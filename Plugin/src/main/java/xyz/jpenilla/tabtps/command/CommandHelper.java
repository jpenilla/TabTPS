package xyz.jpenilla.tabtps.command;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.bukkit.BukkitCommandMetaBuilder;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import xyz.jpenilla.tabtps.TabTPS;

import java.util.function.Function;

public class CommandHelper {

    @Getter private PaperCommandManager<CommandSender> mgr;
    @Getter private MinecraftHelp<CommandSender> help;
    @Getter private AnnotationParser<CommandSender> annotationParser;

    public CommandHelper(TabTPS tabTPS) {
        try {
            mgr = new PaperCommandManager<>(
                    tabTPS,
                    AsynchronousCommandExecutionCoordinator.<CommandSender>newBuilder().build(),
                    Function.identity(),
                    Function.identity()
            );
            help = new MinecraftHelp<>("/tabtps help", tabTPS.getAudience()::sender, mgr);
            annotationParser = new AnnotationParser<>(mgr, CommandSender.class,
                    p -> metaWithDescription(p.get(StandardParameters.DESCRIPTION, "No description")));

            help.setHelpColors(MinecraftHelp.HelpColors.of(
                    TextColor.color(0x00a3ff),
                    NamedTextColor.WHITE,
                    TextColor.color(0x284fff),
                    NamedTextColor.GRAY,
                    NamedTextColor.DARK_GRAY
            ));
            help.setMessage(MinecraftHelp.MESSAGE_HELP_TITLE, "TabTPS Help");

            new MinecraftExceptionHandler<CommandSender>()
                    .withDefaultHandlers()
                    .withDecorator(c -> tabTPS.getPrefixComponent().append(Component.space()).append(c))
                    .apply(mgr, tabTPS.getAudience()::sender);

            /* Register Brigadier */
            try {
                mgr.registerBrigadier();
                tabTPS.getLogger().info("Successfully registered Mojang Brigadier support for commands.");
            } catch (Exception ignored) {
            }

            /* Register Asynchronous Completion Listener */
            try {
                mgr.registerAsynchronousCompletions();
                tabTPS.getLogger().info("Successfully registered asynchronous command completion listener.");
            } catch (Exception ignored) {
            }

            /* Register Commands */
            ImmutableList.of(
                    new CommandTabTPS(tabTPS, mgr),
                    new CommandTPS(tabTPS, mgr),
                    new CommandMemory(tabTPS, mgr),
                    new CommandPing(tabTPS, mgr)
            ).forEach(annotationParser::parse);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SimpleCommandMeta metaWithDescription(final String description) {
        return BukkitCommandMetaBuilder.builder().withDescription(description).build();
    }
}
