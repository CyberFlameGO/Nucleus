/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.teleport.data.TeleportResult;
import io.github.nucleuspowered.nucleus.api.teleport.data.TeleportScanners;
import io.github.nucleuspowered.nucleus.modules.teleport.TeleportPermissions;
import io.github.nucleuspowered.nucleus.modules.teleport.config.TeleportConfig;
import io.github.nucleuspowered.nucleus.modules.teleport.services.PlayerTeleporterService;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandContext;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.core.scaffold.command.ICommandResult;
import io.github.nucleuspowered.nucleus.core.scaffold.command.annotation.Command;
import io.github.nucleuspowered.nucleus.core.scaffold.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.core.scaffold.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.core.scaffold.command.modifier.CommandModifiers;
import io.github.nucleuspowered.nucleus.core.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.core.services.interfaces.IPermissionService;
import io.github.nucleuspowered.nucleus.core.services.interfaces.IReloadableService;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Optional;
import java.util.function.Supplier;

@EssentialsEquivalent(value = {"tp", "tele", "tp2p", "teleport", "tpo"}, isExact = false,
        notes = "If you have permission, this will override '/tptoggle' automatically.")
@Command(
        aliases = {"teleport", "tele", "$tp"},
        basePermission = TeleportPermissions.BASE_TELEPORT,
        commandDescriptionKey = "teleport",
        modifiers = {
                @CommandModifier(
                        value = CommandModifiers.HAS_WARMUP,
                        exemptPermission = TeleportPermissions.EXEMPT_WARMUP_TELEPORT
                ),
                @CommandModifier(
                        value = CommandModifiers.HAS_COOLDOWN,
                        exemptPermission = TeleportPermissions.EXEMPT_COOLDOWN_TELEPORT
                ),
                @CommandModifier(
                        value = CommandModifiers.HAS_COST,
                        exemptPermission = TeleportPermissions.EXEMPT_COST_TELEPORT
                )
        },
        associatedPermissions = {
                TeleportPermissions.TELEPORT_OFFLINE,
                TeleportPermissions.TELEPORT_QUIET,
                TeleportPermissions.OTHERS_TELEPORT,
                TeleportPermissions.TPTOGGLE_EXEMPT
        }
)
public class TeleportCommand implements ICommandExecutor, IReloadableService.Reloadable {

    private final Parameter.Value<User> userToWarp;
    private final Parameter.Value<ServerPlayer> playerToWarp;

    private final Parameter.Value<User> userToWarpTo;
    private final Parameter.Value<ServerPlayer> playerToWarpTo;

    private final Parameter.Value<Boolean> quietOption = Parameter.bool().setKey("quiet").orDefault(true).build();

    private boolean isDefaultQuiet = false;

    @Inject
    public TeleportCommand(final INucleusServiceCollection serviceCollection) {
        final IPermissionService permissionService = serviceCollection.permissionService();
        this.userToWarp = Parameter.user()
                .setKey("Offline player to warp")
                .setRequirements(cause ->
                        permissionService.hasPermission(cause, TeleportPermissions.OTHERS_TELEPORT) &&
                                permissionService.hasPermission(cause, TeleportPermissions.TELEPORT_OFFLINE))
                .build();
        this.playerToWarp = Parameter.player()
                .setKey("Player to warp")
                .setRequirements(cause -> permissionService.hasPermission(cause, TeleportPermissions.OTHERS_TELEPORT))
                .build();

        this.userToWarpTo = Parameter.user()
                .setKey("Offline player to warp to")
                .setRequirements(cause -> permissionService.hasPermission(cause, TeleportPermissions.TELEPORT_OFFLINE))
                .build();
        this.playerToWarpTo = Parameter.player()
                .setKey("Player to warp to")
                .build();
    }

    @Override public void onReload(final INucleusServiceCollection serviceCollection) {
        this.isDefaultQuiet = serviceCollection.configProvider().getModuleConfig(TeleportConfig.class).isDefaultQuiet();
    }

