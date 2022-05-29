/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.core.services.impl.chatmessageformatter;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import io.github.nucleuspowered.nucleus.api.util.NoExceptionAutoClosable;
import io.github.nucleuspowered.nucleus.core.services.interfaces.IChatMessageFormatterService;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

@Singleton
public class ChatMessageFormatterService implements IChatMessageFormatterService {

    private final Map<UUID, Channel> chatChannels = new HashMap<>();
    private final Map<Audience, Channel> sourceChannelMap = new WeakHashMap<>();

    @Override
    public Optional<Channel> getNucleusChannel(final Audience source) {
        if (source instanceof ServerPlayer) {
            return this.getNucleusChannel(((ServerPlayer) source).uniqueId());
        }
        return Optional.ofNullable(this.sourceChannelMap.get(source));
    }

    @Override
    public Optional<Channel> getNucleusChannel(final UUID uuid) {
        return Optional.ofNullable(this.chatChannels.get(uuid));
    }

    @Override
    public void setPlayerNucleusChannel(final UUID uuid, @Nullable final Channel channel) {
        if (channel == null) {
            this.chatChannels.remove(uuid);
        } else {
            this.chatChannels.put(uuid, channel);
        }
    }

    @Override
    public NoExceptionAutoClosable setAudienceNucleusChannelTemporarily(final Audience audience, final Channel channel) {
        if (audience instanceof ServerPlayer) {
            return this.setPlayerNucleusChannelTemporarily(((ServerPlayer) audience).uniqueId(), channel);
        }
        Preconditions.checkNotNull(channel);
        this.sourceChannelMap.put(audience, channel);
        return () -> {
            this.sourceChannelMap.remove(audience);
           // Sponge.server().getConsole().setMessageChannel(originalChannel);
        };
    }

    @Override
    public NoExceptionAutoClosable setPlayerNucleusChannelTemporarily(final UUID uuid, final Channel channel) {
        Preconditions.checkNotNull(channel);
        final Channel original = this.chatChannels.get(uuid);
        this.chatChannels.put(uuid, channel);
        return () -> {
            this.setPlayerNucleusChannel(uuid, original);
        };
    }

}
