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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.type.tuple.Pair;
import xyz.jpenilla.tabtps.common.command.commands.TickInfoCommand;
import xyz.jpenilla.tabtps.common.util.TPSUtil;

import static java.lang.invoke.MethodType.methodType;

public final class PaperTickInfoCommandFormatter implements TickInfoCommand.Formatter {

  private final MethodHandle _getServer;
  private final MethodHandle _tickRateManager;
  private final MethodHandle _nanosecondsPerTick;
  private final MethodHandle _generateTickReport;
  private final MethodHandle _timePerTickData;
  private final MethodHandle _rawData;

  private final Pair<String, Field>[] tickTimesFields;

  @SuppressWarnings({"unchecked", "rawtypes"})
  public PaperTickInfoCommandFormatter() {
    try {
      final Class<?> _TickData = Class.forName("ca.spottedleaf.moonrise.common.time.TickData");
      final Class<?> _TickTime = Class.forName("ca.spottedleaf.moonrise.common.time.TickTime");
      final Class<?> _TickReportData = Class.forName("ca.spottedleaf.moonrise.common.time.TickData$TickReportData");
      final Class<?> _SegmentedAverage = Class.forName("ca.spottedleaf.moonrise.common.time.TickData$SegmentedAverage");
      final Class<?> _TickRateManager = Class.forName("net.minecraft.server.ServerTickRateManager");
      final Class<?> _MinecraftServer = Class.forName("net.minecraft.server.MinecraftServer");

      final MethodHandles.Lookup lookup = MethodHandles.lookup();

      this._getServer = lookup.findStatic(_MinecraftServer, "getServer", methodType(_MinecraftServer));
      this._tickRateManager = lookup.findVirtual(_MinecraftServer, "tickRateManager", methodType(_TickRateManager));
      this._nanosecondsPerTick = lookup.findVirtual(_TickRateManager, "nanosecondsPerTick", methodType(long.class));
      this._generateTickReport = lookup.findVirtual(_TickData, "generateTickReport", methodType(_TickReportData, _TickTime, long.class, long.class));
      this._timePerTickData = lookup.findVirtual(_TickReportData, "timePerTickData", methodType(_SegmentedAverage));
      this._rawData = lookup.findVirtual(_SegmentedAverage, "rawData", methodType(long[].class));

      final List<Pair<String, Field>> tickTimesFieldsList = new ArrayList<>();
      for (final Field f : _MinecraftServer.getDeclaredFields()) {
        if (f.getType() == _TickData && f.getName().startsWith("tickTimes")) {
          f.setAccessible(true);
          tickTimesFieldsList.add(Pair.of(f.getName().substring("tickTimes".length()), f));
        }
      }
      if (tickTimesFieldsList.size() < 3) {
        throw new IllegalStateException("Expected at least 3 tickTimes fields, found " + tickTimesFieldsList.size() + ": " + tickTimesFieldsList);
      }
      this.tickTimesFields = tickTimesFieldsList.toArray(new Pair[0]);
    } catch (final ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to initialize", e);
    }
  }

  @Override
  public @NonNull List<Component> formatTickTimes() {
    try {
      final Object minecraftServer = this._getServer.invoke();
      final Object tickRateManager = this._tickRateManager.invoke(minecraftServer);
      final long nanosecondsPerTick = (long) this._nanosecondsPerTick.invoke(tickRateManager);
      final long now = System.nanoTime();

      final List<Pair<String, long[]>> formatList = new ArrayList<>();
      for (final Pair<String, Field> pair : this.tickTimesFields) {
        final Object tickData = pair.second().get(minecraftServer);
        if (tickData == null) {
          throw new IllegalStateException("TickData field " + pair.first() + " was null");
        }
        final long[] rawData = this.extractRawData(tickData, now, nanosecondsPerTick);
        formatList.add(Pair.of(pair.first(), rawData));
      }

      return TPSUtil.formatTickTimes(formatList);
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
