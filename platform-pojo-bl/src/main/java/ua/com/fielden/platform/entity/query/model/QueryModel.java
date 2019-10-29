package ua.com.fielden.platform.entity.query.model;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.rightPad;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public abstract class QueryModel<T extends AbstractEntity<?>> extends AbstractModel {
    private Class<T> resultType;
    private boolean filterable = false;
    private boolean yieldAll;

    protected QueryModel() {
    }

    public QueryModel(final List<Pair<TokenCategory, Object>> tokens, final Class<T> resultType, final boolean yieldAll) {
        super(tokens);
        this.resultType = resultType;
        this.yieldAll = yieldAll;
    }

    public Class<T> getResultType() {
        return resultType;
    }

    public boolean isFilterable() {
        return filterable;
    }

    public boolean isYieldAll() {
        return yieldAll;
    }

    public QueryModel<T> setFilterable(final boolean filterable) {
        this.filterable = filterable;
        return this;
    }

    @Override
    public String toString() {
        return super.toString() + format("%n\t%s%s", rightPad("is filterable", 32, '.'), filterable);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
        result = prime * result + (yieldAll ? 1231 : 1237);
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
        if (!(obj instanceof QueryModel)) {
            return false;
        }
        final QueryModel other = (QueryModel) obj;
        if (resultType == null) {
            if (other.resultType != null) {
                return false;
            }
        } else if (!resultType.equals(other.resultType)) {
            return false;
        }
        if (yieldAll != other.yieldAll) {
            return false;
        }
        return true;
    }
}