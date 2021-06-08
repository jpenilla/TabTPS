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

import cloud.commandframework.CommandHelpHandler;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.minecraft.extras.MinecraftExtrasMetaKeys;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.minecraft.extras.RichDescription;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.Messages;
import xyz.jpenilla.tabtps.common.TabTPS;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.command.Commands;
import xyz.jpenilla.tabtps.common.command.TabTPSCommand;

import static net.kyori.adventure.text.Component.translatable;

public final class HelpCommand extends TabTPSCommand {
  public HelpCommand(final @NonNull TabTPS tabTPS, final @NonNull Commands commands) {
    super(tabTPS, commands);
  }

  @Override
  public void register() {
    final CommandArgument<Commander, String> queryArgument = StringArgument.<Commander>newBuilder("query")
      .greedy()
      .asOptional()
      .withSuggestionsProvider(this::helpQuerySuggestions)
      .build();

    this.commands.registerSubcommand(builder -> builder.literal("help")
      .argument(queryArgument, RichDescription.of(Messages.COMMAND_HELP_ARGUMENTS_QUERY))
      .meta(MinecraftExtrasMetaKeys.DESCRIPTION, Messages.COMMAND_HELP_DESCRIPTION.plain())
      .handler(this::executeHelp));
  }

  private void executeHelp(final @NonNull CommandContext<Commander> context) {
    final String query = context.getOrDefault("query", null);
    this.help().queryCommands(query == null ? "" : query, context.getSender());
  }

  public @NonNull List<String> helpQuerySuggestions(final @NonNull CommandContext<Commander> context, final @NonNull String input) {
    return ((CommandHelpHandler.IndexHelpTopic<Commander>) this.commands.commandManager().getCommandHelpHandler().queryHelp(context.getSender(), ""))
      .getEntries().stream().map(CommandHelpHandler.VerboseHelpEntry::getSyntaxString).collect(Collectors.toList());
  }

  private @NonNull MinecraftHelp<Commander> help() {
    final MinecraftHelp<Commander> help = MinecraftHelp.createNative("/tabtps help", this.commands.commandManager());
    help.setHelpColors(this.tabTPS.configManager().pluginSettings().helpColors().toCloud());
    help.messageProvider(HelpCommand::helpMessage);
    return help;
  }

  private static @NonNull Component helpMessage(final @NonNull Commander sender, final @NonNull String key, final @NonNull String... args) {
    return translatable(
      Messages.bundleName() + "/help." + key,
      Arrays.stream(args)
        .map(Component::text)
        .collect(Collectors.toList())
    );
  }
}
