/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.serverlist;

import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IGeneralDataObject;
import io.github.nucleuspowered.nucleus.util.GeAnTyRefTypeTokens;
import io.github.nucleuspowered.storage.dataobjects.keyed.DataKey;

import java.time.Instant;

public final class ServerListKeys {

    public static DataKey<String, IGeneralDataObject> LINE_ONE = DataKey.of(
            null,
            GeAnTyRefTypeTokens.STRING,
            IGeneralDataObject.class,
            "lineone");

    public static DataKey<String, IGeneralDataObject> LINE_TWO = DataKey.of(
            null,
            GeAnTyRefTypeTokens.STRING,
            IGeneralDataObject.class,
            "linetwo");

    public static DataKey<Instant, IGeneralDataObject> EXPIRY = DataKey.of(
            null,
            GeAnTyRefTypeTokens.INSTANT,
            IGeneralDataObject.class,
            "expiry");
}
