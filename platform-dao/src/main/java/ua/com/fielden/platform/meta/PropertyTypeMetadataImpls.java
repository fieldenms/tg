package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.lang.reflect.Type;

/* Implementations of PropertyTypeMetadata.
 */

final class PropertyTypeMetadataImpls {}

record PrimitivePropertyTypeMetadata(Class<?> javaType) implements PropertyTypeMetadata.Primitive {}

record EntityPropertyTypeMetadata(Class<? extends AbstractEntity<?>> javaType) implements PropertyTypeMetadata.Entity {}

record CollectionalPropertyTypeMetadata
        (Class<?> collectionType, PropertyTypeMetadata elementType)
        implements PropertyTypeMetadata.Collectional {}

record CompositePropertyTypeMetadata(Class<?> javaType) implements PropertyTypeMetadata.Composite {}

record OtherPropertyTypeMetadata(Type javaType) implements PropertyTypeMetadata.Other {}
