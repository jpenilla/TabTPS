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

import java.util.Arrays;
import java.util.Collections;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Utility class for creating {@link TextColor} gradients.
 *
 * <p>Based on <a href="https://github.com/KyoriPowered/adventure-text-minimessage/blob/b44cb90632c03c78c50dbae5c86e616a570afa3d/src/main/java/net/kyori/adventure/text/minimessage/fancy/Gradient.java">
 * MiniMessage 4.0.0-SNAPSHOT's Gradient Class</a>, which is released under the MIT license.</p>
 */
public final class Gradient {

  private int index = 0;
  private int colorIndex = 0;
  private float factorStep = 0;
  private float phase;

  private final boolean negativePhase;
  private final TextColor[] colors;

  /**
   * Create a new {@link Gradient}.
   *
   * @param colors colors to use for this gradient, in order
   */
  public Gradient(final @NonNull TextColor... colors) {
    this(0, colors);
  }

  /**
   * Create a new {@link Gradient}.
   *
   * @param phase  phase to use for this gradient, must be in range [-1, 1]
   * @param colors colors to use for this gradient, in order
   */
  public Gradient(final float phase, final @NonNull TextColor... colors) {
    if (colors.length < 2) {
      throw new IllegalArgumentException("Gradients must have at least two colors! colors=" + Arrays.toString(colors));
    }
    if (phase > 1.0 || phase < -1.0) {
      throw new IllegalArgumentException(String.format("Phase must be in range [-1, 1]. '%s' is not valid.", phase));
    }
    this.colors = colors;
    if (phase < 0) {
      this.negativePhase = true;
      this.phase = 1 + phase;
      Collections.reverse(Arrays.asList(this.colors));
    } else {
      this.negativePhase = false;
      this.phase = phase;
    }
  }

  /**
   * Set the total length of the content you are applying a gradient to.
   *
   * @param size length of content
   */
  public void length(final int size) {
    this.colorIndex = 0;
    this.index = 0;
    final int sectorLength = size / (this.colors.length - 1);
    this.factorStep = 1.0f / sectorLength;
    this.phase = this.phase * sectorLength;
  }

  /**
   * Get the next color needed in order to create a gradient.
   *
   * <p>If {@link #length(int)} has not been called, behaviour is undefined.</p>
   *
   * @return the next color needed to form the gradient
   */
  public @NonNull TextColor nextColor() {
    // color switch needed?
    if (this.factorStep * this.index > 1) {
      this.colorIndex++;
      this.index = 0;
    }

    float factor = this.factorStep * (this.index++ + this.phase);
    // loop around if needed
    if (factor > 1) {
      factor = 1 - (factor - 1);
    }
    if (this.negativePhase && this.colors.length % 2 != 0) {
      // flip the gradient segment for to allow for looping phase -1 through 1
      return this.interpolate(this.colors[this.colorIndex + 1], this.colors[this.colorIndex], factor);
    } else {
      return this.interpolate(this.colors[this.colorIndex], this.colors[this.colorIndex + 1], factor);
    }
  }

  private @NonNull TextColor interpolate(final @NonNull TextColor color1, final @NonNull TextColor color2, final float factor) {
    return TextColor.color(
      Math.round(color1.red() + factor * (color2.red() - color1.red())),
      Math.round(color1.green() + factor * (color2.green() - color1.green())),
      Math.round(color1.blue() + factor * (color2.blue() - color1.blue()))
    );
  }
}
