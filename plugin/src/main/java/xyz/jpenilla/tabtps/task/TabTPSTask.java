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

import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.config.DisplayConfig;
import xyz.jpenilla.tabtps.config.Theme;
import xyz.jpenilla.tabtps.module.ModuleRenderer;

public class TabTPSTask extends BukkitRunnable {
  private final ModuleRenderer headerRenderer;
  private final ModuleRenderer footerRenderer;
  private final Player player;
  private final TabTPS tabTPS;

  public TabTPSTask(final @NonNull TabTPS tabTPS, final @NonNull Player player, final DisplayConfig.@NonNull TabSettings settings) {
    final Theme theme = tabTPS.configManager().theme(settings.theme());
    this.headerRenderer = ModuleRenderer.builder()
      .modules(tabTPS, tabTPS.configManager().theme(settings.theme()), player, settings.headerModules())
      .separator(tabTPS.miniMessage().parse(theme.separator()))
      .moduleRenderFunction(ModuleRenderer.standardRenderFunction(theme))
      .build();
    this.footerRenderer = ModuleRenderer.builder()
      .modules(tabTPS, tabTPS.configManager().theme(settings.theme()), player, settings.footerModules())
      .separator(tabTPS.miniMessage().parse(theme.separator()))
      .moduleRenderFunction(ModuleRenderer.standardRenderFunction(theme))
      .build();
    this.player = player;
    this.tabTPS = tabTPS;
  }

  @Override
  public void run() {
    if (!this.player.isOnline()) {
      this.tabTPS.taskManager().stopTabTask(this.player);
    }
    final Audience player = this.tabTPS.audiences().player(this.player);
    if (this.headerRenderer.moduleCount() > 0) {
      player.sendPlayerListHeader(this.headerRenderer.render());
    }
    if (this.footerRenderer.moduleCount() > 0) {
      player.sendPlayerListFooter(this.footerRenderer.render());
    }
  }
}
