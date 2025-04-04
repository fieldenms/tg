package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;

import java.util.List;

public interface EntityMetadataUtils {

    /**
     * Returns an ordered sequence of composite key members for an entity.
     * The order is ascending and based on {@link CompositeKeyMember#value()}.
     * If an entity does not have a composite key, an empty sequence is returned.
     *
     * @see #keyMembers(EntityMetadata)
     */
    List<PropertyMetadata> compositeKeyMembers(EntityMetadata entityMetadata);

    /**
     * The same as {@link #compositeKeyMembers(EntityMetadata)} but if an entity is not composite, a list with single element representing property {@code key} is returned.
     */
    List<PropertyMetadata> keyMembers(EntityMetadata entityMetadata);

    /**
     * Returns union members (aka union properties) for a union entity.
     *
     * @param entityMetadata
     * @return
     */
    List<PropertyMetadata> unionMembers(EntityMetadata.Union entityMetadata);

}
