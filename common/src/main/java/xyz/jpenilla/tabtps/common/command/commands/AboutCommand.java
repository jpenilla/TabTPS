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
package xyz.jpenilla.tabtps.common.command.commands;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.util.Constants;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.command.Commands;
import xyz.jpenilla.tabtps.common.command.TabTPSCommand;
import xyz.jpenilla.tabtps.common.util.ComponentUtil;

public final class AboutCommand extends TabTPSCommand {
  public AboutCommand(final @NonNull TabTPS tabTPS, final @NonNull Commands commands) {
    super(tabTPS, commands);
  }

  @Override
  public void register() {
    this.commands.registerSubcommand(builder ->
      builder.literal("about")
        .meta(CommandMeta.DESCRIPTION, "tabtps.command.about.description")
        .handler(this::executeAbout)
    );
  }

  private void executeAbout(final @NonNull CommandContext<Commander> ctx) {
    final Component header = ComponentUtil.gradient("----------------------------------", NamedTextColor.WHITE, NamedTextColor.BLACK, NamedTextColor.WHITE).decorate(TextDecoration.STRIKETHROUGH);
    ImmutableList.of(
      header,
      Constants.MINIMESSAGE.parse("<hover:show_text:'<rainbow>click me!'><click:open_url:https://github.com/jpenilla/TabTPS>TabTPS <gradient:blue:aqua>{version}"), // todo
      Constants.MINIMESSAGE.parse("<gray>By <gradient:gold:yellow>jmp"),
      header
    ).forEach(ctx.getSender()::sendMessage);
  }
}
