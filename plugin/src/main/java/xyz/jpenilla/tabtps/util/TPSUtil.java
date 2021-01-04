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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.jmplib.Environment;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.config.Theme;

import java.text.DecimalFormat;

public final class TPSUtil {
  private static final DecimalFormat FORMAT = new DecimalFormat("###.00");

  private final TabTPS tabTPS;

  public TPSUtil(final @NonNull TabTPS tabTPS) {
    this.tabTPS = tabTPS;
  }

  public double[] tps() {
    if (Environment.majorMinecraftVersion() < 16 || !Environment.paper()) {
      return this.tabTPS.nmsHandler().tps();
    }
    return Bukkit.getServer().getTPS();
  }

  public double mspt() {
    if (Environment.majorMinecraftVersion() < 16 || !Environment.paper()) {
      return this.tabTPS.nmsHandler().mspt();
    }
    return Bukkit.getServer().getAverageTickTime();
  }

  public static @NonNull String round(final double value) {
    final String formatted = FORMAT.format(value);
    if (formatted.startsWith(".")) {
      return String.format("0%s", formatted);
    }
    return formatted;
  }

  public static @NonNull Component coloredTps(final double tps, final Theme.@NonNull Colors colors) {
    final TextColor color1;
    final TextColor color2;
    if (tps >= 18.5) {
      color1 = colors.goodPerformance();
      color2 = colors.goodPerformanceSecondary();
    } else if (tps > 15.0) {
      color1 = colors.mediumPerformance();
      color2 = colors.mediumPerformanceSecondary();
    } else {
      color1 = colors.lowPerformance();
      color2 = colors.lowPerformanceSecondary();
    }
    return ComponentUtil.gradient(round(tps), color1, color2);
  }

  public static double toMilliseconds(final long time) {
    return time * 1.0E-6D;
  }

  public static double toMilliseconds(final double time) {
    return time * 1.0E-6D;
  }

  public static @NonNull Component coloredMspt(final double mspt, final Theme.@NonNull Colors colors) {
    final TextColor color1;
    final TextColor color2;
    if (mspt <= 25.0) {
      color1 = colors.goodPerformance();
      color2 = colors.goodPerformanceSecondary();
    } else if (mspt <= 40) {
      color1 = colors.mediumPerformance();
      color2 = colors.mediumPerformanceSecondary();
    } else {
      color1 = colors.lowPerformance();
      color2 = colors.lowPerformanceSecondary();
    }
    return ComponentUtil.gradient(round(mspt), color1, color2);
  }
}
