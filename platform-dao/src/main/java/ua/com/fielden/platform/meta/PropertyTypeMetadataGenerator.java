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

import static ua.com.fielden.platform.meta.TypeRegistry.COMPOSITE_TYPES;
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
     * Prefer {@link #generate(Field)} if the property's {@link Field} is available. This method should be used only in
     * special cases. It's uncapable of handling collectional types, for example, since that would require knowledge
     * of property's annotations.
     */
    public Optional<PropertyTypeMetadata> generate(final Type type) {
        // start with empty() to type-check
        return Optional.<PropertyTypeMetadata>empty()
                .or(() -> asPrimitive(type))
                .or(() -> asEntity(type))
                .or(() -> asComposite(type))
                .or(() -> DynamicEntityKey.class == type
                        ? Optional.of(PropertyTypeMetadata.COMPOSITE_KEY)
                        : Optional.empty())
                .or(() -> NoKey.class == type
                        ? Optional.of(PropertyTypeMetadata.NO_KEY)
                        : Optional.empty());
    }

    private Optional<PropertyTypeMetadata.Primitive> asPrimitive(final Type type) {
        return type instanceof Class<?> klass && PRIMITIVE_PROPERTY_TYPES.contains(klass)
                ? Optional.of(new PrimitivePropertyTypeMetadata(klass))
                : Optional.empty();
    }

    private Optional<PropertyTypeMetadata.Entity> asEntity(final Type type) {
        return rawClass(type)
                .filter(EntityUtils::isEntityType)
                .map(klass -> new EntityPropertyTypeMetadata((Class<? extends AbstractEntity<?>>) klass));
    }

    private Optional<PropertyTypeMetadata.Composite> asComposite(final Type type) {
        return type instanceof Class<?> klass && COMPOSITE_TYPES.contains(klass)
                ? Optional.of(new CompositePropertyTypeMetadata(klass))
                : Optional.empty();
    }

    private Optional<PropertyTypeMetadata.Collectional> asCollectional(final Class<?> rawType, final Class<?> elementType) {
        return generate(elementType)
                .filter(PropertyTypeMetadataGenerator::isValidCollectionalElementType)
                .map(eltTypeMd -> new CollectionalPropertyTypeMetadata(rawType, eltTypeMd));
        // new EqlMetadataGenerationException("Failed to generate metadata for element type of collectional property type [%s].".formatted(type));
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
