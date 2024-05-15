package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.types.Money;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;

/**
 * Represents type metadata that is derived from the type's definition.
 */
public sealed interface TypeMetadata permits EntityMetadata, TypeMetadata.Composite {

    /**
     * Returns the underlying Java type.
     */
    Type javaType();

    /**
     * Represents composite type metadata.
     * <p>
     * Examples of composite types: {@link Money}.
     */
    non-sealed interface Composite extends TypeMetadata {

        Collection<? extends PropertyMetadata> properties();

        Optional<PropertyMetadata> property(String name);

    }

}
