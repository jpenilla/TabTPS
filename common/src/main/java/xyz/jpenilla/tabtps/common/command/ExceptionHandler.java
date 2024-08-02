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
package xyz.jpenilla.tabtps.common.command;

import io.leangen.geantyref.GenericTypeReflector;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.util.ComponentMessageThrowable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.exception.handling.ExceptionContext;
import org.incendo.cloud.exception.parsing.ParserException;
import xyz.jpenilla.tabtps.common.Messages;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.command.exception.CommandCompletedException;
import xyz.jpenilla.tabtps.common.util.Components;
import xyz.jpenilla.tabtps.common.util.Constants;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.event.ClickEvent.copyToClipboard;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

public final class ExceptionHandler {
  private final TabTPS tabTPS;

  ExceptionHandler(final @NonNull TabTPS tabTPS) {
    this.tabTPS = tabTPS;
  }

  private static void decorateAndSend(final @NonNull Commander commander, final @NonNull ComponentLike componentLike) {
    commander.sendMessage(Components.ofChildren(Constants.PREFIX, space(), componentLike));
  }

  public void apply(final @NonNull CommandManager<Commander> manager) {
    manager.exceptionController().registerHandler(CommandExecutionException.class, this::commandExecution);
    manager.exceptionController().registerHandler(NoPermissionException.class, this::noPermission);
    manager.exceptionController().registerHandler(ArgumentParseException.class, this::argumentParsing);
    manager.exceptionController().registerHandler(InvalidCommandSenderException.class, this::invalidSender);
    manager.exceptionController().registerHandler(InvalidSyntaxException.class, this::invalidSyntax);
  }

  private void commandExecution(final @NonNull ExceptionContext<Commander, CommandExecutionException> ctx) {
    final Throwable cause = ctx.exception().getCause();

    if (cause instanceof CommandCompletedException) {
      final Component message = ((CommandCompletedException) cause).componentMessage();
      if (message != null) {
        ctx.context().sender().sendMessage(message);
      }
      return;
    }

    this.tabTPS.platform().logger().warn("An unexpected error occurred during command execution", cause);

    final StringWriter writer = new StringWriter();
    cause.printStackTrace(new PrintWriter(writer));
    final String stackTrace = writer.toString().replaceAll("\t", "    ");
    final TextComponent.Builder hoverText = text();
    final Component throwableMessage = ComponentMessageThrowable.getOrConvertMessage(this.tabTPS.platform().asComponentMessageThrowable(cause));
    if (throwableMessage != null) {
      hoverText.append(throwableMessage)
        .append(newline())
        .append(newline());
    }
    hoverText.append(text(stackTrace))
      .append(newline())
      .append(text("    "))
      .append(Messages.MISC_TEXT_CLICK_TO_COPY.styled(GRAY, ITALIC));
    final TextComponent.Builder message = text();
    message.append(Messages.COMMAND_EXCEPTION_COMMAND_EXECUTION.styled(RED));
    if (ctx.context().sender().hasPermission(Constants.PERMISSION_COMMAND_ERROR_HOVER_STACKTRACE)) {
      message.hoverEvent(hoverText.build());
      message.clickEvent(copyToClipboard(stackTrace));
    }
    decorateAndSend(ctx.context().sender(), message);
  }

  private void noPermission(final @NonNull ExceptionContext<Commander, NoPermissionException> ctx) {
    decorateAndSend(ctx.context().sender(), Messages.COMMAND_EXCEPTION_NO_PERMISSION.styled(RED));
  }

  private void argumentParsing(final @NonNull ExceptionContext<Commander, ArgumentParseException> ctx) {
    final Throwable cause = ctx.exception().getCause();
    final Component message;
    if (cause instanceof ParserException) {
      final ParserException ex = (ParserException) cause;
      message = translatable(
        Messages.bundleName() + "/command.caption." + ex.errorCaption().key(),
        GRAY,
        Arrays.stream(ex.captionVariables())
          .map(CaptionVariable::value)
          .map(Component::text)
          .collect(Collectors.toList())
      );
    } else {
      message = Objects.requireNonNull(ComponentMessageThrowable.getOrConvertMessage(this.tabTPS.platform().asComponentMessageThrowable(cause))).color(GRAY);
    }
    decorateAndSend(ctx.context().sender(), Messages.COMMAND_EXCEPTION_INVALID_ARGUMENT.styled(RED, message));
  }

  private void invalidSender(final @NonNull ExceptionContext<Commander, InvalidCommandSenderException> ctx) {
    if (ctx.exception().requiredSenderTypes().size() > 1) {
      // We don't have any command chains that would cause this to happen
      throw new IllegalStateException();
    }
    final Component message = Messages.COMMAND_EXCEPTION_INVALID_SENDER_TYPE.styled(
      RED,
      text(GenericTypeReflector.erase(ctx.exception().requiredSenderTypes().iterator().next()).getSimpleName())
    );
    decorateAndSend(ctx.context().sender(), message);
  }

  private void invalidSyntax(final @NonNull ExceptionContext<Commander, InvalidSyntaxException> ctx) {
    final Component message = Messages.COMMAND_EXCEPTION_INVALID_SYNTAX.styled(
      RED,
      Components.highlight(text(String.format("/%s", ctx.exception().correctSyntax()), GRAY), WHITE)
    );
    decorateAndSend(ctx.context().sender(), message);
  }
}
