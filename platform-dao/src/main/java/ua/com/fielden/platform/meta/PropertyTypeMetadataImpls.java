package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.utils.EntityUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import static java.util.stream.Collectors.joining;

/**
 * Implementations of PropertyTypeMetadata.
 */
public final class PropertyTypeMetadataImpls {}


record PrimitivePropertyTypeMetadata(Class<?> javaType)
        implements PropertyTypeMetadata.Primitive
{
    @Override
    public String toString() {
        return "PrimitiveType(%s)".formatted(javaType.getTypeName());
    }
}

record EntityPropertyTypeMetadata(Class<? extends AbstractEntity<?>> javaType)
        implements PropertyTypeMetadata.Entity
{

    @Override
    public Class<? extends AbstractEntity<?>> genericJavaType() {
        return javaType;
    }

    @Override
    public String toString() {
        return "EntityType(%s)".formatted(javaType.getTypeName());
    }

}

record ParameterizedEntityPropertyTypeMetadata(ParameterizedType genericJavaType)
        implements PropertyTypeMetadata.Entity
{

    public static final String ERR_NOT_AN_ENTITY_TYPE = "Invalid parameterised entity type [%s]. Raw type [%s] is not an entity type.";

    ParameterizedEntityPropertyTypeMetadata {
        if (!(genericJavaType.getRawType() instanceof Class<?> klass && EntityUtils.isEntityType(klass))) {
            throw new InvalidArgumentException(ERR_NOT_AN_ENTITY_TYPE.formatted(
                    genericJavaType.getTypeName(), genericJavaType.getRawType().getTypeName()));
        }
    }

    @Override
    public Class<? extends AbstractEntity<?>> javaType() {
        return (Class<? extends AbstractEntity<?>>) genericJavaType.getRawType();
    }

    @Override
    public String toString() {
        return "EntityType(%s<%s>)"
                .formatted(genericJavaType.getRawType().getTypeName(),
                           Arrays.stream(genericJavaType.getActualTypeArguments())
                                   .map(Type::getTypeName)
                                   .collect(joining(", ")));
    }

}

record CollectionalPropertyTypeMetadata(Class<?> collectionType, PropertyTypeMetadata elementType)
        implements PropertyTypeMetadata.Collectional
{
    @Override
    public String toString() {
        return "CollectionalType(%s %s)".formatted(collectionType.getTypeName(), elementType);
    }
}

record ComponentPropertyTypeMetadata(Class<?> javaType)
        implements PropertyTypeMetadata.Component
{

    @Override
    public Class<?> genericJavaType() {
        return javaType;
    }

    @Override
    public String toString() {
        return "ComponentType(%s)".formatted(javaType.getTypeName());
    }

}

