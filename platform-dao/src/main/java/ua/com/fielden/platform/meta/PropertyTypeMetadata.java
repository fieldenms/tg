package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.types.Money;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a property type. This abstraction can be viewed as being in a class-instance relationship with {@link TypeMetadata}:
 * {@link PropertyTypeMetadata} is an instance of {@link TypeMetadata}. This means that {@link PropertyTypeMetadata} can
 * refine {@link TypeMetadata} with arbitrary information.
 * <p>
 * However, the mentioned class-instance relationship is not a universal rule. There are property types that don't directly
 * correspond to any {@link TypeMetadata}. For example, types of collectional properties or primitive types such as {@link String}.
 *
 * <h5> Property types that aren't modelled </h5>
 * This abstraction is not exhaustive, i.e., it does not cover all possible property types.
 * <p>
 * Examples property types that aren't modelled:
 * <ul>
 *   <li> {@link Map}
 *   <li> Collectional types parameterised with unmodelled property types (e.g, with a type variable)
 * </ul>
 */
public sealed interface PropertyTypeMetadata {

    Type javaType();

    default boolean isPrimitive() {
        return this instanceof Primitive;
    }

    default boolean isEntity() {
        return this instanceof Entity;
    }

    default boolean isCollectional() {
        return this instanceof Collectional;
    }

    default boolean isComposite() {
        return this instanceof Composite;
    }

    default boolean isCompositeKey() {
        return this instanceof CompositeKey;
    }

    default boolean isNoKey() {
        return this instanceof NoKey;
    }

    default Optional<Primitive> asPrimitive() {
        return this instanceof Primitive it ? Optional.of(it) : Optional.empty();
    }

    default Optional<Composite> asComposite() {
        return this instanceof Composite it ? Optional.of(it) : Optional.empty();
    }

    default Optional<Entity> asEntity() {
        return this instanceof Entity it ? Optional.of(it) : Optional.empty();
    }

    default Optional<Collectional> asCollectional() {
        return this instanceof Collectional it ? Optional.of(it) : Optional.empty();
    }

    default Optional<CompositeKey> asCompositeKey() {
        return this instanceof CompositeKey it ? Optional.of(it) : Optional.empty();
    }

    default Optional<NoKey> asNoKey() {
        return this instanceof NoKey it ? Optional.of(it) : Optional.empty();
    }

    /**
     * Type of primitive properties.
     * <p>
     * Examples: {@link String}, {@link BigDecimal}, {@link Integer}.
     * <p>
     * Has no corresponding {@link TypeMetadata}.
     */
    non-sealed interface Primitive extends PropertyTypeMetadata {
        @Override
        Class<?> javaType();
    }

    /**
     * Type of entity-typed properties.
     * <p>
     * Corresponds to {@link EntityMetadata}.
     */
    non-sealed interface Entity extends PropertyTypeMetadata {
        @Override
        Class<? extends AbstractEntity<?>> javaType();
    }

    /**
     * Type of collectional properties.
     * <p>
     * Has no corresponding {@link TypeMetadata}.
     * <p>
     * Is a wrapper type: wraps a collection's element type.
     */
    non-sealed interface Collectional extends PropertyTypeMetadata, Wrapper {
        Class<?> collectionType();

        PropertyTypeMetadata elementType();

//        String linkProp();

        @Override
        default PropertyTypeMetadata wrappedType() {
            return elementType();
        }

        @Override
        default ParameterizedType javaType() {
            return Reflector.newParameterizedType(collectionType(), elementType().javaType());
        }
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

    CompositeKey COMPOSITE_KEY = new CompositeKey();

    /**
     * Type of a composite key property, i.e., property named "key" defined in an entity whose key type is {@link DynamicEntityKey}.
     * Such a property is implicitly calculated as concatenation of all composite key members and its Java type is {@link String}.
     * <p>
     * Has no corresponding {@link TypeMetadata}.
     */
    final class CompositeKey implements PropertyTypeMetadata {
        private CompositeKey() {}

        @Override
        public Class<String> javaType() {
            return String.class;
        }

        @Override
        public String toString() {
            return "CompositeKey";
        }
    }

    NoKey NO_KEY = new NoKey();

    final class NoKey implements PropertyTypeMetadata {
        private NoKey() {}

        @Override
        public Class<ua.com.fielden.platform.entity.NoKey> javaType() {
            return ua.com.fielden.platform.entity.NoKey.class;
        }

        @Override
        public String toString() {
            return "NoKey";
        }
    }

    interface Wrapper {
        PropertyTypeMetadata wrappedType();

        static PropertyTypeMetadata unwrap(PropertyTypeMetadata typeMetadata) {
            return typeMetadata instanceof Wrapper wrapper
                    ? wrapper.wrappedType()
                    : typeMetadata;
        }
    }

}