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
package xyz.jpenilla.tabtps.util;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.jmplib.Environment;
import xyz.jpenilla.tabtps.Constants;
import xyz.jpenilla.tabtps.TabTPS;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PermissionManager {
  private final TabTPS tabTPS;
  private final Map<UUID, PermissionAttachment> permissionAttachments = new HashMap<>();

  public PermissionManager(final @NonNull TabTPS tabTPS) {
    this.tabTPS = tabTPS;
  }

  public void attach(final @NonNull Player player) {
    this.detach(player, false);

    final PermissionAttachment attachment = player.addAttachment(this.tabTPS);
    this.permissionAttachments.put(player.getUniqueId(), attachment);

    this.tabTPS.configManager().findDisplayConfig(player).ifPresent(config -> {
      if (config.actionBarSettings().allow()) {
        attachment.setPermission(Constants.PERMISSION_TOGGLE_ACTIONBAR, true);
      }
      if (config.bossBarSettings().allow()) {
        attachment.setPermission(Constants.PERMISSION_TOGGLE_BOSSBAR, true);
      }
      if (config.tabSettings().allow()) {
        attachment.setPermission(Constants.PERMISSION_TOGGLE_TAB, true);
      }
    });

    player.recalculatePermissions();
    updateCommands(player);
  }

  public void detach(final @NonNull Player player) {
    this.detach(player, true);
  }

  public void detach(final @NonNull Player player, final boolean updateCommands) {
    final PermissionAttachment existing = this.permissionAttachments.remove(player.getUniqueId());
    if (existing != null) {
      existing.remove();
      player.recalculatePermissions();
      if (updateCommands) {
        updateCommands(player);
      }
    }
  }

  private static void updateCommands(final @NonNull Player player) {
    if (Environment.majorMinecraftVersion() >= 13) {
      player.updateCommands();
    }
  }
}
