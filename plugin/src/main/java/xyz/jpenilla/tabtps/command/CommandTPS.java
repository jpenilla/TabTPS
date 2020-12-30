/*
 * This file is part of TabTPS, licensed under the MIT License.
 *
 * Copyright (c) 2020-2021 Jason Penilla
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package xyz.jpenilla.tabtps.command;

import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.jmplib.Crafty;
import xyz.jpenilla.jmplib.Environment;
import xyz.jpenilla.tabtps.Constants;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.module.MemoryModule;
import xyz.jpenilla.tabtps.module.Module;
import xyz.jpenilla.tabtps.module.ModuleRenderer;
import xyz.jpenilla.tabtps.util.MemoryUtil;
import xyz.jpenilla.tabtps.util.TPSUtil;

import java.lang.invoke.MethodHandle;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.LongPredicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class CommandTPS {
  private static final Function<Module, Component> MODULE_RENDERER = module -> Component.text()
    .append(Component.text(module.label(), NamedTextColor.GRAY))
    .append(Component.text(":", NamedTextColor.WHITE))
    .append(Component.space())
    .append(module.display())
    .build();
  private static final LongPredicate NOT_ZERO = l -> l != 0;

  private final TabTPS tabTPS;
  private final ModuleRenderer msptRenderer;
  private final ModuleRenderer cpuRenderer;
  private final ModuleRenderer memoryRenderer;

  public CommandTPS(final @NonNull TabTPS tabTPS, final @NonNull CommandManager mgr) {
    this.tabTPS = tabTPS;
    this.msptRenderer = ModuleRenderer.builder().modules(tabTPS, "mspt").moduleRenderFunction(MODULE_RENDERER).build();
    this.cpuRenderer = ModuleRenderer.builder().modules(tabTPS, "cpu").moduleRenderFunction(MODULE_RENDERER).build();
    this.memoryRenderer = ModuleRenderer.builder().modules(new MemoryModule(tabTPS, true)).moduleRenderFunction(MODULE_RENDERER).build();
  }

  @CommandDescription("Displays the current TPS and MSPT of the server.")
  @CommandPermission(Constants.PERMISSION_COMMAND_TICKINFO)
  @CommandMethod("tickinfo|mspt|tps")
  public void onTPS(final @NonNull CommandSender sender) {
    final List<Component> messages = new ArrayList<>();
    messages.add(Component.empty());
    messages.add(Constants.PREFIX.append(Component.text(" Server Tick Information", NamedTextColor.GRAY, TextDecoration.ITALIC)));
    messages.add(this.formatTPS());
    messages.addAll(this.formatTickTimes());
    messages.add(this.cpuRenderer.render().hoverEvent(HoverEvent.showText(
      Component.text("CPU usage for the Minecraft server process as well as the system CPU usage.", NamedTextColor.GRAY)
    )));
    messages.add(this.renderMemory());
    messages.add(MemoryUtil.renderBar(null, ManagementFactory.getMemoryMXBean().getHeapMemoryUsage(), 91));
    this.tabTPS.chat().send(sender, messages);
  }

  private @NonNull Component renderMemory() {
    return this.memoryRenderer.render().hoverEvent(HoverEvent.showText(
      Component.text()
        .color(NamedTextColor.GRAY)
        .append(Component.text("Megabytes of Memory/RAM"))
        .append(Component.text(".", NamedTextColor.WHITE))
        .append(Component.space())
        .append(Component.text("Used"))
        .append(Component.text("/", NamedTextColor.WHITE))
        .append(Component.text("Allocated"))
        .append(Component.space())
        .append(Component.text("(", NamedTextColor.WHITE))
        .append(Component.text("Maximum"))
        .append(Component.text(")", NamedTextColor.WHITE))
        .build()
    ));
  }

  private @NonNull Component formatTPS() {
    final double[] tps = this.tabTPS.tpsUtil().tps();
    final StringBuilder tpsBuilder = new StringBuilder("<hover:show_text:'Ticks per second<gray>.</gray> <green>20</green> is optimal<gray>.</gray>'><gray>TPS<white>:</white> ");
    final Iterator<Double> tpsIterator = Arrays.stream(tps).iterator();
    while (tpsIterator.hasNext()) {
      tpsBuilder.append(TPSUtil.coloredTps(tpsIterator.next()));
      tpsBuilder.append(" <italic>(%s)</italic>");
      if (tpsIterator.hasNext()) {
        tpsBuilder.append("<white>,</white> ");
      }
    }
    final String miniMessage;
    if (tps.length == 4) {
      miniMessage = String.format(tpsBuilder.toString(), "5s", "1m", "5m", "15m");
    } else {
      miniMessage = String.format(tpsBuilder.toString(), "1m", "5m", "15m");
    }
    return this.tabTPS.miniMessage().parse(miniMessage);
  }

  private @NonNull List<Component> formatTickTimes() {
    if (Environment.majorMinecraftVersion() >= 15 && Environment.paper()) {
      try {
        final List<String> output = new ArrayList<>();

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

        output.add("<hover:show_text:'Milliseconds per tick<gray>.</gray> Avg. MSPT <gray>≤ <white>50</white> -></gray> <green>20 TPS</green>'>"
          + "<gray>MSPT <white>-</white> Average<white>,</white> Minimum<white>,</white> Maximum</hover>");
        output.add(this.formatStatistics("<white> ├─ <gray>5s</gray> - ", statistics5s));
        output.add(this.formatStatistics("<white> ├─ <gray>10s</gray> - ", statistics10s));
        output.add(this.formatStatistics("<white> └─ <gray>60s</gray> - ", statistics60s));

        return output.stream().map(string -> this.tabTPS.miniMessage().parse(string)).collect(Collectors.toList());
      } catch (final Throwable throwable) {
        this.tabTPS.getLogger().log(Level.WARNING, "Failed to retrieve tick time statistics", throwable);
        throw new IllegalStateException("Failed to retrieve tick time statistics", throwable);
      }
    } else {
      return Collections.singletonList(this.msptRenderer.render().hoverEvent(HoverEvent.showText(
        this.tabTPS.miniMessage().parse("Milliseconds per tick<gray>.</gray> Avg. MSPT <gray>≤ <white>50</white> -></gray> <green>20 TPS</green>")
      )));
    }
  }

  private String formatStatistics(final @NonNull String prefix, final @NonNull LongSummaryStatistics statistics) {
    return String.format(
      "%s%s, %s, %s",
      prefix,
      TPSUtil.coloredMspt(TPSUtil.toMilliseconds(statistics.getAverage())),
      TPSUtil.coloredMspt(TPSUtil.toMilliseconds(statistics.getMin())),
      TPSUtil.coloredMspt(TPSUtil.toMilliseconds(statistics.getMax()))
    );
  }
}
