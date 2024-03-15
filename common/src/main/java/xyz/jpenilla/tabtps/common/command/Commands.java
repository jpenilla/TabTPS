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
package xyz.jpenilla.tabtps.common.command;

import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import xyz.jpenilla.tabtps.common.TabTPS;

public final class Commands {
  private final CommandManager<Commander> commandManager;

  public Commands(final @NonNull TabTPS tabTPS, final @NonNull CommandManager<Commander> commandManager) {
    this.commandManager = commandManager;
    new ExceptionHandler(tabTPS).apply(commandManager);
  }

  public @NonNull CommandManager<Commander> commandManager() {
    return this.commandManager;
  }

  public void registerSubcommand(final @NonNull Function<Command.Builder<Commander>, Command.Builder<? extends Commander>> modifier) {
    this.commandManager.command(modifier.apply(this.rootBuilder()));
  }

  public Command.@NonNull Builder<Commander> rootBuilder() {
    return this.commandManager.commandBuilder("tabtps");
  }

  public void register(final Command.@NonNull Builder<? extends Commander> builder) {
    this.commandManager.command(builder);
  }
}
