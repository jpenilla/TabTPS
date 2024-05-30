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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

public final class UserListener {
  private final TabTPSPlugin plugin;

  UserListener(final @NonNull TabTPSPlugin plugin) {
    this.plugin = plugin;
  }

  @Listener
  public void handleJoin(final ServerSideConnectionEvent.@NonNull Join event) {
    this.plugin.userService().handleJoin(event.player());
  }

  @Listener
  public void handleQuit(final ServerSideConnectionEvent.@NonNull Leave event) {
    this.plugin.userService().handleQuit(event.player());
  }

  @Listener
  public void handleRespawn(final RespawnPlayerEvent.@NonNull Post event) {
    this.plugin.userService().replacePlayer(event.entity());
  }
}
