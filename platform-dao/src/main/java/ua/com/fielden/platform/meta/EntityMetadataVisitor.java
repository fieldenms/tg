package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.AbstractUnionEntity;

public interface EntityMetadataVisitor<R> {

    R otherwise(TypeMetadata.Entity<?> metadata);

    default R persistent(TypeMetadata.Entity<EntityNature.Persistent> metadata,
                         EntityNature.Persistent.Data natureData) {
        return otherwise(metadata);
    }

    /**
     * @param entityType narrowed down type of this entity (the same value as {@link TypeMetadata.Entity#javaType()}
     */
    default R union(TypeMetadata.Entity<EntityNature.Union> metadata,
                    EntityNature.Union.Data natureData,
                    Class<? extends AbstractUnionEntity> entityType) {
        return otherwise(metadata);
    }

    default R synthetic(TypeMetadata.Entity<EntityNature.Synthetic> metadata,
                        EntityNature.Synthetic.Data natureData) {
        return otherwise(metadata);
    }

}
