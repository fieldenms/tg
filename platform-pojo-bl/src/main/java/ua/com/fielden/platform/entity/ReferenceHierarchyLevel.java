package ua.com.fielden.platform.entity;

public enum ReferenceHierarchyLevel {

    TYPE {
        @Override
        public ReferenceHierarchyLevel nextLevel() {
            return REFERENCE_BY_INSTANCE;
        }
    },
    REFERENCE_INSTANCE {
        @Override
        public ReferenceHierarchyLevel nextLevel() {
            return REFERENCE_GROUP;
        }
    },
    REFERENCE_BY_INSTANCE {
        @Override
        public ReferenceHierarchyLevel nextLevel() {
            return REFERENCE_GROUP;
        }
    },
    REFERENCE_GROUP {
        @Override
        public ReferenceHierarchyLevel nextLevel() {
            return null;
        }
    },
    REFERENCES {
        @Override
        public ReferenceHierarchyLevel nextLevel() {
            return null;
        }
    },
    REFERENCED_BY {
        @Override
        public ReferenceHierarchyLevel nextLevel() {
            return null;
        }
    };

    public abstract ReferenceHierarchyLevel nextLevel();
}
