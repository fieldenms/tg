package ua.com.fielden.platform.swing.review.report.analysis.grid.configuration;

import java.util.Map;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.details.customiser.IDetailsCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.DefaultGridAnalysisToolbarCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.DefaultGridAnalysisViewCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IAnalysisViewCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IToolbarCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.swing.review.report.analysis.wizard.AnalysisWizardView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

public class GridConfigurationView<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisConfigurationView<T, CDTME, IAbstractAnalysisDomainTreeManager, GridAnalysisView<T, CDTME>> {

    private static final long serialVersionUID = -7385497832761082274L;

    private final IToolbarCustomiser<GridAnalysisView<T, CDTME>> toolbarCustomiser;

    private final IAnalysisViewCustomiser<GridAnalysisView<T, CDTME>> analysisViewCustomiser;

    /**
     * Creates the {@link GridConfigurationView} without details support.
     *
     * @param model
     * @param owner
     * @param toolbarCustomiser
     * @param analysisViewCustomiser
     * @param progressLayer
     * @return
     */
    public static <E extends AbstractEntity<?>, C extends ICentreDomainTreeManagerAndEnhancer> GridConfigurationView<E, C> createDefaultConfigView(//
	    final GridConfigurationModel<E, C> model, //
	    final AbstractEntityCentre<E, C> owner, //
	    final BlockingIndefiniteProgressLayer progressLayer) {
	return new GridConfigurationView<>(model,owner, progressLayer);
    }

    /**
     * Creates the {@link GridConfigurationView} instance with both customiser: tool bar and view customisers.
     *
     * @param model
     * @param owner
     * @param toolbarCustomiser
     * @param analysisViewCustomiser
     * @param progressLayer
     * @return
     */
    public static <E extends AbstractEntity<?>, C extends ICentreDomainTreeManagerAndEnhancer> GridConfigurationView<E, C> createCustomisableConfigView(//
	    final GridConfigurationModel<E, C> model, //
	    final AbstractEntityCentre<E, C> owner, //
	    final IToolbarCustomiser<GridAnalysisView<E, C>> toolbarCustomiser, //
	    final IAnalysisViewCustomiser<GridAnalysisView<E, C>> analysisViewCustomiser, //
	    final BlockingIndefiniteProgressLayer progressLayer) {
	return new GridConfigurationView<>(model, owner, toolbarCustomiser, analysisViewCustomiser, progressLayer);
    }

    /**
     * Creates the {@link GridConfigurationView} instance with both customiser: tool bar, details and view customisers.
     *
     * @param model
     * @param detailsCache
     * @param detailsCustomiser
     * @param owner
     * @param toolbarCustomiser
     * @param analysisViewCustomiser
     * @param progressLayer
     * @return
     */
    public static <E extends AbstractEntity<?>, C extends ICentreDomainTreeManagerAndEnhancer> GridConfigurationView<E, C> createCustomisableConfigView(//
	    final GridConfigurationModel<E, C> model, //
	    final AbstractEntityCentre<E, C> owner, //
	    final Map<Object, DetailsFrame> detailsCache, //
	    final IDetailsCustomiser detailsCustomiser, //
	    final IToolbarCustomiser<GridAnalysisView<E, C>> toolbarCustomiser, //
	    final IAnalysisViewCustomiser<GridAnalysisView<E, C>> analysisViewCustomiser, //
	    final BlockingIndefiniteProgressLayer progressLayer) {
	return new GridConfigurationView<>(model, detailsCache, detailsCustomiser, owner, toolbarCustomiser, analysisViewCustomiser, progressLayer);
    }


    /**
     * Initiates new {@link GridConfigurationView} with tool bar and view customisers.
     *
     * @param model
     * @param owner
     * @param toolbarCustomiser
     * @param analysisViewCustomiser
     * @param progressLayer
     */
   protected GridConfigurationView(//
	   final GridConfigurationModel<T, CDTME> model, //
	    final AbstractEntityCentre<T, CDTME> owner, //
	    final IToolbarCustomiser<GridAnalysisView<T, CDTME>> toolbarCustomiser, //
	    final IAnalysisViewCustomiser<GridAnalysisView<T, CDTME>> analysisViewCustomiser, //
	    final BlockingIndefiniteProgressLayer progressLayer) {
	this(model, null, null, owner, toolbarCustomiser, analysisViewCustomiser, progressLayer);
   }

    /**
     * Initialises new {@link GridConfigurationView} without details cache and customisers (e.a. tool bar and view customiser).
     *
     * @param model
     * @param owner
     * @param progressLayer
     */
    protected GridConfigurationView(//
	    final GridConfigurationModel<T, CDTME> model, //
	    final AbstractEntityCentre<T, CDTME> owner, //
	    final BlockingIndefiniteProgressLayer progressLayer) {
	this(model, null, null, owner, null, null, progressLayer);
    }

    /**
     * Initialises Grid analysis configuration view with specific tool bar customiser.
     *
     * @param model
     * @param owner
     * @param toolbarCustomiser
     * @param progressLayer
     */
    private GridConfigurationView(//
	    final GridConfigurationModel<T, CDTME> model, //
	    final Map<Object, DetailsFrame> detailsCache, //
	    final IDetailsCustomiser detailsCustomiser, //
	    final AbstractEntityCentre<T, CDTME> owner, //
	    final IToolbarCustomiser<GridAnalysisView<T, CDTME>> toolbarCustomiser, //
	    final IAnalysisViewCustomiser<GridAnalysisView<T, CDTME>> analysisViewCustomiser, //
	    final BlockingIndefiniteProgressLayer progressLayer){
	super(model, detailsCache, detailsCustomiser, owner, progressLayer);
	this.toolbarCustomiser = toolbarCustomiser == null ? new DefaultGridAnalysisToolbarCustomiser<T, CDTME>() : toolbarCustomiser;
	this.analysisViewCustomiser = analysisViewCustomiser == null ? new DefaultGridAnalysisViewCustomiser<T, CDTME>() : analysisViewCustomiser;
    }

    public IToolbarCustomiser<GridAnalysisView<T, CDTME>> getToolbarCustomiser() {
	return toolbarCustomiser;
    }

    public IAnalysisViewCustomiser<GridAnalysisView<T, CDTME>> getAnalysisViewCustomiser() {
	return analysisViewCustomiser;
    }

    @Override
    public GridConfigurationModel<T, CDTME> getModel() {
	return (GridConfigurationModel<T, CDTME>) super.getModel();
    }

    @Override
    protected GridAnalysisView<T, CDTME> createConfigurableView() {
	return new GridAnalysisView<T, CDTME>(getModel().createGridAnalysisModel(), this);
    }

    @Override
    protected AnalysisWizardView<T, CDTME> createWizardView() {
	throw new UnsupportedOperationException("Main details can not be configured!");
    }

    @Override
    public void close() {
	if (getPreviousView() != null) {
	    getPreviousView().getModel().stopDeltaRetrievalIfAny();
	}

	super.close();
    }

}
