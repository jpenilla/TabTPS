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

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Based on the MIT licensed Paper-Server patch "Further improve server tick loop".
 *
 * @author Daniel Ennis/Aikar
 */
public final class RollingAverage {
  public static final int TPS = 20;
  public static final int SAMPLE_INTERVAL = 20;
  public static final long SEC_IN_NANO = 1000000000;
  public static final int TICK_TIME = (int) SEC_IN_NANO / SAMPLE_INTERVAL;
  public static final BigDecimal TPS_BASE = new BigDecimal("1E9").multiply(new BigDecimal(SAMPLE_INTERVAL));

  private final int size;
  private final BigDecimal[] samples;
  private final long[] times;
  private long time;
  private BigDecimal total;
  private int index = 0;

  public RollingAverage(final int size) {
    this.size = size;
    this.samples = new BigDecimal[size];
    this.times = new long[size];
    this.time = size * SEC_IN_NANO;
    this.total = dec(TPS).multiply(dec(SEC_IN_NANO)).multiply(dec(size));
    for (int i = 0; i < size; i++) {
      this.samples[i] = dec(TPS);
      this.times[i] = SEC_IN_NANO;
    }
  }

  private static @NonNull BigDecimal dec(final long t) {
    return new BigDecimal(t);
  }

  public void add(final @NonNull BigDecimal x, final long t) {
    this.time -= this.times[this.index];
    this.total = this.total.subtract(this.samples[this.index].multiply(dec(this.times[this.index])));
    this.samples[this.index] = x;
    this.times[this.index] = t;
    this.time += t;
    this.total = this.total.add(x.multiply(dec(t)));
    if (++this.index == this.size) {
      this.index = 0;
    }
  }

  public double average() {
    return this.total.divide(dec(this.time), 30, RoundingMode.HALF_UP).doubleValue();
  }
}
