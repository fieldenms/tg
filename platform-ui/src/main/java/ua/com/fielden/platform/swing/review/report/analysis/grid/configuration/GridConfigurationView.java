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
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IToolbarCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.swing.review.report.analysis.wizard.AnalysisWizardView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

public class GridConfigurationView<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisConfigurationView<T, CDTME, IAbstractAnalysisDomainTreeManager, GridAnalysisView<T, CDTME>> {

    private static final long serialVersionUID = -7385497832761082274L;


    private final IToolbarCustomiser<GridAnalysisView<T, CDTME>> toolbarCustomiser;

    /**
     * Creates and returns main details analysis with default analysis customiser.
     *
     * @param model
     * @param owner
     * @param progressLayer
     * @return
     */
    public static <T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> GridConfigurationView<T, CDTME> createMainDetailsWithDefaultCustomiser( //
	    	    final GridConfigurationModel<T, CDTME> model, //
	    	    final Map<Object, DetailsFrame> detailsCache, //
	    	    final IDetailsCustomiser detailsCustomiser, //
		    final AbstractEntityCentre<T, CDTME> owner, //
		    final BlockingIndefiniteProgressLayer progressLayer){
	 return new GridConfigurationView<>(model, detailsCache, detailsCustomiser, owner, null, progressLayer);
    }

    /**
     * Creates and returns main details analysis with specified analysis customiser.
     *
     * @param model
     * @param owner
     * @param toolbarCustomiser
     * @param progressLayer
     * @return
     */
    public static <T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> GridConfigurationView<T, CDTME> createMainDetailsWithSpecificCustomiser( //
	     	    final GridConfigurationModel<T, CDTME> model, //
	     	    final Map<Object, DetailsFrame> detailsCache, //
	     	    final IDetailsCustomiser detailsCustomiser, //
		    final AbstractEntityCentre<T, CDTME> owner, //
		    final IToolbarCustomiser<GridAnalysisView<T, CDTME>> toolbarCustomiser,//
		    final BlockingIndefiniteProgressLayer progressLayer){
	 return new GridConfigurationView<>(model, detailsCache, detailsCustomiser, owner, toolbarCustomiser, progressLayer);
    }

    /**
     * Initialises Grid analysis configuration view with specific tool bar customiser.
     *
     * @param model
     * @param owner
     * @param toolbarCustomiser
     * @param progressLayer
     */
    protected GridConfigurationView(//
	    final GridConfigurationModel<T, CDTME> model, //
	    final Map<Object, DetailsFrame> detailsCache, //
	    final IDetailsCustomiser detailsCustomiser, //
	    final AbstractEntityCentre<T, CDTME> owner, //
	    final IToolbarCustomiser<GridAnalysisView<T, CDTME>> toolbarCustomiser, //
	    final BlockingIndefiniteProgressLayer progressLayer){
	super(model, detailsCache, detailsCustomiser, owner, progressLayer);
	this.toolbarCustomiser = toolbarCustomiser == null ? new DefaultGridAnalysisToolbarCustomiser<T, CDTME>() : toolbarCustomiser;
    }

    public IToolbarCustomiser<GridAnalysisView<T, CDTME>> getToolbarCustomiser() {
	return toolbarCustomiser;
    }

    @Override
    public GridConfigurationModel<T, CDTME> getModel() {
	return (GridConfigurationModel<T, CDTME>)super.getModel();
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
