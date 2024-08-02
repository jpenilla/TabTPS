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
package xyz.jpenilla.tabtps.neoforge.command;

import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.modded.data.MultiplePlayerSelector;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.command.Commands;
import xyz.jpenilla.tabtps.common.command.commands.PingCommand;
import xyz.jpenilla.tabtps.neoforge.TabTPSNeoForge;

import static org.incendo.cloud.minecraft.modded.parser.VanillaArgumentParsers.multiplePlayerSelectorParser;

public final class NeoForgePingCommand extends PingCommand {
  private final TabTPSNeoForge tabTPSNeoForge;

  public NeoForgePingCommand(final @NonNull TabTPSNeoForge tabTPSNeoForge, final @NonNull Commands commands) {
    super(tabTPSNeoForge.tabTPS(), commands);
    this.tabTPSNeoForge = tabTPSNeoForge;
  }

  @Override
  public void register() {
    this.registerPingTargetsCommand(multiplePlayerSelectorParser(), this::handlePingTargets);
  }

  private void handlePingTargets(final @NonNull CommandContext<Commander> context) {
    final MultiplePlayerSelector target = context.get("target");
    this.pingTargets(
      context.sender(),
      target.values().stream()
        .map(this.tabTPSNeoForge.userService()::user)
        .collect(Collectors.toList()),
      target.inputString(),
      context.get("page")
    );
  }
}
