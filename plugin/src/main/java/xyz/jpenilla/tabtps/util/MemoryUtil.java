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
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.jpenilla.tabtps.Constants;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.stream.IntStream;

public final class MemoryUtil {
  private MemoryUtil() {
  }

  public static int usedMemory() {
    return Math.round(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / 1048576f);
  }

  public static int committedMemory() {
    return Math.round(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted() / 1048576f);
  }

  public static int maxMemory() {
    return Math.round(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() / 1048576f);
  }

  public static Component renderBar(final @Nullable String name, final @NonNull MemoryUsage usage, final int barLength) {
    final long used = usage.getUsed();
    final long committed = usage.getCommitted();
    final long max = usage.getMax();
    final long init = usage.getInit();
    final long adjustedMax = max == -1 ? committed : max;
    final long adjustedInit = init == -1 ? 0 : init;

    final float usedPercent = (float) used / adjustedMax;
    final float committedPercent = (float) committed / adjustedMax;
    final float initPercent = (float) adjustedInit / adjustedMax;
    final int usedLength = Math.round(barLength * usedPercent);
    final int committedLength = Math.round(barLength * (committedPercent - usedPercent));
    final int unallocatedLength = barLength - usedLength - committedLength;
    final int initPointer = Math.min(barLength, Math.max(1, Math.round(barLength * initPercent)));
    final Gradient usedGradient = new Gradient(NamedTextColor.GREEN, NamedTextColor.DARK_GREEN);
    final Gradient committedGradient = new Gradient(NamedTextColor.AQUA, NamedTextColor.BLUE);
    final Gradient unallocatedGradient = new Gradient(NamedTextColor.GRAY, NamedTextColor.DARK_GRAY);
    usedGradient.length(usedLength);
    committedGradient.length(committedLength);
    unallocatedGradient.length(unallocatedLength);

    final TextComponent.Builder builder = Component.text();

    final StringBuilder hover = new StringBuilder();
    hover.append(humanReadableByteCountBin(used)).append(" <white>Used</white>/").append(humanReadableByteCountBin(committed)).append(" <white>Committed</white>\n");
    if (max != -1) {
      hover.append(humanReadableByteCountBin(adjustedMax)).append(" <white>Max</white><gray>,</gray> ");
    }
    hover.append(humanReadableByteCountBin(adjustedInit)).append(" <white>Init</white>");
    builder.hoverEvent(HoverEvent.showText(Constants.MINIMESSAGE.parse(hover.toString())));

    builder.append(Component.text("[", NamedTextColor.GRAY));
    IntStream.rangeClosed(1, barLength).forEach(i -> {
      if (i == initPointer) {
        builder.append(Component.text("|", TextColor.color(0xFF48A8)));
      } else if (i <= usedLength) {
        builder.append(Component.text("|", usedGradient.nextColor()));
      } else if (i <= usedLength + committedLength) {
        builder.append(Component.text("|", committedGradient.nextColor()));
      } else {
        builder.append(Component.text("|", unallocatedGradient.nextColor()));
      }
    });
    builder.append(Component.text("]", NamedTextColor.GRAY));
    if (name != null && !name.isEmpty()) {
      builder.append(
        Component.space(),
        Component.text(name, NamedTextColor.WHITE, TextDecoration.ITALIC)
      );
    }

    return builder.build();
  }

  public static String humanReadableByteCountBin(final long bytes) {
    final long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
    if (absB < 1024) {
      return "<gradient:blue:aqua>" + bytes + "</gradient></gray>B</gray>";
    }
    long value = absB;
    final CharacterIterator ci = new StringCharacterIterator("KMGTPE");
    for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
      value >>= 10;
      ci.next();
    }
    value *= Long.signum(bytes);
    return String.format("<gradient:blue:aqua>%.1f</gradient><gray>%ciB<gray>", value / 1024.0, ci.current());
  }
}
