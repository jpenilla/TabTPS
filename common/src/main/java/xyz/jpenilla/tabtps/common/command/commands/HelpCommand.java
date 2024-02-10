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
package xyz.jpenilla.tabtps.common.command.commands;

import java.util.Map;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.component.TypedCommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.help.result.CommandEntry;
import org.incendo.cloud.minecraft.extras.AudienceProvider;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import xyz.jpenilla.tabtps.common.Messages;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.command.Commands;
import xyz.jpenilla.tabtps.common.command.TabTPSCommand;

import static net.kyori.adventure.text.Component.translatable;
import static org.incendo.cloud.minecraft.extras.RichDescription.richDescription;
import static org.incendo.cloud.parser.standard.StringParser.greedyStringParser;
import static org.incendo.cloud.suggestion.SuggestionProvider.blockingStrings;

public final class HelpCommand extends TabTPSCommand {
  public HelpCommand(final @NonNull TabTPS tabTPS, final @NonNull Commands commands) {
    super(tabTPS, commands);
  }

  @Override
  public void register() {
    final TypedCommandComponent<Commander, String> queryArgument = TypedCommandComponent.<Commander, String>builder()
      .name("query")
      .parser(greedyStringParser())
      .optional()
      .defaultValue(DefaultValue.constant(""))
      .suggestionProvider(blockingStrings(this::helpQuerySuggestions))
      .description(richDescription(Messages.COMMAND_HELP_ARGUMENTS_QUERY))
      .build();

    this.commands.registerSubcommand(builder -> builder.literal("help")
      .argument(queryArgument)
      .commandDescription(richDescription(Messages.COMMAND_HELP_DESCRIPTION.plain()))
      .handler(this::executeHelp));
  }

  private void executeHelp(final @NonNull CommandContext<Commander> context) {
    this.help().queryCommands(context.get("query"), context.sender());
  }

  public @NonNull Iterable<String> helpQuerySuggestions(final @NonNull CommandContext<Commander> context, final @NonNull CommandInput input) {
    return this.commands.commandManager().createHelpHandler()
      .queryRootIndex(context.sender())
      .entries()
      .stream()
      .map(CommandEntry::syntax)
      .collect(Collectors.toList());
  }

  private @NonNull MinecraftHelp<Commander> help() {
    return MinecraftHelp.<Commander>builder()
      .commandManager(this.commandManager)
      .audienceProvider(AudienceProvider.nativeAudience())
      .commandPrefix("/tabtps help")
      .colors(this.tabTPS.configManager().pluginSettings().helpColors().toCloud())
      .messageProvider(HelpCommand::helpMessage)
      .build();
  }

  private static @NonNull Component helpMessage(final @NonNull Commander sender, final @NonNull String key, final @NonNull Map<String, String> args) {
    return translatable(
      Messages.bundleName() + "/help." + key,
      args.values().stream()
        .map(Component::text)
        .collect(Collectors.toList())
    );
  }
}
