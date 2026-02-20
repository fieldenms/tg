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

/// Represents a property type.
///
/// This abstraction can be viewed as being in a class-instance relationship with [TypeMetadata]:
/// * [PropertyTypeMetadata] is an instance of [TypeMetadata].
///
/// This means that [PropertyTypeMetadata] can extend [TypeMetadata] with additional information.
///
/// However, the mentioned class-instance relationship is not a universal rule.
/// There are property types that do not correspond directly to any [TypeMetadata].
/// For example, types of collectional properties or primitive types such as [String].
///
/// #####  Property types that aren't modelled
/// This abstraction is not exhaustive – it does not cover all possible property types.
///
/// Examples of property types that are not modelled:
///
/// -  [Map];
/// -  Collectional types parameterised with unmodelled property types (e.g. with a type variable).
///
public sealed interface PropertyTypeMetadata {

    /// Returns a [Class] object that identifies the declared property type.
    ///
    /// If the property type is parameterised, its raw type is returned.
    ///
    /// @see #genericJavaType()
    ///
    Class<?> javaType();

    /// Returns a [Type] object that identifies the declared property type.
    ///
    /// If the property type is parameterised, the returned type is a [ParameterizedType].
    ///
    /// @see #javaType()
    ///
    Type genericJavaType();

    default boolean isPrimitive() {
        return this instanceof Primitive;
    }

    default boolean isEntity() {
        return this instanceof Entity;
    }

    default boolean isCollectional() {
        return this instanceof Collectional;
    }

    default boolean isComponent() {
        return this instanceof Component;
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

    default Optional<Component> asComponent() {
        return this instanceof Component it ? Optional.of(it) : Optional.empty();
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

    /// Type of primitive properties.
    ///
    /// Examples: [String], [BigDecimal], [Integer].
    ///
    /// Has no corresponding [TypeMetadata].
    ///
    non-sealed interface Primitive extends PropertyTypeMetadata {
        @Override
        Class<?> javaType();

        @Override
        default Class<?> genericJavaType() {
            return javaType();
        }
    }

    /// Type of entity-typed properties.
    ///
    /// Corresponds to [EntityMetadata].
    ///
    non-sealed interface Entity extends PropertyTypeMetadata {
        @Override
        Class<? extends AbstractEntity<?>> javaType();
    }

    /// Type for representing collectional properties.
    ///
    /// Has no corresponding [TypeMetadata].
    ///
    /// Is a wrapper type: wraps a collection's element type.
    ///
    non-sealed interface Collectional extends PropertyTypeMetadata, Wrapper {
        Class<?> collectionType();

        PropertyTypeMetadata elementType();

        @Override
        default PropertyTypeMetadata wrappedType() {
            return elementType();
        }

        @Override
        default Class<?> javaType() {
            return collectionType();
        }

        @Override
        default ParameterizedType genericJavaType() {
            return Reflector.newParameterizedType(collectionType(), elementType().genericJavaType());
        }
    }

    /// Type for representing properties that are component-like product types that have one or more distinct attributes.
    ///
    /// Examples: [Money].
    ///
    /// Corresponds to [TypeMetadata.Component].
    ///
    sealed interface Component extends PropertyTypeMetadata permits ComponentPropertyTypeMetadata {
        Class<?> javaType();
    }

    CompositeKey COMPOSITE_KEY = new CompositeKey();

    /// Type representing a composite key property, in other words, property named "key" defined in an entity whose key type is [DynamicEntityKey].
    /// Such properties hava Java type [String] and they are implicitly calculated by concatenating the values of all composite key members with a corresponding key separator.
    ///
    /// Has no corresponding [TypeMetadata].
    ///
    final class CompositeKey implements PropertyTypeMetadata {
        private CompositeKey() {}

        @Override
        public Class<String> javaType() {
            return String.class;
        }

        @Override
        public Class<String> genericJavaType() {
            return javaType();
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
        public Class<ua.com.fielden.platform.entity.NoKey> genericJavaType() {
            return javaType();
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
