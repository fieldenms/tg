package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.Money;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;

/**
 * Represents type metadata that is derived from the type's definition.
 */
public sealed interface TypeMetadata {

    /**
     * Returns the underlying Java type.
     */
    Type javaType();

    /**
     * Represents entity type metadata.
     * <p>
     * Each entity type is classified according to its nature. A nature may specify essential metadata that is attributed
     * an entity type. This essential data can be accessed via {@link #data()} but since its type depends on the nature,
     * {@link #match(EntityMetadataVisitor)} should be used.
     * <p>
     * The set of properties includes both declared and inherited ones.
     *
     * @param <N> entity nature
     */
    non-sealed interface Entity<N extends EntityNature> extends TypeMetadata {

        @Override
        Class<? extends AbstractEntity<?>> javaType();

        N nature();

        EntityNature.Data<N> data();

        Collection<? extends PropertyMetadata<?>> properties();

        Optional<PropertyMetadata<?>> property(String name);

        <R> R match(EntityMetadataVisitor<R> visitor);

    }

    /**
     * Represents composite type metadata.
     * <p>
     * Examples of composite types: {@link Money}.
     */
    non-sealed interface Composite extends TypeMetadata {

        Collection<? extends PropertyMetadata<?>> properties();

        Optional<PropertyMetadata<?>> property(String name);

    }

}
