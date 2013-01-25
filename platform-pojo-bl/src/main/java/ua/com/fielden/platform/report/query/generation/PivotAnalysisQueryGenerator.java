package ua.com.fielden.platform.report.query.generation;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

public class PivotAnalysisQueryGenerator<T extends AbstractEntity<?>> extends GroupAnalysisQueryGenerator<T> {

    public PivotAnalysisQueryGenerator(final Class<T> root, final ICentreDomainTreeManagerAndEnhancer cdtme, final IPivotDomainTreeManager pdtm) {
	super(root, cdtme, pdtm);
    }

    @SuppressWarnings("unchecked")
    @Override
    public AnalysisResultClassBundle<T> generateQueryModel() {

	//Generate analysis result map that is based on analysis domain manager associated with this query generator.
	final AnalysisResultClassBundle<T> classBundle = (AnalysisResultClassBundle<T>)AnalysisResultClass.generateAnalysisQueryClass(//
		(Class<T>)getCdtme().getEnhancer().getManagedType(getRoot()),//
		adtm().getFirstTick().checkedProperties(getRoot()),//
		adtm().getSecondTick().checkedProperties(getRoot()),//
		adtm().getFirstTick().usedProperties(getRoot()),//
		adtm().getSecondTick().usedProperties(getRoot()));

	final List<String> distributionProperties = adtm().getFirstTick().usedProperties(getRoot());

	final List<String> groups = new ArrayList<String>();
	final List<QueryExecutionModel<T, EntityResultQueryModel<T>>> resultQueryList = new ArrayList<>();
	resultQueryList.add(createQueryAndGroupBy(classBundle.getGeneratedClass(), groups));
	for(final String groupProperty : distributionProperties){
	    groups.add(groupProperty);
	    resultQueryList.add(createQueryAndGroupBy(classBundle.getGeneratedClass(), groups));
	}
	return new AnalysisResultClassBundle<>(classBundle.getGeneratedClass(), classBundle.getGeneratedClassRepresentation(), resultQueryList);
    }

}
