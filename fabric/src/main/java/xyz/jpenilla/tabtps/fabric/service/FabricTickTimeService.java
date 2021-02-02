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
package xyz.jpenilla.tabtps.fabric.service;

import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.service.TickTimeService;
import xyz.jpenilla.tabtps.common.util.TPSUtil;
import xyz.jpenilla.tabtps.fabric.TabTPSFabric;
import xyz.jpenilla.tabtps.fabric.access.MinecraftServerAccess;

public final class FabricTickTimeService implements TickTimeService {
  private final TabTPSFabric tabTPSFabric;

  public FabricTickTimeService(final @NonNull TabTPSFabric tabTPSFabric) {
    this.tabTPSFabric = tabTPSFabric;
  }

  @Override
  public double averageMspt() {
    return TPSUtil.toMilliseconds(TPSUtil.average(this.tabTPSFabric.server().tickTimes));
  }

  @Override
  public double @NonNull [] recentTps() {
    final MinecraftServerAccess access = (MinecraftServerAccess) this.tabTPSFabric.server();
    final double[] tps = new double[4];
    tps[0] = access.tabtps$tps5s().average();
    tps[1] = access.tabtps$tps1m().average();
    tps[2] = access.tabtps$tps5m().average();
    tps[3] = access.tabtps$tps15m().average();
    return tps;
  }
}
