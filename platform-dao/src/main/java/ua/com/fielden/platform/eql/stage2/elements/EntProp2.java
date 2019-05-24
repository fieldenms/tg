package ua.com.fielden.platform.eql.stage2.elements;

public class EntProp2 extends AbstractElement2 implements ISingleOperand2 {
    private final String name;
    private final IQrySource2 source;
    private final String type;

    public EntProp2(final String name, final IQrySource2 source, final String type, final int contextId) {
        super(contextId);
        this.name = name;
        this.source = source;
        this.type = type;
    }

    @Override
    public String toString() {
        return " name = " + name + ";\n source = " + source + ";\n type = " + type + " contextId = " + contextId;
    }

    @Override
    public boolean ignore() {
        return false;
    }

    public String getName() {
        return name;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!super.equals(obj)) {
            return false;
        }
        
        if (obj == null) {
            return false;
        }
        
        if (!(obj instanceof EntProp2)) {
            return false;
        }
        final EntProp2 other = (EntProp2) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (source == null) {
            if (other.source != null) {
                return false;
            }
        } else if (!source.equals(other.source)) {
            return false;
        }
        return true;
    }
}