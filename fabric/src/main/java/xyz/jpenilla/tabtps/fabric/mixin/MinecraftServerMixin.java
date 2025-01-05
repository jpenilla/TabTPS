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
package xyz.jpenilla.tabtps.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.BooleanSupplier;
import net.minecraft.server.MinecraftServer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.jpenilla.tabtps.common.service.TickTimeService;
import xyz.jpenilla.tabtps.common.util.RollingAverage;
import xyz.jpenilla.tabtps.common.util.TPSUtil;
import xyz.jpenilla.tabtps.common.util.TickTimes;
import xyz.jpenilla.tabtps.common.util.TickingState;
import xyz.jpenilla.tabtps.fabric.access.MinecraftServerAccess;

/**
 * Adds TPS and tick time rolling averages.
 */
@Unique
@Mixin(MinecraftServer.class)
@Implements({@Interface(iface = TickTimeService.class, prefix = "tabtps$")})
abstract class MinecraftServerMixin implements MinecraftServerAccess {
  @Unique
  private final TickTimes tickTimes5s = new TickTimes(100);
  @Unique
  private final TickTimes tickTimes10s = new TickTimes(200);
  @Unique
  private final TickTimes tickTimes60s = new TickTimes(1200);

  @Unique
  private final RollingAverage tps5s = new RollingAverage(5);
  @Unique
  private final RollingAverage tps1m = new RollingAverage(60);
  @Unique
  private final RollingAverage tps5m = new RollingAverage(60 * 5);
  @Unique
  private final RollingAverage tps15m = new RollingAverage(60 * 15);

  @Unique
  private long previousTime;
  @Unique
  private TickingState tickingState = TickingState.NOT_TICKING;

  @Shadow private int tickCount;
  @Shadow @Final private long[] tickTimesNanos;

  @Inject(
    method = "tickServer",
    at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V", ordinal = 0, remap = false)
  )
  private void injectPause(final BooleanSupplier keepTicking, final CallbackInfo ci) {
    this.tickingState = TickingState.NOT_TICKING;
  }

  @Inject(method = "tickServer", at = @At(value = "RETURN", ordinal = 1))
  public void injectTick(
    final BooleanSupplier keepTicking,
    final CallbackInfo ci,
    @Local(ordinal = 0) final long tickStartTimeNanos,
    @Local(ordinal = 1) final long tickDurationNanos
  ) {
    this.tickTimes5s.add(this.tickCount, tickDurationNanos);
    this.tickTimes10s.add(this.tickCount, tickDurationNanos);
    this.tickTimes60s.add(this.tickCount, tickDurationNanos);

    if (this.tickCount % RollingAverage.SAMPLE_INTERVAL == 0) {
      if (this.tickingState == TickingState.NOT_TICKING) {
        this.tickingState = TickingState.INITIALIZING;
      } else if (this.tickingState == TickingState.INITIALIZING) {
        this.tickingState = TickingState.TICKING;
      }
      final long diff = tickStartTimeNanos - this.previousTime;
      this.previousTime = tickStartTimeNanos;
      // Start measuring on the second tick
      if (this.tickingState == TickingState.TICKING) {
        final BigDecimal currentTps = RollingAverage.TPS_BASE.divide(new BigDecimal(diff), 30, RoundingMode.HALF_UP);
        this.tps5s.add(currentTps, diff);
        this.tps1m.add(currentTps, diff);
        this.tps5m.add(currentTps, diff);
        this.tps15m.add(currentTps, diff);
      }
    }
  }

  public double tabtps$averageMspt() {
    return TPSUtil.toMilliseconds(TPSUtil.average(this.tickTimesNanos));
  }

  public double @NonNull [] tabtps$recentTps() {
    final double[] tps = new double[4];
    tps[0] = this.tps5s.average();
    tps[1] = this.tps1m.average();
    tps[2] = this.tps5m.average();
    tps[3] = this.tps15m.average();
    return tps;
  }

  @Override
  public @NonNull TickTimes tickTimes5s() {
    return this.tickTimes5s;
  }

  @Override
  public @NonNull TickTimes tickTimes10s() {
    return this.tickTimes10s;
  }

  @Override
  public @NonNull TickTimes tickTimes60s() {
    return this.tickTimes60s;
  }
}
