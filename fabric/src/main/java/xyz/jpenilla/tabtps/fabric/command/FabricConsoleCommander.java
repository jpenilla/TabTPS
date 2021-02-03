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
package xyz.jpenilla.tabtps.fabric.command;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.audience.Audience;
import net.minecraft.commands.CommandSourceStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.command.ConsoleCommander;
import xyz.jpenilla.tabtps.fabric.TabTPSFabric;

public final class FabricConsoleCommander implements ConsoleCommander {
  private final TabTPSFabric tabTPSFabric;
  private final CommandSourceStack commandSourceStack;

  public FabricConsoleCommander(final @NonNull TabTPSFabric tabTPSFabric, final @NonNull CommandSourceStack commandSourceStack) {
    this.tabTPSFabric = tabTPSFabric;
    this.commandSourceStack = commandSourceStack;
  }

  @Override
  public boolean hasPermission(final @NonNull String permissionString) {
    return Permissions.check(this.commandSourceStack, permissionString);
  }

  @Override
  public @NonNull Audience audience() {
    return this.tabTPSFabric.serverAudiences().audience(this.commandSourceStack);
  }

  public @NonNull CommandSourceStack commandSourceStack() {
    return this.commandSourceStack;
  }
}
