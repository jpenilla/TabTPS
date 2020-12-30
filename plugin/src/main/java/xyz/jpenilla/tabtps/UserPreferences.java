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
package xyz.jpenilla.tabtps;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UserPreferences {
  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  private static final JsonParser jsonParser = new JsonParser();

  public static @NonNull UserPreferences deserialize(final @NonNull File json) throws FileNotFoundException {
    final JsonObject jsonObject = jsonParser.parse(new FileReader(json)).getAsJsonObject();
    return gson.fromJson(jsonObject, UserPreferences.class);
  }

  private final Set<UUID> tabEnabled = new HashSet<>();
  private final Set<UUID> actionBarEnabled = new HashSet<>();
  private final Set<UUID> bossBarEnabled = new HashSet<>();

  public @NonNull Set<UUID> tabEnabled() {
    return this.tabEnabled;
  }

  public @NonNull Set<UUID> actionBarEnabled() {
    return this.actionBarEnabled;
  }

  public @NonNull Set<UUID> bossBarEnabled() {
    return this.bossBarEnabled;
  }

  public void serialize(final @NonNull FileWriter writer) throws IOException {
    writer.write(gson.toJson(this));
    writer.flush();
    writer.close();
  }
}
