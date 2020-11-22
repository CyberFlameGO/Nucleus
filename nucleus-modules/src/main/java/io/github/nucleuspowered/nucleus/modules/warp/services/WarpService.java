/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.services;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.nucleuspowered.nucleus.api.module.warp.NucleusWarpService;
import io.github.nucleuspowered.nucleus.api.module.warp.data.Warp;
import io.github.nucleuspowered.nucleus.api.module.warp.data.WarpCategory;
import io.github.nucleuspowered.nucleus.modules.warp.WarpKeys;
import io.github.nucleuspowered.nucleus.modules.warp.data.WarpCategoryData;
import io.github.nucleuspowered.nucleus.modules.warp.data.WarpData;
import io.github.nucleuspowered.nucleus.modules.warp.parameters.WarpCategoryParameter;
import io.github.nucleuspowered.nucleus.modules.warp.parameters.WarpParameter;
import io.github.nucleuspowered.nucleus.scaffold.service.ServiceBase;
import io.github.nucleuspowered.nucleus.scaffold.service.annotations.APIService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IGeneralDataObject;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Singleton
@APIService(NucleusWarpService.class)
public class WarpService implements NucleusWarpService, ServiceBase {

    public static final String WARP_KEY = "warp";
    public static final String WARP_CATEGORY_KEY = "warp category";

    @Nullable private Map<String, Warp> warpCache = null;
    @Nullable private Map<String, WarpCategory> warpCategoryCache = null;
    @Nullable private List<Warp> uncategorised = null;
    private final Map<String, List<Warp>> categoryCollectionMap = new HashMap<>();

    private final INucleusServiceCollection serviceCollection;

    private final Parameter.Value<Warp> warpPermissionArgument;
    private final Parameter.Value<Warp> warpNoPermissionArgument;
    private final Parameter.Value<WarpCategory> warpCategoryParameter;

    @Inject
    public WarpService(final INucleusServiceCollection serviceCollection) {
        this.serviceCollection = serviceCollection;
        this.warpPermissionArgument = Parameter.builder(Warp.class)
                .parser(new WarpParameter(
                    serviceCollection.permissionService(),
                    serviceCollection.messageProvider(),
                    this,
                    true))
                .setKey("warp")
                .build();
        this.warpNoPermissionArgument = Parameter.builder(Warp.class)
                .parser(new WarpParameter(
                        serviceCollection.permissionService(),
                        serviceCollection.messageProvider(),
                        this,
                        false))
                .setKey("warp")
                .build();
        this.warpCategoryParameter = Parameter.builder(WarpCategory.class)
                .setKey("category")
                .parser(new WarpCategoryParameter(
                        serviceCollection,
                        this))
                .build();
    }

    private Map<String, Warp> getWarpCache() {
        if (this.warpCache == null) {
            this.updateCache();
        }

        return this.warpCache;
    }

    private Map<String, WarpCategory> getWarpCategoryCache() {
        if (this.warpCategoryCache == null) {
            this.updateCache();
        }

        return this.warpCategoryCache;
    }

    private void updateCache() {
        this.categoryCollectionMap.clear();
        this.warpCache = new HashMap<>();
        this.warpCategoryCache = new HashMap<>();
        this.uncategorised = null;
        final IGeneralDataObject dataObject =
                this.serviceCollection
                        .storageManager()
                        .getGeneralService()
                        .getOrNewOnThread();

        dataObject.get(WarpKeys.WARP_NODES)
                .orElseGet(ImmutableMap::of)
                .forEach((key, value) -> this.warpCache.put(key.toLowerCase(), value));

                this.warpCategoryCache.putAll(dataObject.get(WarpKeys.WARP_CATEGORIES)
                                .orElseGet(ImmutableMap::of));
    }

    private void saveFromCache() {
        if (this.warpCache == null || this.warpCategoryCache == null) {
            return; // not loaded
        }

        final IGeneralDataObject dataObject =
                this.serviceCollection
                        .storageManager()
                        .getGeneralService()
                        .getOrNewOnThread();
        dataObject.set(WarpKeys.WARP_NODES, new HashMap<>(this.warpCache));
        dataObject.set(WarpKeys.WARP_CATEGORIES, new HashMap<>(this.warpCategoryCache));
        this.serviceCollection.storageManager().getGeneralService().save(dataObject);
    }

    public Parameter.Value<Warp> warpElement(final boolean requirePermission) {
        if (requirePermission) {
            return this.warpPermissionArgument;
        } else {
            return this.warpNoPermissionArgument;
        }
    }

    public Parameter.Value<WarpCategory> warpCategoryElement() {
        return this.warpCategoryParameter;
    }

    @Override
    public Optional<Warp> getWarp(final String warpName) {
        return Optional.ofNullable(this.getWarpCache().get(warpName.toLowerCase()));
    }

    @Override
    public boolean removeWarp(final String warpName) {
        if (this.getWarpCache().remove(warpName.toLowerCase()) != null) {
            this.saveFromCache();
            return true;
        }

        return false;
    }

    @Override
    public boolean setWarp(final String warpName, final ServerLocation location, final Vector3d rotation) {
        final Map<String, Warp> cache = this.getWarpCache();
        final String key = warpName.toLowerCase();
        if (!cache.containsKey(key)) {
            cache.put(key, new WarpData(
                    null,
                    0,
                    null,
                    location.getWorldKey(),
                    location.getPosition(),
                    rotation,
                    warpName
            ));

            this.saveFromCache();
            return true;
        }

        return false;
    }

    @Override
    public List<Warp> getAllWarps() {
        return ImmutableList.copyOf(this.getWarpCache().values());
    }

