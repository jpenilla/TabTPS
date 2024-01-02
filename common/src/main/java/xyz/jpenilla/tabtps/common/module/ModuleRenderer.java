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
package xyz.jpenilla.tabtps.common.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.User;
import xyz.jpenilla.tabtps.common.config.Theme;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;

public final class ModuleRenderer {
  private final List<Module> modules;
  private final Function<Module, Component> moduleRenderFunction;
  private final Component separator;

  public static @NonNull Function<Module, Component> standardRenderFunction(final @NonNull Theme theme) {
    return module -> text()
      .append(module.label())
      .append(text(":", theme.colorScheme().textSecondary()))
      .append(space())
      .append(module.display())
      .build();
  }

  private ModuleRenderer(
    final @NonNull List<Module> modules,
    final @NonNull Function<Module, Component> moduleRenderFunction,
    final @Nullable Component separator
  ) {
    this.modules = modules;
    this.moduleRenderFunction = moduleRenderFunction;
    this.separator = separator;
  }

  public @NonNull Component render() {
    final TextComponent.Builder builder = text();
    final Iterator<Module> iterator = this.modules.iterator();
    while (iterator.hasNext()) {
      final Module module = iterator.next();
      builder.append(this.moduleRenderFunction.apply(module));
      if (iterator.hasNext()) {
        builder.append(Objects.requireNonNull(this.separator, "separator is null but there is more than one module"));
      }
    }
    return builder.build();
  }

  public int moduleCount() {
    return this.modules.size();
  }

  /**
   * Create a new ModuleRenderer builder.
   *
   * @return A new {@link Builder}
   */
  public static @NonNull Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private final List<Module> modules = new ArrayList<>();
    private Function<Module, Component> moduleRenderFunction;
    private Component separator = null;

    private Builder() {
    }

    public @NonNull Builder moduleRenderFunction(final @NonNull Function<Module, Component> function) {
      this.moduleRenderFunction = function;
      return this;
    }

    public @NonNull Builder separator(final @NonNull Component separator) {
      this.separator = separator;
      return this;
    }

    public @NonNull Builder modules(final @NonNull List<Module> modules) {
      this.modules.clear();
      this.modules.addAll(modules);
      return this;
    }

    public @NonNull Builder modules(final @NonNull Module... modules) {
      return this.modules(Arrays.asList(modules));
    }

    /**
     * Sets the list of {@link Module}s to use from a comma separated {@link String}.
     *
     * <p>If Modules which need a {@link User} are used, {@link Builder#modules(TabTPS, Theme, User, String)} should be used instead.</p>
     *
     * @param tabTPS  The TabTPS instance
     * @param modules The list of Modules to use in the builder, separated by commas.
     * @return The {@link Builder}
     */
    public @NonNull Builder modules(
      final @NonNull TabTPS tabTPS,
      final @NonNull Theme theme,
      final @NonNull String modules
    ) {
      return this.modules(tabTPS, theme, null, modules);
    }

    /**
     * Sets the list of {@link Module}s to use from a comma separated {@link String}.
     *
     * @param tabTPS  The TabTPS instance
     * @param theme   Theme to use
     * @param player  The Player to use
     * @param modules The list of Modules to use in the builder, separated by commas.
     * @return The {@link Builder}
     */
    public @NonNull Builder modules(
      final @NonNull TabTPS tabTPS,
      final @NonNull Theme theme,
      final @Nullable User<?> player,
      final @NonNull String modules
    ) {
      return this.modules(Arrays.stream(modules.replace(" ", "").split(","))
        .filter(s -> s != null && !s.isEmpty())
        .map(ModuleType::fromName)
        .filter(type -> !type.needsPlayer() || player != null)
        .map(type -> type.createModule(tabTPS, theme, player))
        .collect(Collectors.toList()));
    }

    /**
     * Build a ModuleRenderer from this Builder.
     *
     * @return The built {@link ModuleRenderer}
     * @throws IllegalArgumentException When a needed parameter has not been provided
     */
    public @NonNull ModuleRenderer build() throws IllegalArgumentException {
      if (this.separator == null && this.modules.size() > 1) {
        throw new IllegalArgumentException("separator is null but there is more than one module");
      }
      if (this.moduleRenderFunction == null) {
        throw new IllegalArgumentException("must provide a module render function");
      }
      return new ModuleRenderer(this.modules, this.moduleRenderFunction, this.separator);
    }
  }
}
