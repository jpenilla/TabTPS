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
package xyz.jpenilla.tabtps.fabric.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.jpenilla.tabtps.fabric.TabTPSFabric;

@Mixin(PlayerList.class)
abstract class PlayerListMixin {
  @Inject(method = "placeNewPlayer", at = @At(value = "RETURN"))
  public void injectPlaceNewPlayer(final Connection connection, final ServerPlayer serverPlayer, final CommonListenerCookie commonListenerCookie, final CallbackInfo ci) {
    TabTPSFabric.get().userService().handleJoin(serverPlayer);
  }

  @Inject(method = "remove", at = @At(value = "HEAD"))
  public void injectRemove(final ServerPlayer serverPlayer, final CallbackInfo ci) {
    TabTPSFabric.get().userService().handleQuit(serverPlayer);
  }

  @Inject(method = "respawn", at = @At(value = "RETURN"))
  public void injectRespawn(final ServerPlayer originalPlayer, final boolean bl, final Entity.RemovalReason removalReason, final CallbackInfoReturnable<ServerPlayer> cir) {
    TabTPSFabric.get().userService().replacePlayer(cir.getReturnValue());
  }
}
