package ua.com.fielden.platform.entity.query;

import java.util.Map;
import java.util.SortedSet;

import ua.com.fielden.platform.dao2.PropertyPersistenceInfo;
import ua.com.fielden.platform.entity.AbstractEntity;

public final class QueryModelResult<T extends AbstractEntity<?>> {
    private Class<T> resultType;
    private String sql;
    private Map<String, Object> paramValues;
    private final SortedSet<PropertyPersistenceInfo> yieldedPropsInfo;

    public QueryModelResult(final Class<T> resultType, final String sql, final SortedSet<PropertyPersistenceInfo> yieldedPropsInfo, final Map<String, Object> paramValues) {
	this.resultType = resultType;
	this.sql = sql;
	this.paramValues = paramValues;
	this.yieldedPropsInfo = yieldedPropsInfo;
    }

    public Class<T> getResultType() {
        return resultType;
    }

    public String getSql() {
        return sql;
    }

    public Map<String, Object> getParamValues() {
        return paramValues;
    }

    public SortedSet<PropertyPersistenceInfo> getYieldedPropsInfo() {
	return yieldedPropsInfo;
    }
}