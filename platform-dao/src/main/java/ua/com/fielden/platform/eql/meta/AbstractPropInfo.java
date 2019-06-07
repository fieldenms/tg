package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A structure that captures a query source yield-able property resolution related info within a query source of type <code>PARENT</code>. 
 * 
 * @author TG Team
 *
 */
public abstract class AbstractPropInfo<T, PARENT extends AbstractEntity<?>> implements IResolvable<T> {
    private final String name;
    private final EntityInfo<PARENT> parent;

    /**
     * Principal constructor.
     * 
     * @param name - property yield alias or property name.
     * @param parent - property holder structure, which represents either query source or query-able entity of type <code>PARENT</code>.
     */
    public AbstractPropInfo(final String name, final EntityInfo<PARENT> parent) {
        this.name = name;
        this.parent = parent;
        parent.addProp(this);
    }

    protected String getName() {
        return name;
    }

    protected EntityInfo<PARENT> getParent() {
        return parent;
    }
    
    @Override
    public String toString() {
        return parent + "." + name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
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
        if (!(obj instanceof AbstractPropInfo)) {
            return false;
        }
        final AbstractPropInfo other = (AbstractPropInfo) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (parent == null) {
            if (other.parent != null) {
                return false;
            }
        } else if (!parent.equals(other.parent)) {
            return false;
        }
        return true;
    }
}