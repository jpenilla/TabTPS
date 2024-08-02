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

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.Style.style;

@DefaultQualifier(NonNull.class)
public final class TranslatableProvider implements ComponentLike {
  private static final Logger LOGGER = LoggerFactory.getLogger(TranslatableProvider.class);
  private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
  private static final String PROPERTIES_EXTENSION = ".properties";

  private final String key;

  private TranslatableProvider(final String bundleName, final String key) {
    this.key = formatKey(bundleName, key);
  }

  public TranslatableComponent plain(final ComponentLike... args) {
    return translatable(this.key, args);
  }

  @Override
  public TranslatableComponent asComponent() {
    return this.plain();
  }

  public TranslatableComponent.Builder builder() {
    return translatable().key(this.key);
  }

  public TranslatableComponent build(final Consumer<TranslatableComponent.Builder> op) {
    final TranslatableComponent.Builder builder = this.builder();
    op.accept(builder);
    return builder.build();
  }

  public TranslatableComponent styled(final TextColor color, final ComponentLike... args) {
    return translatable(this.key, color, args);
  }

  public TranslatableComponent styled(final TextColor color, final TextDecoration decoration, final ComponentLike... args) {
    return translatable(this.key, style(color, decoration), args);
  }

  public @NonNull String key() {
    return this.key;
  }

  public static TranslatableProvider create(final String bundleName, final String key) {
    return new TranslatableProvider(bundleName, key);
  }

  private static String formatKey(final String bundleName, final String key) {
    return bundleName + '/' + key;
  }

  public static void loadBundle(final String bundleName) {
    final TranslationRegistry registry = TranslationRegistry.create(Key.key("tabtps", bundleName));
    registry.defaultLocale(DEFAULT_LOCALE);

    registerAll(
      registry,
      availableLocales(bundleName),
      bundleName,
      false
    );

    GlobalTranslator.translator().addSource(registry);
  }

  private static final boolean HAS_LIST_OF;

  static {
    boolean hasListOf;
    try {
      List.class.getDeclaredMethod("of");
      hasListOf = true;
    } catch (final ReflectiveOperationException e) {
      hasListOf = false;
    }
    HAS_LIST_OF = hasListOf;
  }

  private static void registerAll(final TranslationRegistry registry, final Set<Locale> locales, final String bundleName, final boolean escapeSingleQuotes) {
    for (final Locale locale : locales) {
      final ResourceBundle bundle;
      // custom Control not supported in named modules, so don't try and use one where it's not needed (JRE 9+)
      if (HAS_LIST_OF) {
        bundle = PropertyResourceBundle.getBundle(bundleName, locale, TranslatableProvider.class.getClassLoader());
      } else {
        bundle = PropertyResourceBundle.getBundle(bundleName, locale, TranslatableProvider.class.getClassLoader(), UTF8ResourceBundleControl.get());
      }
      for (final String key : bundle.keySet()) {
        try {
          registry.register(
            formatKey(bundleName, key),
            locale,
            new MessageFormat(
              escapeSingleQuotes ? TranslationRegistry.SINGLE_QUOTE_PATTERN.matcher(bundle.getString(key)).replaceAll("''") : bundle.getString(key),
              locale
            )
          );
        } catch (final IllegalArgumentException | MissingResourceException | ClassCastException ex) {
          LOGGER.warn("Failed to load translation for key '{}' from bundle '{}' with the '{}' locale.", key, bundleName, locale.getDisplayName(), ex);
        }
      }
    }
  }

  public static Path MOD_JAR_OVERRIDE = null;

  private static Path modJar() throws URISyntaxException, MalformedURLException {
    if (MOD_JAR_OVERRIDE != null) {
      return MOD_JAR_OVERRIDE;
    }

    URL sourceUrl = TranslatableProvider.class.getProtectionDomain().getCodeSource().getLocation();
    // Some class loaders give the full url to the class, some give the URL to its jar.
    // We want the containing jar, so we will unwrap jar-schema code sources.
    if (sourceUrl.getProtocol().equals("jar")) {
      final int exclamationIdx = sourceUrl.getPath().lastIndexOf('!');
      if (exclamationIdx != -1) {
        sourceUrl = new URL(sourceUrl.getPath().substring(0, exclamationIdx));
      }
    }
    return Paths.get(sourceUrl.toURI());
  }

  private static Set<Locale> availableLocales(final String bundleName) {
    final String bundlePath = bundleName.replace('.', '/');
    try {
      // If this were meant to be more generic we would pass in the ClassLoader and use its code source here...
      final Path codeSource = modJar();

      final Set<Locale> known = new HashSet<>();

      walkJar(codeSource, stream -> stream
        .filter(Files::isRegularFile)
        .map(Path::toString)
        .filter(it -> it.endsWith(PROPERTIES_EXTENSION))
        .map(it -> it.replace('\\', '/'))
        .map(it -> it.startsWith("/") ? it.substring(1) : it)
        .filter(it -> it.startsWith(bundlePath))
        .map(it -> it.substring(bundlePath.length()).replaceFirst(PROPERTIES_EXTENSION, ""))
        .filter(it -> it.isEmpty() || it.startsWith("_"))
        .map(string -> {
          if (string.isEmpty()) {
            return DEFAULT_LOCALE;
          } else {
            return requireNonNull(Translator.parseLocale(string.substring(1)), "Could not parse locale from: '" + string.substring(1) + "'");
          }
        })
        .forEach(known::add));

      return known;
    } catch (final URISyntaxException | IOException ex) {
      LOGGER.warn("Failed to discover available locales for bundle '{}'.", bundleName, ex);
      return ImmutableSet.of(DEFAULT_LOCALE);
    }
  }

  private static void walkJar(final Path jarPath, final Consumer<Stream<Path>> user) throws IOException {
    if (Files.isDirectory(jarPath)) {
      try (final Stream<Path> stream = Files.walk(jarPath)) {
        user.accept(stream.map(it -> it.relativize(jarPath)));
      }
      return;
    }
    try (final FileSystem jar = FileSystems.newFileSystem(jarPath, TranslatableProvider.class.getClassLoader())) {
      final Path root = jar.getRootDirectories()
        .iterator()
        .next();
      try (final Stream<Path> stream = Files.walk(root)) {
        user.accept(stream);
      }
    }
  }
}
