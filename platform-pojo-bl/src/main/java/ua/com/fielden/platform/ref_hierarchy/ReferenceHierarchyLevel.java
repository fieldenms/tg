package ua.com.fielden.platform.ref_hierarchy;

/**
 * All available reference hierarchy levels that can be loaded and allows one to determine which level should be loaded next.
 *
 * @author TG Team
 *
 */
public enum ReferenceHierarchyLevel {
    /**
     * Type level of hierarchy, should be under referenced by group entry.
     */
    TYPE {
        @Override
        public ReferenceHierarchyLevel nextLevel() {
            return REFERENCE_BY_INSTANCE;
        }
    },
    /**
     * Reference level of hierarchy should be under reference group entry.
     */
    REFERENCE_INSTANCE {
        @Override
        public ReferenceHierarchyLevel nextLevel() {
            return REFERENCE_GROUP;
        }
    },
    /**
     * Referenced by entry that should be under type level entry.
     */
    REFERENCE_BY_INSTANCE {
        @Override
        public ReferenceHierarchyLevel nextLevel() {
            return REFERENCE_GROUP;
        }
    },
    /**
     * The level that joins reference and referenced by entries.
     */
    REFERENCE_GROUP {
        @Override
        public ReferenceHierarchyLevel nextLevel() {
            return null;
        }
    },
    /**
     * Represents references group entry level of examining instance, should be under reference group entry.
     */
    REFERENCES {
        @Override
        public ReferenceHierarchyLevel nextLevel() {
            return null;
        }
    },
    /**
     * The referenced by group entry level, should be under reference group entry.
     */
    REFERENCED_BY {
        @Override
        public ReferenceHierarchyLevel nextLevel() {
            return null;
        }
    };

    public abstract ReferenceHierarchyLevel nextLevel();
}
