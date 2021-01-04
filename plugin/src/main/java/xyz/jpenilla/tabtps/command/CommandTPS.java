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
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.jmplib.Crafty;
import xyz.jpenilla.jmplib.Environment;
import xyz.jpenilla.tabtps.Constants;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.config.Theme;
import xyz.jpenilla.tabtps.module.MemoryModule;
import xyz.jpenilla.tabtps.module.Module;
import xyz.jpenilla.tabtps.module.ModuleRenderer;
import xyz.jpenilla.tabtps.util.MemoryUtil;
import xyz.jpenilla.tabtps.util.TPSUtil;

import java.lang.invoke.MethodHandle;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.LongPredicate;
import java.util.logging.Level;
import java.util.stream.LongStream;

final class CommandTPS {
  private static final Function<Module, Component> MODULE_RENDERER = ModuleRenderer.standardRenderFunction(Theme.DEFAULT);
  private static final LongPredicate NOT_ZERO = l -> l != 0;

  private final TabTPS tabTPS;
  private final ModuleRenderer msptRenderer;
  private final ModuleRenderer cpuRenderer;
  private final ModuleRenderer memoryRenderer;

  CommandTPS(final @NonNull TabTPS tabTPS, final @NonNull CommandManager mgr) {
    this.tabTPS = tabTPS;
    this.msptRenderer = ModuleRenderer.builder().modules(tabTPS, Theme.DEFAULT, "mspt").moduleRenderFunction(MODULE_RENDERER).build();
    this.cpuRenderer = ModuleRenderer.builder().modules(tabTPS, Theme.DEFAULT, "cpu").moduleRenderFunction(MODULE_RENDERER).build();
    this.memoryRenderer = ModuleRenderer.builder().modules(new MemoryModule(tabTPS, Theme.DEFAULT, true)).moduleRenderFunction(MODULE_RENDERER).build();
  }

  @CommandDescription("tabtps.command.tickinfo.description")
  @CommandPermission(Constants.PERMISSION_COMMAND_TICKINFO)
  @CommandMethod("tickinfo|mspt|tps")
  public void onTPS(final @NonNull CommandSender sender) {
    final List<Component> messages = new ArrayList<>();
    messages.add(Component.empty());
    messages.add(
      LinearComponents.linear(
        Constants.PREFIX,
        Component.space(),
        Component.translatable("tabtps.command.tickinfo.text.header", NamedTextColor.GRAY, TextDecoration.ITALIC)
      )
    );
    messages.add(this.formatTPS());
    messages.addAll(this.formatTickTimes());
    messages.add(this.cpuRenderer.render().hoverEvent(HoverEvent.showText(
      Component.translatable("tabtps.command.tickinfo.text.cpu_hover", NamedTextColor.GRAY)
    )));
    messages.add(this.renderMemory());
    messages.add(MemoryUtil.renderBar(null, ManagementFactory.getMemoryMXBean().getHeapMemoryUsage(), 91));
    this.tabTPS.chat().send(sender, messages);
  }

  private @NonNull Component renderMemory() {
    return this.memoryRenderer.render()
      .hoverEvent(HoverEvent.showText(
        Component.text()
          .color(NamedTextColor.GRAY)
          .append(Component.translatable("tabtps.command.tickinfo.text.memory_hover"))
          .append(Component.newline())
          .append(Component.translatable("tabtps.label.used"))
          .append(Component.text("/", NamedTextColor.WHITE))
          .append(Component.translatable("tabtps.label.allocated"))
          .append(Component.space())
          .append(Component.text("(", NamedTextColor.WHITE))
          .append(Component.translatable("tabtps.label.maximum"))
          .append(Component.text(")", NamedTextColor.WHITE))
          .build()
      ));
  }

