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
package xyz.jpenilla.tabtps;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.task.TaskManager;

import java.util.UUID;

public class JoinQuitListener implements Listener {
  private final TabTPS tabTPS;

  public JoinQuitListener(final @NonNull TabTPS tabTPS) {
    this.tabTPS = tabTPS;
  }

  @EventHandler
  public void onJoin(final @NonNull PlayerJoinEvent e) {
    final TaskManager taskManager = this.tabTPS.taskManager();
    final UserPreferences userPreferences = this.tabTPS.userPreferences();
    final Player player = e.getPlayer();
    final UUID uuid = player.getUniqueId();

    this.tabTPS.permissionManager().attach(player);

    this.tabTPS.configManager().findDisplayConfig(player).ifPresent(config -> {

      if (config.actionBarSettings().allow()) {
        if (config.actionBarSettings().enableOnLogin()) {
          userPreferences.actionBarEnabled().add(uuid);
        }
        if (userPreferences.actionBarEnabled().contains(uuid)) {
          taskManager.startActionBarTask(player);
        }
      }

      if (config.bossBarSettings().allow()) {
        if (config.bossBarSettings().enableOnLogin()) {
          userPreferences.bossBarEnabled().add(uuid);
        }
        if (userPreferences.bossBarEnabled().contains(uuid)) {
          taskManager.startBossTask(player);
        }
      }

      if (config.tabSettings().allow()) {
        if (config.tabSettings().enableOnLogin()) {
          userPreferences.tabEnabled().add(uuid);
        }
        if (userPreferences.tabEnabled().contains(uuid)) {
          taskManager.startTabTask(player);
        }
      }

    });
  }

  @EventHandler
  public void onQuit(final @NonNull PlayerQuitEvent e) {
    final Player player = e.getPlayer();
    this.tabTPS.taskManager().stopTabTask(player);
    this.tabTPS.taskManager().stopActionBarTask(player);
  }
}
