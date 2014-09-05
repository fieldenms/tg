package ua.com.fielden.platform.dao;

public enum PropertyCategory {
    PRIMITIVE {
        @Override
        boolean affectsMappings() {
            return true;
        }
    }, //
    COLLECTIONAL {
        @Override
        boolean affectsMappings() {
            return false;
        }
    }, //
    ENTITY {
        @Override
        boolean affectsMappings() {
            return true;
        }
    }, //
    ONE2ONE_ID {
        @Override
        boolean affectsMappings() {
            return true;
        }
    }, //
    PRIMITIVE_AS_KEY {
        @Override
        boolean affectsMappings() {
            return true;
        }
    }, //
    ENTITY_AS_KEY {
        @Override
        boolean affectsMappings() {
            return true;
        }
    }, //
    ENTITY_MEMBER_OF_COMPOSITE_KEY {
        @Override
        boolean affectsMappings() {
            return true;
        }
    }, //
    PRIMITIVE_MEMBER_OF_COMPOSITE_KEY {
        @Override
        boolean affectsMappings() {
            return true;
        }
    }, //
    COMPONENT_HEADER {
        @Override
        boolean affectsMappings() {
            return true;
        }
    }, //
    COMPONENT_DETAILS {
        @Override
        boolean affectsMappings() {
            return false;
        }
    }, //
    UNION_ENTITY_HEADER {
        @Override
        boolean affectsMappings() {
            return true;
        }
    }, //
    UNION_ENTITY_DETAILS {
        @Override
        boolean affectsMappings() {
            return false;
        }
    }, //
    EXPRESSION {
        @Override
        boolean affectsMappings() {
            return false;
        }
    }, //
    SYNTHETIC {
        @Override
        boolean affectsMappings() {
            return false;
        }
    }, //
    EXPRESSION_COMMON {
        @Override
        boolean affectsMappings() {
            return false;
        }
    }, //
    VIRTUAL_OVERRIDE {
        @Override
        boolean affectsMappings() {
            return false;
        }
    }; // the case of virtual generation of composite entity key by concatenation of all members during eQuery processing.

    abstract boolean affectsMappings();

}
