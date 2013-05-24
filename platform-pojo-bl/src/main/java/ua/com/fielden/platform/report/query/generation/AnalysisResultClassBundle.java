package ua.com.fielden.platform.report.query.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;

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
    private final List<IQueryComposer<T>> queries;
    private final ICentreDomainTreeManagerAndEnhancer cdtmeWithWhichAnalysesQueryHaveBeenCreated;

    public AnalysisResultClassBundle(final ICentreDomainTreeManagerAndEnhancer cdtmeWithWhichAnalysesQueryHaveBeenCreated, final Class<T> generatedClass, final byte[] generatedClassRepresentation, final List<IQueryComposer<T>> queries){
	this.cdtmeWithWhichAnalysesQueryHaveBeenCreated = cdtmeWithWhichAnalysesQueryHaveBeenCreated;
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

    public List<IQueryComposer<T>> getQueries() {
	return Collections.unmodifiableList(queries);
    }

    public ICentreDomainTreeManagerAndEnhancer getCdtmeWithWhichAnalysesQueryHaveBeenCreated() {
	return cdtmeWithWhichAnalysesQueryHaveBeenCreated;
    }
}
