/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.handlers;

import com.flowpowered.math.vector.Vector3d;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.data.JailData;
import io.github.nucleuspowered.nucleus.api.data.WarpLocation;
import io.github.nucleuspowered.nucleus.api.service.NucleusJailService;
import io.github.nucleuspowered.nucleus.config.GeneralDataStore;
import io.github.nucleuspowered.nucleus.internal.interfaces.InternalNucleusUser;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;

public class JailHandler implements NucleusJailService {

    @Inject private GeneralDataStore store;

    private final Nucleus plugin;

    public JailHandler(Nucleus plugin) {
        this.plugin = plugin;
    }

    @Override
    public Optional<WarpLocation> getJail(String warpName) {
        return store.getJailLocation(warpName);
    }

    @Override
    public boolean removeJail(String warpName) {
        return store.removeJail(warpName);
    }

    @Override
    public boolean setJail(String warpName, Location<World> location, Vector3d rotation) {
        return store.addJail(warpName, location, rotation);
    }

    @Override
    public Map<String, WarpLocation> getJails() {
        return store.getJails();
    }

    @Override
    public boolean isPlayerJailed(User user) {
        return getPlayerJailData(user).isPresent();
    }

    @Override
    public Optional<JailData> getPlayerJailData(User user) {
        try {
            return plugin.getUserLoader().getUser(user).getJailData();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public boolean jailPlayer(User user, JailData data) {
        InternalNucleusUser iqsu;
        try {
            iqsu = plugin.getUserLoader().getUser(user);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (iqsu.getJailData().isPresent()) {
            return false;
        }

        // Get the jail.
        Optional<WarpLocation> owl = getJail(data.getJailName());
        WarpLocation wl = owl.orElseGet(() -> {
            if (!getJails().isEmpty()) {
                return null;
            }

            return getJails().entrySet().stream().findFirst().get().getValue();
        });

        if (wl == null) {
            return false;
        }

        iqsu.setJailData(data);
        if (user.isOnline()) {
            Sponge.getScheduler().createSyncExecutor(plugin).execute(() -> {
                Player player = user.getPlayer().get();
                player.setLocationAndRotation(owl.get().getLocation(), owl.get().getRotation());
                iqsu.setFlying(false);
            });
        } else {
            iqsu.setJailOnNextLogin(true);
        }

        return true;
    }

    @Override
    public boolean unjailPlayer(User user) {
        InternalNucleusUser iqsu;
        try {
            iqsu = plugin.getUserLoader().getUser(user);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        Optional<JailData> ojd = iqsu.getJailData();
        if (!ojd.isPresent()) {
            return false;
        }

        Optional<Location<World>> ow = ojd.get().getPreviousLocation();
        iqsu.removeJailData();
        if (user.isOnline()) {
            Player player = user.getPlayer().get();
            Sponge.getScheduler().createSyncExecutor(plugin).execute(() -> {
                player.setLocation(ow.isPresent() ? ow.get() : player.getWorld().getSpawnLocation());
                player.sendMessage(Util.getTextMessageWithFormat("jail.elapsed"));
            });
        } else {
            iqsu.sendToLocationOnLogin(ow.isPresent() ? ow.get()
                    : new Location<>(Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorld().get().getUniqueId()).get(),
                            Sponge.getServer().getDefaultWorld().get().getSpawnPosition()));
        }

        return true;
    }

    public Optional<WarpLocation> getWarpLocation(Player user) {
        if (!isPlayerJailed(user)) {
            return Optional.empty();
        }

        Optional<WarpLocation> owl = getJail(getPlayerJailData(user).get().getJailName());
        if (!owl.isPresent()) {
            Collection<WarpLocation> wl = getJails().values();
            if (wl.isEmpty()) {
                return Optional.empty();
            }

            owl = wl.stream().findFirst();
        }

        return owl;
    }
}
