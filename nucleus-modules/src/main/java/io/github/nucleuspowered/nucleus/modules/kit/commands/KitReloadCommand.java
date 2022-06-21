/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import io.github.nucleuspowered.nucleus.modules.kit.KitPermissions;
import io.github.nucleuspowered.nucleus.modules.kit.services.KitService;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.core.scaffold.command.annotation.Command;
import org.spongepowered.api.command.exception.CommandException;
import java.util.concurrent.CompletableFuture;

@Command(
        aliases = { "reload" },
        basePermission = KitPermissions.BASE_KIT_RELOAD,
        commandDescriptionKey = "kit.reload",
        parentCommand = KitCommand.class
)
public class KitReloadCommand implements ICommandExecutor {

    @Override
    public ICommandResult execute(final ICommandContext context) throws CommandException {
        final CompletableFuture<Void> res = context.getServiceCollection().getServiceUnchecked(KitService.class).getKitStorageService().reload();
        res.whenComplete((v, e) -> {
            if (e == null) {
                context.sendMessage("command.kit.reload.success");
            } else {
                context.sendMessage("command.kit.reload.fail", e.getMessage());
                e.printStackTrace();
            }
        });
        return context.successResult();
    }
}
