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
package xyz.jpenilla.tabtps.fabric.command;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.fabric.FabricCommandContextKeys;
import cloud.commandframework.fabric.FabricServerCommandManager;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.tabtps.common.User;
import xyz.jpenilla.tabtps.common.command.Commander;
import xyz.jpenilla.tabtps.common.command.Commands;
import xyz.jpenilla.tabtps.common.command.commands.PingCommand;
import xyz.jpenilla.tabtps.common.command.exception.CommandCompletedException;
import xyz.jpenilla.tabtps.common.util.Constants;
import xyz.jpenilla.tabtps.fabric.TabTPSFabric;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class FabricPingCommand extends PingCommand {
  private final TabTPSFabric tabTPSFabric;

  public FabricPingCommand(final @NonNull TabTPSFabric tabTPSFabric, final @NonNull Commands commands) {
    super(tabTPSFabric.tabTPS(), commands);
    this.tabTPSFabric = tabTPSFabric;
    this.commandManager.getParserRegistry().registerParserSupplier(TypeToken.get(EntitySelector.class), p -> new WrappedBrigadierParser<>(EntityArgument.players()));
    ((FabricServerCommandManager<Commander>) this.commandManager).brigadierManager().registerMapping(
      new TypeToken<WrappedBrigadierParser<Commander, EntitySelector>>() {
      },
      builder -> builder.toConstant(EntityArgument.players())
    );
  }

  @Override
  public void register() {
    final CommandArgument<Commander, EntitySelector> selectorArgument = this.commandManager.argumentBuilder(EntitySelector.class, "target")
      .withParser(new WrappedBrigadierParser<>(EntityArgument.players()))
      .build();

    this.registerPingTargetsCommand(selectorArgument, this::handlePingTargets);
  }

  private void handlePingTargets(final @NonNull CommandContext<Commander> context) {
    final EntitySelector target = context.get("target");
    List<User<?>> users;
    try {
      users = target.findPlayers((CommandSourceStack) context.get(FabricCommandContextKeys.NATIVE_COMMAND_SOURCE)).stream()
        .map(this.tabTPSFabric.userService()::user)
        .collect(Collectors.toList());
    } catch (final CommandSyntaxException e) {
      users = Collections.emptyList();
    }
    if (users.isEmpty()) {
      throw CommandCompletedException.withMessage(LinearComponents.linear(
        Constants.PREFIX,
        Component.space(),
        Component.translatable(
          "argument.entity.notfound.player",
          NamedTextColor.RED
        )
      ));
    }
    this.pingTargets(
      context.getSender(),
      users,
      "", // todo: use selector input string here and remove above special case
      context.get("page")
    );
  }
}
