/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.storage;

import com.google.common.collect.ImmutableMap;
import io.github.nucleuspowered.nucleus.api.module.kit.data.Kit;
import io.github.nucleuspowered.nucleus.modules.kit.serialiser.SingleKitTypeSerilaiser;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.configurate.AbstractConfigurateBackedDataObject;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class KitDataObject extends AbstractConfigurateBackedDataObject implements IKitDataObject {

    private ImmutableMap<String, Kit> cached;

    @Override
    public ImmutableMap<String, Kit> getKitMap() {
        if (this.cached == null) {
            try {
                final Map<String, Kit> map = SingleKitTypeSerilaiser.INSTANCE.deserialize(this.backingNode);
                if (map == null) {
                    this.cached = ImmutableMap.of();
                } else {
                    this.cached = ImmutableMap.copyOf(map);
                }
            } catch (final Exception e) {
                e.printStackTrace();
                return ImmutableMap.of();
            }
        }
        return this.cached;
    }

    @Override
    public void setKitMap(final Map<String, Kit> map) throws Exception {
        SingleKitTypeSerilaiser.INSTANCE.serialize(map, this.backingNode);
        this.cached = ImmutableMap.copyOf(map);
    }

    @Override
    public boolean hasKit(final String name) {
        return this.getKitMap().containsKey(name.toLowerCase());
    }

    @Override
    public Optional<Kit> getKit(final String name) {
        return Optional.ofNullable(this.getKitMap().get(name.toLowerCase()));
    }

    @Override
    public void setKit(final Kit kit) throws Exception {
        final Map<String, Kit> m = new HashMap<>(this.getKitMap());
        m.put(kit.getName().toLowerCase(), kit);
        this.setKitMap(m);
    }

    @Override
    public boolean removeKit(final String name) throws Exception {
        final Map<String, Kit> m = new HashMap<>(this.getKitMap());
        final boolean b = m.remove(name.toLowerCase()) != null;
        this.setKitMap(m);
        return b;
    }

    @Override
    public void setBackingNode(final ConfigurationNode node) {
        super.setBackingNode(node);
        this.cached = null;
    }

}