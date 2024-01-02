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

import java.util.function.Consumer;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

@DefaultQualifier(NonNull.class)
public final class Components {
  public static final Pattern SPECIAL_CHARACTERS_PATTERN = Pattern.compile("[^\\s\\w\\-]");

  private Components() {
  }

  public static Component highlight(
    final Component component,
    final TextColor highlightColor
  ) {
    return component.replaceText(config -> {
      config.match(SPECIAL_CHARACTERS_PATTERN);
      config.replacement(match -> match.color(highlightColor));
    });
  }

  public static Component gradient(final String textContent, final @Nullable Consumer<Style.Builder> style, final TextColor ... colors) {
    final Gradient gradient = new Gradient(colors);
    final TextComponent.Builder builder = text();
    if (style != null) {
      builder.style(style);
    }
    final char[] content = textContent.toCharArray();
    gradient.length(content.length);
    for (final char c : content) {
      builder.append(text(c, gradient.nextColor()));
    }
    return builder.build();
  }

  public static Component gradient(final String textContent, final TextColor ... colors) {
    return gradient(textContent, null, colors);
  }

  public static TextComponent ofChildren(final ComponentLike... children) {
    if (children.length == 0) {
      return empty();
    }

    return text().append(children).build();
  }
}
