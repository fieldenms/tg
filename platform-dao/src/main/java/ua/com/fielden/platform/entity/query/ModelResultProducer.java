package ua.com.fielden.platform.entity.query;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao2.PropertyPersistenceInfo;
import ua.com.fielden.platform.dao2.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.generation.DbVersion;
import ua.com.fielden.platform.entity.query.generation.EntQueryGenerator;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.generation.elements.Yield;
import ua.com.fielden.platform.entity.query.generation.elements.Yields;

public class ModelResultProducer {

    public QueryModelResult getModelResult(final QueryExecutionModel qem, final DbVersion dbVersion, final MappingsGenerator mappingsGenerator, final IFilter filter, final String username) {
	final EntQueryGenerator gen = new EntQueryGenerator(dbVersion, mappingsGenerator, filter, username);
	final EntQuery entQuery = gen.generateEntQueryAsResultQuery(qem.getQueryModel(), qem.getOrderModel(), qem.getParamValues());
	final String sql = entQuery.sql();
	return new QueryModelResult(entQuery.getResultType(), sql, getResultPropsInfos(entQuery.getYields()), entQuery.getValuesForSqlParams());
    }

    private SortedSet<PropertyPersistenceInfo> getResultPropsInfos(final Yields model) {
	final SortedSet<PropertyPersistenceInfo> result = new TreeSet<PropertyPersistenceInfo>();
	for (final Map.Entry<String, Yield> yieldEntry : model.getYields().entrySet()) {
	    //result.add(new ResultPropertyInfo(yieldEntry.getKey(), yieldEntry.getValue().getInfo().getColumn(), yieldEntry.getValue().getInfo().getJavaType()));
	    result.add(new PropertyPersistenceInfo.Builder(yieldEntry.getKey(), yieldEntry.getValue().getInfo().getJavaType(), false/*?*/). //
		    column(yieldEntry.getValue().getInfo().getColumn()). //
		    hibType(yieldEntry.getValue().getInfo().getHibType()). //
		    build());
	}
	return result;
    }
}
