package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;

/* Implementations of PropertyTypeMetadata.
 */

final class PropertyTypeMetadataImpls {}

record PrimitivePropertyTypeMetadata(Class<?> javaType) implements PropertyTypeMetadata.Primitive {
    @Override
    public String toString() {
        return "PrimitiveType(%s)".formatted(javaType.getTypeName());
    }
}

record EntityPropertyTypeMetadata(Class<? extends AbstractEntity<?>> javaType) implements PropertyTypeMetadata.Entity {
    @Override
    public String toString() {
        return "EntityType(%s)".formatted(javaType.getTypeName());
    }
}

record CollectionalPropertyTypeMetadata
        (Class<?> collectionType, PropertyTypeMetadata elementType)
        implements PropertyTypeMetadata.Collectional
{
    @Override
    public String toString() {
        return "CollectionalType(%s %s)".formatted(collectionType.getTypeName(), elementType);
    }
}

record CompositePropertyTypeMetadata(Class<?> javaType) implements PropertyTypeMetadata.Composite {
    @Override
    public String toString() {
        return "CompositeType(%s)".formatted(javaType.getTypeName());
    }

}