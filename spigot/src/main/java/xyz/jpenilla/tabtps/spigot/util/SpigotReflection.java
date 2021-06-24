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
package xyz.jpenilla.tabtps.spigot.util;

import io.papermc.lib.PaperLib;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.jpenilla.jmplib.Crafty;
import xyz.jpenilla.tabtps.common.util.TPSUtil;

import static xyz.jpenilla.jmplib.Crafty.findField;
import static xyz.jpenilla.jmplib.Crafty.needCraftClass;
import static xyz.jpenilla.jmplib.Crafty.needNMSClassOrElse;

public final class SpigotReflection {
  private static final class Holder {
    static final SpigotReflection INSTANCE = new SpigotReflection();
  }

  public static @NonNull SpigotReflection get() {
    return Holder.INSTANCE;
  }

  private static final Class<?> MinecraftServer_class = needNMSClassOrElse(
    "MinecraftServer",
    "net.minecraft.server.MinecraftServer"
  );
  private static final Class<?> CraftPlayer_class = needCraftClass("entity.CraftPlayer");
  private static final Class<?> EntityPlayer_class = needNMSClassOrElse(
    "EntityPlayer",
    "net.minecraft.server.level.EntityPlayer",
    "net.minecraft.server.level.ServerPlayer"
  );

  private static final MethodHandle CraftPlayer_getHandle_method = needMethod(CraftPlayer_class, "getHandle", EntityPlayer_class);
  private static final MethodHandle MinecraftServer_getServer_method = needStaticMethod(MinecraftServer_class, "getServer", MinecraftServer_class);

  private static final @Nullable Field EntityPlayer_ping_field = findField(EntityPlayer_class, "ping");
  private static final Field MinecraftServer_recentTps_field = needField(MinecraftServer_class, "recentTps"); // Spigot added field

  private final Field MinecraftServer_recentTickTimes_field = tickTimesField();

  private static @NonNull Field tickTimesField() {
    final String recentTimes;
    final int ver = PaperLib.getMinecraftVersion();
    if (ver < 13) {
      recentTimes = "h";
    } else if (ver == 14 || ver == 15) {
      recentTimes = "f";
    } else if (ver == 16) {
      recentTimes = "h";
    } else { // else if (ver >= 17) {
      recentTimes = "n";
    }
    return needField(MinecraftServer_class, recentTimes);
  }

  public int ping(final @NonNull Player player) {
    if (EntityPlayer_ping_field == null) {
      throw new IllegalStateException("The ping Field is null!");
    }
    final Object nmsPlayer = invokeOrThrow(CraftPlayer_getHandle_method, player);
    try {
      return EntityPlayer_ping_field.getInt(nmsPlayer);
    } catch (final IllegalAccessException e) {
      throw new IllegalStateException(String.format("Failed to get ping for player: '%s'", player.getName()), e);
    }
  }

  public double averageTickTime() {
    final Object server = invokeOrThrow(MinecraftServer_getServer_method);
    try {
      final long[] recentMspt = (long[]) this.MinecraftServer_recentTickTimes_field.get(server);
      return TPSUtil.toMilliseconds(TPSUtil.average(recentMspt));
    } catch (final IllegalAccessException e) {
      throw new IllegalStateException("Failed to get server mspt", e);
    }
  }

  public double @NonNull [] recentTps() {
    final Object server = invokeOrThrow(MinecraftServer_getServer_method);
    try {
      return (double[]) MinecraftServer_recentTps_field.get(server);
    } catch (final IllegalAccessException e) {
      throw new IllegalStateException("Failed to get server TPS", e);
    }
  }

  private static @NonNull MethodHandle needMethod(final @NonNull Class<?> holderClass, final @NonNull String methodName, final @NonNull Class<?> returnClass, final @NonNull Class<?> @NonNull ... parameterClasses) {
    return Objects.requireNonNull(
      Crafty.findMethod(holderClass, methodName, returnClass, parameterClasses),
      String.format(
        "Could not locate method '%s' in class '%s'",
        methodName,
        holderClass.getCanonicalName()
      )
    );
  }

  private static @NonNull MethodHandle needStaticMethod(final @NonNull Class<?> holderClass, final @NonNull String methodName, final @NonNull Class<?> returnClass, final @NonNull Class<?> @NonNull ... parameterClasses) {
    return Objects.requireNonNull(
      Crafty.findStaticMethod(holderClass, methodName, returnClass, parameterClasses),
      String.format(
        "Could not locate static method '%s' in class '%s'",
        methodName,
        holderClass.getCanonicalName()
      )
    );
  }

  public static @NonNull Field needField(final @NonNull Class<?> holderClass, final @NonNull String fieldName) {
    final Field field;
    try {
      field = holderClass.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field;
    } catch (final NoSuchFieldException e) {
      throw new IllegalStateException(String.format("Unable to find field '%s' in class '%s'", fieldName, holderClass.getCanonicalName()), e);
    }
  }

  private static @Nullable Object invokeOrThrow(final @NonNull MethodHandle methodHandle, final @Nullable Object @NonNull ... params) {
    try {
      if (params.length == 0) {
        return methodHandle.invoke();
      }
      return methodHandle.invokeWithArguments(params);
    } catch (final Throwable throwable) {
      throw new IllegalStateException(String.format("Unable to invoke method with args '%s'", Arrays.toString(params)), throwable);
    }
  }
}
