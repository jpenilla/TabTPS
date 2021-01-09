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
package xyz.jpenilla.tabtps.task;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.util.RunnableFuturePair;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class TaskManager {
  private final TabTPS tabTPS;
  private final ScheduledExecutorService executor;
  private final Map<UUID, RunnableFuturePair<TabTPSTask, Future<?>>> tabTasks = new HashMap<>();
  private final Map<UUID, RunnableFuturePair<ActionBarTPSTask, Future<?>>> actionBarTasks = new HashMap<>();
  private final Map<UUID, RunnableFuturePair<BossBarTPSTask, Future<?>>> bossBarTasks = new HashMap<>();

  public TaskManager(final @NonNull TabTPS tabTPS) {
    this.tabTPS = tabTPS;
    final ScheduledThreadPoolExecutor ex = new ScheduledThreadPoolExecutor(4);
    ex.setRemoveOnCancelPolicy(true);
    this.executor = Executors.unconfigurableScheduledExecutorService(ex);
  }

  public boolean hasTabTask(final @NonNull Player player) {
    return this.tabTasks.containsKey(player.getUniqueId());
  }

  public void startTabTask(final @NonNull Player player) {
    this.stopTabTask(player);
    this.tabTPS.configManager().findDisplayConfig(player).ifPresent(config -> {
      if (!config.tabSettings().allow()) {
        return;
      }
      final TabTPSTask task = new TabTPSTask(this.tabTPS, player, config.tabSettings());
      final Future<?> future = this.executor.scheduleAtFixedRate(task, 0L, this.tabTPS.pluginSettings().updateRates().tab(), TimeUnit.MILLISECONDS);
      this.tabTasks.put(player.getUniqueId(), new RunnableFuturePair<>(task, future));
    });
  }

  public void stopTabTask(final @NonNull Player player) {
    final RunnableFuturePair<TabTPSTask, Future<?>> pair = this.tabTasks.remove(player.getUniqueId());
    if (pair != null) {
      pair.future().cancel(false);
      if (player.isOnline()) {
        this.tabTPS.audiences().player(player).sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
      }
    }
  }

  public boolean hasActionBarTask(final @NonNull Player player) {
    return this.actionBarTasks.containsKey(player.getUniqueId());
  }

  public void startActionBarTask(final @NonNull Player player) {
    this.stopActionBarTask(player);
    this.tabTPS.configManager().findDisplayConfig(player).ifPresent(config -> {
      if (!config.actionBarSettings().allow()) {
        return;
      }
      final ActionBarTPSTask task = new ActionBarTPSTask(this.tabTPS, player, config.actionBarSettings());
      final Future<?> future = this.executor.scheduleAtFixedRate(task, 0L, this.tabTPS.pluginSettings().updateRates().actionBar(), TimeUnit.MILLISECONDS);
      this.actionBarTasks.put(player.getUniqueId(), new RunnableFuturePair<>(task, future));
    });
  }

  public void stopActionBarTask(final @NonNull Player player) {
    final RunnableFuturePair<ActionBarTPSTask, Future<?>> pair = this.actionBarTasks.remove(player.getUniqueId());
    if (pair != null) {
      pair.future().cancel(false);
    }
  }

  public boolean hasBossTask(final @NonNull Player player) {
    return this.bossBarTasks.containsKey(player.getUniqueId());
  }

  public void startBossTask(final @NonNull Player player) {
    this.stopBossTask(player);
    this.tabTPS.configManager().findDisplayConfig(player).ifPresent(config -> {
      if (!config.bossBarSettings().allow()) {
        return;
      }
      final BossBarTPSTask task = new BossBarTPSTask(this.tabTPS, player, config.bossBarSettings());
      final Future<?> future = this.executor.scheduleAtFixedRate(task, 0L, this.tabTPS.pluginSettings().updateRates().bossBar(), TimeUnit.MILLISECONDS);
      this.bossBarTasks.put(player.getUniqueId(), new RunnableFuturePair<>(task, future));
    });
  }

  public void stopBossTask(final @NonNull Player player) {
    final RunnableFuturePair<BossBarTPSTask, Future<?>> pair = this.bossBarTasks.remove(player.getUniqueId());
    if (pair != null) {
      pair.future().cancel(false);
      pair.runnable().removeViewer();
    }
  }

  public void shutdown() {
    this.executor.shutdownNow();
    try {
      if (!this.executor.awaitTermination(1, TimeUnit.SECONDS)) {
        throw new IllegalStateException("Thread pool did not shut down after a 1 second time out");
      }
    } catch (final InterruptedException | IllegalStateException e) {
      this.tabTPS.getLogger().log(Level.SEVERE, "Failed to shut down thread pool", e);
    }
  }
}
