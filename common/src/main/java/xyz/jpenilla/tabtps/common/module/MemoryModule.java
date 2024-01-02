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
package xyz.jpenilla.tabtps.common.module;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.Messages;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.config.Theme;
import xyz.jpenilla.tabtps.common.util.Components;
import xyz.jpenilla.tabtps.common.util.MemoryUtil;

public final class MemoryModule extends AbstractModule {
  private final boolean alwaysShowMax;

  public MemoryModule(
    final @NonNull TabTPS tabTPS,
    final @NonNull Theme theme,
    final boolean alwaysShowMax
  ) {
    super(tabTPS, theme);
    this.alwaysShowMax = alwaysShowMax;
  }

  public MemoryModule(
    final @NonNull TabTPS tabTPS,
    final @NonNull Theme theme
  ) {
    this(tabTPS, theme, false);
  }

  @Override
  public @NonNull Component label() {
    return Messages.LABEL_MEMORY.styled(this.theme.colorScheme().text());
  }

  @Override
  public @NonNull Component display() {
    final TextColor color1 = this.theme.colorScheme().goodPerformance();
    final TextColor color2 = this.theme.colorScheme().goodPerformanceSecondary();
    final TextComponent.Builder builder = Component.text()
      .append(Components.gradient(String.valueOf(MemoryUtil.usedMemory()), color1, color2))
      .append(Component.text("M", this.theme.colorScheme().text()))
      .append(Component.text("/", this.theme.colorScheme().textSecondary()))
      .append(Components.gradient(String.valueOf(MemoryUtil.committedMemory()), color1, color2))
      .append(Component.text("M", this.theme.colorScheme().text()));
    if (this.alwaysShowMax || MemoryUtil.committedMemory() != MemoryUtil.maxMemory()) {
      builder.append(Component.space())
        .append(Component.text("(", this.theme.colorScheme().textSecondary()))
        .append(Messages.LABEL_MAXIMUM_SHORT_LOWER.styled(this.theme.colorScheme().text()))
        .append(Component.space())
        .append(Components.gradient(String.valueOf(MemoryUtil.maxMemory()), color1, color2))
        .append(Component.text("M", this.theme.colorScheme().text()))
        .append(Component.text(")", this.theme.colorScheme().textSecondary()));
    }
    return builder.build();
  }
}
