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
package xyz.jpenilla.tabtps.common.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.function.LongPredicate;
import java.util.stream.LongStream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.type.tuple.Pair;
import xyz.jpenilla.tabtps.common.Messages;
import xyz.jpenilla.tabtps.common.config.Theme;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static xyz.jpenilla.tabtps.common.util.Components.gradient;

public final class TPSUtil {
  private static final DecimalFormat FORMAT = new DecimalFormat("0.00");
  private static final LongPredicate NOT_ZERO = l -> l != 0;

  private TPSUtil() {
  }

  private static @NonNull String formatDouble(final double value) {
    return FORMAT.format(value);
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
    return gradient(formatDouble(tps), color1, color2);
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
    return gradient(formatDouble(mspt), color1, color2);
  }

  public static @NonNull List<Component> formatTickTimes(final @NonNull List<Pair<String, long[]>> times) {
    final Component header = text()
      .color(GRAY)
      .append(
        Messages.LABEL_MSPT,
        text(" - ", WHITE),
        Messages.LABEL_AVERAGE,
        text(", ", WHITE),
        Messages.LABEL_MINIMUM,
        text(", ", WHITE),
        Messages.LABEL_MAXIMUM
      )
      .hoverEvent(Messages.COMMAND_TICKINFO_TEXT_MSPT_HOVER.styled(GRAY))
      .build();

    final List<Component> output = new ArrayList<>();
    output.add(header);

    final Iterator<Pair<String, long[]>> iterator = times.iterator();
    while (iterator.hasNext()) {
      final Pair<String, long[]> pair = iterator.next();
      final String branch = iterator.hasNext() ? "├─" : "└─";
      output.add(formatStatistics(
        branch,
        text(pair.first()),
        pair.second()
      ));
    }
    return output;
  }

  private static @NonNull Component formatStatistics(final @NonNull String branch, final @NonNull Component time, final long @NonNull [] times) {
    final LongSummaryStatistics statistics = LongStream.of(times).filter(NOT_ZERO).summaryStatistics();
    return Components.ofChildren(
      space(),
      text(branch, WHITE),
      space(),
      time.color(GRAY),
      text(" - ", WHITE),
      TPSUtil.coloredMspt(TPSUtil.toMilliseconds(statistics.getAverage()), Theme.DEFAULT.colorScheme()),
      text(", ", WHITE),
      TPSUtil.coloredMspt(TPSUtil.toMilliseconds(statistics.getMin()), Theme.DEFAULT.colorScheme()),
      text(", ", WHITE),
      TPSUtil.coloredMspt(TPSUtil.toMilliseconds(statistics.getMax()), Theme.DEFAULT.colorScheme())
    );
  }

  public static double average(final long @NonNull [] longs) {
    long i = 0L;
    for (final long l : longs) {
      i += l;
    }
    return i / (double) longs.length;
  }
}
