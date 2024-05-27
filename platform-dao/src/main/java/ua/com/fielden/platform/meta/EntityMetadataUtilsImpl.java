package ua.com.fielden.platform.meta;

import java.util.List;

import static ua.com.fielden.platform.meta.PropertyMetadataKeys.UNION_MEMBER;

final class EntityMetadataUtilsImpl implements EntityMetadataUtils {

    @Override
    public List<PropertyMetadata> compositeKeyMembers(final EntityMetadata entityMetadata) {
        // TODO
        return List.of();
    }

    @Override
    public List<PropertyMetadata> unionMembers(final EntityMetadata.Union entityMetadata) {
        return entityMetadata.properties().stream()
                .filter(pm -> pm.is(UNION_MEMBER))
                .toList();
    }

}
