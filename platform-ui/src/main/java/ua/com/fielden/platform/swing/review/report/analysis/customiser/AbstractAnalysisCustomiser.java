package ua.com.fielden.platform.swing.review.report.analysis.customiser;

import ua.com.fielden.platform.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.report.query.generation.IReportQueryGeneration;
import ua.com.fielden.platform.swing.review.details.IDetails;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;

/**
 * Stub implementation of the {@link IAnalysisCustomiser} interface.
 *
 * @author TG Team
 *
 * @param <A>
 */
public class AbstractAnalysisCustomiser<A extends AbstractAnalysisReview<?, ?, ?>> implements IAnalysisCustomiser<A> {

    @Override
    public ActionPanelBuilder createToolBar(final A analysisView) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <DT> IDetails<DT> getDetails(final Class<DT> detailsParamType) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <T extends AbstractEntity<?>> IReportQueryGeneration<T> getQueryGenerator(final A analysisView, final Class<T> queryClass) {
	// TODO Auto-generated method stub
	return null;
    }

}
