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
package xyz.jpenilla.tabtps.sponge;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import xyz.jpenilla.tabtps.common.AbstractUser;
import xyz.jpenilla.tabtps.common.TabTPS;

public final class SpongeUser extends AbstractUser<ServerPlayer> {
  private SpongeUser(final @NonNull TabTPS tabTPS, final @NonNull ServerPlayer player) {
    super(tabTPS, player, player.uniqueId());
  }

  public static @NonNull SpongeUser from(final @NonNull TabTPS tabTPS, final @NonNull ServerPlayer player) {
    return new SpongeUser(tabTPS, player);
  }

  @Override
  public @NonNull Component displayName() {
    return this.base.displayName().get();
  }

  @Override
  public boolean hasPermission(final @NonNull String permissionString) {
    return this.base.hasPermission(permissionString);
  }

  @Override
  public boolean online() {
    return this.base.isOnline();
  }

  @Override
  public int ping() {
    return ((net.minecraft.server.level.ServerPlayer) this.base).latency;
  }

  @Override
  public @NonNull Audience audience() {
    return this.base;
  }
}
