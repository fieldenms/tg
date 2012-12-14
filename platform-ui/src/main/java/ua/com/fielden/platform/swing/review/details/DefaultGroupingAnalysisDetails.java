package ua.com.fielden.platform.swing.review.details;

import ua.com.fielden.platform.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.report.query.generation.DetailsQueryGenerator;
import ua.com.fielden.platform.report.query.generation.IReportQueryGenerator;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.report.analysis.chart.AnalysisDetailsData;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IToolbarCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.PlainQueryCustomiser;
import ua.com.fielden.platform.swing.review.report.centre.configuration.AnalysisDetailsConfigurationModel;
import ua.com.fielden.platform.swing.review.report.centre.configuration.AnalysisDetailsConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.factory.DefaultGridAnalysisFactory;
import ua.com.fielden.platform.swing.review.report.centre.factory.IAnalysisFactory;
import ua.com.fielden.platform.swing.view.ICloseHook;

public class DefaultGroupingAnalysisDetails<T extends AbstractEntity<?>> implements IDetails<AnalysisDetailsData<T>> {

    private final EntityFactory entityFactory;
    private final ICriteriaGenerator criteriaGenerator;
    private final IEntityMasterManager masterManager;


    public DefaultGroupingAnalysisDetails(//
	    final EntityFactory entityFactory, //
	    final ICriteriaGenerator criteriaGenerator, //
	    final IEntityMasterManager masterManager){
	this.entityFactory = entityFactory;
	this.criteriaGenerator = criteriaGenerator;
	this.masterManager = masterManager;
    }

    private IAnalysisFactory<T, ?> createDetailsFactory(final AnalysisDetailsData<T> detailsParam) {
	final DefaultGridAnalysisFactory<T> analysisFactory = new DefaultGridAnalysisFactory<>();
	analysisFactory.setToolbarCustomiser(new IToolbarCustomiser<GridAnalysisView<T, ICentreDomainTreeManagerAndEnhancer>>() {

	    @Override
	    public ActionPanelBuilder createToolbar(final GridAnalysisView<T, ICentreDomainTreeManagerAndEnhancer> analysisView) {
		return new ActionPanelBuilder();
	    }
	});
	final IReportQueryGenerator<T> detailQueryGenerator = new DetailsQueryGenerator<T, ICentreDomainTreeManagerAndEnhancer>(detailsParam.getRoot(), detailsParam.getBaseCdtme(), detailsParam.getLinkPropValuePairs());
	analysisFactory.setQueryCustomiser(new PlainQueryCustomiser<>(detailQueryGenerator));
	return analysisFactory;
    }

    @Override
    public DetailsFrame createDetailsView(final AnalysisDetailsData<T> detailsParam, final ICloseHook<DetailsFrame> closeHook) {
	final AnalysisDetailsConfigurationModel<T> detailsModel = new AnalysisDetailsConfigurationModel<>(//
		detailsParam.getRoot(), //
		null, createDetailsFactory(detailsParam),//
		entityFactory, criteriaGenerator, masterManager, detailsParam.getBaseCdtme());
	final BlockingIndefiniteProgressLayer progressLayer = new BlockingIndefiniteProgressLayer(null, "Loading");
	final AnalysisDetailsConfigurationView<T> detailsConfigView = new AnalysisDetailsConfigurationView<>(detailsModel, progressLayer);
	progressLayer.setView(detailsConfigView);
	final DetailsFrame detailsFrame = new DetailsFrame(detailsParam, detailsParam.getFrameTitle(), progressLayer, closeHook);
	detailsConfigView.open();
	return detailsFrame;
    }

}
