package ua.com.fielden.platform.swing.review.report.analysis.customiser;

import ua.com.fielden.platform.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;

/**
 * The default {@link GridAnalysisView} customiser.
 *
 * @author TG Team
 *
 */
public class DefaultGridAnalysisToolbarCustomiser<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> implements IToolbarCustomiser<GridAnalysisView<T, CDTME>> {

    @Override
    public ActionPanelBuilder createToolbar(final GridAnalysisView<T, CDTME> analysisView) {
	if (hasMasterManager(analysisView)) {
	    final ActionPanelBuilder panelBuilder = new ActionPanelBuilder();
	    return addMasterRelatedButtons(analysisView, panelBuilder);
	}
	return new ActionPanelBuilder();
    }


    /**
     * Adds the master related buttons to the specified panelBuilder.
     *
     * @param analysisView
     * @param panelBuilder
     * @return
     */
    protected final ActionPanelBuilder addMasterRelatedButtons(//
	    final GridAnalysisView<T, CDTME> analysisView,//
	    final ActionPanelBuilder panelBuilder){
	return panelBuilder//
	    .addButton(analysisView.getOpenMasterWithNewEntityCommand())//
	    .addButton(analysisView.getOpenMasterAndEditEntityCommand())//
	    .addButton(analysisView.getDeleteEntityCommand());
    }

    /**
     * Determines whether the specified {@link GridAnalysisView} has the entity master manager.
     *
     * @param analysisView
     * @return
     */
    protected final boolean hasMasterManager(final GridAnalysisView<T, CDTME> analysisView){
	return analysisView.getMasterManager() != null;
    }
}
