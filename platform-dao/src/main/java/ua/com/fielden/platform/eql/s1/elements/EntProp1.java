package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.EntProp2;

public class EntProp1 implements ISingleOperand1<EntProp2> {
    private String name;
    private boolean external;

    public EntProp1(final String name, final boolean external) {
        this.name = name;
        this.external = external;
    }

    public EntProp1(final String name) {
        this(name, false);
    }

    @Override
    public String toString() {
        return name + " " + hashCode();
    }

    public EntProp2 transform(final TransformatorToS2 resolver) {
        return resolver.getTransformedProp(this);
    }

    public String getName() {
        return name;
    }

    public boolean isExternal() {
        return external;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof EntProp1)) {
            return false;
        }
        final EntProp1 other = (EntProp1) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}