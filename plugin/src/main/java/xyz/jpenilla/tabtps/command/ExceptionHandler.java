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
package xyz.jpenilla.tabtps.command;

import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.parsing.ParserException;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler.ExceptionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.Constants;
import xyz.jpenilla.tabtps.TabTPS;
import xyz.jpenilla.tabtps.util.ComponentUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;

final class ExceptionHandler {
  private final MinecraftExceptionHandler<CommandSender> minecraftExceptionHandler;
  private final TabTPS tabTPS;

  protected ExceptionHandler(
    final @NonNull TabTPS tabTPS
  ) {
    this.tabTPS = tabTPS;
    this.minecraftExceptionHandler = new MinecraftExceptionHandler<CommandSender>()
      .withHandler(ExceptionType.NO_PERMISSION, ExceptionHandler::noPermission)
      .withHandler(ExceptionType.ARGUMENT_PARSING, ExceptionHandler::argumentParsing)
      .withHandler(ExceptionType.INVALID_SENDER, ExceptionHandler::invalidSender)
      .withHandler(ExceptionType.INVALID_SYNTAX, ExceptionHandler::invalidSyntax)
      .withHandler(ExceptionType.COMMAND_EXECUTION, this::commandExecution)
      .withDecorator(ExceptionHandler::decorate);
  }

  private static @NonNull Component decorate(final @NonNull Component component) {
    return LinearComponents.linear(
      Constants.PREFIX,
      Component.space(),
      component
    );
  }

  public void apply(final @NonNull CommandManager manager) {
    this.minecraftExceptionHandler.apply(manager, this.tabTPS.audiences()::sender);
  }

  private @NonNull Component commandExecution(final @NonNull Exception ex) {
    final Throwable cause = ex.getCause();
    this.tabTPS.getLogger().log(Level.WARNING, "An unexpected error occurred during command execution", cause);

    final StringWriter writer = new StringWriter();
    cause.printStackTrace(new PrintWriter(writer));
    final String stackTrace = writer.toString().replaceAll("\t", "    ");
    final HoverEvent<Component> hover = HoverEvent.showText(
      Component.text()
        .append(Component.text(stackTrace))
        .append(Component.newline())
        .append(Component.text("    "))
        .append(Component.translatable(
          "tabtps.misc.text.click_to_copy",
          NamedTextColor.GRAY,
          TextDecoration.ITALIC
        ))
    );
    return Component.text()
      .append(Component.translatable(
        "tabtps.command.exception.command_execution",
        NamedTextColor.RED
      ))
      .hoverEvent(hover)
      .clickEvent(ClickEvent.copyToClipboard(stackTrace))
      .build();
  }

  private static @NonNull Component noPermission(final @NonNull Exception ex) {
    return Component.translatable("tabtps.command.exception.no_permission", NamedTextColor.RED);
  }

  private static @NonNull Component argumentParsing(final @NonNull Exception ex) {
    final ParserException exception = (ParserException) ex.getCause();
    return Component.translatable(
      "tabtps.command.exception.invalid_argument",
      NamedTextColor.RED,
      Component.translatable(
        "tabtps.command.caption." + exception.errorCaption().getKey(),
        NamedTextColor.GRAY,
        Arrays.stream(exception.captionVariables())
          .map(CaptionVariable::getValue)
          .map(Component::text)
          .collect(Collectors.toList())
      )
    );
  }

  private static @NonNull Component invalidSender(final @NonNull Exception ex) {
    final InvalidCommandSenderException exception = (InvalidCommandSenderException) ex;
    return Component.text()
      .append(Component.translatable(
        "tabtps.command.exception.invalid_sender_type",
        NamedTextColor.RED,
        Component.text(exception.getRequiredSender().getSimpleName())
      ))
      .build();
  }

  private static @NonNull Component invalidSyntax(final @NonNull Exception ex) {
    final InvalidSyntaxException exception = (InvalidSyntaxException) ex;
    return Component.translatable(
      "tabtps.command.exception.invalid_syntax",
      NamedTextColor.RED,
      ComponentUtil.highlight(
        Component.text(
          String.format("/%s", exception.getCorrectSyntax()),
          NamedTextColor.GRAY
        ),
        NamedTextColor.WHITE
      )
    );
  }
}
