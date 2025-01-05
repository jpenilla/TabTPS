/*
 * This file is part of TabTPS, licensed under the MIT License.
 *
 * Copyright (c) 2020-2024 Jason Penilla
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

import java.lang.management.ManagementFactory;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import xyz.jpenilla.tabtps.common.Messages;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.command.Commands;
import xyz.jpenilla.tabtps.common.command.TabTPSCommand;
import xyz.jpenilla.tabtps.common.config.Theme;
import xyz.jpenilla.tabtps.common.module.MemoryModule;
import xyz.jpenilla.tabtps.common.module.Module;
import xyz.jpenilla.tabtps.common.module.ModuleRenderer;
import xyz.jpenilla.tabtps.common.util.Components;
import xyz.jpenilla.tabtps.common.util.Constants;
import xyz.jpenilla.tabtps.common.util.MemoryUtil;
import xyz.jpenilla.tabtps.common.util.TPSUtil;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;
import static org.incendo.cloud.minecraft.extras.RichDescription.richDescription;

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
    this.commands.register(this.commandManager.commandBuilder("tickinfo", "mspt", "tps")
      .permission(Constants.PERMISSION_COMMAND_TICKINFO)
      .commandDescription(richDescription(Messages.COMMAND_TICKINFO_DESCRIPTION.plain()))
      .handler(this::executeTickInfo));
  }

  public static @NonNull TickInfoCommand defaultFormatter(final @NonNull TabTPS tabTPS, final @NonNull Commands commands) {
    return withFormatter(tabTPS, commands, new DefaultFormatter(tabTPS));
  }

  public static @NonNull TickInfoCommand withFormatter(final @NonNull TabTPS tabTPS, final @NonNull Commands commands, final @NonNull Formatter formatter) {
    return new TickInfoCommand(tabTPS, commands, formatter);
  }

  private void executeTickInfo(final @NonNull CommandContext<Commander> ctx) {
    final List<Component> messages = new ArrayList<>();
    messages.add(empty());
    messages.add(Components.ofChildren(
      Constants.PREFIX,
      space(),
      Messages.COMMAND_TICKINFO_TEXT_HEADER.styled(GRAY, ITALIC)
    ));
    messages.add(this.formatTPS());
    messages.addAll(this.formatter.formatTickTimes());
    messages.add(this.cpuRenderer.render().hoverEvent(
      Messages.COMMAND_TICKINFO_TEXT_CPU_HOVER.styled(GRAY)
    ));
    messages.add(this.renderMemory());
    messages.add(MemoryUtil.renderBar(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage(), 91));
    messages.forEach(ctx.sender()::sendMessage);
  }

  private @NonNull Component renderMemory() {
    return this.memoryRenderer.render()
      .hoverEvent(text()
        .color(GRAY)
        .append(Messages.COMMAND_TICKINFO_TEXT_MEMORY_HOVER)
        .append(newline())
        .append(Messages.LABEL_USED)
        .append(text("/", WHITE))
        .append(Messages.LABEL_ALLOCATED)
        .append(space())
        .append(text("(", WHITE))
        .append(Messages.LABEL_MAXIMUM)
        .append(text(")", WHITE))
        .build());
  }

  private @NonNull Component formatTPS() {
    final double[] tps = this.tabTPS.platform().tickTimeService().recentTps();
    final TextComponent.Builder builder = text()
      .hoverEvent(Messages.COMMAND_TICKINFO_TEXT_TPS_HOVER.styled(GRAY))
      .append(Messages.LABEL_TPS.styled(GRAY))
      .append(text(":", WHITE))
      .append(space());
    final Iterator<Double> tpsIterator = Arrays.stream(tps).iterator();
    final Deque<String> tpsDurations = tps.length == 4
      ? new ArrayDeque<>(Arrays.asList("5s", "1m", "5m", "15m"))
      : new ArrayDeque<>(Arrays.asList("1m", "5m", "15m"));
    while (tpsIterator.hasNext()) {
      builder.append(TPSUtil.coloredTps(tpsIterator.next(), Theme.DEFAULT.colorScheme()))
        .append(space())
        .append(text("(", GRAY, ITALIC))
        .append(text(tpsDurations.removeFirst(), GRAY, ITALIC))
        .append(text(")", GRAY, ITALIC));
      if (tpsIterator.hasNext()) {
        builder.append(text(",", WHITE))
          .append(space());
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
        this.msptRenderer.render().hoverEvent(Messages.COMMAND_TICKINFO_TEXT_MSPT_HOVER.styled(GRAY))
      );
    }
  }
}
