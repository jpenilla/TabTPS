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
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import xyz.jpenilla.tabtps.common.Messages;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.command.Commands;
import xyz.jpenilla.tabtps.common.command.TabTPSCommand;
import xyz.jpenilla.tabtps.common.util.Components;
import xyz.jpenilla.tabtps.common.util.Constants;
import xyz.jpenilla.tabtps.common.util.MemoryUtil;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;
import static org.incendo.cloud.minecraft.extras.RichDescription.richDescription;

public final class MemoryCommand extends TabTPSCommand {
  public MemoryCommand(final @NonNull TabTPS tabTPS, final @NonNull Commands commands) {
    super(tabTPS, commands);
  }

  @Override
  public void register() {
    this.commands.register(this.commandManager.commandBuilder("memory", "mem", "ram")
      .permission(Constants.PERMISSION_COMMAND_TICKINFO)
      .commandDescription(richDescription(Messages.COMMAND_MEMORY_DESCRIPTION.plain()))
      .handler(this::executeMemory));
  }

  private void executeMemory(final @NonNull CommandContext<Commander> ctx) {
    final List<Component> messages = new ArrayList<>();
    messages.add(empty());
    final Component header = Components.ofChildren(
      Constants.PREFIX,
      space(),
      Messages.COMMAND_MEMORY_TEXT_HEADER.styled(GRAY, ITALIC)
    );
    messages.add(header);
    if (!this.tabTPS.configManager().pluginSettings().ignoredMemoryPools().contains("Heap Memory Usage")) {
      messages.add(MemoryUtil.renderBar("Heap Memory Usage", ManagementFactory.getMemoryMXBean().getHeapMemoryUsage(), 60));
    }
    if (!this.tabTPS.configManager().pluginSettings().ignoredMemoryPools().contains("Non-Heap Memory Usage")) {
      messages.add(MemoryUtil.renderBar("Non-Heap Memory Usage", ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage(), 60));
    }
    ManagementFactory.getMemoryPoolMXBeans().stream()
      .filter(bean -> bean != null && !this.tabTPS.configManager().pluginSettings().ignoredMemoryPools().contains(bean.getName()))
      .sorted(Comparator.comparing(MemoryPoolMXBean::getName))
      .map(bean -> MemoryUtil.renderBar(bean.getName(), bean.getUsage(), 60))
      .forEach(messages::add);
    messages.forEach(ctx.sender()::sendMessage);
  }
}
