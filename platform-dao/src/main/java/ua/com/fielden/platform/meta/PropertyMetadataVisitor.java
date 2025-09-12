package ua.com.fielden.platform.meta;

public interface PropertyMetadataVisitor<R> {

    R persistent(PropertyMetadata.Persistent metadata);

    /////////////////////////////////////////////////////////
    /////// Subcategories of the Transient nature. //////////
    /////////////////////////////////////////////////////////

    R calculated(PropertyMetadata.Calculated metadata);

    R critOnly(PropertyMetadata.CritOnly metadata);

    R plain(PropertyMetadata.Plain metadata);

}
