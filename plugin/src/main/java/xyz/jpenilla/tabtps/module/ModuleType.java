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
package xyz.jpenilla.tabtps.module;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.jpenilla.tabtps.TabTPS;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class ModuleType<T extends Module> {
  private static final Map<String, ModuleType<? extends Module>> TYPES_BY_NAME = new HashMap<>();
  private static final Map<Class<? extends Module>, ModuleType<? extends Module>> TYPES_BY_CLASS = new HashMap<>();

  public static final ModuleType<CPUModule> CPU = withoutPlayer(CPUModule.class, CPUModule::new, "cpu");
  public static final ModuleType<MemoryModule> MEMORY = withoutPlayer(MemoryModule.class, MemoryModule::new, "memory");
  public static final ModuleType<MSPTModule> MSPT = withoutPlayer(MSPTModule.class, MSPTModule::new, "mspt");
  public static final ModuleType<TPSModule> TPS = withoutPlayer(TPSModule.class, TPSModule::new, "tps");
  public static final ModuleType<PingModule> PING = withPlayer(PingModule.class, PingModule::new, "ping");

  public static Collection<ModuleType<?>> moduleTypes() {
    return Collections.unmodifiableCollection(TYPES_BY_NAME.values());
  }

  @SuppressWarnings("unchecked")
  public static <T extends Module> ModuleType<T> fromName(final @NonNull String name) {
    final ModuleType<T> type = (ModuleType<T>) TYPES_BY_NAME.get(name);
    if (type == null) {
      throw new IllegalArgumentException("Unknown or invalid module type: " + name);
    }
    return type;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Module> @NonNull ModuleType<T> fromClass(final @NonNull Class<? extends Module> moduleClass) {
    final ModuleType<T> type = (ModuleType<T>) TYPES_BY_CLASS.get(moduleClass);
    if (type == null) {
      throw new IllegalArgumentException("Unknown or invalid module class: " + moduleClass.getSimpleName());
    }
    return type;
  }

  private static <T extends Module> @NonNull ModuleType<T> withoutPlayer(
    final @NonNull Class<T> moduleClass,
    final @NonNull Function<@NonNull TabTPS, @NonNull T> createModuleFunction,
    final @NonNull String name
  ) {
    return new ModuleType<>(
      moduleClass,
      (plugin, player) -> createModuleFunction.apply(plugin),
      name,
      false
    );
  }

  private static <T extends Module> @NonNull ModuleType<T> withPlayer(
    final @NonNull Class<T> moduleClass,
    final @NonNull BiFunction<@NonNull TabTPS, @NonNull Player, @NonNull T> createModuleFunction,
    final @NonNull String name
  ) {
    return new ModuleType<>(moduleClass, createModuleFunction, name, true);
  }

  private final Class<? extends Module> moduleClass;
  private final BiFunction<@NonNull TabTPS, @Nullable Player, @NonNull T> createModuleFunction;
  private final String name;
  private final boolean needsPlayer;

  private ModuleType(
    final @NonNull Class<? extends Module> moduleClass,
    final @NonNull BiFunction<@NonNull TabTPS, @Nullable Player, @NonNull T> createModuleFunction,
    final @NonNull String name,
    final boolean needsPlayer
  ) {
    this.moduleClass = moduleClass;
    this.createModuleFunction = createModuleFunction;
    this.name = name;
    this.needsPlayer = needsPlayer;
    TYPES_BY_NAME.put(name, this);
    TYPES_BY_CLASS.put(moduleClass, this);
  }

  public @NonNull String name() {
    return this.name;
  }

  public boolean needsPlayer() {
    return this.needsPlayer;
  }

  public @NonNull Class<? extends Module> moduleClass() {
    return this.moduleClass;
  }

  public @NonNull T createModule(
    final @NonNull TabTPS tabTPS,
    final @Nullable Player player
  ) {
    if (this.needsPlayer && player == null) {
      throw new IllegalArgumentException(String.format("Module type '%s' requires a player", this.name));
    }
    return this.createModuleFunction.apply(tabTPS, player);
  }
}
