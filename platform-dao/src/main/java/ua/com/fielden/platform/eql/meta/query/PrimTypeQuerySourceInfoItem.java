package ua.com.fielden.platform.eql.meta.query;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;

public class PrimTypeQuerySourceInfoItem<T> extends AbstractQuerySourceInfoItem<T> {
    private final Class<T> itemType;

    public PrimTypeQuerySourceInfoItem(final String name, final Class<T> itemType, final Object hibType) {
        this(name, itemType, hibType, null, false);
    }
    
    public PrimTypeQuerySourceInfoItem(final String name, final Class<T> itemType, final Object hibType, final ExpressionModel expression, final boolean implicit) {
        super(name, hibType, expression, implicit);
        this.itemType = itemType;
    }

    @Override
    public AbstractQuerySourceInfoItem<T> cloneWithoutExpression() {
        return new PrimTypeQuerySourceInfoItem<T>(name, itemType, hibType);
    }
    
    @Override
    public PropResolutionProgress resolve(final PropResolutionProgress context) {
        return context;
    }
    
    @Override
    public Class<T> javaType() {
        return itemType;
    }
    
    @Override
    public String toString() {
        return String.format("%20s %20s", name, itemType.getSimpleName());

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + itemType.hashCode();
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

        if (!(obj instanceof PrimTypeQuerySourceInfoItem)) {
            return false;
        }

        final PrimTypeQuerySourceInfoItem other = (PrimTypeQuerySourceInfoItem) obj;

        return Objects.equals(itemType, other.itemType);
    }
}