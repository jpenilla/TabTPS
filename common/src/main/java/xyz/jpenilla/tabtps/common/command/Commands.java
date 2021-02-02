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
package xyz.jpenilla.tabtps.common.command;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.permission.PredicatePermission;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.TabTPS;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

public final class Commands {
  private final TabTPS tabTPS;
  private final CommandManager<Commander> commandManager;
  private final Map<String, PredicatePermission<Commander>> permissionPredicates = new HashMap<>();

  public Commands(final @NonNull TabTPS tabTPS, final @NonNull CommandManager<Commander> commandManager) {
    this.tabTPS = tabTPS;
    this.commandManager = commandManager;
    new ExceptionHandler(tabTPS).apply(commandManager);
  }

  public @NonNull CommandManager<Commander> commandManager() {
    return this.commandManager;
  }

  public void registerPermissionPredicate(final @NonNull String key, final @NonNull PredicatePermission<Commander> predicatePermission) {
    this.permissionPredicates.put(key, predicatePermission);
  }

  public @NonNull PredicatePermission<Commander> permissionPredicate(final @NonNull String key) {
    return Objects.requireNonNull(this.permissionPredicates.get(key), "No permission predicate for key: " + key);
  }

  public void registerSubcommand(final @NonNull UnaryOperator<Command.Builder<Commander>> modifier) {
    this.commandManager.command(modifier.apply(
      this.commandManager.commandBuilder("tabtps")
    ));
  }
}
