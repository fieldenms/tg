package ua.com.fielden.platform.entity.query.model;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.antlr.tokens.util.ListTokenSource;
import ua.com.fielden.platform.utils.ToString;
import ua.com.fielden.platform.utils.ToString.IFormat;
import ua.com.fielden.platform.utils.ToString.IFormattable;

import java.util.Objects;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.rightPad;

public abstract class QueryModel<T extends AbstractEntity<?>> extends AbstractModel implements IFormattable {
    private final Class<T> resultType;
    private boolean filterable = false;
    private final boolean yieldAll;
    private boolean shouldMaterialiseCalcPropsAsColumnsInSqlQuery;

    public QueryModel(final ListTokenSource tokens, final Class<T> resultType, final boolean yieldAll) {
        super(tokens);
        this.resultType = resultType;
        this.yieldAll = yieldAll;
        this.shouldMaterialiseCalcPropsAsColumnsInSqlQuery = false;
    }

    public boolean shouldMaterialiseCalcPropsAsColumnsInSqlQuery() {
        return shouldMaterialiseCalcPropsAsColumnsInSqlQuery;
    }

    protected QueryModel<T> setShouldMaterialiseCalcPropsAsColumnsInSqlQuery(final boolean value) {
        this.shouldMaterialiseCalcPropsAsColumnsInSqlQuery = value;
        return this;
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
    public String toString(final IFormat format) {
        return format.toString(this)
                .add("resultType", resultType)
                .add("filterable", filterable)
                .add("yieldAll", yieldAll)
                .add("shouldMaterialiseCalcPropsAsColumnsInSqlQuery", shouldMaterialiseCalcPropsAsColumnsInSqlQuery)
                .addLiteral(super.toString())
                .$();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
        result = prime * result + (yieldAll ? 1231 : 1237);
        result = prime * result + (shouldMaterialiseCalcPropsAsColumnsInSqlQuery ? 1231 : 1237);
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

        final QueryModel<?> other = (QueryModel<?>) obj;

        return Objects.equals(resultType, other.resultType) && (yieldAll == other.yieldAll) && (shouldMaterialiseCalcPropsAsColumnsInSqlQuery == other.shouldMaterialiseCalcPropsAsColumnsInSqlQuery);
    }
}
