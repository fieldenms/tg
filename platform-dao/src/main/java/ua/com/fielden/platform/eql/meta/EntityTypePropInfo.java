package ua.com.fielden.platform.eql.meta;

import java.util.Objects;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.exceptions.EqlMetadataGenerationException;
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
    public final EntityInfo<T> propEntityInfo;
    public final boolean required;

    public EntityTypePropInfo(final String name, final EntityInfo<T> propEntityInfo, final Object hibType, final boolean required) {
        this(name, propEntityInfo, hibType, required, null, false);
    }

    public EntityTypePropInfo(final String name, final EntityInfo<T> propEntityInfo, final Object hibType, final boolean required, final ExpressionModel expression, final boolean implicit) {
        super(name, hibType, expression, implicit);
        if (propEntityInfo == null) {
            throw new EqlMetadataGenerationException("Argument [propEntityInfo] should not be null for property [%s].".formatted(name));
        }
        this.propEntityInfo = propEntityInfo;
        this.required = required;
    }

    @Override
    public EntityTypePropInfo<T> cloneRenamed(final String newName) {
        return new EntityTypePropInfo<T>(newName, propEntityInfo, hibType, required);
    }

    @Override
    public AbstractPropInfo<T> cloneWithoutExpression() {
        return new EntityTypePropInfo<T>(name, propEntityInfo, hibType, required);
    }

    @Override
    public PropResolutionProgress resolve(final PropResolutionProgress context) {
        return propEntityInfo.resolve(context);
    }

    @Override
    public Class<T> javaType() {
        return propEntityInfo.javaType();
    }

    @Override
    public String toString() {
        return String.format("%20s %20s", name, propEntityInfo.javaType().getSimpleName());
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

        final EntityTypePropInfo<?> other = (EntityTypePropInfo<?>) obj;

        return Objects.equals(propEntityInfo, other.propEntityInfo) && Objects.equals(required, other.required);
    }
}