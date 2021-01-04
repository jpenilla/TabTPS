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

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.TabTPS;

import java.util.Arrays;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public final class CommandManager extends PaperCommandManager<CommandSender> {

  private final TabTPS tabTPS;
  private final MinecraftHelp<CommandSender> help;
  private final AnnotationParser<CommandSender> annotationParser;

  public CommandManager(final @NonNull TabTPS tabTPS) throws Exception {
    super(
      tabTPS,
      AsynchronousCommandExecutionCoordinator
        .<CommandSender>newBuilder().build(),
      UnaryOperator.identity(),
      UnaryOperator.identity()
    );

    this.tabTPS = tabTPS;

    this.annotationParser = new AnnotationParser<>(
      this,
      CommandSender.class,
      params -> CommandMeta.simple()
        .with(CommandMeta.DESCRIPTION, params.get(StandardParameters.DESCRIPTION, "tabtps.help.no_description"))
        .build()
    );

    this.help = new MinecraftHelp<>("/tabtps help", tabTPS.audiences()::sender, this);
    this.help.messageProvider((sender, key, args) ->
      Component.translatable(
        "tabtps.help." + key,
        Arrays.stream(args)
          .map(Component::text)
          .collect(Collectors.toList())
      )
    );
    this.help.descriptionDecorator(Component::translatable);
    this.setupHelpColors();

    new ExceptionHandler(tabTPS).apply(this);

    /* Register Brigadier */
    if (this.queryCapability(CloudBukkitCapabilities.BRIGADIER)) {
      this.registerBrigadier();
      final CloudBrigadierManager<CommandSender, ?> brigadierManager = this.brigadierManager();
      if (brigadierManager != null) {
        brigadierManager.setNativeNumberSuggestions(false);
      }
      tabTPS.getLogger().info("Successfully registered Mojang Brigadier support for commands.");
    }

    /* Register Asynchronous Completion Listener */
    if (this.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
      this.registerAsynchronousCompletions();
      tabTPS.getLogger().info("Successfully registered asynchronous command completion listener.");
    }

    /* Register Commands */
    ImmutableSet.of(
      new CommandTabTPS(tabTPS, this),
      new CommandTPS(tabTPS, this),
      new CommandMemory(tabTPS, this),
      new CommandPing(tabTPS, this)
    ).forEach(this.annotationParser::parse);
  }

  public void onReload() {
    this.setupHelpColors();
  }

  private void setupHelpColors() {
    this.help.setHelpColors(this.tabTPS.pluginSettings().helpColors().toCloud());
  }

  public @NonNull MinecraftHelp<CommandSender> help() {
    return this.help;
  }
}
