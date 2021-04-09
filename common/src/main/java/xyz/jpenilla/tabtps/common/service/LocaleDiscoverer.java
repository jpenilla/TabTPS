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

import net.kyori.adventure.translation.Translator;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public interface LocaleDiscoverer {
  String PROPERTIES_EXTENSION = ".properties";

  static @NonNull LocaleDiscoverer standard(final String bundleName) {
    return new DefaultImpl(bundleName);
  }

  static @NonNull Set<Locale> localesFromPathStrings(final @NonNull String bundleId, final @NonNull Stream<String> pathStrings) {
    return pathStrings
      .filter(path -> path.startsWith(bundleId + "_") && path.endsWith(PROPERTIES_EXTENSION))
      .map(path -> path.replaceFirst(bundleId + "_", "").replaceFirst(PROPERTIES_EXTENSION, ""))
      .map(Translator::parseLocale)
      .collect(Collectors.toSet());
  }

  @NonNull Set<Locale> availableLocales() throws IOException;

  final class DefaultImpl implements LocaleDiscoverer {
    private final String bundleName;

    private DefaultImpl(final String bundleName) {
      this.bundleName = bundleName;
    }

    @Override
    public @NonNull Set<Locale> availableLocales() throws IOException {
      final Set<Locale> locales = new HashSet<>();
      final Enumeration<URL> urls = this.getClass().getClassLoader().getResources("META-INF");
      while (urls.hasMoreElements()) {
        final URL url = urls.nextElement();
        final JarURLConnection connection = (JarURLConnection) (url.openConnection());
        try (final JarFile jar = connection.getJarFile()) {
          locales.addAll(localesFromPathStrings(
            this.bundleName,
            jar.stream().map(ZipEntry::toString)
          ));
        }
      }
      return locales;
    }
  }
}
