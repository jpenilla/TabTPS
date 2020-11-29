package xyz.jpenilla.tabtps.command;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import org.bukkit.command.CommandSender;
import xyz.jpenilla.jmplib.Crafty;
import xyz.jpenilla.tabtps.Constants;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.module.Memory;
import xyz.jpenilla.tabtps.module.Module;
import xyz.jpenilla.tabtps.module.ModuleRenderer;
import xyz.jpenilla.tabtps.util.MemoryUtil;
import xyz.jpenilla.tabtps.util.TPSUtil;

import java.lang.invoke.MethodHandle;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.LongPredicate;
import java.util.logging.Level;
import java.util.stream.LongStream;

public class CommandTPS {
    private static final Function<Module, String> MODULE_RENDERER = module -> String.format(
            "<gray>%s</gray><white>:</white> %s",
            module.getLabel(),
            module.getData()
    );
    private static final LongPredicate NOT_ZERO = l -> l != 0;

    private final TabTPS tabTPS;
    private final ModuleRenderer msptRenderer;
    private final ModuleRenderer cpuRenderer;
    private final ModuleRenderer memoryRenderer;

    public CommandTPS(TabTPS tabTPS, CommandManager mgr) {
        this.tabTPS = tabTPS;
        this.msptRenderer = ModuleRenderer.builder().modules(tabTPS, "mspt").moduleRenderFunction(MODULE_RENDERER).build();
        this.cpuRenderer = ModuleRenderer.builder().modules(tabTPS, "cpu").moduleRenderFunction(MODULE_RENDERER).build();
        this.memoryRenderer = ModuleRenderer.builder().modules(new Memory(true)).moduleRenderFunction(MODULE_RENDERER).build();
    }

    @CommandDescription("Displays the current TPS and MSPT of the server.")
    @CommandPermission(Constants.PERMISSION_COMMAND_TICKINFO)
    @CommandMethod("tickinfo|mspt|tps")
    public void onTPS(CommandSender sender) {
        final List<String> messages = new ArrayList<>();
        messages.add("");
        messages.add(TabTPS.getInstance().getPrefix() + "<italic> <gray>Server Tick Information");
        messages.add(this.formatTPS());
        messages.addAll(this.formatTickTimes());
        messages.add("<hover:show_text:'CPU usage for the Minecraft server process as well as the system CPU usage.'>" + cpuRenderer.render());
        messages.add("<hover:show_text:'Megabytes of Memory/RAM<gray>.</gray> Used<gray>/</gray>Allocated <white>(<gray>Maximum</gray>)</white>'>" + memoryRenderer.render());
        tabTPS.getChat().send(sender, messages);
        tabTPS.getChat().send(sender, MemoryUtil.renderBar(null, ManagementFactory.getMemoryMXBean().getHeapMemoryUsage(), 91));
    }

    private String formatTPS() {
        final double[] tps = tabTPS.getTpsUtil().getTps();
        final StringBuilder tpsBuilder = new StringBuilder("<hover:show_text:'Ticks per second<gray>.</gray> <green>20</green> is optimal<gray>.</gray>'><gray>TPS<white>:</white> ");
        final Iterator<Double> tpsIterator = Arrays.stream(tps).iterator();
        while (tpsIterator.hasNext()) {
            tpsBuilder.append(TPSUtil.getColoredTps(tpsIterator.next()));
            tpsBuilder.append(" <italic>(%s)</italic>");
            if (tpsIterator.hasNext()) {
                tpsBuilder.append("<white>,</white> ");
            }
        }
        if (tps.length == 4) {
            return String.format(tpsBuilder.toString(), "5s", "1m", "5m", "15m");
        } else {
            return String.format(tpsBuilder.toString(), "1m", "5m", "15m");
        }
    }

    private List<String> formatTickTimes() {
        final List<String> output = new ArrayList<>();
        if (this.tabTPS.getMajorMinecraftVersion() >= 15 && this.tabTPS.isPaperServer()) {
            try {
                final Class<?> _MinecraftServer = Crafty.needNmsClass("MinecraftServer");
                final MethodHandle _getServer = Objects.requireNonNull(Crafty.findStaticMethod(_MinecraftServer, "getServer", _MinecraftServer));
                final Object minecraftServer = _getServer.invoke();

                final Field _tickTimes5s = Crafty.needField(_MinecraftServer, "tickTimes5s");
                final Field _tickTimes10s = Crafty.needField(_MinecraftServer, "tickTimes10s");
                final Field _tickTimes60s = Crafty.needField(_MinecraftServer, "tickTimes60s");

                final Object tickTimes5s = _tickTimes5s.get(minecraftServer);
                final Object tickTimes10s = _tickTimes10s.get(minecraftServer);
                final Object tickTimes60s = _tickTimes60s.get(minecraftServer);

                final Class<?> _MinecraftServer_TickTimes = Crafty.needNmsClass("MinecraftServer$TickTimes");
                final MethodHandle _getTimes = Objects.requireNonNull(Crafty.findMethod(_MinecraftServer_TickTimes, "getTimes", long[].class));

                final long[] times5s = (long[]) _getTimes.bindTo(tickTimes5s).invoke();
                final long[] times10s = (long[]) _getTimes.bindTo(tickTimes10s).invoke();
                final long[] times60s = (long[]) _getTimes.bindTo(tickTimes60s).invoke();

                final LongSummaryStatistics statistics5s = LongStream.of(times5s).filter(NOT_ZERO).summaryStatistics();
                final LongSummaryStatistics statistics10s = LongStream.of(times10s).filter(NOT_ZERO).summaryStatistics();
                final LongSummaryStatistics statistics60s = LongStream.of(times60s).filter(NOT_ZERO).summaryStatistics();

                output.add("<hover:show_text:'Milliseconds per tick<gray>.</gray> Avg. MSPT <gray>\\<</gray> 50 <gray>-></gray> <green>20 TPS</green>'>"
                        + "<gray>MSPT <white>-</white> Average<white>,</white> Minimum<white>,</white> Maximum</hover>");
                output.add(this.formatStatistics("<white> ├─ <gray>5s</gray> - ", statistics5s));
                output.add(this.formatStatistics("<white> ├─ <gray>10s</gray> - ", statistics10s));
                output.add(this.formatStatistics("<white> └─ <gray>60s</gray> - ", statistics60s));
            } catch (Throwable throwable) {
                this.tabTPS.getLogger().log(Level.WARNING, "Failed to retrieve tick time statistics", throwable);
            }
        } else {
            output.add("<hover:show_text:'Milliseconds per tick<gray>.</gray> MSPT <gray>\\<</gray> 50 <gray>-></gray> <green>20 TPS</green>'>" + msptRenderer.render());
        }
        return output;
    }

    private String formatStatistics(String prefix, LongSummaryStatistics statistics) {
        return String.format(
                "%s%s, %s, %s",
                prefix,
                TPSUtil.getColoredMspt(TPSUtil.toMilliseconds(statistics.getAverage())),
                TPSUtil.getColoredMspt(TPSUtil.toMilliseconds(statistics.getMin())),
                TPSUtil.getColoredMspt(TPSUtil.toMilliseconds(statistics.getMax()))
        );
    }
}
