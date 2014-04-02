package ua.com.fielden.platform.swing.review.report.analysis.customiser;

import ua.com.fielden.platform.actionpanelmodel.ActionPanelBuilder;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.analysis.lifecycle.LifecycleAnalysisView;

public class DefaultLifecycleAnalysisToolbarCustomiser<T extends AbstractEntity<?>> implements IToolbarCustomiser<LifecycleAnalysisView<T>> {

    @Override
    public ActionPanelBuilder createToolbar(final LifecycleAnalysisView<T> analysisView) {
        return new ActionPanelBuilder()//
        .addButton(analysisView.getConfigureAction())//
        .addSeparator()//
        .addComponent(analysisView.getBarChartButton())//
        .addComponent(analysisView.getStackedChartButton())//
        .addComponent(analysisView.getLineChartButton())//
        .addSeparator()//
        .addComponent(analysisView.getPeriodPanel());
    }

}
