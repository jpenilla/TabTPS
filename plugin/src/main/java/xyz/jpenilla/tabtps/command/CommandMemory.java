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
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.Constants;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.util.MemoryUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class CommandMemory {
  private final TabTPS tabTPS;

  CommandMemory(final @NonNull TabTPS tabTPS, final @NonNull CommandManager mgr) {
    this.tabTPS = tabTPS;
  }

  @CommandDescription("tabtps.command.memory.description")
  @CommandPermission(Constants.PERMISSION_COMMAND_TICKINFO)
  @CommandMethod("memory|mem|ram")
  public void onMemory(final @NonNull CommandSender sender) {
    final List<Component> messages = new ArrayList<>();
    messages.add(Component.empty());
    final Component header = LinearComponents.linear(
      Constants.PREFIX,
      Component.space(),
      Component.translatable("tabtps.command.memory.text.header", NamedTextColor.GRAY, TextDecoration.ITALIC)
    );
    messages.add(header);
    if (!this.tabTPS.pluginSettings().ignoredMemoryPools().contains("Heap Memory Usage")) {
      messages.add(MemoryUtil.renderBar("Heap Memory Usage", ManagementFactory.getMemoryMXBean().getHeapMemoryUsage(), 60));
    }
    if (!this.tabTPS.pluginSettings().ignoredMemoryPools().contains("Non-Heap Memory Usage")) {
      messages.add(MemoryUtil.renderBar("Non-Heap Memory Usage", ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage(), 60));
    }
    ManagementFactory.getMemoryPoolMXBeans().stream()
      .filter(bean -> bean != null && !this.tabTPS.pluginSettings().ignoredMemoryPools().contains(bean.getName()))
      .sorted(Comparator.comparing(MemoryPoolMXBean::getName))
      .map(bean -> MemoryUtil.renderBar(bean.getName(), bean.getUsage(), 60))
      .forEach(messages::add);
    this.tabTPS.chat().send(sender, messages);
  }
}
