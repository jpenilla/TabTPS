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
package xyz.jpenilla.tabtps.paper.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.checkerframework.common.reflection.qual.ForName;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Reflection utilities for accessing {@code net.minecraft.server}.
 */
public final class Crafty {
  private Crafty() {
  }

  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
  private static final String PREFIX_NMS = "net.minecraft.server";
  private static final String PREFIX_CRAFTBUKKIT = "org.bukkit.craftbukkit";
  private static final String CRAFT_SERVER = "CraftServer";
  private static final @Nullable String VERSION;

  static {
    final Class<?> serverClass = Bukkit.getServer().getClass(); // TODO: use reflection here too?
    if (!serverClass.getSimpleName().equals(CRAFT_SERVER)) {
      VERSION = null;
    } else if (serverClass.getName().equals(PREFIX_CRAFTBUKKIT + "." + CRAFT_SERVER)) {
      VERSION = ".";
    } else {
      String name = serverClass.getName();
      name = name.substring(PREFIX_CRAFTBUKKIT.length());
      name = name.substring(0, name.length() - CRAFT_SERVER.length());
      VERSION = name;
    }
  }

  public static @NonNull Class<?> needNMSClassOrElse(
    final @NonNull String nms,
    final @NonNull String... classNames
  ) throws RuntimeException {
    final Class<?> nmsClass = findNmsClass(nms);
    if (nmsClass != null) {
      return nmsClass;
    }
    for (final String name : classNames) {
      final Class<?> maybe = findClass(name);
      if (maybe != null) {
        return maybe;
      }
    }
    throw new IllegalStateException(String.format(
      "Couldn't find a class! NMS: '%s' or '%s'.",
      nms,
      Arrays.toString(classNames)
    ));
  }

  /**
   * Gets a class by its name.
   *
   * @param className a class name
   * @return a class or {@code null} if not found
   */
  @ForName
  public static @Nullable Class<?> findClass(final @NonNull String className) {
    try {
      return Class.forName(className);
    } catch (final ClassNotFoundException e) {
      return null;
    }
  }

  /**
   * Gets a {@code org.bukkit.craftbukkit} class.
   *
   * @param className a class name, without the {@code org.bukkit.craftbukkit} prefix
   * @return a class
   * @throws NullPointerException if the class was not found
   */
  @ForName
  public static @NonNull Class<?> needCraftClass(final @NonNull String className) {
    return requireNonNull(findCraftClass(className), "Could not find org.bukkit.craftbukkit class " + className);
  }

  /**
   * Gets a handle for a class method.
   *
   * @param holderClass      a class
   * @param methodName       a method name
   * @param returnClass      a method return class
   * @param parameterClasses an array of method parameter classes
   * @return a method handle or {@code null} if not found
   */
  public static @Nullable MethodHandle findMethod(final @Nullable Class<?> holderClass, final String methodName, final @Nullable Class<?> returnClass, final Class<?>... parameterClasses) {
    if (holderClass == null || returnClass == null) return null;
    for (final Class<?> parameterClass : parameterClasses) {
      if (parameterClass == null) return null;
    }

    try {
      return LOOKUP.findVirtual(holderClass, methodName, MethodType.methodType(returnClass, parameterClasses));
    } catch (final NoSuchMethodException | IllegalAccessException e) {
      return null;
    }
  }

  /**
   * Gets a handle for a class method.
   *
   * @param holderClass      a class
   * @param methodName       a method name
   * @param returnClass      a method return class
   * @param parameterClasses an array of method parameter classes
   * @return a method handle or {@code null} if not found
   */
  public static @Nullable MethodHandle findStaticMethod(final @Nullable Class<?> holderClass, final String methodName, final @Nullable Class<?> returnClass, final Class<?>... parameterClasses) {
    if (holderClass == null || returnClass == null) return null;
    for (final Class<?> parameterClass : parameterClasses) {
      if (parameterClass == null) return null;
    }

    try {
      return LOOKUP.findStatic(holderClass, methodName, MethodType.methodType(returnClass, parameterClasses));
    } catch (final NoSuchMethodException | IllegalAccessException e) {
      return null;
    }
  }

  /**
   * Gets a class field and makes it accessible.
   *
   * @param holderClass a class
   * @param fieldName   a field name
   * @return an accessible field
   */
  public static @NonNull Field needField(final @NonNull Class<?> holderClass, final @NonNull String fieldName) {
    try {
      final Field field = holderClass.getDeclaredField(fieldName);
      field.setAccessible(true);
      return field;
    } catch (final NoSuchFieldException ex) {
      throw new IllegalStateException(String.format("Unable to find field '%s' in class '%s'", fieldName, holderClass.getCanonicalName()), ex);
    }
  }

  /**
   * Gets a class field if possible and makes it accessible.
   *
   * @param holderClass a class
   * @param fieldName   a field name
   * @return an accessible field
   */
  public static @Nullable Field findField(final @Nullable Class<?> holderClass, final @NonNull String fieldName) {
    return findField(holderClass, fieldName, null);
  }

  /**
   * Gets a class field if it exists and is of the appropriate type and makes it accessible.
   *
   * @param holderClass a class
   * @param fieldName   a field name
   * @return an accessible field
   */
  public static @Nullable Field findField(final @Nullable Class<?> holderClass, final @NonNull String fieldName, final @Nullable Class<?> expectedType) {
    if (holderClass == null) return null;

    final Field field;
    try {
      field = holderClass.getDeclaredField(fieldName);
    } catch (final NoSuchFieldException ex) {
      return null;
    }

    field.setAccessible(true);
    if (expectedType != null && !expectedType.isAssignableFrom(field.getType())) {
      return null;
    }

    return field;
  }

  /**
   * Gets whether CraftBukkit is available.
   *
   * @return if CraftBukkit is available
   */
  public static boolean isCraftBukkit() {
    return VERSION != null;
  }

  /**
   * Gets a {@code org.bukkit.craftbukkit} class name.
   *
   * @param className a class name, without the {@code org.bukkit.craftbukkit} prefix
   * @return a class name or {@code null} if not found
   */
  public static @Nullable String findCraftClassName(final @NonNull String className) {
    return isCraftBukkit() ? PREFIX_CRAFTBUKKIT + VERSION + className : null;
  }

  /**
   * Gets a {@code org.bukkit.craftbukkit} class.
   *
   * @param className a class name, without the {@code org.bukkit.craftbukkit} prefix
   * @return a class or {@code null} if not found
   */
  @ForName
  public static @Nullable Class<?> findCraftClass(final @NonNull String className) {
    final String craftClassName = findCraftClassName(className);
    if (craftClassName == null) {
      return null;
    }

    return findClass(craftClassName);
  }

  /**
   * Gets a {@code net.minecraft.server} class name.
   *
   * @param className a class name, without the {@code net.minecraft.server} prefix
   * @return a class name or {@code null} if not found
   */
  public static @Nullable String findNmsClassName(final @NonNull String className) {
    return isCraftBukkit() ? PREFIX_NMS + VERSION + className : null;
  }

  /**
   * Get a {@code net.minecraft.server} class.
   *
   * @param className a class name, without the {@code net.minecraft.server} prefix
   * @return a class name or {@code null} if not found
   */
  @ForName
  public static @Nullable Class<?> findNmsClass(final @NonNull String className) {
    final String nmsClassName = findNmsClassName(className);
    if (nmsClassName == null) {
      return null;
    }

    return findClass(nmsClassName);
  }
}
