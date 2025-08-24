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
package xyz.jpenilla.tabtps.paper.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.command.ConsoleCommander;

public final class BukkitConsoleCommander implements ConsoleCommander {
  private final CommandSender commandSender;
  private final Audience audience;

  private BukkitConsoleCommander(final @NonNull BukkitAudiences bukkitAudiences, final @NonNull CommandSender sender) {
    this.commandSender = sender;
    this.audience = bukkitAudiences.sender(sender);
  }

  public static @NonNull BukkitConsoleCommander from(final @NonNull BukkitAudiences bukkitAudiences, final @NonNull CommandSender sender) {
    return new BukkitConsoleCommander(bukkitAudiences, sender);
  }

  @Override
  public boolean hasPermission(final @NonNull String permissionString) {
    return this.commandSender.hasPermission(permissionString);
  }

  @Override
  public @NonNull Audience audience() {
    return this.audience;
  }

  public @NonNull CommandSender commandSender() {
    return this.commandSender;
  }
}
