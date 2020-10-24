/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.core.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.core.CorePermissions;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.scaffold.command.control.CommandControl;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.interfaces.IMessageProviderService;
import io.github.nucleuspowered.nucleus.util.AdventureUtils;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Command(
        aliases = "commandinfo",
        basePermission = CorePermissions.BASE_COMMANDINFO,
        commandDescriptionKey = "commandinfo",
        hasHelpCommand = false
)
public class CommandInfoCommand implements ICommandExecutor {

    private final Parameter.Value<CommandMapping> commandParameter;

    public CommandInfoCommand(final IMessageProviderService messageProviderService) {
        this.commandParameter = Parameter.builder(CommandMapping.class)
                .setKey("command")
                .parser(new CommandChoicesArgument(messageProviderService))
                .build();
    }

    @Override
    public Parameter[] parameters(final INucleusServiceCollection serviceCollection) {
        return new Parameter[] {
                this.commandParameter
        };
    }

    @Override
    public ICommandResult execute(final ICommandContext context) throws CommandException {
        // we have the command, get the mapping
        final CommandMapping mapping = context.requireOne(this.commandParameter);

        final IMessageProviderService provider = context.getServiceCollection().messageProvider();
        final Component header = context.getMessage("command.commandinfo.title", mapping.getPrimaryAlias());

        final List<Component> content = new ArrayList<>();

        // Owner
        final PluginContainer owner = mapping.getPlugin();
        content.add(provider.getMessage("command.commandinfo.owner", owner.getMetadata().getName()
                .orElseGet(() -> context.getMessageString("standard.unknown")) + " (" + owner.getMetadata().getId() + ")"));
        content.add(context.getMessage("command.commandinfo.aliases", String.join(", ", mapping.getAllAliases())));

        if (mapping.getPlugin().equals(context.getServiceCollection().pluginContainer())) {
            // we did it, do we have a control for it?
            this.nucleusCommand(content, context, provider, mapping);
        } else {
            this.lowCommand(content, context, provider, mapping);
        }

        Util.getPaginationBuilder(context.getAudience())
                .title(header)
                .contents(content)
                .sendTo(context.getAudience());
        return context.successResult();
    }

    private void nucleusCommand(
            final List<Component> content,
            final ICommandContext context,
            final IMessageProviderService provider,
            final CommandMapping mapping) throws CommandException {
        content.add(context.getMessage("command.commandinfo.type", "loc:command.commandinfo.nucleus"));
        content.add(Component.empty());
        final CommandControl control = context.getServiceCollection().commandMetadataService().getControl(mapping.getPrimaryAlias());
        final Component text = control.getUsage(context);
        if (AdventureUtils.isEmpty(text)) {
            content.add(context.getMessage("command.commandinfo.noinfo"));
        } else {
            content.add(text);
        }
    }

    private void lowCommand(
            final List<Component> content,
            final ICommandContext context,
            final IMessageProviderService provider,
            final CommandMapping mapping) throws CommandException {
        content.add(context.getMessage("command.commandinfo.type", "loc:command.commandinfo.callable"));
        content.add(Component.empty());
        final Optional<Component> help = mapping.getRegistrar().help(context.getCause(), mapping);
        help.ifPresent(content::add);
    }

    private static class CommandChoicesArgument implements ValueParameter<CommandMapping> {

        private final IMessageProviderService messageProviderService;

        CommandChoicesArgument(final IMessageProviderService messageProviderService) {
            this.messageProviderService = messageProviderService;
        }

        @Override
        public List<String> complete(final CommandContext context, final String string) {
            return new ArrayList<>(Sponge.getCommandManager().suggest(context.getSubject(), context.getCause().getAudience(), string));
        }

        @Override
        public Optional<? extends CommandMapping> getValue(
                final Parameter.@NonNull Key<? super CommandMapping> parameterKey,
                final ArgumentReader.@NonNull Mutable reader,
                final CommandContext.@NonNull Builder context) throws ArgumentParseException {
            final String next = reader.parseString();
            final Optional<CommandMapping> commandMapping = Sponge.getCommandManager().getCommandMapping(next);
            if (commandMapping.filter(x -> x.getRegistrar().canExecute(context.getCause(), x)).isPresent()) {
                return commandMapping;
            }
            throw reader.createException(this.messageProviderService.getMessageFor(context.getCause().getAudience(), "command.commandinfo.nocommand", next));
        }
    }
}