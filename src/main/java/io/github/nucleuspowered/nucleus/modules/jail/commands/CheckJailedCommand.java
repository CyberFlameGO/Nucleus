/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.NamedLocation;
import io.github.nucleuspowered.nucleus.argumentparsers.JailArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

@Permissions
@RunAsync
@NoModifiers
@RegisterCommand("checkjailed")
@NonnullByDefault
public class CheckJailedCommand extends AbstractCommand<CommandSource> {

    private final String jailNameKey = "jail name";
    private final JailHandler handler;

    @Inject
    public CheckJailedCommand(JailHandler handler) {
        this.handler = handler;
    }

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optional(
                new JailArgument(Text.of(jailNameKey), handler)
            )
        };
    }

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        // Using the cache, tell us who is jailed.
        MessageProvider provider = plugin.getMessageProvider();
        Optional<NamedLocation> jail = args.getOne(jailNameKey);
        List<UUID> usersInJail = jail.map(x -> plugin.getUserCacheService().getJailedIn(x.getName()))
                .orElseGet(() -> plugin.getUserCacheService().getJailed());
        String jailName = jail.map(NamedLocation::getName).orElseGet(() -> provider.getMessageWithFormat("standard.alljails"));

        if (usersInJail.isEmpty()) {
            src.sendMessage(provider.getTextMessageWithFormat("command.checkjailed.none", jailName));
            return CommandResult.success();
        }

        // Get the users in this jail, or all jails
        Util.getPaginationBuilder(src)
            .title(provider.getTextMessageWithFormat("command.checkjailed.header", jailName))
            .contents(usersInJail.stream().map(x -> {
                Text name = plugin.getNameUtil().getName(x).orElseGet(() -> Text.of("unknown: ", x.toString()));
                return name.toBuilder()
                    .onHover(TextActions.showText(provider.getTextMessageWithFormat("command.checkjailed.hover")))
                    .onClick(TextActions.runCommand("/nucleus:checkjail " + x.toString()))
                    .build();
            }).collect(Collectors.toList())).sendTo(src);
        return CommandResult.success();
    }
}
