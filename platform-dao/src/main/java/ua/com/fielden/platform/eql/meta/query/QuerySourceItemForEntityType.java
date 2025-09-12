package ua.com.fielden.platform.eql.meta.query;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.meta.CalcPropInfo;
import ua.com.fielden.platform.eql.stage1.PropResolutionProgress;

import java.util.Objects;

/**
 * A structure that represents a query source item of entity type {@code T}.
 *
 * @author TG Team
 */
public class QuerySourceItemForEntityType<T extends AbstractEntity<?>> extends AbstractQuerySourceItem<T> {
    public final QuerySourceInfo<T> querySourceInfo;
    public final boolean nonnullable;

    public QuerySourceItemForEntityType(
            final String name,
            final QuerySourceInfo<T> querySourceInfo,
            final @Nullable Object hibType,
            final boolean nonnullable)
    {
        this(name, querySourceInfo, hibType, nonnullable, null);
    }

    public QuerySourceItemForEntityType(
            final String name,
            final QuerySourceInfo<T> querySourceInfo,
            final @Nullable Object hibType,
            final boolean nonnullable,
            final @Nullable CalcPropInfo expression)
    {
        super(name, hibType, expression);
        this.querySourceInfo = querySourceInfo;
        this.nonnullable = nonnullable;
    }

    @Override
    public AbstractQuerySourceItem<T> cloneWithoutExpression() {
        return new QuerySourceItemForEntityType<T>(name, querySourceInfo, hibType, nonnullable);
    }

    @Override
    public PropResolutionProgress resolve(final PropResolutionProgress context) {
        return querySourceInfo.resolve(context);
    }

    @Override
    public Class<T> javaType() {
        return querySourceInfo.javaType();
    }

    @Override
    public String toString() {
        return String.format("%20s %20s", name, querySourceInfo.javaType().getSimpleName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + querySourceInfo.hashCode();
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

        if (!(obj instanceof QuerySourceItemForEntityType)) {
            return false;
        }

        final QuerySourceItemForEntityType<?> other = (QuerySourceItemForEntityType<?>) obj;

        return Objects.equals(querySourceInfo, other.querySourceInfo) && Objects.equals(nonnullable, other.nonnullable);
    }
}
