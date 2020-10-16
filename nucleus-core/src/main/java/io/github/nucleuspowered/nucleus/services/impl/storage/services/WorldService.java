/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.storage.services;

import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IWorldDataObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.queryobjects.IWorldQueryObject;
import io.github.nucleuspowered.nucleus.services.interfaces.IDataVersioning;
import io.github.nucleuspowered.nucleus.services.interfaces.IStorageManager;
import io.github.nucleuspowered.storage.services.AbstractKeyedService;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.plugin.PluginContainer;

public class WorldService extends AbstractKeyedService<ResourceKey, IWorldQueryObject, IWorldDataObject> {

    public WorldService(final IStorageManager repository, final PluginContainer pluginContainer, final IDataVersioning dataVersioning) {
        super(repository::getWorldDataAccess, repository::getWorldRepository, dataVersioning::migrate, dataVersioning::setVersion, pluginContainer);
    }

}
