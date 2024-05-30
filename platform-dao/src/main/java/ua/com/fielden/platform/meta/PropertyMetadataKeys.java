package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.meta.PropertyMetadata.AnyKey;

public final class PropertyMetadataKeys {

    public static final AnyKey<Boolean>
            REQUIRED = mkAnyKey("Required"),
            UNION_MEMBER = mkAnyKey("Union Member");

    /**
     * Denotes a composite key member. Does not apply to simple keys.
     */
    public static final AnyKey<CompositeKeyMember> KEY_MEMBER = mkAnyKey("Key Member");

    private PropertyMetadataKeys() {}

    private static <V> AnyKey<V> mkAnyKey(final String name) {
        return new AnyKeyImpl<>(name);
    }

    private static final class AnyKeyImpl<V> implements AnyKey<V> {
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
