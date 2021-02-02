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
package xyz.jpenilla.tabtps.common.command.commands;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.command.Commands;
import xyz.jpenilla.tabtps.common.command.TabTPSCommand;
import xyz.jpenilla.tabtps.common.config.Theme;
import xyz.jpenilla.tabtps.common.module.MemoryModule;
import xyz.jpenilla.tabtps.common.module.Module;
import xyz.jpenilla.tabtps.common.module.ModuleRenderer;
import xyz.jpenilla.tabtps.common.util.Constants;
import xyz.jpenilla.tabtps.common.util.MemoryUtil;
import xyz.jpenilla.tabtps.common.util.TPSUtil;

import java.lang.management.ManagementFactory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public final class TickInfoCommand extends TabTPSCommand {
  private static final Function<Module, Component> MODULE_RENDERER = ModuleRenderer.standardRenderFunction(Theme.DEFAULT);

  private final ModuleRenderer cpuRenderer;
  private final ModuleRenderer memoryRenderer;
  private final Formatter formatter;

  private TickInfoCommand(final @NonNull TabTPS tabTPS, final @NonNull Commands commands, final @NonNull Formatter formatter) {
    super(tabTPS, commands);
    this.formatter = formatter;
    this.cpuRenderer = ModuleRenderer.builder().modules(tabTPS, Theme.DEFAULT, "cpu").moduleRenderFunction(MODULE_RENDERER).build();
    this.memoryRenderer = ModuleRenderer.builder().modules(new MemoryModule(tabTPS, Theme.DEFAULT, true)).moduleRenderFunction(MODULE_RENDERER).build();
  }

  @Override
  public void register() {
    this.commandManager.command(
      this.commandManager.commandBuilder("tickinfo", "mspt", "tps")
        .permission(Constants.PERMISSION_COMMAND_TICKINFO)
        .meta(CommandMeta.DESCRIPTION, "tabtps.command.tickinfo.description")
        .handler(this::executeTickInfo)
    );
  }

  public static @NonNull TickInfoCommand defaultFormatter(final @NonNull TabTPS tabTPS, final @NonNull Commands commands) {
    return withFormatter(tabTPS, commands, new DefaultFormatter(tabTPS));
  }

  public static @NonNull TickInfoCommand withFormatter(final @NonNull TabTPS tabTPS, final @NonNull Commands commands, final @NonNull Formatter formatter) {
    return new TickInfoCommand(tabTPS, commands, formatter);
  }

  private void executeTickInfo(final @NonNull CommandContext<Commander> ctx) {
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
    messages.addAll(this.formatter.formatTickTimes());
    messages.add(this.cpuRenderer.render().hoverEvent(
      Component.translatable("tabtps.command.tickinfo.text.cpu_hover", NamedTextColor.GRAY)
    ));
    messages.add(this.renderMemory());
    messages.add(MemoryUtil.renderBar(null, ManagementFactory.getMemoryMXBean().getHeapMemoryUsage(), 91));
    messages.forEach(ctx.getSender()::sendMessage);
  }

  private @NonNull Component renderMemory() {
    return this.memoryRenderer.render()
      .hoverEvent(
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
      );
  }

  private @NonNull Component formatTPS() {
    final double[] tps = this.tabTPS.platform().tickTimeService().recentTps();
    final TextComponent.Builder builder = Component.text()
      .hoverEvent(Component.translatable("taptps.command.tickinfo.text.tps_hover", NamedTextColor.GRAY))
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

  public interface Formatter {
    @NonNull List<Component> formatTickTimes();
  }

  private static final class DefaultFormatter implements Formatter {
    private final ModuleRenderer msptRenderer;

    DefaultFormatter(final @NonNull TabTPS tabTPS) {
      this.msptRenderer = ModuleRenderer.builder().modules(tabTPS, Theme.DEFAULT, "mspt").moduleRenderFunction(MODULE_RENDERER).build();
    }

    @Override
    public @NonNull List<Component> formatTickTimes() {
      return Collections.singletonList(
        this.msptRenderer.render()
          .hoverEvent(
            Component.translatable("tabtps.command.tickinfo.text.mspt_hover", NamedTextColor.GRAY)
          )
      );
    }
  }
}
