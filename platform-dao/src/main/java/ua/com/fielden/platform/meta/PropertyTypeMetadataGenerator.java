package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import static ua.com.fielden.platform.meta.TypeRegistry.COMPONENT_TYPES;
import static ua.com.fielden.platform.meta.TypeRegistry.PRIMITIVE_PROPERTY_TYPES;

final class PropertyTypeMetadataGenerator {

    /**
     * Generates metadata for the property's type.
     */
    public Optional<PropertyTypeMetadata> generate(final Field field) {
        // start with empty() to type-check
        return Optional.<PropertyTypeMetadata>empty()
                .or(() -> PropertyTypeDeterminator.collectionalType(field)
                        .flatMap(rawType_eltType -> asCollectional(rawType_eltType._1, rawType_eltType._2)))
                .or(() -> generate(field.getGenericType()));
    }

    /**
     * Prefer {@link #generate(Field)} if the property's {@link Field} is available.
     * This method should be used only in special cases.
     * For example, it is incapable of handling collectional types, since that would require information about property's annotations.
     */
    public Optional<PropertyTypeMetadata> generate(final Type type) {
        // start with empty() to type-check
        return Optional.<PropertyTypeMetadata>empty()
                .or(() -> asPrimitive(type))
                .or(() -> asEntity(type))
                .or(() -> asComponent(type))
                .or(() -> DynamicEntityKey.class == type
                        ? Optional.of(PropertyTypeMetadata.COMPOSITE_KEY)
                        : Optional.empty())
                .or(() -> NoKey.class == type
                        ? Optional.of(PropertyTypeMetadata.NO_KEY)
                        : Optional.empty());
    }

    private Optional<PropertyTypeMetadata.Primitive> asPrimitive(final Type type) {
        return rawClass(type)
                .filter(PRIMITIVE_PROPERTY_TYPES::contains)
                .map(PrimitivePropertyTypeMetadata::new);
    }

    private Optional<PropertyTypeMetadata.Entity> asEntity(final Type type) {
        return switch (type) {
            case Class<?> klass when EntityUtils.isEntityType(klass)
                    -> Optional.of(new EntityPropertyTypeMetadata((Class<? extends AbstractEntity<?>>) klass));
            case ParameterizedType paramType when EntityUtils.isEntityType((Class<?>) paramType.getRawType())
                    -> Optional.of(new ParameterizedEntityPropertyTypeMetadata(paramType));
            default -> Optional.empty();
        };
    }

    private Optional<PropertyTypeMetadata.Component> asComponent(final Type type) {
        return type instanceof Class<?> klass && COMPONENT_TYPES.contains(klass)
                ? Optional.of(new ComponentPropertyTypeMetadata(klass))
                : Optional.empty();
    }

    private Optional<PropertyTypeMetadata.Collectional> asCollectional(final Class<?> rawType, final Class<?> elementType) {
        return generate(elementType)
                .filter(PropertyTypeMetadataGenerator::isValidCollectionalElementType)
                .map(eltTypeMd -> new CollectionalPropertyTypeMetadata(rawType, eltTypeMd));
    }

    private static boolean isValidCollectionalElementType(final PropertyTypeMetadata eltTypeMetadata) {
        return switch (eltTypeMetadata) {
            case PropertyTypeMetadata.Entity $ -> true;
            case PropertyTypeMetadata.Primitive $ -> true;
            default -> false;
        };
    }

    private static Optional<Class<?>> rawClass(final Type type) {
        return type instanceof Class<?> klass
                ? Optional.of(klass)
                : type instanceof ParameterizedType paramType
                        ? rawClass(paramType.getRawType())
                        : Optional.empty();
    }

}
