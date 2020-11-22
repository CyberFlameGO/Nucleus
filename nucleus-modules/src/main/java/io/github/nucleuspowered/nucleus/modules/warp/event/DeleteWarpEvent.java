/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.event;

import io.github.nucleuspowered.nucleus.api.module.warp.data.Warp;
import io.github.nucleuspowered.nucleus.api.module.warp.event.NucleusWarpEvent;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.world.ServerLocation;

import java.util.Optional;

public class DeleteWarpEvent extends AbstractWarpEvent implements NucleusWarpEvent.Delete {

    private final Warp warp;

    public DeleteWarpEvent(final Cause cause, final Warp warp) {
        super(cause, warp.getName());
        this.warp = warp;
    }

    @Override public Warp getWarp() {
        return this.warp;
    }

    @Override public Optional<ServerLocation> getLocation() {
        return this.warp.getLocation();
    }
}
