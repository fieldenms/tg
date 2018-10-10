package ua.com.fielden.platform.serialisation.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.serialisation.api.ISerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;

public class SerialisationTypeEncoder implements ISerialisationTypeEncoder {
    @Override
    public <T extends AbstractEntity<?>> String encode(final Class<T> entityType) {
        return entityType.getName();
    }

    @Override
    public <T extends AbstractEntity<?>> Class<T> decode(final String entityTypeId) {
        final String entityTypeName = entityTypeId;
        final Class<T> decodedEntityType = (Class<T>) ClassesRetriever.findClass(entityTypeName);
        return decodedEntityType;
    }

    @Override
    public ISerialisationTypeEncoder setTgJackson(final ISerialiserEngine tgJackson) {
        return this;
    }
}
