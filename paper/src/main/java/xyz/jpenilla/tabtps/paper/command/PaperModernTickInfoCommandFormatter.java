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

/**
 * Formatter for Paper 1.21.10+ which uses TickData instead of TickTimes.
 */
public final class PaperModernTickInfoCommandFormatter implements TickInfoCommand.Formatter {
  private final Class<?> _MinecraftServer = Crafty.needNMSClassOrElse(
    "MinecraftServer",
    "net.minecraft.server.MinecraftServer"
  );
  private final Class<?> _TickData;
  private final Class<?> _TickReportData;
  private final Class<?> _SegmentData;
  private final Class<?> _TickRateManager;

  private final MethodHandle _getServer;
  private final MethodHandle _tickRateManager;
  private final MethodHandle _nanosecondsPerTick;
  private final MethodHandle _generateTickReport;
  private final MethodHandle _timePerTickData;
  private final MethodHandle _rawData;

  private final Field _tickTimes5s;
  private final Field _tickTimes10s;
  private final Field _tickTimes1m;

  /**
   * Check if the modern TickData API is available.
   *
   * @return true if the required classes exist
   */
  public static boolean isAvailable() {
    // Check if the core TickData classes exist
    if (Crafty.findClass("ca.spottedleaf.moonrise.common.time.TickData") == null) {
      return false;
    }
    if (Crafty.findClass("ca.spottedleaf.moonrise.common.time.TickData$TickReportData") == null) {
      return false;
    }
    if (Crafty.findClass("ca.spottedleaf.moonrise.common.time.TickData$SegmentData") == null) {
      return false;
    }
    // Check if TickRateManager exists (in either location)
    if (Crafty.findClass("net.minecraft.world.tick.TickRateManager") == null && 
        Crafty.findNmsClass("TickRateManager") == null) {
      return false;
    }
    return true;
  }

  public PaperModernTickInfoCommandFormatter() {
    // Try to find all required classes
    this._TickData = Crafty.findClass("ca.spottedleaf.moonrise.common.time.TickData");
    this._TickReportData = Crafty.findClass("ca.spottedleaf.moonrise.common.time.TickData$TickReportData");
    this._SegmentData = Crafty.findClass("ca.spottedleaf.moonrise.common.time.TickData$SegmentData");
    
    // Try to find TickRateManager - might be in different packages or not exist
    Class<?> tickRateManager = Crafty.findClass("net.minecraft.world.tick.TickRateManager");
    if (tickRateManager == null) {
      tickRateManager = Crafty.findNmsClass("TickRateManager");
    }
    this._TickRateManager = tickRateManager;

    // Verify all required classes were found
    if (this._TickData == null || this._TickReportData == null || 
        this._SegmentData == null || this._TickRateManager == null) {
      throw new IllegalStateException("Failed to find required TickData classes");
    }

    // Get method handles
    this._getServer = Objects.requireNonNull(Crafty.findStaticMethod(this._MinecraftServer, "getServer", this._MinecraftServer));
    this._tickRateManager = Objects.requireNonNull(Crafty.findMethod(this._MinecraftServer, "tickRateManager", this._TickRateManager));
    this._nanosecondsPerTick = Objects.requireNonNull(Crafty.findMethod(this._TickRateManager, "nanosecondsPerTick", long.class));
    this._generateTickReport = Objects.requireNonNull(Crafty.findMethod(this._TickData, "generateTickReport", this._TickReportData, String.class, long.class, long.class));
    this._timePerTickData = Objects.requireNonNull(Crafty.findMethod(this._TickReportData, "timePerTickData", this._SegmentData));
    this._rawData = Objects.requireNonNull(Crafty.findMethod(this._SegmentData, "rawData", long[].class));

    try {
      this._tickTimes5s = Crafty.needField(this._MinecraftServer, "tickTimes5s");
      this._tickTimes10s = Crafty.needField(this._MinecraftServer, "tickTimes10s");
      this._tickTimes1m = Crafty.needField(this._MinecraftServer, "tickTimes1m");
    } catch (final NoSuchFieldException e) {
      throw new IllegalStateException("Failed to initialize formatter", e);
    }
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
