package ua.com.fielden.platform.meta;

public interface PropertyMetadataVisitor<R> {

    R otherwise(PropertyMetadata metadata);

    default R persistent(PropertyMetadata.Persistent metadata) {
        return otherwise(metadata);
    }

    default R calculated(PropertyMetadata.Calculated metadata) {
        return otherwise(metadata);
    }

    default R critOnly(PropertyMetadata.CritOnly metadata) {
        return otherwise(metadata);
    }

    default R transient_(PropertyMetadata.Transient metadata) {
        return otherwise(metadata);
    }

}
