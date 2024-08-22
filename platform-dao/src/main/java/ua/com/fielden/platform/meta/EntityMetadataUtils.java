package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;

import java.util.List;

public interface EntityMetadataUtils {

    /**
     * Returns an ordered sequence of composite key members of an entity. The order is ascending and based on {@link CompositeKeyMember#value()}.
     * If an entity does not have a composite key, an empty sequence is returned.
     *
     * @see #keyMembers(EntityMetadata)
     */
    List<PropertyMetadata> compositeKeyMembers(EntityMetadata entityMetadata);

    /**
     * The same as {@link #compositeKeyMembers(EntityMetadata)} but if an entity doesn't have a composite key, returns
     * a list of a single element -- property {@code key}.
     */
    List<PropertyMetadata> keyMembers(EntityMetadata entityMetadata);

    List<PropertyMetadata> unionMembers(EntityMetadata.Union entityMetadata);

}
