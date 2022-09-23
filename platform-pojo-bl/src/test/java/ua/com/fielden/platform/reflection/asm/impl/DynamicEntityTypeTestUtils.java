package ua.com.fielden.platform.reflection.asm.impl;

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;

public class DynamicEntityTypeTestUtils {
    
    public static <T extends AbstractEntity<?>> T assertInstantiation(final Class<T> type, final EntityFactory factory) {
        final T instance = factory.newEntity(type);
        assertNotNull("Could not instantiate entity type %s.".formatted(type.getName()), instance);
        return instance;
    }

}
