package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.types.Money;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;

/**
 * Represents type metadata derived from the type's definition.
 */
public sealed interface TypeMetadata permits EntityMetadata, TypeMetadata.Component {

    /**
     * Returns the underlying Java type.
     */
    Type javaType();

    default Optional<EntityMetadata> asEntity() {
        return this instanceof EntityMetadata em ? Optional.of(em) : Optional.empty();
    }

    default Optional<Component> asComponent() {
        return this instanceof Component ctm ? Optional.of(ctm) : Optional.empty();
    }

    /**
     * Represents metadata for a type that is a component.
     * <p>
     * Examples of component types: {@link Money}.
     */
    sealed interface Component extends TypeMetadata permits ComponentTypeMetadataImpl {

        @Override
        Class<?> javaType();

        Collection<? extends PropertyMetadata> properties();

        /**
         * Retrieves metadata for a property if it exists in this type, otherwise throws an exception.
         * </p>
         * {@link #propertyOpt(String)} is a non-throwing alternative.
         */
        PropertyMetadata property(String name);

        /**
         * Non-throwing alternative to {@link #property(String)}.
         */
        Optional<PropertyMetadata> propertyOpt(String name);

    }

}
