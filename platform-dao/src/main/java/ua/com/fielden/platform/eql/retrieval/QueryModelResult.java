package ua.com.fielden.platform.eql.retrieval;

import static java.util.Collections.unmodifiableMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IRetrievalModel;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;

public final class QueryModelResult<T extends AbstractEntity<?>> {
    public Class<T> resultType;
    public String sql;
    public final IRetrievalModel<T> fetchModel;
    private Map<String, Object> paramValues;
    private final SortedSet<ResultQueryYieldDetails> yieldedPropsInfo;

    public QueryModelResult(final Class<T> resultType, final String sql, final SortedSet<ResultQueryYieldDetails> yieldedPropsInfo, final Map<String, Object> paramValues, final IRetrievalModel<T> fetchModel) {
        this.resultType = resultType;
        this.sql = sql;
        this.paramValues = paramValues;
        this.yieldedPropsInfo = yieldedPropsInfo;
        this.fetchModel = fetchModel;
    }

    public Map<String, Object> getParamValues() {
        return unmodifiableMap(paramValues);
    }

    public SortedSet<ResultQueryYieldDetails> getYieldedPropsInfo() {
        return Collections.unmodifiableSortedSet(yieldedPropsInfo);
    }
    
    public List<T2<String, ResultQueryYieldDetails>> getSortedYieldedPropsInfo() {
        final List<T2<String, ResultQueryYieldDetails>> result = new ArrayList<>();
        for (ResultQueryYieldDetails el : yieldedPropsInfo) {
            result.add(T2.t2(el.name, el));
        }
        
        return result;
    }
    
    public boolean idOnlyQuery() {
        return EntityUtils.isPersistedEntityType(resultType) && yieldedPropsInfo.size() == 1 && "id".equals(yieldedPropsInfo.iterator().next().name);
    }
}