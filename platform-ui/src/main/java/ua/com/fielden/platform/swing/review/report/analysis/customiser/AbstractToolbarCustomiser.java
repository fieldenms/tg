package ua.com.fielden.platform.swing.review.report.analysis.customiser;

import ua.com.fielden.platform.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;

/**
 * Stub implementation of the {@link IToolbarCustomiser} interface.
 *
 * @author TG Team
 *
 * @param <A>
 */
public class AbstractToolbarCustomiser<A extends AbstractAnalysisReview<?, ?, ?>> implements IToolbarCustomiser<A> {

    @Override
    public ActionPanelBuilder createToolbar(final A analysisView) {
	// TODO Auto-generated method stub
	return null;
    }
}
