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
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.command.Commands;
import xyz.jpenilla.tabtps.common.command.TabTPSCommand;
import xyz.jpenilla.tabtps.common.util.Constants;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.event.ClickEvent.openUrl;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.TextDecoration.STRIKETHROUGH;
import static xyz.jpenilla.tabtps.common.util.ComponentUtil.gradient;
import static xyz.jpenilla.tabtps.common.util.Serializers.MINIMESSAGE;

public final class AboutCommand extends TabTPSCommand {
  public AboutCommand(final @NonNull TabTPS tabTPS, final @NonNull Commands commands) {
    super(tabTPS, commands);
  }

  @Override
  public void register() {
    this.commands.registerSubcommand(builder -> builder.literal("about")
      .meta(MinecraftExtrasMetaKeys.DESCRIPTION, translatable("tabtps.command.about.description"))
      .handler(this::executeAbout));
  }

  private void executeAbout(final @NonNull CommandContext<Commander> ctx) {
    final Component header = gradient("                                  ", style -> style.decorate(STRIKETHROUGH), BLUE, WHITE, BLUE);
    ctx.getSender().sendMessage(TextComponent.ofChildren(
      header,
      newline(),
      text()
        .content("TabTPS ")
        .append(gradient(Constants.TABTPS_VERSION, BLUE, AQUA))
        .clickEvent(openUrl("https://github.com/jpenilla/TabTPS"))
        .hoverEvent(MINIMESSAGE.parse("<rainbow>click me!")),
      newline(),
      text()
        .content("By ")
        .color(GRAY)
        .append(text("jmp", BLUE)),
      newline(),
      header
    ));
  }
}
