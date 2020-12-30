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
package xyz.jpenilla.tabtps.config;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@ConfigSerializable
public final class PluginSettings {

  @Comment("How many ticks in between updates")
  private UpdateRates updateRates = new UpdateRates();

  @Comment("These memory pools will not be displayed in the '/memory' command")
  private final Set<String> ignoredMemoryPools = new HashSet<>(Arrays.asList(
    "Metaspace",
    "Code Cache",
    "Compressed Class Space",
    "Non-Heap Memory Usage",
    "CodeHeap 'non-nmethods'",
    "CodeHeap 'non-profiled nmethods'",
    "CodeHeap 'profiled nmethods'"
  ));

  @Comment("A player may only have a single display config active at once, even if they have permissions for multiple\n"
    + "This list allows defining the order in which permissions will be checked")
  private final Set<String> permissionPriorities = new LinkedHashSet<>();

  public @NonNull Set<String> ignoredMemoryPools() {
    return this.ignoredMemoryPools;
  }

  public @NonNull UpdateRates updateRates() {
    return this.updateRates;
  }

  public @NonNull Set<String> permissionPriorities() {
    return this.permissionPriorities;
  }

  @ConfigSerializable
  public static final class UpdateRates {
    private int tab = 5;
    private int actionBar = 5;
    private int bossBar = 5;

    public int tab() {
      return this.tab;
    }

    public int actionBar() {
      return this.actionBar;
    }

    public int bossBar() {
      return this.bossBar;
    }
  }
}
