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
package xyz.jpenilla.tabtps.common;

import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.display.DisplayHandler;
import xyz.jpenilla.tabtps.common.display.task.ActionBarDisplayTask;
import xyz.jpenilla.tabtps.common.display.task.BossBarDisplayTask;
import xyz.jpenilla.tabtps.common.display.task.TabDisplayTask;

import java.util.UUID;

public abstract class AbstractUser<P> implements User<P> {
  protected final transient TabTPS tabTPS;
  protected final transient P base;
  protected final transient UUID uuid;
  private final DisplayHandler<TabDisplayTask> tabDisplayHandler;
  private final DisplayHandler<ActionBarDisplayTask> actionBarDisplayHandler;
  private final DisplayHandler<BossBarDisplayTask> bossBarDisplayHandler;

  protected AbstractUser(final @NonNull TabTPS tabTPS, final @NonNull P base, final @NonNull UUID uuid) {
    this.tabTPS = tabTPS;
    this.base = base;
    this.uuid = uuid;

    this.tabDisplayHandler = new DisplayHandler<>(
      tabTPS,
      this,
      tabTPS.configManager().pluginSettings().updateRates().tab(),
      config -> new TabDisplayTask(this.tabTPS, this, config.tabSettings())
    );
    this.actionBarDisplayHandler = new DisplayHandler<>(
      tabTPS,
      this,
      tabTPS.configManager().pluginSettings().updateRates().actionBar(),
      config -> new ActionBarDisplayTask(this.tabTPS, this, config.actionBarSettings())
    );
    this.bossBarDisplayHandler = new DisplayHandler<>(
      tabTPS,
      this,
      tabTPS.configManager().pluginSettings().updateRates().bossBar(),
      config -> new BossBarDisplayTask(this.tabTPS, this, config.bossBarSettings())
    );
  }

  @Override
  public final @NonNull UUID uuid() {
    return this.uuid;
  }

  @Override
  public final @NonNull P base() {
    return this.base;
  }

  @Override
  public @NonNull DisplayHandler<TabDisplayTask> tab() {
    return this.tabDisplayHandler;
  }

  @Override
  public @NonNull DisplayHandler<ActionBarDisplayTask> actionBar() {
    return this.actionBarDisplayHandler;
  }

  @Override
  public @NonNull DisplayHandler<BossBarDisplayTask> bossBar() {
    return this.bossBarDisplayHandler;
  }

  @Override
  public final void populate(final @NonNull User<P> deserialized) {
    this.bossBarDisplayHandler.enabled(deserialized.bossBar().enabled());
    this.actionBarDisplayHandler.enabled(deserialized.actionBar().enabled());
    this.tabDisplayHandler.enabled(deserialized.tab().enabled());
  }
}
