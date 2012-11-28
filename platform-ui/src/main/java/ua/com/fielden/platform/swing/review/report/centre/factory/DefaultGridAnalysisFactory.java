package ua.com.fielden.platform.swing.review.report.centre.factory;

import java.util.Map;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IToolbarCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.IAnalysisQueryCustomiser;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;


public class DefaultGridAnalysisFactory<T extends AbstractEntity<?>> implements IAnalysisFactory<T, GridConfigurationView<T, ICentreDomainTreeManagerAndEnhancer>> {

    private IToolbarCustomiser<GridAnalysisView<T,ICentreDomainTreeManagerAndEnhancer>> toolbarCustomiser;

    private IAnalysisQueryCustomiser<T, GridAnalysisModel<T,ICentreDomainTreeManagerAndEnhancer>> queryCustomiser;

    @Override
    public GridConfigurationView<T, ICentreDomainTreeManagerAndEnhancer> createAnalysis(//
	    final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, //
	    final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, //
	    final String name, //
	    final Map<Object, DetailsFrame> detailsCache,//
	    final BlockingIndefiniteProgressLayer progressLayer) {
	final GridConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> analysisModel =GridConfigurationModel.createWithCustomQueryCustomiser(criteria, queryCustomiser);
	return GridConfigurationView.createMainDetailsWithSpecificCustomiser(analysisModel, owner, toolbarCustomiser, progressLayer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DefaultGridAnalysisFactory<T> setToolbarCustomiser(final IToolbarCustomiser<?> toolbarCustomiser) {
	this.toolbarCustomiser = (IToolbarCustomiser<GridAnalysisView<T,ICentreDomainTreeManagerAndEnhancer>>)toolbarCustomiser;
	return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IAnalysisFactory<T, GridConfigurationView<T, ICentreDomainTreeManagerAndEnhancer>> setQueryCustomiser(final IAnalysisQueryCustomiser<T, ?> queryCustomiser) {
	this.queryCustomiser = (IAnalysisQueryCustomiser<T, GridAnalysisModel<T,ICentreDomainTreeManagerAndEnhancer>>)queryCustomiser;
	return this;
    }


}
