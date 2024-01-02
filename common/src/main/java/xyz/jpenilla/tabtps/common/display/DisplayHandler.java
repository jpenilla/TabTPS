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
package xyz.jpenilla.tabtps.common.display;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.User;
import xyz.jpenilla.tabtps.common.config.DisplayConfig;
import xyz.jpenilla.tabtps.common.util.RunnableFuturePair;

public final class DisplayHandler<D extends Display> {
  private transient final TabTPS tabTPS;
  private transient final User<?> user;
  private transient final Function<DisplayConfig, D> displayFactory;
  private transient final int updateRate;
  private transient RunnableFuturePair<D> futurePair = null;
  private boolean enabled = false;

  public DisplayHandler(
    final @NonNull TabTPS tabTPS,
    final @NonNull User<?> user,
    final int updateRate,
    final @NonNull Function<DisplayConfig, D> displayFactory
  ) {
    this.tabTPS = tabTPS;
    this.user = user;
    this.updateRate = updateRate;
    this.displayFactory = displayFactory;
  }

  public boolean enabled() {
    return this.enabled;
  }

  public void enabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public void startDisplay() {
    this.stopDisplay();
    this.tabTPS.findDisplayConfig(this.user).ifPresent(config -> {
      final D task = this.displayFactory.apply(config);
      final Future<?> future = this.tabTPS.executor()
        .scheduleAtFixedRate(task, 0L, this.updateRate, TimeUnit.MILLISECONDS);
      this.futurePair = new RunnableFuturePair<>(task, future);
    });
  }

  public void stopDisplay() {
    if (this.futurePair != null) {
      this.futurePair.future().cancel(false);
      this.futurePair.runnable().disable();
      this.futurePair = null;
    }
  }
}