    @Override
    public List<Warp> getUncategorisedWarps() {
        if (this.uncategorised == null) {
            this.uncategorised = this.getAllWarps()
                    .stream()
                    .filter(x -> !x.getCategory().isPresent())
                    .collect(Collectors.toList());
        }

        return ImmutableList.copyOf(this.uncategorised);
    }

    @Override
    public List<Warp> getWarpsForCategory(final String category) {
        final List<Warp> warps = this.categoryCollectionMap.computeIfAbsent(category.toLowerCase(),
                c -> Lists.newArrayList(this.getAllWarps().stream().filter(x ->
                        x.getCategory().map(cat -> cat.equalsIgnoreCase(c)).orElse(false))
                        .collect(Collectors.toList())));
        return ImmutableList.copyOf(warps);
    }

    public Map<WarpCategory, List<Warp>> getWarpsWithCategories() {
        return this.getWarpsWithCategories(t -> true);
    }

    @Override
    public Map<WarpCategory, List<Warp>> getWarpsWithCategories(final Predicate<Warp> warpDataPredicate) {
        // Populate cache
        final Map<WarpCategory, List<Warp>> map = new HashMap<>();
        this.getWarpCategoryCache().keySet().forEach(x -> {
            final List<Warp> warps = this.getWarpsForCategory(x).stream().filter(warpDataPredicate).collect(Collectors.toList());
            if (!warps.isEmpty()) {
                map.put(this.getWarpCategoryCache().get(x.toLowerCase()), warps);
            }
        });
        return map;
    }

    @Override
    public boolean removeWarpCost(final String warpName) {
        final Optional<Warp> warp = this.getWarp(warpName);
        if (warp.isPresent()) {
            final Warp w = warp.get();
            this.removeWarp(warpName);
            this.getWarpCache().put(w.getName().toLowerCase(), new WarpData(
                    w.getCategory().orElse(null),
                    0,
                    w.getDescription().orElse(null),
                    w.getResourceKey(),
                    w.getPosition(),
                    w.getRotation(),
                    w.getName()
            ));
            this.saveFromCache();
            return true;
        }
        return false;
    }

    @Override
    public boolean setWarpCost(final String warpName, final double cost) {
        if (cost < 0) {
            return false;
        }

        final Optional<Warp> warp = this.getWarp(warpName);
        if (warp.isPresent()) {
            final Warp w = warp.get();
            this.removeWarp(warpName);
            this.getWarpCache().put(w.getName().toLowerCase(), new WarpData(
                    w.getCategory().orElse(null),
                    cost,
                    w.getDescription().orElse(null),
                    w.getResourceKey(),
                    w.getPosition(),
                    w.getRotation(),
                    w.getName()
            ));
            this.saveFromCache();
            return true;
        }
        return false;
    }

    @Override
    public boolean setWarpCategory(final String warpName, @Nullable String category) {
        if (category != null) {
            final Optional<WarpCategory> c = this.getWarpCategory(category);
            if (!c.isPresent()) {
                final WarpCategory wc = new WarpCategoryData(
                        category,
                        null,
                        null);
                this.getWarpCategoryCache().put(category.toLowerCase(), wc);
            } else {
                this.categoryCollectionMap.remove(category.toLowerCase());
            }

            category = category.toLowerCase();
        } else {
            this.uncategorised = null;
        }

        final Optional<Warp> warp = this.getWarp(warpName);
        if (warp.isPresent()) {
            final Warp w = warp.get();
            this.removeWarp(warpName);
            this.getWarpCache().put(w.getName().toLowerCase(), new WarpData(
                    category,
                    w.getCost().orElse(0d),
                    w.getDescription().orElse(null),
                    w.getResourceKey(),
                    w.getPosition(),
                    w.getRotation(),
                    w.getName()
            ));
            this.saveFromCache();
            return true;
        }
        return false;
    }

    @Override
    public boolean setWarpDescription(final String warpName, @Nullable final Component description) {
        final Optional<Warp> warp = this.getWarp(warpName);
        if (warp.isPresent()) {
            final Warp w = warp.get();
            this.removeWarp(warpName);
            this.getWarpCache().put(w.getName().toLowerCase(), new WarpData(
                    w.getCategory().orElse(null),
                    w.getCost().orElse(0d),
                    description,
                    w.getResourceKey(),
                    w.getPosition(),
                    w.getRotation(),
                    w.getName()
            ));
            this.saveFromCache();
            return true;
        }
        return false;
    }

    @Override
    public Set<String> getWarpNames() {
        return this.getWarpCache().keySet();
    }

    @Override
    public Optional<WarpCategory> getWarpCategory(final String category) {
        return Optional.ofNullable(this.getWarpCategoryCache().get(category.toLowerCase()));
    }

    @Override
    public boolean setWarpCategoryDisplayName(final String category, @Nullable final Component displayName) {
        final Optional<WarpCategory> c = this.getWarpCategory(category);
        if (c.isPresent()) {
            final WarpCategory cat = c.get();
            this.getWarpCategoryCache().remove(category.toLowerCase());
            this.getWarpCategoryCache().put(category.toLowerCase(), new WarpCategoryData(
                    cat.getId(),
                    displayName,
                    cat.getDescription().orElse(null)
            ));
            this.saveFromCache();
            return true;
        }

        return false;
    }

    @Override
    public boolean setWarpCategoryDescription(final String category, @Nullable final Component description) {
        final Optional<WarpCategory> c = this.getWarpCategory(Objects.requireNonNull(category));
        if (c.isPresent()) {
            final WarpCategory cat = c.get();
            this.getWarpCategoryCache().remove(category.toLowerCase());
            this.getWarpCategoryCache().put(category.toLowerCase(), new WarpCategoryData(
                    cat.getId(),
                    cat.getDisplayName(),
                    description
            ));
            this.saveFromCache();
            return true;
        }

        return false;
    }
}
