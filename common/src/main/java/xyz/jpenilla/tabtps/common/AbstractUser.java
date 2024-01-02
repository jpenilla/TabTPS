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
package xyz.jpenilla.tabtps.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import xyz.jpenilla.tabtps.common.config.PluginSettings;
import xyz.jpenilla.tabtps.common.display.DisplayHandler;
import xyz.jpenilla.tabtps.common.display.task.ActionBarDisplayTask;
import xyz.jpenilla.tabtps.common.display.task.BossBarDisplayTask;
import xyz.jpenilla.tabtps.common.display.task.TabDisplayTask;

@DefaultQualifier(NonNull.class)
public abstract class AbstractUser<P> implements User<P> {
  private final P base;
  private final UUID uuid;
  private final State state;

  protected AbstractUser(final TabTPS tabTPS, final P base, final UUID uuid) {
    this.base = base;
    this.uuid = uuid;
    this.state = new StateImpl(tabTPS, this);
  }

  @Override
  public final UUID uuid() {
    return this.uuid;
  }

  @Override
  public final P base() {
    return this.base;
  }

  @Override
  public State state() {
    return this.state;
  }

  public static final class StateImpl implements State {
    private final DisplayHandler<TabDisplayTask> tabDisplayHandler;
    private final DisplayHandler<ActionBarDisplayTask> actionBarDisplayHandler;
    private final DisplayHandler<BossBarDisplayTask> bossBarDisplayHandler;
    private transient boolean dirty = false;

    private StateImpl(
      final TabTPS tabTPS,
      final User<?> user
    ) {
      final PluginSettings.UpdateRates rates = tabTPS.configManager().pluginSettings().updateRates();
      this.tabDisplayHandler = new DisplayHandler<>(
        tabTPS,
        user,
        rates.tab(),
        config -> new TabDisplayTask(tabTPS, user, config.tabSettings())
      );
      this.actionBarDisplayHandler = new DisplayHandler<>(
        tabTPS,
        user,
        rates.actionBar(),
        config -> new ActionBarDisplayTask(tabTPS, user, config.actionBarSettings())
      );
      this.bossBarDisplayHandler = new DisplayHandler<>(
        tabTPS,
        user,
        rates.bossBar(),
        config -> new BossBarDisplayTask(tabTPS, user, config.bossBarSettings())
      );
    }

    @Override
    public void populate(final State from) {
      this.bossBarDisplayHandler.enabled(from.bossBar().enabled());
      this.actionBarDisplayHandler.enabled(from.actionBar().enabled());
      this.tabDisplayHandler.enabled(from.tab().enabled());
      if (from.shouldSave()) {
        this.markDirty();
      }
    }

    @Override
    public DisplayHandler<TabDisplayTask> tab() {
      return this.tabDisplayHandler;
    }

    @Override
    public DisplayHandler<ActionBarDisplayTask> actionBar() {
      return this.actionBarDisplayHandler;
    }

    @Override
    public DisplayHandler<BossBarDisplayTask> bossBar() {
      return this.bossBarDisplayHandler;
    }

    @Override
    public List<DisplayHandler<?>> displays() {
      return Collections.unmodifiableList(Arrays.asList(
        this.tab(), this.actionBar(), this.bossBar()
      ));
    }

    @Override
    public void markDirty() {
      this.dirty = true;
    }

    @Override
    public boolean shouldSave() {
      return this.dirty;
    }
  }
}
