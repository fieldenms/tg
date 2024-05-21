package ua.com.fielden.platform.meta;

public final class PropertyMetadataKeys {

    public static final PropertyMetadata.AnyKey<Boolean> REQUIRED = mkAnyKey("Required");
    public static final PropertyMetadata.AnyKey<Boolean> KEY_MEMBER = mkAnyKey("Key Member");

    private PropertyMetadataKeys() {}

    private static <V> PropertyMetadata.AnyKey<V> mkAnyKey(final String name) {
        return new AnyKeyImpl<>(name);
    }

    private static final class AnyKeyImpl<V> implements PropertyMetadata.AnyKey<V> {
        private final String name;

        private AnyKeyImpl(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Property Metadata Key [%s]".formatted(name);
        }
    }

}
