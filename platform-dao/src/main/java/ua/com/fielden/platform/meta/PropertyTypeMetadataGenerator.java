package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static ua.com.fielden.platform.meta.TypeRegistry.COMPOSITE_TYPES;
import static ua.com.fielden.platform.meta.TypeRegistry.PRIMITIVE_PROPERTY_TYPES;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;

final class PropertyTypeMetadataGenerator {

    // TODO use Either to capture the error message
    public Optional<PropertyTypeMetadata> fromType(final Type type) {
        // start with empty() to type-check
        return Optional.<PropertyTypeMetadata>empty()
                .or(() -> asPrimitive(type))
                .or(() -> PropertyTypeDeterminator.asCollectional(type)
                        .flatMap(rawType_eltType -> asCollectional(rawType_eltType._1, rawType_eltType._2)))
                .or(() -> type instanceof Class<?> klass && isEntityType(klass)
                        ? Optional.of(new EntityPropertyTypeMetadata((Class<? extends AbstractEntity<?>>) klass))
                        : Optional.empty())
                .or(() -> asComposite(type))
                .or(() -> DynamicEntityKey.class == type
                        ? Optional.of(PropertyTypeMetadata.COMPOSITE_KEY)
                        : Optional.empty())
                .or(() -> NoKey.class == type
                        ? Optional.of(PropertyTypeMetadata.NO_KEY)
                        : Optional.empty())
                // TODO remove Other?
                .or(() -> Optional.of(new OtherPropertyTypeMetadata(type)));
    }

    private Optional<PropertyTypeMetadata.Primitive> asPrimitive(final Type type) {
        return type instanceof Class<?> klass && PRIMITIVE_PROPERTY_TYPES.contains(klass)
                ? Optional.of(new PrimitivePropertyTypeMetadata(klass))
                : Optional.empty();
    }

    private Optional<PropertyTypeMetadata.Composite> asComposite(final Type type) {
        return type instanceof Class<?> klass && COMPOSITE_TYPES.contains(klass)
                ? Optional.of(new CompositePropertyTypeMetadata(klass))
                : Optional.empty();
    }

    private Optional<PropertyTypeMetadata.Collectional> asCollectional(final Class<?> rawType, final Class<?> elementType) {
        return fromType(elementType)
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

}
