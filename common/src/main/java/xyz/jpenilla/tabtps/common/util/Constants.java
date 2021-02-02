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
package xyz.jpenilla.tabtps.common.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Constants {
  private Constants() {
  }

  public static final MiniMessage MINIMESSAGE = MiniMessage.get();
  public static final String PREFIX_MINIMESSAGE = "<white>[<gradient:blue:aqua>TabTPS</gradient>]</white>";
  public static final Component PREFIX = MINIMESSAGE.parse(PREFIX_MINIMESSAGE);

  private static final String DOT = ".";
  private static final String PERMISSION_ROOT = "tabtps";

  private static final String PERMISSION_TOGGLE_ROOT = PERMISSION_ROOT + DOT + "internal" + DOT + "toggle";
  public static final String PERMISSION_TOGGLE_TAB = PERMISSION_TOGGLE_ROOT + DOT + "tab";
  public static final String PERMISSION_TOGGLE_ACTIONBAR = PERMISSION_TOGGLE_ROOT + DOT + "actionbar";
  public static final String PERMISSION_TOGGLE_BOSSBAR = PERMISSION_TOGGLE_ROOT + DOT + "bossbar";

  public static final String PERMISSION_COMMAND_TICKINFO = PERMISSION_ROOT + DOT + "tps";
  public static final String PERMISSION_COMMAND_PING = PERMISSION_ROOT + DOT + "ping";
  public static final String PERMISSION_COMMAND_PING_OTHERS = PERMISSION_ROOT + DOT + "ping" + DOT + "others";
  public static final String PERMISSION_COMMAND_RELOAD = PERMISSION_ROOT + DOT + "reload";

  public static final String PERMISSION_COMMAND_ERROR_HOVER_STACKTRACE = PERMISSION_ROOT + DOT + "command" + DOT + "hover_stacktrace";
}
