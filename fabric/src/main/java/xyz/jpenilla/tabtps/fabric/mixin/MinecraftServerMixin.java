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
package xyz.jpenilla.tabtps.fabric.mixin;

import net.minecraft.server.MinecraftServer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.jpenilla.tabtps.fabric.access.MinecraftServerAccess;
import xyz.jpenilla.tabtps.fabric.util.RollingAverage;
import xyz.jpenilla.tabtps.fabric.util.TickTimes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.BooleanSupplier;

/**
 * Adds TPS and tick time rolling averages, based on MIT-licensed code from the PaperMC project.
 */
@Unique
@Mixin(MinecraftServer.class)
@Implements({@Interface(iface = MinecraftServerAccess.class, prefix = "tabtps$")})
abstract class MinecraftServerMixin {
  private final TickTimes tickTimes5s = new TickTimes(100);
  private final TickTimes tickTimes10s = new TickTimes(200);
  private final TickTimes tickTimes60s = new TickTimes(1200);

  private final RollingAverage tps5s = new RollingAverage(5);
  private final RollingAverage tps1m = new RollingAverage(60);
  private final RollingAverage tps5m = new RollingAverage(60 * 5);
  private final RollingAverage tps15m = new RollingAverage(60 * 15);

  @Shadow private int tickCount;
  private long previousTime;

  @Inject(method = "tickServer", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
  public void injectTick(final BooleanSupplier var1, final CallbackInfo ci, final long tickStartTimeNanos, final long tickDurationNanos) {
    this.tickTimes5s.add(this.tickCount, tickDurationNanos);
    this.tickTimes10s.add(this.tickCount, tickDurationNanos);
    this.tickTimes60s.add(this.tickCount, tickDurationNanos);

    if (this.tickCount % RollingAverage.SAMPLE_INTERVAL == 0) {
      if (this.previousTime == 0) {
        this.previousTime = tickStartTimeNanos - RollingAverage.TICK_TIME;
      }
      final long diff = tickStartTimeNanos - this.previousTime;
      this.previousTime = tickStartTimeNanos;
      final BigDecimal currentTps = RollingAverage.TPS_BASE.divide(new BigDecimal(diff), 30, RoundingMode.HALF_UP);
      this.tps5s.add(currentTps, diff);
      this.tps1m.add(currentTps, diff);
      this.tps5m.add(currentTps, diff);
      this.tps15m.add(currentTps, diff);
    }
  }

  public @NonNull TickTimes tabtps$tickTimes5s() {
    return this.tickTimes5s;
  }

  public @NonNull TickTimes tabtps$tickTimes10s() {
    return this.tickTimes10s;
  }

  public @NonNull TickTimes tabtps$tickTimes60s() {
    return this.tickTimes60s;
  }

  public @NonNull RollingAverage tabtps$tps5s() {
    return this.tps5s;
  }

  public @NonNull RollingAverage tabtps$tps1m() {
    return this.tps1m;
  }

  public @NonNull RollingAverage tabtps$tps5m() {
    return this.tps5m;
  }

  public @NonNull RollingAverage tabtps$tps15m() {
    return this.tps15m;
  }
}
