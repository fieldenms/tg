package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;

/**
 * Represents a property type. This abstraction can be viewed as being in a class-instance relationship with {@link TypeMetadata}:
 * {@link PropertyTypeMetadata} is an instance of {@link TypeMetadata}. This means that {@link PropertyTypeMetadata} can
 * refine {@link TypeMetadata} with arbitrary information.
 * <p>
 * However, the mentioned class-instance relationship is not a universal rule. There are property types that don't directly
 * correspond to any {@link TypeMetadata}. For example, types of collectional properties or primitive types such as {@link String}.
 */
public sealed interface PropertyTypeMetadata {

    /**
     * Type of primitive properties.
     * <p>
     * Examples: {@link String}, {@link BigDecimal}, {@link Integer}.
     * <p>
     * Has no corresponding {@link TypeMetadata}.
     */
    non-sealed interface Primitive extends PropertyTypeMetadata {
        Class<?> javaType();
    }

    /**
     * Type of entity-typed properties.
     * <p>
     * Corresponds to {@link TypeMetadata.Entity}.
     */
    non-sealed interface Entity extends PropertyTypeMetadata {
        Class<? extends AbstractEntity<?>> javaType();
    }

    /**
     * Type of collectional properties.
     * <p>
     * Has no corresponding {@link TypeMetadata}.
     */
    non-sealed interface Collectional extends PropertyTypeMetadata {
        Class<?> collectionType();

        PropertyTypeMetadata elementType();

        String linkProp();
    }

    /**
     * Type of properties with composite type.
     * <p>
     * Examples: {@link Money}.
     * <p>
     * Corresponds to {@link TypeMetadata.Composite}.
     */
    non-sealed interface Composite extends PropertyTypeMetadata {
        Class<?> javaType();
    }

    /**
     * Type of a composite key property, i.e., property named "key" defined in an entity whose key type is {@link DynamicEntityKey}.
     * <p>
     * Has no corresponding {@link TypeMetadata}.
     */
    CompositeKey COMPOSITE_KEY = new CompositeKey();

    final class CompositeKey implements PropertyTypeMetadata {
        private CompositeKey() {}
    }

    /**
     * Type of other, yet unclassified property types.
     */
    non-sealed interface Other extends PropertyTypeMetadata {
        Class<?> javaType();
    }

}
