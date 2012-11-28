package ua.com.fielden.platform.swing.review.report.analysis.grid.configuration;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.DefaultGridAnalysisToolBarCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IToolBarCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;
import ua.com.fielden.platform.swing.review.report.analysis.wizard.AnalysisWizardView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;

public class GridConfigurationView<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractAnalysisConfigurationView<T, CDTME, IAbstractAnalysisDomainTreeManager, GridAnalysisView<T, CDTME>> {

    private static final long serialVersionUID = -7385497832761082274L;


    private final IToolBarCustomiser<GridAnalysisView<T, CDTME>> toolBarCustomiser;

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
		    final AbstractEntityCentre<T, CDTME> owner, //
		    final BlockingIndefiniteProgressLayer progressLayer){
	 return new GridConfigurationView<>(model, owner, null, progressLayer);
    }

    /**
     * Creates and returns main details analysis with specified analysis customiser.
     *
     * @param model
     * @param owner
     * @param toolBarCustomiser
     * @param progressLayer
     * @return
     */
    public static <T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> GridConfigurationView<T, CDTME> createMainDetailsWithSpecificCustomiser( //
	     	    final GridConfigurationModel<T, CDTME> model, //
		    final AbstractEntityCentre<T, CDTME> owner, //
		    final IToolBarCustomiser<GridAnalysisView<T, CDTME>> toolBarCustomiser,//
		    final BlockingIndefiniteProgressLayer progressLayer){
	 return new GridConfigurationView<>(model, owner, toolBarCustomiser, progressLayer);
    }

    /**
     * Initialises Grid analysis configuration view with specific tool bar customiser.
     *
     * @param model
     * @param owner
     * @param toolBarCustomiser
     * @param progressLayer
     */
    protected GridConfigurationView(//
	    final GridConfigurationModel<T, CDTME> model, //
	    final AbstractEntityCentre<T, CDTME> owner, //
	    final IToolBarCustomiser<GridAnalysisView<T, CDTME>> toolBarCustomiser, //
	    final BlockingIndefiniteProgressLayer progressLayer){
	super(model, null, owner, progressLayer);
	this.toolBarCustomiser = toolBarCustomiser == null ? new DefaultGridAnalysisToolBarCustomiser<T, CDTME>() : toolBarCustomiser;
    }

    public IToolBarCustomiser<GridAnalysisView<T, CDTME>> getAnalysisCustomiser() {
	return toolBarCustomiser;
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

}
