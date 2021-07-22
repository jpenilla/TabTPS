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

import cloud.commandframework.types.tuples.Pair;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.TabTPSPlatform;
import xyz.jpenilla.tabtps.common.User;
import xyz.jpenilla.tabtps.common.display.DisplayHandler;

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
      } catch (final Exception ex) {
        this.platform.logger().warn("Failed to load data for user with UUID: " + uniqueId, ex);
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

  /**
   * Replace the {@link U user} instance for a player when it has become invalidated,
   * usually due to respawning.
   *
   * @param newPlayer the new backing {@link P player} instance
   */
  public void replacePlayer(final @NonNull P newPlayer) {
    final UUID uuid = this.uuid(newPlayer);
    final U oldUser = this.userMap.get(uuid);
    if (oldUser == null) {
      throw new IllegalArgumentException("Cannot replace a player who is not logged in!");
    }
    this.shutdownDisplays(oldUser);
    final U newUser = this.create(newPlayer);
    newUser.populate(oldUser);
    this.startEnabledDisplays(newUser);
    this.userMap.put(uuid, newUser);
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
    this.platformPlayers().stream()
      .map(this::user)
      .forEach(this::startEnabledDisplays);
  }

  public void flush() {
    final Set<UUID> users = ImmutableSet.copyOf(this.userMap.keySet());
    users.forEach(this::removeUser);
  }

  public void removeUser(final @NonNull UUID uniqueId) {
    final U removed = this.userMap.remove(uniqueId);
    if (removed == null) {
      throw new IllegalStateException("Cannot remove non-existing user " + uniqueId);
    }
    this.shutdownDisplays(removed);
    this.saveUser(uniqueId, removed);
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
      Stream.of(
        Pair.of(config.actionBarSettings(), user.actionBar()),
        Pair.of(config.bossBarSettings(), user.bossBar()),
        Pair.of(config.tabSettings(), user.tab())
      ).forEach(pair -> {
        if (pair.getFirst().allow() && pair.getFirst().enableOnLogin()) {
          pair.getSecond().enabled(true);
        }
      });
      this.startEnabledDisplays(user);
    });
  }

  public void handleQuit(final @NonNull P platformPlayer) {
    this.removeUser(this.uuid(platformPlayer));
  }

  private void shutdownDisplays(final @NonNull U user) {
    Stream.of(user.tab(), user.actionBar(), user.bossBar())
      .forEach(DisplayHandler::stopDisplay);
  }

  private void startEnabledDisplays(final @NonNull U user) {
    Stream.of(user.tab(), user.actionBar(), user.bossBar())
      .forEach(display -> {
        if (display.enabled()) {
          display.startDisplay();
        }
      });
  }
}
