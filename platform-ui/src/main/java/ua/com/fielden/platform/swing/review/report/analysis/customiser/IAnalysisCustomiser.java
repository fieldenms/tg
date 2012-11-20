package ua.com.fielden.platform.swing.review.report.analysis.customiser;

import ua.com.fielden.platform.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.report.query.generation.IReportQueryGeneration;
import ua.com.fielden.platform.swing.review.details.IDetails;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;

/**
 * The contract that enables analysis customisation. The analysis customisation allows one to:
 *<ul>
 *<li>Customise analysis tool bar.
 *<li>Customise analysis query.
 *<li>Customise analysis details view.
 *<li>Customise the grid colour schema (only for grid analysis).
 *</ul>
 *
 * @author TG Team
 *
 * @param <A>
 */
public interface IAnalysisCustomiser<A extends AbstractAnalysisReview<?, ?, ?>> {

    /**
     * Provides the custom tool bar with actions related to the specified analysis view.
     *
     * @param analysisView - the analysis view for which tool bar must be created.
     * @return
     */
    ActionPanelBuilder createToolBar(A analysisView);

    /**
     *	Returns the details contract provided by this analysis customiser.
     *
     * @param detailsParamType
     * @return
     */
    <DT> IDetails<DT> getDetails(Class<DT> detailsParamType);

    /**
     * Returns the instance of {@link IReportQueryGeneration} provided by this analysis customiser.
     *
     * @param queryClass
     * @return
     */
    <T extends AbstractEntity<?>> IReportQueryGeneration<T> getQueryGenerator(A analysisView, Class<T> queryClass);
}