    @Override
    public Flag[] flags(final INucleusServiceCollection serviceCollection) {
        return new Flag[] {
                Flag.of("f"),
                Flag.builder().alias("q")
                        .setParameter(this.quietOption)
                        .setRequirement(commandCause -> serviceCollection.permissionService().hasPermission(commandCause, TeleportPermissions.TELEPORT_QUIET))
                        .build()
        };
    }

    @Override
    public Parameter[] parameters(final INucleusServiceCollection serviceCollection) {
       return new Parameter[] {
               Parameter.firstOf(
                   Parameter.seq(this.playerToWarp, Parameter.firstOf(this.playerToWarpTo, this.userToWarpTo)), // <player from> <to>
                   Parameter.seq(this.userToWarp, Parameter.firstOf(this.playerToWarpTo, this.userToWarpTo)), // <user from> <to>
                   this.playerToWarpTo,  // <player to>
                   this.userToWarpTo // <user to>
               )
       };
    }

    @Override
    public Optional<ICommandResult> preExecute(final ICommandContext context) {
        @Nullable final User source =
                context.getOne(this.userToWarp).orElseGet(() -> context.getOne(this.playerToWarp).map(ServerPlayer::getUser).orElse(null));
        final boolean isOther = source != null && context.getUniqueId().filter(x -> !x.equals(source.getUniqueId())).isPresent();
        final User to = context.getOne(this.userToWarpTo).orElseGet(() -> context.requireOne(this.playerToWarpTo).getUser());
        return context.getServiceCollection()
                    .getServiceUnchecked(PlayerTeleporterService.class)
                    .canTeleportTo(context.getAudience(), source, to, isOther) ?
                Optional.empty() :
                Optional.of(context.failResult());
    }

    @Override public ICommandResult execute(final ICommandContext context) throws CommandException {
        @Nullable final User externalSource =
                context.getOne(this.userToWarp)
                        .orElseGet(() -> context.getOne(this.playerToWarp).map(ServerPlayer::getUser).orElse(null));
        @NonNull final User source;
        if (externalSource != null) {
            source = externalSource;
        } else {
            source = context.requirePlayer().getUser();
        }
        final boolean isOther = source != null && context.getUniqueId().filter(x -> !x.equals(source.getUniqueId())).isPresent();
        final User to = context.getOne(this.userToWarpTo).orElseGet(() -> context.requireOne(this.playerToWarpTo).getUser());

        final boolean beQuiet = context.getOne(this.quietOption).orElse(this.isDefaultQuiet);

        if (source.isOnline() && to.isOnline()) {
            final TeleportResult result =
                    context.getServiceCollection()
                        .getServiceUnchecked(PlayerTeleporterService.class)
                            .teleportWithMessage(
                                    context.getAudience(),
                                    source.getPlayer().get(),
                                    to.getPlayer().get(),
                                    !context.hasFlag("f"),
                                    false,
                                    beQuiet
                            );
            return result.isSuccessful() ? context.successResult() : context.failResult();
        }

        // We have an offline player.
        if (!context.testPermission(TeleportPermissions.TELEPORT_OFFLINE)) {
            return context.errorResult("command.teleport.noofflineperms");
        }

        // Can we get a location?
        final Supplier<CommandException> r = () -> context.createException("command.teleport.nolastknown", to.getName());

        if (!source.isOnline()) {
            if (source.setLocation(to.getWorldKey(), to.getPosition())) {
                context.sendMessage("command.teleport.offline.other", source.getName(), to.getName());
                return context.successResult();
            }
        } else {
            final ServerWorld w = Sponge.getServer().getWorldManager().getWorld(to.getWorldKey()).orElseThrow(r);
            final ServerLocation l = ServerLocation.of(w, to.getPosition());

            final boolean result = context.getServiceCollection()
                    .teleportService()
                    .teleportPlayerSmart(
                            source.getPlayer().get(),
                            l,
                            false,
                            true,
                            TeleportScanners.NO_SCAN.get()
                    ).isSuccessful();
            if (result) {
                if (isOther) {
                    context.sendMessage("command.teleport.offline.other", source.getName(), to.getName());
                }

                context.sendMessage("command.teleport.offline.self", to.getName());
                return context.successResult();
            }
        }

        return context.errorResult("command.teleport.error");
    }

}
