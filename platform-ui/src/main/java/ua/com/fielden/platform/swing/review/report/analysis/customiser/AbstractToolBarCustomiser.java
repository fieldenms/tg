package ua.com.fielden.platform.swing.review.report.analysis.customiser;

import ua.com.fielden.platform.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;

/**
 * Stub implementation of the {@link IToolBarCustomiser} interface.
 *
 * @author TG Team
 *
 * @param <A>
 */
public class AbstractToolBarCustomiser<A extends AbstractAnalysisReview<?, ?, ?>> implements IToolBarCustomiser<A> {

    @Override
    public ActionPanelBuilder createToolBar(final A analysisView) {
	// TODO Auto-generated method stub
	return null;
    }
}
