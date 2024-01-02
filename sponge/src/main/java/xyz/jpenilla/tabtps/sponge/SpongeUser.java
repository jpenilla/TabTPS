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
package xyz.jpenilla.tabtps.sponge;

import java.lang.reflect.Field;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import xyz.jpenilla.tabtps.common.AbstractUser;
import xyz.jpenilla.tabtps.common.TabTPS;

@DefaultQualifier(NonNull.class)
public final class SpongeUser extends AbstractUser<ServerPlayer> {
  private SpongeUser(final TabTPS tabTPS, final ServerPlayer player) {
    super(tabTPS, player, player.uniqueId());
  }

  public static SpongeUser from(final TabTPS tabTPS, final ServerPlayer player) {
    return new SpongeUser(tabTPS, player);
  }

  @Override
  public Component displayName() {
    return this.base().displayName().get();
  }

  @Override
  public boolean hasPermission(final String permissionString) {
    return this.base().hasPermission(permissionString);
  }

  @Override
  public boolean online() {
    return this.base().isOnline();
  }

  @Override
  public int ping() {
    final Throwable err;
    try {
      return ((net.minecraft.server.level.ServerPlayer) this.base()).connection.latency();
    } catch (final LinkageError e) {
      err = e;
    } catch (final NullPointerException e) {
      return -1;
    }
    try {
      final Field latency = ServerPlayer.class.getDeclaredField("latency");
      return latency.getInt(this.base());
    } catch (final ReflectiveOperationException e) {
      err.addSuppressed(e);
    }
    throw new RuntimeException("Failed to get ping", err);
  }

  @Override
  public Audience audience() {
    return this.base();
  }
}
