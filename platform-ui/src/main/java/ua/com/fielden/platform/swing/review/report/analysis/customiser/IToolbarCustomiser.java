package ua.com.fielden.platform.swing.review.report.analysis.customiser;

import ua.com.fielden.platform.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;

/**
 * The contract that enables analysis' tool bar customisation.
 * 
 * @author TG Team
 * 
 * @param <A>
 */
public interface IToolbarCustomiser<A extends AbstractAnalysisReview<?, ?, ?>> {

    /**
     * Provides the custom tool bar with actions related to the specified analysis view.
     * 
     * @param analysisView
     *            - the analysis view for which tool bar must be created.
     * @return
     */
    ActionPanelBuilder createToolbar(A analysisView);
}
