package ua.com.fielden.platform.swing.review.report.analysis.customiser;

import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;

public interface IAnalysisViewCustomiser<A extends AbstractAnalysisReview<?, ?, ?>> {

    /**
     * Provides ability for the developer to customise the analysis view (e.g. custom colouring, custom layout etc.)
     *
     * @param analysisView - the analysis view that must be customised.
     * @return
     */
    void customiseView(A analysisView);
}
