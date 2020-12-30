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
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.config.DisplayConfig;
import xyz.jpenilla.tabtps.module.Module;
import xyz.jpenilla.tabtps.module.ModuleRenderer;

public class BossBarTPSTask extends BukkitRunnable {
  private final TabTPS tabTPS;
  private final Audience audience;
  private final DisplayConfig.BossBarSettings settings;
  private final ModuleRenderer renderer;
  private final BossBar bar;

  public BossBarTPSTask(
    final @NonNull TabTPS tabTPS,
    final @NonNull Player player,
    final DisplayConfig.@NonNull BossBarSettings settings
  ) {
    this.tabTPS = tabTPS;
    this.audience = this.tabTPS.audiences().player(player);
    this.settings = settings;
    this.renderer = ModuleRenderer.builder()
      .modules(tabTPS, player, settings.modules())
      .separator(Component.space())
      .moduleRenderFunction(BossBarTPSTask::renderModule)
      .build();
    this.bar = BossBar.bossBar(
      this.renderer.render(),
      this.progress(),
      this.color(),
      this.overlay()
    );
    this.audience.showBossBar(this.bar);
  }

  private float progress() {
    switch (this.settings.fillMode()) {
      case MSPT:
        return ensureInRange(this.tabTPS.tpsUtil().mspt() / 50.0f);
      case TPS:
        return ensureInRange(this.tabTPS.tpsUtil().tps()[0] / 20.0f);
      default:
        throw new IllegalStateException("Unknown or invalid fill mode: " + this.settings.fillMode());
    }
  }

  private static float ensureInRange(final double value) {
    return (float) Math.max(0.00D, Math.min(1.00D, value));
  }

  private BossBar.@NonNull Color color() {
    switch (this.settings.fillMode()) {
      case MSPT:
        final double mspt = this.tabTPS.tpsUtil().mspt();
        if (mspt < 25) {
          return BossBar.Color.GREEN;
        } else if (mspt < 40) {
          return BossBar.Color.YELLOW;
        } else {
          return BossBar.Color.RED;
        }
      case TPS:
        final double tps = this.tabTPS.tpsUtil().tps()[0];
        if (tps > 18.50D) {
          return BossBar.Color.GREEN;
        } else if (tps > 15.00D) {
          return BossBar.Color.YELLOW;
        } else {
          return BossBar.Color.RED;
        }
      default:
        throw new IllegalStateException("Unknown or invalid fill mode: " + this.settings.fillMode());
    }
  }

  private BossBar.@NonNull Overlay overlay() {
    return this.settings.overlay();
  }

  private void updateBar() {
    this.bar.progress(this.progress());
    this.bar.color(this.color());
    this.bar.name(this.renderer.render());
  }

  private static @NonNull Component renderModule(final @NonNull Module module) {
    return Component.text()
      .append(Component.text(module.label(), NamedTextColor.GRAY))
      .append(Component.text(":", NamedTextColor.WHITE))
      .append(Component.space())
      .append(module.display())
      .build();
  }

  public void removeViewer() {
    this.audience.hideBossBar(this.bar);
  }

  @Override
  public void run() {
    this.updateBar();
  }
}
