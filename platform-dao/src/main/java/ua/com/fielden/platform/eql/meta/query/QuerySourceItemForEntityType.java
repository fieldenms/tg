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
public class QuerySourceItemForEntityType<T extends AbstractEntity<?>> extends AbstractQuerySourceItem<T> {
    public final QuerySourceInfo<T> querySourceInfo;
    public final boolean nonnullable;

    public QuerySourceItemForEntityType(final String name, final QuerySourceInfo<T> querySourceInfo, final Object hibType, final boolean nonnullable) {
        this(name, querySourceInfo, hibType, nonnullable, null, false);
    }

    public QuerySourceItemForEntityType(final String name, final QuerySourceInfo<T> querySourceInfo, final Object hibType, final boolean nonnullable, final ExpressionModel expression, final boolean implicit) {
        super(name, hibType, expression, implicit);
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