package ua.com.fielden.platform.entity.query;

import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity.query.model.structure.QueryModelResult;

public class ModelResultProducer {

    //lets assume that all master model properties used within its subqueries are handled and transformed correctly and exist in master model query sources (IQuerySource)

    public QueryModelResult getModelResult(final QueryModel query, final fetch fetch) {

	//getSourceQueries recursively invokes getModelResult
	//getProperties
	//getSubqueries
	//getMasterQuery result

	final String sql = generateSql();
	return null;
    }

    private String generateSql() {
	// TODO Auto-generated method stub
	return null;
    }
}
