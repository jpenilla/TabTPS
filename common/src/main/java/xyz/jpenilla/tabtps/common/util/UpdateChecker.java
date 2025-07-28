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
package xyz.jpenilla.tabtps.common.util;

import com.google.common.base.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class UpdateChecker {
  private static final JsonParser parser = new JsonParser();

  private UpdateChecker() {
  }

  public static @NonNull List<String> checkVersion(final @NonNull String version) {
    final JsonArray result;
    try {
      result = parser.parse(new InputStreamReader(new URL("https://api.github.com/repos/jpenilla/TabTPS/releases").openStream(), Charsets.UTF_8)).getAsJsonArray();
    } catch (final IOException exception) {
      return Collections.singletonList("Failed to look for updates: " + exception.getMessage());
    }

    final Map<String, String> versionMap = new LinkedHashMap<>();
    result.forEach(element -> versionMap.put(element.getAsJsonObject().get("tag_name").getAsString(), element.getAsJsonObject().get("html_url").getAsString()));
    final List<String> versionList = new LinkedList<>(versionMap.keySet());
    final String currentVersion = "v" + version;
    if (versionList.get(0).equals(currentVersion)) {
      return Collections.emptyList();
    }
    final List<String> list = new ArrayList<>();
    if (currentVersion.contains("SNAPSHOT")) {
      list.add("This server is running a development build of TabTPS! (" + currentVersion + ")");
      list.add("The latest official release is " + versionList.get(0));
      return list;
    }
    final int versionsBehind = versionList.indexOf(currentVersion);
    list.add("There is an update available for TabTPS!");
    list.add("This server is running version " + currentVersion + ", which is " + (versionsBehind == -1 ? "UNKNOWN" : versionsBehind) + " versions outdated.");
    list.add("Download the latest version, " + versionList.get(0) + " from Modrinth at the link below:");
    list.add("https://modrinth.com/plugin/tabtps");
    return list;
  }
}
