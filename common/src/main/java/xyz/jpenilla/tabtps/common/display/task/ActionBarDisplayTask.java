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
package xyz.jpenilla.tabtps.common.display.task;

import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.User;
import xyz.jpenilla.tabtps.common.config.DisplayConfig;
import xyz.jpenilla.tabtps.common.config.Theme;
import xyz.jpenilla.tabtps.common.display.Display;
import xyz.jpenilla.tabtps.common.module.ModuleRenderer;
import xyz.jpenilla.tabtps.common.util.Serializers;

public final class ActionBarDisplayTask implements Display {
  private final User<?> user;
  private final TabTPS tabTPS;
  private final ModuleRenderer renderer;

  public ActionBarDisplayTask(final @NonNull TabTPS tabTPS, final @NonNull User<?> user, final DisplayConfig.@NonNull ActionBarSettings settings) {
    final Theme theme = tabTPS.configManager().theme(settings.theme());
    this.renderer = ModuleRenderer.builder()
      .modules(tabTPS, theme, user, settings.modules())
      .separator(Serializers.MINIMESSAGE.parse(settings.separator()))
      .moduleRenderFunction(ModuleRenderer.standardRenderFunction(theme))
      .build();
    this.user = user;
    this.tabTPS = tabTPS;
  }

  @Override
  public void run() {
    if (!this.user.online()) {
      this.user.actionBar().stopDisplay();
    }
    this.user.sendActionBar(this.renderer.render());
  }
}
