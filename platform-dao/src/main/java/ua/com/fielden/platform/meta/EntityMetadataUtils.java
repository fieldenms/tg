package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;

import java.util.List;

public interface EntityMetadataUtils {

    /**
     * Returns an ordered sequence of composite key members of an entity. The order is ascending and based on {@link CompositeKeyMember#value()}.
     * If an entity does not have a composite key, an empty sequence is returned.
     */
    List<? extends PropertyMetadata> compositeKeyMembers(TypeMetadata.Entity<?> entityMetadata);

    List<? extends PropertyMetadata> unionMembers(TypeMetadata.Entity<EntityNature.Union> entityMetadata);

}
