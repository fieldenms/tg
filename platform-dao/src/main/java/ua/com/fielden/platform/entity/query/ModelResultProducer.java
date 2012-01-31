package ua.com.fielden.platform.entity.query;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.dao2.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.model.builders.DbVersion;
import ua.com.fielden.platform.entity.query.model.builders.EntQueryGenerator;
import ua.com.fielden.platform.entity.query.model.elements.EntQuery;
import ua.com.fielden.platform.entity.query.model.elements.YieldModel;
import ua.com.fielden.platform.entity.query.model.elements.YieldsModel;
import ua.com.fielden.platform.entity.query.model.structure.QueryModelResult;
import ua.com.fielden.platform.entity.query.model.structure.QueryModelResult.ResultPropertyInfo;

public class ModelResultProducer {

    public QueryModelResult getModelResult(final QueryExecutionModel query, final DbVersion dbVersion) {
	final EntQueryGenerator gen = new EntQueryGenerator(dbVersion);
	final EntQuery entQuery = gen.generateEntQuery(query.getQueryModel(), query.getParamValues());
	final String sql = entQuery.sql();
	return new QueryModelResult(entQuery.getResultType(), sql, getResultPropsInfos(entQuery.getYields()), entQuery.getValuesForParams());
    }

    private SortedSet<ResultPropertyInfo> getResultPropsInfos(final YieldsModel model) {
	final SortedSet<ResultPropertyInfo> result = new TreeSet<ResultPropertyInfo>();
	for (final Map.Entry<String, YieldModel> yieldEntry : model.getYields().entrySet()) {
	    result.add(new ResultPropertyInfo(yieldEntry.getKey(), yieldEntry.getValue().getSqlAlias()));
	}
	return result;
    }
}
