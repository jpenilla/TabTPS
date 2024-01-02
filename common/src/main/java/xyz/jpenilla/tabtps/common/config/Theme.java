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
package xyz.jpenilla.tabtps.common.config;

import java.util.Objects;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public final class Theme {
  public static final Theme DEFAULT = new Theme();

  @Comment("The color scheme to use for this theme. Named text colors or Hex colors may be used.\n"
    + "   Hex color format: \"#FFFFFF\"\n"
    + "   Named text color format: \"color_name\"\n"
    + "   For a list of named text colors, refer to the Minecraft wiki: https://minecraft.gamepedia.com/Formatting_codes#Color_codes")
  private Colors colorScheme = new Colors();

  public @NonNull Colors colorScheme() {
    return this.colorScheme;
  }

  @Override
  public boolean equals(final @Nullable Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Theme theme = (Theme) o;
    return this.colorScheme.equals(theme.colorScheme);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.colorScheme);
  }

  @ConfigSerializable
  public static final class Colors {
    private TextColor text = NamedTextColor.GRAY;
    private TextColor textSecondary = NamedTextColor.WHITE;
    private TextColor lowPerformance = NamedTextColor.RED;
    private TextColor lowPerformanceSecondary = NamedTextColor.GOLD;
    private TextColor mediumPerformance = NamedTextColor.GOLD;
    private TextColor mediumPerformanceSecondary = NamedTextColor.YELLOW;
    private TextColor goodPerformance = NamedTextColor.GREEN;
    private TextColor goodPerformanceSecondary = NamedTextColor.DARK_GREEN;

    public TextColor text() {
      return this.text;
    }

    public TextColor textSecondary() {
      return this.textSecondary;
    }

    public @NonNull TextColor lowPerformance() {
      return this.lowPerformance;
    }

    public @NonNull TextColor lowPerformanceSecondary() {
      return this.lowPerformanceSecondary;
    }

    public @NonNull TextColor mediumPerformance() {
      return this.mediumPerformance;
    }

    public @NonNull TextColor mediumPerformanceSecondary() {
      return this.mediumPerformanceSecondary;
    }

    public @NonNull TextColor goodPerformance() {
      return this.goodPerformance;
    }

    public @NonNull TextColor goodPerformanceSecondary() {
      return this.goodPerformanceSecondary;
    }
  }
}
