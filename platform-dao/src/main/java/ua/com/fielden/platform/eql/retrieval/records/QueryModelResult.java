package ua.com.fielden.platform.eql.retrieval.records;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IRetrievalModel;

public record QueryModelResult<T extends AbstractEntity<?>> (
        Class<T> resultType,
        String sql,
        List<YieldedColumn> yieldedColumns,
        Map<String, Object> paramValues,
        IRetrievalModel<T> fetchModel) {
}
