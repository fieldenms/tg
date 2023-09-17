package ua.com.fielden.platform.eql.meta.query;

import java.util.Objects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;

/**
 * A structure that captures a query source yield-able entity-typed-property resolution related info within a query source of type <code>PARENT</code>.
 * 
 * @author TG Team
 *
 * @param <T>
 * @param <PARENT>
 */
public class EntityTypePropInfo<T extends AbstractEntity<?>> extends AbstractPropInfo<T> {
    public final QuerySourceInfo<T> propQuerySourceInfo;
    public final boolean nonnullable;

    public EntityTypePropInfo(final String name, final QuerySourceInfo<T> propQuerySourceInfo, final Object hibType, final boolean nonnullable) {
        this(name, propQuerySourceInfo, hibType, nonnullable, null, false);
    }

    public EntityTypePropInfo(final String name, final QuerySourceInfo<T> propQuerySourceInfo, final Object hibType, final boolean nonnullable, final ExpressionModel expression, final boolean implicit) {
        super(name, hibType, expression, implicit);
        this.propQuerySourceInfo = propQuerySourceInfo;
        this.nonnullable = nonnullable;
    }
    
    @Override
    public AbstractPropInfo<T> cloneWithoutExpression() {
        return new EntityTypePropInfo<T>(name, propQuerySourceInfo, hibType, nonnullable);
    }    
    
    @Override
    public PropResolutionProgress resolve(final PropResolutionProgress context) {
        return propQuerySourceInfo.resolve(context);
    }

    @Override
    public Class<T> javaType() {
        return propQuerySourceInfo.javaType();
    }

    @Override
    public String toString() {
        return String.format("%20s %20s", name, propQuerySourceInfo.javaType().getSimpleName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + propQuerySourceInfo.hashCode();
        result = prime * result + (nonnullable ? 1231 : 1237);
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

        final EntityTypePropInfo<?> other = (EntityTypePropInfo<?>) obj;

        return Objects.equals(propQuerySourceInfo, other.propQuerySourceInfo) && Objects.equals(nonnullable, other.nonnullable);
    }
}