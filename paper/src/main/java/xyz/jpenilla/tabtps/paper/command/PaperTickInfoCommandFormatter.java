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
package xyz.jpenilla.tabtps.paper.command;

import com.google.common.collect.ImmutableList;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.type.tuple.Pair;
import xyz.jpenilla.tabtps.common.command.commands.TickInfoCommand;
import xyz.jpenilla.tabtps.common.util.TPSUtil;
import xyz.jpenilla.tabtps.paper.util.Crafty;

public final class PaperTickInfoCommandFormatter implements TickInfoCommand.Formatter {

  private final MethodHandle _getServer;
  private final MethodHandle _tickRateManager;
  private final MethodHandle _nanosecondsPerTick;
  private final MethodHandle _generateTickReport;
  private final MethodHandle _timePerTickData;
  private final MethodHandle _rawData;

  private final Field _tickTimes5s;
  private final Field _tickTimes10s;
  private final Field _tickTimes1m;

  public PaperTickInfoCommandFormatter() {
    final Class<?> _TickData = Crafty.needClass("ca.spottedleaf.moonrise.common.time.TickData");
    final Class<?> _TickTime = Crafty.needClass("ca.spottedleaf.moonrise.common.time.TickTime");
    final Class<?> _TickReportData = Crafty.needClass("ca.spottedleaf.moonrise.common.time.TickData$TickReportData");
    final Class<?> _SegmentedAverage = Crafty.needClass("ca.spottedleaf.moonrise.common.time.TickData$SegmentedAverage");
    final Class<?> _TickRateManager = Crafty.needClass("net.minecraft.server.ServerTickRateManager");
    final Class<?> _MinecraftServer = Crafty.needClass("net.minecraft.server.MinecraftServer");

    this._getServer = Objects.requireNonNull(Crafty.findStaticMethod(_MinecraftServer, "getServer", _MinecraftServer));
    this._tickRateManager = Objects.requireNonNull(Crafty.findMethod(_MinecraftServer, "tickRateManager", _TickRateManager));
    this._nanosecondsPerTick = Objects.requireNonNull(Crafty.findMethod(_TickRateManager, "nanosecondsPerTick", long.class));
    this._generateTickReport = Objects.requireNonNull(Crafty.findMethod(_TickData, "generateTickReport", _TickReportData, _TickTime, long.class, long.class));
    this._timePerTickData = Objects.requireNonNull(Crafty.findMethod(_TickReportData, "timePerTickData", _SegmentedAverage));
    this._rawData = Objects.requireNonNull(Crafty.findMethod(_SegmentedAverage, "rawData", long[].class));

    this._tickTimes5s = Crafty.needField(_MinecraftServer, "tickTimes5s");
    this._tickTimes10s = Crafty.needField(_MinecraftServer, "tickTimes10s");
    this._tickTimes1m = Crafty.needField(_MinecraftServer, "tickTimes1m");
  }

  @Override
  public @NonNull List<Component> formatTickTimes() {
    try {
      final Object minecraftServer = this._getServer.invoke();
      final Object tickRateManager = this._tickRateManager.invoke(minecraftServer);
      final long nanosecondsPerTick = (long) this._nanosecondsPerTick.invoke(tickRateManager);
      final long now = System.nanoTime();

      final Object tickData5s = this._tickTimes5s.get(minecraftServer);
      final Object tickData10s = this._tickTimes10s.get(minecraftServer);
      final Object tickData1m = this._tickTimes1m.get(minecraftServer);

      final long[] times5s = extractRawData(tickData5s, now, nanosecondsPerTick);
      final long[] times10s = extractRawData(tickData10s, now, nanosecondsPerTick);
      final long[] times1m = extractRawData(tickData1m, now, nanosecondsPerTick);

      return TPSUtil.formatTickTimes(ImmutableList.of(
        Pair.of("5s", times5s),
        Pair.of("10s", times10s),
        Pair.of("1m", times1m)
      ));
    } catch (final Throwable throwable) {
      throw new IllegalStateException("Failed to retrieve tick time statistics", throwable);
    }
  }

  private long[] extractRawData(final Object tickData, final long now, final long nanosecondsPerTick) throws Throwable {
    final Object reportData = this._generateTickReport.invoke(tickData, null, now, nanosecondsPerTick);
    if (reportData == null) {
      return new long[0];
    }
    final Object timePerTickData = this._timePerTickData.invoke(reportData);
    return (long[]) this._rawData.invoke(timePerTickData);
  }
}
