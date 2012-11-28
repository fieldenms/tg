package ua.com.fielden.platform.report.query.generation;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/**
 * Structure that holds the list of query models and enhanced type that was used for query model generation and binary representation of the generated type.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class AnalysisResultClassBundle<T extends AbstractEntity<?>> {

    private final Class<T> generatedClass;
    private final byte[] generatedClassRepresentation;
    private final List<QueryExecutionModel<T, EntityResultQueryModel<T>>> queries;

    public AnalysisResultClassBundle(final Class<T> generatedClass, final byte[] generatedClassRepresentation, final List<QueryExecutionModel<T, EntityResultQueryModel<T>>> queries){
	this.generatedClass = generatedClass;
	this.generatedClassRepresentation = generatedClassRepresentation;
	this.queries = new ArrayList<>();
	if (queries != null) {
	    this.queries.addAll(queries);
	}
    }

    public Class<T> getGeneratedClass() {
	return generatedClass;
    }

    public byte[] getGeneratedClassRepresentation() {
	return generatedClassRepresentation;
    }

    public List<QueryExecutionModel<T, EntityResultQueryModel<T>>> getQueries() {
	return queries;
    }
}
