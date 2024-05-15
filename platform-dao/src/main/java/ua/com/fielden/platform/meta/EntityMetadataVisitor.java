package ua.com.fielden.platform.meta;

public interface EntityMetadataVisitor<R> {

    R otherwise(EntityMetadata metadata);

    default R persistent(EntityMetadata.Persistent metadata) {
        return otherwise(metadata);
    }

    default R union(EntityMetadata.Union metadata) {
        return otherwise(metadata);
    }

    default R synthetic(EntityMetadata.Synthetic metadata) {
        return otherwise(metadata);
    }

}
