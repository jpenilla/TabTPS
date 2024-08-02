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
package xyz.jpenilla.tabtps.neoforge.service;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import xyz.jpenilla.tabtps.common.service.UserService;
import xyz.jpenilla.tabtps.neoforge.NeoForgeUser;
import xyz.jpenilla.tabtps.neoforge.TabTPSNeoForge;

@DefaultQualifier(NonNull.class)
public final class NeoForgeUserService extends UserService<ServerPlayer, NeoForgeUser> {
  private final TabTPSNeoForge tabTPSNeoForge;

  public NeoForgeUserService(final TabTPSNeoForge platform) {
    super(platform);
    this.tabTPSNeoForge = platform;
  }

  @Override
  protected UUID uuid(final ServerPlayer base) {
    return base.getUUID();
  }

  @Override
  protected NeoForgeUser create(final ServerPlayer base) {
    return NeoForgeUser.from(this.tabTPSNeoForge, base);
  }

  @Override
  protected Collection<ServerPlayer> platformPlayers() {
    return Collections.unmodifiableCollection(this.tabTPSNeoForge.server().getPlayerList().getPlayers());
  }
}
