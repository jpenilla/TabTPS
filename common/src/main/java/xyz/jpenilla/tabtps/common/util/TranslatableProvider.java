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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Consumer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationStore;
import net.kyori.adventure.translation.Translator;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.Style.style;

@DefaultQualifier(NonNull.class)
public final class TranslatableProvider implements ComponentLike {
  private static final Logger LOGGER = LoggerFactory.getLogger(TranslatableProvider.class);
  private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
  private static final String LOCALES_LIST_SUFFIX = "-locales.list";

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

  public String key() {
    return this.key;
  }

  public static TranslatableProvider create(final String bundleName, final String key) {
    return new TranslatableProvider(bundleName, key);
  }

  private static String formatKey(final String bundleName, final String key) {
    return bundleName + '/' + key;
  }

  public static void loadBundle(final String bundleName) {
    final TranslationStore<MessageFormat> registry = TranslationStore.messageFormat(Key.key("tabtps", bundleName));
    registry.defaultLocale(DEFAULT_LOCALE);
    registerAll(registry, availableLocales(bundleName), bundleName);
    GlobalTranslator.translator().addSource(registry);
  }

  private static void registerAll(
    final TranslationStore<MessageFormat> registry,
    final Set<Locale> locales,
    final String bundleName
  ) {
    for (final Locale locale : locales) {
      final ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale, TranslatableProvider.class.getClassLoader());
      for (final String key : bundle.keySet()) {
        try {
          registry.register(
            formatKey(bundleName, key),
            locale,
            new MessageFormat(bundle.getString(key), locale)
          );
        } catch (final IllegalArgumentException | MissingResourceException | ClassCastException ex) {
          LOGGER.warn("Failed to load translation for key '{}' from bundle '{}' with the '{}' locale.", key, bundleName, locale.getDisplayName(), ex);
        }
      }
    }
  }

  private static Set<Locale> availableLocales(final String bundleName) {
    final String bundlePath = bundleName.replace('.', '/');
    final Set<Locale> known = new LinkedHashSet<>();
    known.add(DEFAULT_LOCALE);

    final String localeListPath = bundlePath + LOCALES_LIST_SUFFIX;
    try (final InputStream stream = TranslatableProvider.class.getClassLoader().getResourceAsStream(localeListPath)) {
      if (stream == null) {
        LOGGER.warn("Failed to discover available locales for bundle '{}': resource '{}' was not found.", bundleName, localeListPath);
        return known;
      }
      try (final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
        String localeString;
        while ((localeString = reader.readLine()) != null) {
          localeString = localeString.trim();
          if (localeString.isEmpty()) {
            continue;
          }
          final @Nullable Locale locale = Translator.parseLocale(localeString);
          if (locale == null) {
            LOGGER.warn("Could not parse locale from '{}'; skipping.", localeString);
            continue;
          }
          known.add(locale);
        }
      }
    } catch (final IOException ex) {
      LOGGER.warn("Failed to discover available locales for bundle '{}'.", bundleName, ex);
    }

    return known;
  }
}
