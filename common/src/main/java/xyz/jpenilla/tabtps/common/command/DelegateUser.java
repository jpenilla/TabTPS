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
package xyz.jpenilla.tabtps.common.command;

import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import xyz.jpenilla.tabtps.common.User;

public final class DelegateUser<U, C> implements User<U> {
  private final User<U> user;
  private final C c;

  public DelegateUser(
    final User<U> user,
    final C c
  ) {
    this.user = user;
    this.c = c;
  }

  public C c() {
    return this.c;
  }

  public User<U> wrapped() {
    return this.user;
  }

  @Override
  public @NonNull UUID uuid() {
    return this.user.uuid();
  }

  @Override
  public @NonNull Component displayName() {
    return this.user.displayName();
  }

  @Override
  public boolean online() {
    return this.user.online();
  }

  @Override
  public int ping() {
    return this.user.ping();
  }

  @Override
  public @NonNull U base() {
    return this.user.base();
  }

  @Override
  public @NonNull State state() {
    return this.user.state();
  }

  @Override
  public boolean hasPermission(final @NonNull String permissionString) {
    return this.user.hasPermission(permissionString);
  }

  @Override
  public @NotNull Audience audience() {
    return this.user;
  }
}
