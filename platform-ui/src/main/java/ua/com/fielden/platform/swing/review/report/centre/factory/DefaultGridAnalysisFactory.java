package ua.com.fielden.platform.swing.review.report.centre.factory;

import java.util.Map;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.details.customiser.IDetailsCustomiser;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IAnalysisViewCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IToolbarCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationModel;
import ua.com.fielden.platform.swing.review.report.analysis.grid.configuration.GridConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.IAnalysisQueryCustomiser;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;


public class DefaultGridAnalysisFactory<T extends AbstractEntity<?>> implements IAnalysisFactory<T, GridConfigurationView<T, ICentreDomainTreeManagerAndEnhancer>> {

    protected IToolbarCustomiser<GridAnalysisView<T,ICentreDomainTreeManagerAndEnhancer>> toolbarCustomiser;

    protected IAnalysisViewCustomiser<GridAnalysisView<T,ICentreDomainTreeManagerAndEnhancer>> analysisViewCustomiser;

    protected IAnalysisQueryCustomiser<T, GridAnalysisModel<T,ICentreDomainTreeManagerAndEnhancer>> queryCustomiser;

    /**
     * Details customiser for chart analysis.
     */
    protected IDetailsCustomiser detailsCustomiser = null;

    @Override
    public GridConfigurationView<T, ICentreDomainTreeManagerAndEnhancer> createAnalysis(//
	    final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, //
	    final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, //
	    final String name, //
	    final Map<Object, DetailsFrame> detailsCache, //
	    final BlockingIndefiniteProgressLayer progressLayer) {
	return GridConfigurationView.createCustomisableConfigView(createAnalysisModel(criteria), owner, detailsCache, detailsCustomiser, toolbarCustomiser, analysisViewCustomiser, progressLayer);
    }

    protected GridConfigurationModel<T, ICentreDomainTreeManagerAndEnhancer> createAnalysisModel(final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria){
	return GridConfigurationModel.createWithCustomQueryCustomiser(criteria, queryCustomiser);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DefaultGridAnalysisFactory<T> setToolbarCustomiser(final IToolbarCustomiser<?> toolbarCustomiser) {
	this.toolbarCustomiser = (IToolbarCustomiser<GridAnalysisView<T,ICentreDomainTreeManagerAndEnhancer>>)toolbarCustomiser;
	return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IAnalysisFactory<T, GridConfigurationView<T, ICentreDomainTreeManagerAndEnhancer>> setAnalysisViewCustomiser(final IAnalysisViewCustomiser<?> analysisViewCustomiser) {
	this.analysisViewCustomiser = (IAnalysisViewCustomiser<GridAnalysisView<T,ICentreDomainTreeManagerAndEnhancer>>)analysisViewCustomiser;
	return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IAnalysisFactory<T, GridConfigurationView<T, ICentreDomainTreeManagerAndEnhancer>> setQueryCustomiser(final IAnalysisQueryCustomiser<T, ?> queryCustomiser) {
	this.queryCustomiser = (IAnalysisQueryCustomiser<T, GridAnalysisModel<T,ICentreDomainTreeManagerAndEnhancer>>)queryCustomiser;
	return this;
    }

    @Override
    public IAnalysisFactory<T, GridConfigurationView<T, ICentreDomainTreeManagerAndEnhancer>> setDetailsCustomiser(final IDetailsCustomiser detailsCustomiser) {
	this.detailsCustomiser = detailsCustomiser;
	return this;
    }

    protected IAnalysisQueryCustomiser<T, GridAnalysisModel<T, ICentreDomainTreeManagerAndEnhancer>> getQueryCustomiser() {
	return queryCustomiser;
    }
}
