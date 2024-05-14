package ua.com.fielden.platform.meta;

import java.util.function.Predicate;

public interface PropertyMetadataUtils {

    /**
     * If property type is an entity type, tests it with the given predicate, otherwise returns {@code false}.
     */
    boolean isPropEntityType(PropertyTypeMetadata propType, Predicate<TypeMetadata.Entity<?>> predicate);

    /**
     * If property type is an entity type, tests it with the given predicate, otherwise returns {@code false}.
     */
    default boolean isPropEntityType(final PropertyMetadata<?> pm, final Predicate<TypeMetadata.Entity<?>> predicate) {
        return isPropEntityType(pm.type(), predicate);
    }

}
