package ua.com.fielden.platform.eql.meta;

import java.util.Objects;

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
    public final boolean required;

    /**
     * Principal constructor.
     * 
     * @param name - property yield alias or property name.
     * @param propEntityInfo -- entity info for property.  
     * @param parent - property holder structure, which represents either query source or query-able entity of type <code>PARENT</code>.
     */
    public EntityTypePropInfo(final String name, final EntityInfo<T> propEntityInfo, final EntityInfo<PARENT> parent, final boolean required) {
        super(name, parent);
        this.propEntityInfo = propEntityInfo;
        this.required = required;
    }

    @Override
    public ResolutionContext resolve(final ResolutionContext context) {
        return context.isSuccessful() ? context : propEntityInfo.resolve(context);
    }

    @Override
    public Class<T> javaType() {
        return propEntityInfo.javaType();
    }

    @Override
    public String toString() {
        return super.toString() + ": " + propEntityInfo.javaType().getSimpleName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + propEntityInfo.hashCode();
        result = prime * result + (required ? 1231 : 1237);
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

        if (!(obj instanceof EntityTypePropInfo)) {
            return false;
        }

        final EntityTypePropInfo other = (EntityTypePropInfo) obj;

        return Objects.equals(propEntityInfo, other.propEntityInfo) && Objects.equals(required, other.required);
    }
}