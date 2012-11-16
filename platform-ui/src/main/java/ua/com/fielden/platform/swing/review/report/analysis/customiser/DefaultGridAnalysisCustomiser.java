package ua.com.fielden.platform.swing.review.report.analysis.customiser;

import ua.com.fielden.platform.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.report.query.generation.IReportQueryGeneration;
import ua.com.fielden.platform.swing.review.details.IDetails;
import ua.com.fielden.platform.swing.review.report.analysis.grid.GridAnalysisView;

/**
 * The default {@link GridAnalysisView} customiser.
 *
 * @author TG Team
 *
 */
public class DefaultGridAnalysisCustomiser<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> implements IAnalysisCustomiser<GridAnalysisView<T, CDTME>> {

    @Override
    public ActionPanelBuilder createToolBar(final GridAnalysisView<T, CDTME> analysisView) {
	if (hasMasterManager(analysisView)) {
	    final ActionPanelBuilder panelBuilder = new ActionPanelBuilder();
	    return addMasterRelatedButtons(analysisView, panelBuilder);
	}
	return null;
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
	    .addButton(analysisView.getRemoveEntityCommand());
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


    @Override
    public <DT> IDetails<DT> getDetails(final Class<DT> detailsParamType) {
	// TODO Auto-generated method stub
	return null;
    }


    @Override
    public <E extends AbstractEntity<?>> IReportQueryGeneration<E> getQueryGenerator(final GridAnalysisView<T, CDTME> analysisView, final Class<E> queryClass) {
	// TODO Auto-generated method stub
	return null;
    }



}
