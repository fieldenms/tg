package ua.com.fielden.platform.swing.review.report.analysis.grid;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.IAnalysisQueryCustomiser;

public class GridAnalysisModelForLocator<T extends AbstractEntity<?>> extends GridAnalysisModel<T, ILocatorDomainTreeManagerAndEnhancer> {

    public GridAnalysisModelForLocator(final EntityQueryCriteria<ILocatorDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, final IAnalysisQueryCustomiser<T, GridAnalysisModel<T, ILocatorDomainTreeManagerAndEnhancer>> queryCustomiser) {
	super(criteria, queryCustomiser);
    }

    @Override
    public Result executeAnalysisQuery() {
	getAnalysisView().resetLocatorSelection();
	getAnalysisView().getEgiPanel().getEgi().getSelectionModel().clearSelection();
	return super.executeAnalysisQuery();

    }

    @Override
    protected GridAnalysisViewForLocator<T> getAnalysisView() {
        return (GridAnalysisViewForLocator<T>)super.getAnalysisView();
    }

}
