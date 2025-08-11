package ua.com.fielden.platform.eql.meta.query;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.eql.meta.CalcPropInfo;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;

import java.util.Objects;

public class QuerySourceItemForPrimType<T> extends AbstractQuerySourceItem<T> {
    /** The type of this item or {@code null}. */
    private final @Nullable Class<T> itemType;

    public QuerySourceItemForPrimType(final String name, final @Nullable Class<T> itemType, final @Nullable Object hibType) {
        this(name, itemType, hibType, null);
    }

    public QuerySourceItemForPrimType(
            final String name,
            final @Nullable Class<T> itemType,
            final @Nullable Object hibType,
            final @Nullable CalcPropInfo expression)
    {
        super(name, hibType, expression);
        this.itemType = itemType;
    }

    @Override
    public AbstractQuerySourceItem<T> cloneWithoutExpression() {
        return new QuerySourceItemForPrimType<T>(name, itemType, hibType);
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
        return String.format("%20s %20s", name, itemType == null ? "null" : itemType.getSimpleName());

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(itemType);
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

        if (!(obj instanceof QuerySourceItemForPrimType)) {
            return false;
        }

        final QuerySourceItemForPrimType<?> other = (QuerySourceItemForPrimType<?>) obj;

        return Objects.equals(itemType, other.itemType);
    }
}
