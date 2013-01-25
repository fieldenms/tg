package ua.com.fielden.platform.report.query.generation;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IMultipleDecDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

public class MultipleDecAnalysisQueryGenerator<T extends AbstractEntity<?>> extends GroupAnalysisQueryGenerator<T> {

    public MultipleDecAnalysisQueryGenerator(final Class<T> root, final ICentreDomainTreeManagerAndEnhancer cdtme, final IMultipleDecDomainTreeManager adtm) {
	super(root, cdtme, adtm);
    }

    @SuppressWarnings("unchecked")
    @Override
    public AnalysisResultClassBundle<T> generateQueryModel() {
	//Generate analysis result map that is based on analysis domain manager associated with this query generator.
	final AnalysisResultClassBundle<T> classBundle = (AnalysisResultClassBundle<T>) AnalysisResultClass.generateAnalysisQueryClass(//
		(Class<T>)getCdtme().getEnhancer().getManagedType(getRoot()),//
		adtm().getFirstTick().checkedProperties(getRoot()),//
		adtm().getSecondTick().checkedProperties(getRoot()),//
		adtm().getFirstTick().usedProperties(getRoot()),//
		adtm().getSecondTick().usedProperties(getRoot()));
	final List<QueryExecutionModel<T, EntityResultQueryModel<T>>> result = new ArrayList<>();
	result.add(createQueryAndGroupBy(classBundle.getGeneratedClass(), adtm().getFirstTick().usedProperties(getRoot())));
	return new AnalysisResultClassBundle<>(classBundle.getGeneratedClass(), classBundle.getGeneratedClassRepresentation(), result);
    }

}
