package ua.com.fielden.platform.entity.query;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao2.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.QueryModelResult.ResultPropertyInfo;
import ua.com.fielden.platform.entity.query.generation.DbVersion;
import ua.com.fielden.platform.entity.query.generation.EntQueryGenerator;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.generation.elements.YieldModel;
import ua.com.fielden.platform.entity.query.generation.elements.YieldsModel;

public class ModelResultProducer {

    public QueryModelResult getModelResult(final QueryExecutionModel qem, final DbVersion dbVersion, final MappingsGenerator mappingsGenerator) {
	final EntQueryGenerator gen = new EntQueryGenerator(dbVersion, mappingsGenerator);
	final EntQuery entQuery = gen.generateEntQueryAsResultQuery(qem.getQueryModel(), qem.getParamValues());
	final String sql = entQuery.sql();
	return new QueryModelResult(entQuery.getResultType(), sql, getResultPropsInfos(entQuery.getYields()), entQuery.getValuesForSqlParams());
    }

    private SortedSet<ResultPropertyInfo> getResultPropsInfos(final YieldsModel model) {
	final SortedSet<ResultPropertyInfo> result = new TreeSet<ResultPropertyInfo>();
	for (final Map.Entry<String, YieldModel> yieldEntry : model.getYields().entrySet()) {
	    result.add(new ResultPropertyInfo(yieldEntry.getKey(), yieldEntry.getValue().getInfo().getColumn()/*getSqlAlias()*/, yieldEntry.getValue().getInfo().getJavaType()/*getType()*/));
	}
	return result;
    }
}
