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
package xyz.jpenilla.tabtps.common.task;

import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.TabTPS;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class TaskManager {
  private final TabTPS tabTPS;
  private final ScheduledExecutorService executor;

  public TaskManager(final @NonNull TabTPS tabTPS) {
    this.tabTPS = tabTPS;
    final ScheduledThreadPoolExecutor ex = new ScheduledThreadPoolExecutor(4);
    ex.setRemoveOnCancelPolicy(true);
    this.executor = Executors.unconfigurableScheduledExecutorService(ex);
  }

  public @NonNull ScheduledExecutorService executor() {
    return this.executor;
  }

  public void shutdown() {
    this.executor.shutdownNow();
    try {
      if (!this.executor.awaitTermination(1, TimeUnit.SECONDS)) {
        throw new IllegalStateException("Thread pool did not shut down after a 1 second time out");
      }
    } catch (final InterruptedException | IllegalStateException e) {
      this.tabTPS.platform().logger().error("Failed to shut down thread pool", e);
    }
  }
}
