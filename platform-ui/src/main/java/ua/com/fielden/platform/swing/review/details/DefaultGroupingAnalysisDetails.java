package ua.com.fielden.platform.swing.review.details;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.report.query.generation.DetailsQueryGenerator;
import ua.com.fielden.platform.report.query.generation.IReportQueryGenerator;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;

public class DefaultGroupingAnalysisDetails<T extends AbstractEntity<?>> extends CentreBasedDetails<AnalysisDetailsData<T>, T> {

    public DefaultGroupingAnalysisDetails(//
	    final EntityFactory entityFactory, //
	    final ICriteriaGenerator criteriaGenerator, //
	    final IEntityMasterManager masterManager){
	super(entityFactory, criteriaGenerator, masterManager);
    }

    @Override
    protected IReportQueryGenerator<T> createQueryGenerator(final AnalysisDetailsData<T> detailsParam) {
	return new DetailsQueryGenerator<T, ICentreDomainTreeManagerAndEnhancer>(detailsParam.root, detailsParam.getBaseCdtme(), detailsParam.getLinkPropValuePairs());
    }

    @Override
    protected ICentreDomainTreeManagerAndEnhancer getCdtme(final AnalysisDetailsData<T> detailsParam) {
	return detailsParam.getBaseCdtme();
    }

}
