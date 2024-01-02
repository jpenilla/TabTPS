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
package xyz.jpenilla.tabtps.common.display.task;

import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.User;
import xyz.jpenilla.tabtps.common.config.DisplayConfig;
import xyz.jpenilla.tabtps.common.config.Theme;
import xyz.jpenilla.tabtps.common.display.Display;
import xyz.jpenilla.tabtps.common.module.ModuleRenderer;

public final class TabDisplayTask implements Display {
  private final ModuleRenderer headerRenderer;
  private final ModuleRenderer footerRenderer;
  private final User<?> user;

  public TabDisplayTask(final @NonNull TabTPS tabTPS, final @NonNull User<?> user, final DisplayConfig.@NonNull TabSettings settings) {
    final Theme theme = tabTPS.configManager().theme(settings.theme());
    this.headerRenderer = ModuleRenderer.builder()
      .modules(tabTPS, tabTPS.configManager().theme(settings.theme()), user, settings.headerModules())
      .separator(settings.separator())
      .moduleRenderFunction(ModuleRenderer.standardRenderFunction(theme))
      .build();
    this.footerRenderer = ModuleRenderer.builder()
      .modules(tabTPS, tabTPS.configManager().theme(settings.theme()), user, settings.footerModules())
      .separator(settings.separator())
      .moduleRenderFunction(ModuleRenderer.standardRenderFunction(theme))
      .build();
    this.user = user;
  }

  @Override
  public void run() {
    if (!this.user.online()) {
      this.user.tab().stopDisplay();
      return;
    }
    if (this.headerRenderer.moduleCount() > 0) {
      this.user.sendPlayerListHeader(this.headerRenderer.render());
    }
    if (this.footerRenderer.moduleCount() > 0) {
      this.user.sendPlayerListFooter(this.footerRenderer.render());
    }
  }

  @Override
  public void disable() {
    if (this.user.online()) {
      this.user.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
    }
  }
}
