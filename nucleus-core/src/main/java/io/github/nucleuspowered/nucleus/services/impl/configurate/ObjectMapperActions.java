package io.github.nucleuspowered.nucleus.services.impl.configurate;

import io.github.nucleuspowered.nucleus.services.interfaces.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.interfaces.annotation.configuratehelper.DefaultValueSetting;
import io.github.nucleuspowered.nucleus.services.interfaces.annotation.configuratehelper.LocalisedComment;
import org.spongepowered.configurate.CommentedConfigurationNodeIntermediary;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;
import org.spongepowered.configurate.objectmapping.meta.Processor;

public final class ObjectMapperActions {

    static Processor.Factory<LocalisedComment, Object> localisedComments(final IMessageProviderService source) {
        return (data, fieldType) -> {
            final String translated = source.getMessageString(data.value(), (Object[]) data.replacements());
            return (value, destination) -> {
                if (destination instanceof CommentedConfigurationNodeIntermediary<?>) {
                    final CommentedConfigurationNodeIntermediary<?> commented = (CommentedConfigurationNodeIntermediary<?>) destination;
                    commented.setComment(translated);
                }
            };
        };
    }

    static NodeResolver.Factory defaultValue() {
        return (name, element) -> {
            if (element.isAnnotationPresent(DefaultValueSetting.class)) {
                return node -> {
                    final ConfigurationNode node1 = node.getNode(node.getKey());
                    if (node1.isVirtual()) {
                        node1.setValue(element.getAnnotation(DefaultValueSetting.class).defaultValue());
                    }
                    return node1;
                };
            }
            return null;
        };
    }

}