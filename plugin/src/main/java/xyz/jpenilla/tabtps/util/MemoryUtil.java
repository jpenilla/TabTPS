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
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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

  public static @NonNull Component renderBar(final @Nullable String name, final @NonNull MemoryUsage usage, final int barLength) {
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

    final TextComponent.Builder hover = Component.text()
      .append(humanReadableByteCountBin(used))
      .append(Component.space())
      .append(Component.translatable("tabtps.label.used", NamedTextColor.WHITE))
      .append(Component.text("/", NamedTextColor.GRAY))
      .append(humanReadableByteCountBin(committed))
      .append(Component.space())
      .append(Component.translatable("tabtps.label.allocated", NamedTextColor.WHITE))
      .append(Component.newline());
    if (max != -1) {
      hover.append(humanReadableByteCountBin(adjustedMax))
        .append(Component.space())
        .append(Component.translatable("tabtps.label.maximum", NamedTextColor.WHITE))
        .append(Component.text(",", NamedTextColor.GRAY))
        .append(Component.space());
    }
    hover.append(humanReadableByteCountBin(adjustedInit))
      .append(Component.space())
      .append(Component.translatable("tabtps.label.initial_amount"));
    builder.hoverEvent(HoverEvent.showText(hover));

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

  public static @NonNull Component humanReadableByteCountBin(final long bytes) {
    final long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
    if (absB < 1024) {
      return LinearComponents.linear(
        ComponentUtil.gradient(String.valueOf(bytes), NamedTextColor.BLUE, NamedTextColor.AQUA),
        Component.text("B", NamedTextColor.GRAY)
      );
    }
    long value = absB;
    final CharacterIterator ci = new StringCharacterIterator("KMGTPE");
    for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
      value >>= 10;
      ci.next();
    }
    value *= Long.signum(bytes);
    return LinearComponents.linear(
      ComponentUtil.gradient(String.format("%.1f", value / 1024.0), NamedTextColor.BLUE, NamedTextColor.AQUA),
      Component.text(String.format("%ciB", ci.current()), NamedTextColor.GRAY)
    );
  }
}