  private @NonNull Component formatTPS() {
    final double[] tps = this.tabTPS.tpsUtil().tps();
    final TextComponent.Builder builder = Component.text()
      .hoverEvent(HoverEvent.showText(Component.translatable("taptps.command.tickinfo.text.tps_hover", NamedTextColor.GRAY)))
      .append(Component.translatable("tabtps.label.tps", NamedTextColor.GRAY))
      .append(Component.text(":", NamedTextColor.WHITE))
      .append(Component.space());
    final Iterator<Double> tpsIterator = Arrays.stream(tps).iterator();
    final Deque<String> tpsDurations = tps.length == 4
      ? new ArrayDeque<>(Arrays.asList("5s", "1m", "5m", "15m"))
      : new ArrayDeque<>(Arrays.asList("1m", "5m", "15m"));
    while (tpsIterator.hasNext()) {
      builder.append(TPSUtil.coloredTps(tpsIterator.next(), Theme.DEFAULT.colorScheme()))
        .append(Component.space())
        .append(Component.text("(", NamedTextColor.GRAY, TextDecoration.ITALIC))
        .append(Component.text(tpsDurations.removeFirst(), NamedTextColor.GRAY, TextDecoration.ITALIC))
        .append(Component.text(")", NamedTextColor.GRAY, TextDecoration.ITALIC));
      if (tpsIterator.hasNext()) {
        builder.append(Component.text(",", NamedTextColor.WHITE))
          .append(Component.space());
      }
    }
    return builder.build();
  }

  private @NonNull List<Component> formatTickTimes() {
    if (Environment.majorMinecraftVersion() >= 15 && Environment.paper()) {
      try {
        final List<Component> output = new ArrayList<>();

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

        output.add(
          LinearComponents.linear(
            Component.translatable("tabtps.label.mspt", NamedTextColor.GRAY),
            Component.space(),
            Component.text("-", NamedTextColor.WHITE),
            Component.space(),
            Component.translatable("tabtps.label.average", NamedTextColor.GRAY),
            Component.text(", ", NamedTextColor.WHITE),
            Component.translatable("tabtps.label.minimum", NamedTextColor.GRAY),
            Component.text(", ", NamedTextColor.WHITE),
            Component.translatable("tabtps.label.maximum", NamedTextColor.GRAY)
          ).hoverEvent(HoverEvent.showText(Component.translatable("tabtps.command.tickinfo.text.mspt_hover", NamedTextColor.GRAY)))
        );

        output.add(this.formatStatistics(
          "├─",
          Component.text("5s"),
          statistics5s
        ));
        output.add(this.formatStatistics(
          "├─",
          Component.text("10s"),
          statistics10s
        ));
        output.add(this.formatStatistics(
          "└─",
          Component.text("60s"),
          statistics60s
        ));

        return output;
      } catch (final Throwable throwable) {
        this.tabTPS.getLogger().log(Level.WARNING, "Failed to retrieve tick time statistics", throwable);
        throw new IllegalStateException("Failed to retrieve tick time statistics", throwable);
      }
    } else {
      return Collections.singletonList(
        this.msptRenderer.render()
          .hoverEvent(HoverEvent.showText(
            Component.translatable("tabtps.command.tickinfo.text.mspt_hover", NamedTextColor.GRAY)
          ))
      );
    }
  }

  private @NonNull Component formatStatistics(final @NonNull String branch, final @NonNull Component time, final @NonNull LongSummaryStatistics statistics) {
    return LinearComponents.linear(
      Component.space(),
      Component.text(branch, NamedTextColor.WHITE),
      Component.space(),
      time.color(NamedTextColor.GRAY),
      Component.space(),
      Component.text("-", NamedTextColor.WHITE),
      Component.space(),
      TPSUtil.coloredMspt(TPSUtil.toMilliseconds(statistics.getAverage()), Theme.DEFAULT.colorScheme()),
      Component.text(",", NamedTextColor.WHITE),
      Component.space(),
      TPSUtil.coloredMspt(TPSUtil.toMilliseconds(statistics.getMin()), Theme.DEFAULT.colorScheme()),
      Component.text(",", NamedTextColor.WHITE),
      Component.space(),
      TPSUtil.coloredMspt(TPSUtil.toMilliseconds(statistics.getMax()), Theme.DEFAULT.colorScheme())
    );
  }
}
