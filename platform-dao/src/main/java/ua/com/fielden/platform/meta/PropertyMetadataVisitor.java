package ua.com.fielden.platform.meta;

public interface PropertyMetadataVisitor<R> {

    R otherwise(PropertyMetadata<?> metadata);

    default R persistent(PropertyMetadata<PropertyNature.Persistent> metadata,
                         PropertyNature.Persistent.Data natureData) {
        return otherwise(metadata);
    }

    default R calculated(PropertyMetadata<PropertyNature.Calculated> metadata,
                         PropertyNature.Calculated.Data natureData) {
        return otherwise(metadata);
    }

    default R critOnly(PropertyMetadata<PropertyNature.CritOnly> metadata) {
        return otherwise(metadata);
    }

    default R transient_(PropertyMetadata<PropertyNature.Transient> metadata) {
        return otherwise(metadata);
    }

}
