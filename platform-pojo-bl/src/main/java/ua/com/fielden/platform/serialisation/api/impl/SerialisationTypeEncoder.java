package ua.com.fielden.platform.serialisation.api.impl;

import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.serialisation.api.ISerialisationTypeEncoder;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;

/// A fallback implementation to be used when Web UI modules are not available or necessary.
///
@Singleton
public class SerialisationTypeEncoder implements ISerialisationTypeEncoder {

    @Override
    public <T extends AbstractEntity<?>> Class<T> decode(final String entityTypeName) {
        return (Class<T>) ClassesRetriever.findClass(entityTypeName);
    }

    @Override
    public ISerialisationTypeEncoder setTgJackson(final ISerialiserEngine tgJackson) {
        return this;
    }

}
