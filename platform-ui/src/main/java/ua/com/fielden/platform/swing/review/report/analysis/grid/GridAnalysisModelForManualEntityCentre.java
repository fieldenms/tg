package ua.com.fielden.platform.swing.review.report.analysis.grid;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoin;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.centre.configuration.ManualCentreConfigurationModel;

public class GridAnalysisModelForManualEntityCentre<T extends AbstractEntity<?>, M extends AbstractEntity<?>> extends GridAnalysisModel<T, ICentreDomainTreeManagerAndEnhancer> {

    public GridAnalysisModelForManualEntityCentre(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria) {
	super(criteria);
    }

    @SuppressWarnings("unchecked")
    public String getLinkProperty(){
	return ((ManualCentreConfigurationModel<T, M>) getAnalysisView().getOwner().getOwner().getOwner().getModel()).getLinkProperty();
    }

    @SuppressWarnings("unchecked")
    public M getLinkEntity(){
	return ((ManualCentreConfigurationModel<T, M>) getAnalysisView().getOwner().getOwner().getOwner().getModel()).getLinkPropertyValue();
    }

    @Override
    protected EntityResultQueryModel<T> createQueryModel() {
        return where().prop(DynamicQueryBuilder.createConditionProperty(getLinkProperty())).//
        /*  */eq().val(getLinkEntity()).model();
    }

    private IWhere0<T> where() {
	final ICompleted<T> notOrderedQuery = DynamicQueryBuilder.createQuery(getCriteria().getManagedType(), getCriteria().createQueryProperties());
	if (notOrderedQuery instanceof IJoin) {
	    return ((IJoin<T>) notOrderedQuery).where();
	} else {
	    return ((ICompoundCondition0<T>) notOrderedQuery).and();
	}
    }
}
