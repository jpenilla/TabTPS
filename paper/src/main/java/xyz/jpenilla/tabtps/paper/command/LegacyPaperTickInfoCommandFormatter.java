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

public final class LegacyPaperTickInfoCommandFormatter implements TickInfoCommand.Formatter {
  private final Class<?> _MinecraftServer = Crafty.needNMSClassOrElse(
    "MinecraftServer",
    "net.minecraft.server.MinecraftServer"
  );
  private final Class<?> _MinecraftServer_TickTimes = Crafty.needNMSClassOrElse(
    "MinecraftServer$TickTimes",
    "net.minecraft.server.MinecraftServer$TickTimes"
  );

  private final MethodHandle _getServer = Objects.requireNonNull(Crafty.findStaticMethod(this._MinecraftServer, "getServer", this._MinecraftServer));
  private final MethodHandle _getTimes = Objects.requireNonNull(Crafty.findMethod(this._MinecraftServer_TickTimes, "getTimes", long[].class));

  private final Field _tickTimes5s;
  private final Field _tickTimes10s;
  private final Field _tickTimes60s;

  public LegacyPaperTickInfoCommandFormatter() {
    this._tickTimes5s = Crafty.needField(this._MinecraftServer, "tickTimes5s");
    this._tickTimes10s = Crafty.needField(this._MinecraftServer, "tickTimes10s");
    this._tickTimes60s = Crafty.needField(this._MinecraftServer, "tickTimes60s");
  }

  @Override
  public @NonNull List<Component> formatTickTimes() {
    try {
      final Object minecraftServer = this._getServer.invoke();
      final Object tickTimes5s = this._tickTimes5s.get(minecraftServer);
      final Object tickTimes10s = this._tickTimes10s.get(minecraftServer);
      final Object tickTimes60s = this._tickTimes60s.get(minecraftServer);

      final long[] times5s = (long[]) this._getTimes.bindTo(tickTimes5s).invoke();
      final long[] times10s = (long[]) this._getTimes.bindTo(tickTimes10s).invoke();
      final long[] times60s = (long[]) this._getTimes.bindTo(tickTimes60s).invoke();

      return TPSUtil.formatTickTimes(ImmutableList.of(
        Pair.of("5s", times5s),
        Pair.of("10s", times10s),
        Pair.of("60s", times60s)
      ));
    } catch (final Throwable throwable) {
      throw new IllegalStateException("Failed to retrieve tick time statistics", throwable);
    }
  }
}
