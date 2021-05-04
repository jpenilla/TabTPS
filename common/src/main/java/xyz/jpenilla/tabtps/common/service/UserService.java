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
package xyz.jpenilla.tabtps.common.service;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.TabTPSPlatform;
import xyz.jpenilla.tabtps.common.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class UserService<P, U extends User<P>> {
  private final Gson gson = new GsonBuilder()
    .setPrettyPrinting()
    .create();
  protected final TabTPSPlatform<P, U> platform;
  private final Class<U> userClass;
  private final Path userDataDirectory;
  private final Map<UUID, U> userMap = new ConcurrentHashMap<>();

  protected UserService(
    final @NonNull TabTPSPlatform<P, U> platform,
    final @NonNull Class<U> userClass
  ) {
    this.platform = platform;
    this.userClass = userClass;
    this.userDataDirectory = platform.dataDirectory().resolve("userdata");
  }

  protected abstract @NonNull UUID uuid(final @NonNull P base);

  protected abstract @NonNull U create(final @NonNull P base);

  private @NonNull Path userFile(final @NonNull UUID uniqueId) {
    return this.userDataDirectory.resolve(uniqueId + ".json");
  }

  private @NonNull U loadUser(final @NonNull P base) {
    final UUID uniqueId = this.uuid(base);
    final Path file = this.userFile(uniqueId);
    final U user = this.create(base);
    if (Files.exists(file)) {
      try (final BufferedReader reader = Files.newBufferedReader(file)) {
        final U deserialized = this.gson.fromJson(reader, this.userClass);
        user.populate(deserialized);
      } catch (final IOException e) {
        this.platform.logger().warn("Failed to load data for user with UUID: " + uniqueId, e);
      }
    }
    this.userMap.put(uniqueId, user);
    return user;
  }

  private void saveUser(final @NonNull UUID uuid, final @NonNull U user) {
    final Path file = this.userFile(uuid);
    if (!Files.exists(file)) {
      this.createEmptyFile(file);
    }
    try (final BufferedWriter writer = Files.newBufferedWriter(file)) {
      this.gson.toJson(user, writer);
    } catch (final IOException e) {
      this.platform.logger().warn("Failed to save data for user with UUID: " + uuid, e);
    }
  }

  public @NonNull U user(final @NonNull P base) {
    final U user = this.userMap.get(this.uuid(base));
    if (user != null) {
      return user;
    }
    return this.loadUser(base);
  }

  public @NonNull U user(final @NonNull UUID uniqueId) {
    final U user = this.userMap.get(uniqueId);
    if (user == null) {
      throw new IllegalStateException("No user loaded for UUID: " + uniqueId);
    }
    return user;
  }

  public @NonNull Map<UUID, U> userStorage() {
    return Collections.unmodifiableMap(this.userMap);
  }

  public @NonNull Collection<U> onlineUsers() {
    return Collections.unmodifiableCollection(this.userMap.values());
  }

  protected abstract @NonNull Collection<P> platformPlayers();

  public int onlinePlayers() {
    return this.userMap.size();
  }

  public void reload() {
    this.flush();
    this.platformPlayers().stream().map(this::user).forEach(user -> {
      if (user.tab().enabled()) {
        user.tab().startDisplay();
      }
      if (user.actionBar().enabled()) {
        user.actionBar().startDisplay();
      }
      if (user.bossBar().enabled()) {
        user.bossBar().startDisplay();
      }
    });
  }

  public void flush() {
    final Set<UUID> users = ImmutableSet.copyOf(this.userMap.keySet());
    users.forEach(this::removeUser);
  }

  public void removeUser(final @NonNull UUID uniqueId) {
    final U user = this.userMap.remove(uniqueId);
    if (user == null) {
      throw new IllegalStateException("Cannot remove non-existing user " + uniqueId);
    }
    user.tab().stopDisplay();
    user.actionBar().stopDisplay();
    user.bossBar().stopDisplay();
    this.saveUser(uniqueId, user);
  }

  private void createEmptyFile(final @NonNull Path file) {
    try {
      Files.createDirectories(this.userDataDirectory);
      Files.createFile(file);
    } catch (final IOException e) {
      this.platform.logger().warn("Failed to create empty file: " + file, e);
    }
  }

  public void handleJoin(final @NonNull P platformPlayer) {
    final U user = this.user(platformPlayer);

    this.platform.tabTPS().findDisplayConfig(user).ifPresent(config -> {
      if (config.actionBarSettings().allow()) {
        if (config.actionBarSettings().enableOnLogin()) {
          user.actionBar().enabled(true);
        }
        if (user.actionBar().enabled()) {
          user.actionBar().startDisplay();
        }
      }

      if (config.bossBarSettings().allow()) {
        if (config.bossBarSettings().enableOnLogin()) {
          user.bossBar().enabled(true);
        }
        if (user.bossBar().enabled()) {
          user.bossBar().startDisplay();
        }
      }

      if (config.tabSettings().allow()) {
        if (config.tabSettings().enableOnLogin()) {
          user.tab().enabled(true);
        }
        if (user.tab().enabled()) {
          user.tab().startDisplay();
        }
      }
    });
  }

  public void handleQuit(final @NonNull P platformPlayer) {
    this.removeUser(this.uuid(platformPlayer));
  }
}
