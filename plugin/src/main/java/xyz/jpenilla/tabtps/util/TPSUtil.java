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

import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.jmplib.Environment;
import xyz.jpenilla.tabtps.TabTPS;

import java.text.DecimalFormat;

public class TPSUtil {
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

  public static String round(final double value) {
    final String formatted = FORMAT.format(value);
    if (formatted.startsWith(".")) {
      return String.format("0%s", formatted);
    }
    return formatted;
  }

  public static String coloredTps(final double tps) {
    final StringBuilder s = new StringBuilder();
    if (tps >= 18.5) {
      s.append("<gradient:green:dark_green>");
    } else if (tps > 15.0) {
      s.append("<gradient:gold:yellow>");
    } else {
      s.append("<gradient:red:gold>");
    }
    s.append(round(tps));
    s.append("</gradient>");
    return s.toString();
  }

  public static double toMilliseconds(final long time) {
    return time * 1.0E-6D;
  }

  public static double toMilliseconds(final double time) {
    return time * 1.0E-6D;
  }

  public static String coloredMspt(final double mspt) {
    final StringBuilder m = new StringBuilder();
    if (mspt <= 25.0) {
      m.append("<gradient:green:dark_green>");
    } else if (mspt <= 40) {
      m.append("<gradient:gold:yellow>");
    } else {
      m.append("<gradient:red:gold>");
    }
    m.append(round(mspt));
    m.append("</gradient>");
    return m.toString();
  }
}
