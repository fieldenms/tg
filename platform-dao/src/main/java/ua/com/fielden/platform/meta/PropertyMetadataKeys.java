package ua.com.fielden.platform.meta;

public final class PropertyMetadataKeys {

    public static final PropertyMetadata.AnyKey<Boolean> REQUIRED = mkAnyKey();

    private PropertyMetadataKeys() {}

    private static <V> PropertyMetadata.AnyKey<V> mkAnyKey() {
        return new AnyKeyImpl<>();
    }

    private static final class AnyKeyImpl<V> implements PropertyMetadata.AnyKey<V> {}

}
