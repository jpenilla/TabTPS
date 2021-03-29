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
package xyz.jpenilla.tabtps.common.command;

import cloud.commandframework.CommandManager;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.exceptions.parsing.ParserException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.ComponentMessageThrowable;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.command.exception.CommandCompletedException;
import xyz.jpenilla.tabtps.common.util.ComponentUtil;
import xyz.jpenilla.tabtps.common.util.Constants;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ExceptionHandler {
  private final TabTPS tabTPS;

  ExceptionHandler(final @NonNull TabTPS tabTPS) {
    this.tabTPS = tabTPS;
  }

  private static void decorateAndSend(final @NonNull Commander commander, final @NonNull Component component) {
    commander.sendMessage(TextComponent.ofChildren(
      Constants.PREFIX,
      Component.space(),
      component
    ));
  }

  public void apply(final @NonNull CommandManager<Commander> manager) {
    manager.registerExceptionHandler(CommandExecutionException.class, this::commandExecution);
    manager.registerExceptionHandler(NoPermissionException.class, this::noPermission);
    manager.registerExceptionHandler(ArgumentParseException.class, this::argumentParsing);
    manager.registerExceptionHandler(InvalidCommandSenderException.class, this::invalidSender);
    manager.registerExceptionHandler(InvalidSyntaxException.class, this::invalidSyntax);
  }

  private void commandExecution(final @NonNull Commander commander, final @NonNull CommandExecutionException exception) {
    final Throwable cause = exception.getCause();

    if (cause instanceof CommandCompletedException) {
      final Component message = ((CommandCompletedException) cause).componentMessage();
      if (message != null) {
        commander.sendMessage(message);
      }
      return;
    }

    this.tabTPS.platform().logger().warn("An unexpected error occurred during command execution", cause);

    final StringWriter writer = new StringWriter();
    cause.printStackTrace(new PrintWriter(writer));
    final String stackTrace = writer.toString().replaceAll("\t", "    ");
    final TextComponent.Builder builder = Component.text();
    final Component throwableMessage = ComponentMessageThrowable.getOrConvertMessage(cause);
    if (throwableMessage != null) {
      builder.append(throwableMessage)
        .append(Component.newline())
        .append(Component.newline());
    }
    builder.append(Component.text(stackTrace))
      .append(Component.newline())
      .append(Component.text("    "))
      .append(Component.translatable(
        "tabtps.misc.text.click_to_copy",
        NamedTextColor.GRAY,
        TextDecoration.ITALIC
      ));
    final TextComponent.Builder finalBuilder = Component.text();
    finalBuilder.append(Component.translatable(
      "tabtps.command.exception.command_execution",
      NamedTextColor.RED
    ));
    if (commander.hasPermission(Constants.PERMISSION_COMMAND_ERROR_HOVER_STACKTRACE)) {
      finalBuilder.hoverEvent(builder.build());
      finalBuilder.clickEvent(ClickEvent.copyToClipboard(stackTrace));
    }
    decorateAndSend(commander, finalBuilder.build());
  }

  private void noPermission(final @NonNull Commander commander, final @NonNull NoPermissionException exception) {
    final Component message = Component.translatable("tabtps.command.exception.no_permission", NamedTextColor.RED);
    decorateAndSend(commander, message);
  }

  private void argumentParsing(final @NonNull Commander commander, final @NonNull ArgumentParseException exception) {
    final Throwable cause = exception.getCause();
    final Component message;
    if (cause instanceof ParserException) {
      final ParserException ex = (ParserException) cause;
      message = Component.translatable(
        "tabtps.command.caption." + ex.errorCaption().getKey(),
        NamedTextColor.GRAY,
        Arrays.stream(ex.captionVariables())
          .map(CaptionVariable::getValue)
          .map(Component::text)
          .collect(Collectors.toList())
      );
    } else {
      message = Objects.requireNonNull(ComponentMessageThrowable.getOrConvertMessage(cause));
    }
    decorateAndSend(
      commander,
      Component.translatable(
        "tabtps.command.exception.invalid_argument",
        NamedTextColor.RED,
        message
      )
    );
  }

  private void invalidSender(final @NonNull Commander commander, final @NonNull InvalidCommandSenderException exception) {
    final Component message = Component.text()
      .append(Component.translatable(
        "tabtps.command.exception.invalid_sender_type",
        NamedTextColor.RED,
        Component.text(exception.getRequiredSender().getSimpleName())
      ))
      .build();
    decorateAndSend(commander, message);
  }

  private void invalidSyntax(final @NonNull Commander commander, final @NonNull InvalidSyntaxException exception) {
    final Component message = Component.translatable(
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
    decorateAndSend(commander, message);
  }
}
