/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.core.core.commands.nucleus;

import io.github.nucleuspowered.nucleus.core.Util;
import io.github.nucleuspowered.nucleus.core.core.CorePermissions;
import io.github.nucleuspowered.nucleus.core.core.commands.NucleusCommand;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.core.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.core.services.interfaces.ICompatibilityService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;

import java.util.Collection;
import java.util.Comparator;

@Command(
        aliases = { "compatibility", "compat" },
        basePermission = CorePermissions.BASE_NUCLEUS_COMPATIBILITY,
        commandDescriptionKey = "nucleus.compatibility",
        parentCommand = NucleusCommand.class
)
public class CompatibilityCommand implements ICommandExecutor {

    @Override
    public ICommandResult execute(final ICommandContext context) {
        final ICompatibilityService compatibilityService = context.getServiceCollection().compatibilityService();
        final Collection<ICompatibilityService.CompatibilityMessages> messages = compatibilityService.getApplicableMessages();
        if (messages.isEmpty()) {
            context.sendMessage("command.nucleus.compat.none");
            return context.successResult();
        }

        // Create pagination
        final Component text = messages.stream()
                .sorted(Comparator.comparing(x -> -x.getSeverity().getIndex()))
                .map(x -> {
                    final Component modulesAffected =
                            x.getModules().isEmpty() ?
                                    context.getMessage("command.nucleus.compat.all") :
                                    Component.text(String.join(" ,", x.getModules()));
                    return Component.join(JoinConfiguration.newlines(),
                        context.getMessage("command.nucleus.compat.severity.base",
                            "loc:command.nucleus.compat.severity." + x.getSeverity().name().toLowerCase()),
                            context.getMessage("command.nucleus.compat.modulesaffected", modulesAffected),
                            context.getMessage("command.nucleus.compat.mod", x.getModId()),
                            context.getMessage("command.nucleus.compat.symptom", x.getSeverity()),
                            context.getMessage("command.nucleus.compat.message", x.getMessage()),
                            context.getMessage("command.nucleus.compat.resolution", x.getResolution())
                    );
                })
                .reduce((text1, text2) -> Component.text()
                        .append(text1)
                        .append(Component.newline())
                        .append(Component.newline())
                        .append(text2)
                        .build())
                .orElse(Component.empty());
        Util.getPaginationBuilder(context.audience())
                .header(context.getMessage("command.nucleus.compat.header"))
                .contents(text)
                .sendTo(context.audience());
        return context.successResult();
    }
}