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
        parent.getProps().put(name, this);
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
}