/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.command.ICommandContext;
import io.github.nucleuspowered.nucleus.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.command.ICommandResult;
import io.github.nucleuspowered.nucleus.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.kit.KitPermissions;
import io.github.nucleuspowered.nucleus.modules.kit.parameters.KitParameter;
import io.github.nucleuspowered.nucleus.modules.kit.services.KitService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@Command(
        aliases = { "hidden" },
        async = true,
        basePermission = KitPermissions.BASE_KIT_HIDDEN,
        commandDescriptionKey = "kit.hidden",
        parentCommand = KitCommand.class
)
public class KitHiddenCommand implements ICommandExecutor<CommandSource> {

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                serviceCollection.getServiceUnchecked(KitService.class).createKitElement(false),
                NucleusParameters.ONE_TRUE_FALSE
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        Kit kitInfo = context.requireOne(KitParameter.KIT_PARAMETER_KEY, Kit.class);
        boolean b = context.requireOne(NucleusParameters.Keys.BOOL, Boolean.class);

        // This Kit is a reference back to the version in list, so we don't need
        // to update it explicitly
        kitInfo.setHiddenFromList(b);
        context.getServiceCollection().getServiceUnchecked(KitService.class).saveKit(kitInfo);
        context.sendMessage(b ? "command.kit.hidden.off" : "command.kit.hidden.on", kitInfo.getName());

        return context.successResult();
    }
}
