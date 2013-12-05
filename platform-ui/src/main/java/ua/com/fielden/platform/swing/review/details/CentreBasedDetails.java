package ua.com.fielden.platform.swing.review.details;

import ua.com.fielden.platform.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.report.query.generation.IReportQueryGenerator;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IToolbarCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.swing.review.report.analysis.query.customiser.PlainQueryCustomiser;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationWithoutCriteriaModel;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationWithoutCriteriaView;
import ua.com.fielden.platform.swing.review.report.centre.factory.DefaultGridAnalysisFactory;
import ua.com.fielden.platform.swing.review.report.centre.factory.IAnalysisFactory;
import ua.com.fielden.platform.swing.view.ICloseHook;

/**
 * The detail that is based on the the entity centre.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <ET>
 */
public abstract class CentreBasedDetails<T extends AbstractAnalysisDetailsData<ET>, ET extends AbstractEntity<?>> implements IDetails<T> {

    protected final EntityFactory entityFactory;
    protected final ICriteriaGenerator criteriaGenerator;
    protected final IEntityMasterManager masterManager;

    /**
     * Creates an empty centre based details.
     *
     * @param entityFactory
     * @param criteriaGenerator
     * @param masterManager
     */
    public CentreBasedDetails(//
	    final EntityFactory entityFactory, //
	    final ICriteriaGenerator criteriaGenerator, //
	    final IEntityMasterManager masterManager){
	this.entityFactory = entityFactory;
	this.criteriaGenerator = criteriaGenerator;
	this.masterManager = masterManager;
    }

    @Override
    public DetailsFrame createDetailsView(final T detailsParam, final ICloseHook<DetailsFrame> closeHook) {
	final CentreConfigurationWithoutCriteriaModel<ET> detailsModel = new CentreConfigurationWithoutCriteriaModel<>(//
		detailsParam.root, //
		null, createDefaultAnalysisFactory(detailsParam),//
		entityFactory, criteriaGenerator, masterManager, getCdtme(detailsParam));
	final BlockingIndefiniteProgressLayer progressLayer = new BlockingIndefiniteProgressLayer(null, "Loading");
	final CentreConfigurationWithoutCriteriaView<ET> detailsConfigView = new CentreConfigurationWithoutCriteriaView<>(detailsModel, progressLayer);
	progressLayer.setView(detailsConfigView);
	final DetailsFrame detailsFrame = new DetailsFrame(detailsParam, detailsParam.getFrameTitle(), progressLayer, closeHook);
	detailsConfigView.open();
	return detailsFrame;
    }

    /**
     * Creates the details' analysis factory with empty tool bar.
     *
     * @param detailsParam
     * @return
     */
    protected IAnalysisFactory<ET, ?> createDefaultAnalysisFactory(final T detailsParam) {
	final DefaultGridAnalysisFactory<ET> analysisFactory = new DefaultGridAnalysisFactory<>();
	analysisFactory.setToolbarCustomiser(new IToolbarCustomiser<GridAnalysisView<ET, ICentreDomainTreeManagerAndEnhancer>>() {

	    @Override
	    public ActionPanelBuilder createToolbar(final GridAnalysisView<ET, ICentreDomainTreeManagerAndEnhancer> analysisView) {
		return new ActionPanelBuilder();
	    }
	});
	analysisFactory.setQueryCustomiser(new PlainQueryCustomiser<>(createQueryGenerator(detailsParam)));
	return analysisFactory;
    }

    /**
     * Creates the query report generator for this details.
     *
     * @param detailsParam
     * @return
     */
    protected abstract IReportQueryGenerator<ET> createQueryGenerator(final T detailsParam);

    /**
     * Returns the instance of {@link ICentreDomainTreeManagerAndEnhancer} on which this details is based on.
     *
     * @return
     */
    protected abstract ICentreDomainTreeManagerAndEnhancer getCdtme(final T detailsParam);
}
