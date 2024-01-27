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
package xyz.jpenilla.tabtps.common.service;

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
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.type.tuple.Pair;
import xyz.jpenilla.tabtps.common.AbstractUser;
import xyz.jpenilla.tabtps.common.TabTPSPlatform;
import xyz.jpenilla.tabtps.common.User;
import xyz.jpenilla.tabtps.common.display.DisplayHandler;

@DefaultQualifier(NonNull.class)
public abstract class UserService<P, U extends User<P>> {
  private static final Gson GSON = new GsonBuilder()
    .setPrettyPrinting()
    .create();

  protected final TabTPSPlatform<P, U> platform;
  private final Path userDataDirectory;
  private final Map<UUID, U> userMap = new ConcurrentHashMap<>();

  protected UserService(final TabTPSPlatform<P, U> platform) {
    this.platform = platform;
    this.userDataDirectory = platform.dataDirectory().resolve("userdata");
  }

  protected abstract UUID uuid(final P base);

  protected abstract U create(final P base);

  private Path userFile(final UUID uniqueId) {
    return this.userDataDirectory.resolve(uniqueId + ".json");
  }

  private U loadUser(final P base) {
    final UUID uniqueId = this.uuid(base);
    final Path file = this.userFile(uniqueId);
    final U user = this.create(base);
    if (Files.exists(file)) {
      try (final BufferedReader reader = Files.newBufferedReader(file)) {
        final User.State deserialized = GSON.fromJson(reader, AbstractUser.StateImpl.class);
        user.state().populate(deserialized);
      } catch (final Exception ex) {
        this.platform.logger().warn("Failed to load data for user with UUID: " + uniqueId, ex);
      }
    }
    return user;
  }

  private void saveUser(final UUID uuid, final U user) {
    final Path file = this.userFile(uuid);
    if (!Files.exists(file)) {
      this.createEmptyFile(file);
    }
    try (final BufferedWriter writer = Files.newBufferedWriter(file)) {
      GSON.toJson(user.state(), writer);
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
  public final void replacePlayer(final P newPlayer) {
    final UUID uuid = this.uuid(newPlayer);
    final U oldUser = this.userMap.get(uuid);
    if (oldUser == null) {
      throw new IllegalArgumentException("Cannot replace a player who is not logged in!");
    }
    this.shutdownDisplays(oldUser);
    final U newUser = this.create(newPlayer);
    newUser.state().populate(oldUser.state());
    this.startEnabledDisplays(newUser);
    this.userMap.put(uuid, newUser);
  }

  public final U user(final P base) {
    return this.userMap.computeIfAbsent(this.uuid(base), uuid -> this.loadUser(base));
  }

  public final U user(final UUID uniqueId) {
    final U user = this.userMap.get(uniqueId);
    if (user == null) {
      throw new IllegalStateException("No user loaded for UUID: " + uniqueId);
    }
    return user;
  }

  public final Map<UUID, U> userStorage() {
    return Collections.unmodifiableMap(this.userMap);
  }

  public final Collection<U> onlineUsers() {
    return Collections.unmodifiableCollection(this.userMap.values());
  }

  protected abstract Collection<P> platformPlayers();

  public final int onlinePlayers() {
    return this.userMap.size();
  }

  public final void reload() {
    this.flush();
    this.platformPlayers().stream()
      .map(this::user)
      .forEach(this::startEnabledDisplays);
  }

  public final void flush() {
    final Set<UUID> users = ImmutableSet.copyOf(this.userMap.keySet());
    users.forEach(this::removeUser);
  }

  public final void removeUser(final UUID uniqueId) {
    final U removed = this.userMap.remove(uniqueId);
    if (removed == null) {
      throw new IllegalStateException("Cannot remove non-existing user " + uniqueId);
    }
    this.shutdownDisplays(removed);
    if (removed.shouldSave()) {
      this.saveUser(uniqueId, removed);
    }
  }

  private void createEmptyFile(final Path file) {
    try {
      Files.createDirectories(this.userDataDirectory);
      Files.createFile(file);
    } catch (final IOException e) {
      this.platform.logger().warn("Failed to create empty file: " + file, e);
    }
  }

  public final void handleJoin(final P platformPlayer) {
    final U user = this.user(platformPlayer);

    this.platform.tabTPS().findDisplayConfig(user).ifPresent(config -> {
      Stream.of(
        Pair.of(config.actionBarSettings(), user.actionBar()),
        Pair.of(config.bossBarSettings(), user.bossBar()),
        Pair.of(config.tabSettings(), user.tab())
      ).forEach(pair -> {
        if (pair.first().allow() && pair.first().enableOnLogin()) {
          pair.second().enabled(true);
        }
      });
      this.startEnabledDisplays(user);
    });
  }

  public final void handleQuit(final P platformPlayer) {
    this.removeUser(this.uuid(platformPlayer));
  }

  private void shutdownDisplays(final U user) {
    user.displays().forEach(DisplayHandler::stopDisplay);
  }

  private void startEnabledDisplays(final U user) {
    user.displays().forEach(display -> {
      if (display.enabled()) {
        display.startDisplay();
      }
    });
  }
}
