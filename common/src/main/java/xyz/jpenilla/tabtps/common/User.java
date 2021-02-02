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
package xyz.jpenilla.tabtps.common;

import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.display.DisplayHandler;
import xyz.jpenilla.tabtps.common.display.task.ActionBarDisplayTask;
import xyz.jpenilla.tabtps.common.display.task.BossBarDisplayTask;
import xyz.jpenilla.tabtps.common.display.task.TabDisplayTask;

import java.util.UUID;

public interface User<P> extends Commander {
  @NonNull UUID uuid();

  @NonNull Component displayName();

  boolean online();

  int ping();

  void populate(final @NonNull User<P> deserialized);

  @NonNull P base();

  @NonNull DisplayHandler<TabDisplayTask> tab();

  @NonNull DisplayHandler<ActionBarDisplayTask> actionBar();

  @NonNull DisplayHandler<BossBarDisplayTask> bossBar();
}
