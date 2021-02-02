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
package xyz.jpenilla.tabtps.fabric.command;

import cloud.commandframework.types.tuples.Pair;
import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.command.commands.TickInfoCommand;
import xyz.jpenilla.tabtps.common.util.TPSUtil;
import xyz.jpenilla.tabtps.fabric.TabTPSFabric;
import xyz.jpenilla.tabtps.fabric.access.MinecraftServerAccess;

import java.util.List;

public final class FabricTickInfoCommandFormatter implements TickInfoCommand.Formatter {
  private final TabTPSFabric tabTPSFabric;

  public FabricTickInfoCommandFormatter(final @NonNull TabTPSFabric tabTPSFabric) {
    this.tabTPSFabric = tabTPSFabric;
  }

  @Override
  public @NonNull List<Component> formatTickTimes() {
    final MinecraftServerAccess serverAccess = (MinecraftServerAccess) this.tabTPSFabric.server();
    return TPSUtil.formatTickTimes(ImmutableList.of(
      Pair.of("5s", serverAccess.tabtps$tickTimes5s().times()),
      Pair.of("10s", serverAccess.tabtps$tickTimes10s().times()),
      Pair.of("60s", serverAccess.tabtps$tickTimes60s().times())
    ));
  }
}
