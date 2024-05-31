package ua.com.fielden.platform.meta;

public interface EntityMetadataVisitor<R> {

    R persistent(EntityMetadata.Persistent metadata);

    R union(EntityMetadata.Union metadata);

    R synthetic(EntityMetadata.Synthetic metadata);

}
