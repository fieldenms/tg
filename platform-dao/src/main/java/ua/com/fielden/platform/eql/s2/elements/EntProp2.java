package ua.com.fielden.platform.eql.s2.elements;

import ua.com.fielden.platform.eql.meta.AbstractPropInfo;

public class EntProp2 implements ISingleOperand2 {
    private final String name;
    private final ISource2 source;
    private final AbstractPropInfo resolution;
    private final Expression2 expression;

    public EntProp2(final String name, final ISource2 source, final AbstractPropInfo resolution, final Expression2 expression) {
        this.name = name;
        this.source = source;
        this.resolution = resolution;
        this.expression = expression;
        source.addProp(this);
    }

    @Override
    public String toString() {
        return " name = " + name + "; source = " + source + "; resolution = " + resolution;
    }

    @Override
    public boolean ignore() {
        return false;
    }

    public String getName() {
        return name;
    }

    @Override
    public Class type() {
        return resolution.javaType();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expression == null) ? 0 : expression.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((resolution == null) ? 0 : resolution.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            System.out.println("entProp 1");
            return false;
        }
        if (!(obj instanceof EntProp2)) {
            System.out.println("entProp 2");
            return false;
        }
        final EntProp2 other = (EntProp2) obj;
        if (expression == null) {
            if (other.expression != null) {
                System.out.println("entProp 4");
                return false;
            }
        } else if (!expression.equals(other.expression)) {
            System.out.println("entProp 5");
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                System.out.println("entProp 6");
                return false;
            }
        } else if (!name.equals(other.name)) {
            System.out.println("entProp 7");
            return false;
        }
        if (resolution == null) {
            if (other.resolution != null) {
                System.out.println("entProp 8");
                return false;
            }
        } else if (!resolution.equals(other.resolution)) {
            System.out.println("entProp 9: " + resolution + " vs " + other.resolution);
            return false;
        }
        if (source == null) {
            if (other.source != null) {
                System.out.println("entProp 10");
                return false;
            }
        } else if (!source.equals(other.source)) {
            System.out.println("entProp 11");
            return false;
        }
        return true;
    }
}