package ua.com.fielden.platform.meta;

import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.meta.PropertyMetadata.AnyKey;

public final class PropertyMetadataKeys {

    public static final AnyKey<Boolean>
            REQUIRED = mkAnyKey("Required"),
            UNION_MEMBER = mkAnyKey("Union Member");

    /// Denotes a composite key member. Does not apply to simple keys.
    ///
    public static final KCompositeKeyMember KEY_MEMBER = KCompositeKeyMember.INSTANCE;

    public static final KAuditProperty AUDIT_PROPERTY = KAuditProperty.INSTANCE;

    private PropertyMetadataKeys() {}

    private static <V> AnyKey<V> mkAnyKey(final String name) {
        return new AnyKeyImpl<>(name);
    }

    private static class AnyKeyImpl<V> implements AnyKey<V> {
        private final String name;

        private AnyKeyImpl(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Property Metadata Key [%s]".formatted(name);
        }
    }

    /// Denotes a composite key member.
    /// Does not apply to simple keys.
    ///
    public static class KCompositeKeyMember extends AnyKeyImpl<CompositeKeyMember> {
        private static final KCompositeKeyMember INSTANCE = new KCompositeKeyMember();
        private KCompositeKeyMember() {
            super("Key Member");
        }
    }

    public static final class KAuditProperty extends AnyKeyImpl<KAuditProperty.Data> {
        private static final KAuditProperty INSTANCE = new KAuditProperty();
        private KAuditProperty() {
            super("Audit Property");
        }

        public record Data (boolean active) {}
    }

}
