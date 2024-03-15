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
package xyz.jpenilla.tabtps.common.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public final class PluginSettings {

  @Comment("Should the plugin check GitHub for updates on startup?")
  private boolean updateChecker = true;

  @Comment("How many milliseconds in between updates")
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

  @Comment("Colors used in the command help menus")
  private HelpColors helpColors = new HelpColors();

  public @NonNull HelpColors helpColors() {
    return this.helpColors;
  }

  public @NonNull Set<String> ignoredMemoryPools() {
    return this.ignoredMemoryPools;
  }

  public @NonNull UpdateRates updateRates() {
    return this.updateRates;
  }

  public @NonNull Set<String> permissionPriorities() {
    return this.permissionPriorities;
  }

  public boolean updateChecker() {
    return this.updateChecker;
  }

  @ConfigSerializable
  public static final class UpdateRates {
    private int tab = 250;
    private int actionBar = 250;
    private int bossBar = 250;

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

  @ConfigSerializable
  public static final class HelpColors {
    private TextColor primary = TextColor.color(0x00a3ff);
    private TextColor highlight = NamedTextColor.WHITE;
    private TextColor alternateHighlight = TextColor.color(0x284fff);
    private TextColor text = NamedTextColor.GRAY;
    private TextColor accent = NamedTextColor.DARK_GRAY;

    public MinecraftHelp.@NonNull HelpColors toCloud() {
      return MinecraftHelp.helpColors(this.primary, this.highlight, this.alternateHighlight, this.text, this.accent);
    }
  }
}
