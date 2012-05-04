package ua.com.fielden.platform.entity.query;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.dao.DomainPersistenceMetadataAnalyser;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.generation.DbVersion;
import ua.com.fielden.platform.entity.query.generation.EntQueryGenerator;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails;
import ua.com.fielden.platform.entity.query.generation.elements.Yield;
import ua.com.fielden.platform.entity.query.generation.elements.Yields;

public class ModelResultProducer {

    public <T extends AbstractEntity<?>> QueryModelResult<T> getModelResult(final QueryExecutionModel<T, ?> qem, final DbVersion dbVersion, final DomainPersistenceMetadataAnalyser domainPersistenceMetadataAnalyser, final IFilter filter, final String username) {
	final EntQueryGenerator gen = new EntQueryGenerator(dbVersion, domainPersistenceMetadataAnalyser, filter, username);
	final EntQuery entQuery = gen.generateEntQueryAsResultQuery(qem.getQueryModel(), qem.getOrderModel(), qem.getParamValues());
	final String sql = entQuery.sql();
	return new QueryModelResult<T>(entQuery.getResultType(), sql, getResultPropsInfos(entQuery.getYields()), entQuery.getValuesForSqlParams());
    }

    private SortedSet<ResultQueryYieldDetails> getResultPropsInfos(final Yields model) {
	final SortedSet<ResultQueryYieldDetails> result = new TreeSet<ResultQueryYieldDetails>();
	for (final Map.Entry<String, Yield> yieldEntry : model.getYields().entrySet()) {
	    result.add(new ResultQueryYieldDetails(yieldEntry.getKey(), yieldEntry.getValue().getInfo().getJavaType(), yieldEntry.getValue().getInfo().getHibType(), yieldEntry.getValue().getInfo().getColumn()));
	}
	return result;
    }
}
