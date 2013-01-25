package ua.com.fielden.platform.swing.review.report.analysis.multipledec;

import java.util.List;

import javax.swing.JList;

import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IUsageManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.categorychart.ChartAnalysisAggregationListDragToSupport;
import ua.com.fielden.platform.swing.categorychart.IChartPositioner;
import ua.com.fielden.platform.swing.review.report.analysis.chart.ICategoryAnalysisDataProvider;

public class MultipleDecAggregationDragToSupport<T extends AbstractEntity<?>> extends ChartAnalysisAggregationListDragToSupport<T> {

    public MultipleDecAggregationDragToSupport(final Class<T> root, final IAbstractAnalysisAddToAggregationTickManager tickManager, final JList<String> list, final IChartPositioner multipleChartPanel, final ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> categoryDataProvider) {
	super(root, tickManager, list, multipleChartPanel, categoryDataProvider);
    }

    @Override
    protected List<String> getSelectedOrderedProperties() {
        return ((IUsageManager)tickManager).usedProperties(root);
    }
}
