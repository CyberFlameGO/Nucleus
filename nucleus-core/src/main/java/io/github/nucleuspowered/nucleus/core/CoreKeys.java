/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.core;

import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IGeneralDataObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IUserDataObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IWorldDataObject;
import io.github.nucleuspowered.nucleus.util.GeAnTyRefTypeTokens;
import io.github.nucleuspowered.storage.dataobjects.keyed.DataKey;

import java.time.Instant;

public final class CoreKeys {

    @SuppressWarnings("unchecked")
    public static final DataKey<Integer, IGeneralDataObject> GENERAL_VERSION = DataKey.of(GeAnTyRefTypeTokens.INTEGER, IGeneralDataObject.class, "data_version");

    public static final DataKey<Integer, IWorldDataObject> WORLD_VERSION = DataKey.of(GeAnTyRefTypeTokens.INTEGER, IWorldDataObject.class, "data_version");

    public static final DataKey<Integer, IUserDataObject> USER_VERSION = DataKey.of(GeAnTyRefTypeTokens.INTEGER, IUserDataObject.class, "data_version");

    public static final DataKey<String, IUserDataObject> LAST_KNOWN_NAME = DataKey.of(GeAnTyRefTypeTokens.STRING, IUserDataObject.class, "lastKnownName");

    public static final DataKey<Instant, IUserDataObject> LAST_LOGIN = DataKey.of(GeAnTyRefTypeTokens.INSTANT, IUserDataObject.class, "lastLogin");

    public static final DataKey<Instant, IUserDataObject> LAST_LOGOUT = DataKey.of(GeAnTyRefTypeTokens.INSTANT, IUserDataObject.class, "lastLogout");

    public static final DataKey<String, IUserDataObject> IP_ADDRESS = DataKey.of(GeAnTyRefTypeTokens.STRING, IUserDataObject.class, "lastIP");

    @Deprecated
    public static final DataKey<Instant, IUserDataObject> FIRST_JOIN = DataKey.of(GeAnTyRefTypeTokens.INSTANT, IUserDataObject.class, "firstJoin");

    public static final DataKey<Boolean, IUserDataObject> FIRST_JOIN_PROCESSED = DataKey.of(false, GeAnTyRefTypeTokens.BOOLEAN, IUserDataObject.class, "firstJoinProcessed");
}
