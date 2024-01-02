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
package xyz.jpenilla.tabtps.common;

import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.display.DisplayHandler;
import xyz.jpenilla.tabtps.common.display.task.ActionBarDisplayTask;
import xyz.jpenilla.tabtps.common.display.task.BossBarDisplayTask;
import xyz.jpenilla.tabtps.common.display.task.TabDisplayTask;

@DefaultQualifier(NonNull.class)
public interface User<P> extends Commander {
  UUID uuid();

  Component displayName();

  boolean online();

  int ping();

  P base();

  State state();

  default DisplayHandler<TabDisplayTask> tab() {
    return this.state().tab();
  }

  default DisplayHandler<ActionBarDisplayTask> actionBar() {
    return this.state().actionBar();
  }

  default DisplayHandler<BossBarDisplayTask> bossBar() {
    return this.state().bossBar();
  }

  default List<DisplayHandler<?>> displays() {
    return this.state().displays();
  }

  default void markDirty() {
    this.state().markDirty();
  }

  default boolean shouldSave() {
    return this.state().shouldSave();
  }

  interface State {
    void populate(final State from);

    DisplayHandler<TabDisplayTask> tab();

    DisplayHandler<ActionBarDisplayTask> actionBar();

    DisplayHandler<BossBarDisplayTask> bossBar();

    List<DisplayHandler<?>> displays();

    void markDirty();

    boolean shouldSave();
  }
}
