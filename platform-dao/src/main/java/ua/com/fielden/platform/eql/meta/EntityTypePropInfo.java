package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A structure that captures a query source yield-able entity-typed-property resolution related info within a query source of type <code>PARENT</code>.
 * 
 * @author TG Team
 *
 * @param <T>
 * @param <PARENT>
 */
public class EntityTypePropInfo<T extends AbstractEntity<?>, PARENT extends AbstractEntity<?>> extends AbstractPropInfo<T, PARENT> {
    private final EntityInfo<T> propEntityInfo;

    /**
     * Principal constructor.
     * 
     * @param name - property yield alias or property name.
     * @param propEntityInfo -- entity info for property.  
     * @param parent - property holder structure, which represents either query source or query-able entity of type <code>PARENT</code>.
     */
    public EntityTypePropInfo(final String name, final EntityInfo<T> propEntityInfo, final EntityInfo<PARENT> parent) {
        super(name, parent);
        this.propEntityInfo = propEntityInfo;
    }

    protected EntityInfo<T> getPropEntityInfo() {
        return propEntityInfo;
    }

    @Override
    public AbstractPropInfo<?, ?> resolve(final String dotNotatedSubPropName) {
        return dotNotatedSubPropName != null ? getPropEntityInfo().resolve(dotNotatedSubPropName) : this;
    }

    @Override
    public Class<T> javaType() {
        return propEntityInfo.javaType();
    }

    @Override
    public String toString() {
        return super.toString() + ": " + propEntityInfo.javaType().getSimpleName();
    }
}