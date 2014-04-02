package ua.com.fielden.platform.swing.review.report.analysis.multipledec;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;

import ua.com.fielden.platform.swing.categorychart.ChartPanel;
import ua.com.fielden.platform.swing.review.report.analysis.view.AnalysisDataEvent;

public class DecChartPanel extends ChartPanel {

    private static final long serialVersionUID = 7950397611951314191L;

    public DecChartPanel(final JFreeChart chart) {
        super(chart, 400, 225, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true, true, true, true, true, true);

        addChartMouseListener(new ChartMouseListener() {

            @Override
            public void chartMouseClicked(final ChartMouseEvent event) {
                if (event.getTrigger().getClickCount() == 2 && !event.getTrigger().isConsumed()) {
                    event.getTrigger().consume();
                    mouseDoubleClicked(event);
                } else if (event.getTrigger().getClickCount() == 1) {
                    event.getTrigger().consume();
                    if (getChart().getPlot() instanceof CategoryPlot) {
                        final CategoryPlot plot = getChart().getCategoryPlot();
                        if (event.getEntity() instanceof CategoryItemEntity) {
                            switchRendererLabels(getDataSetIndex(((CategoryItemEntity) event.getEntity()).getDataset()));
                        }
                    }
                }
            }

            private int getDataSetIndex(final CategoryDataset dataset) {
                for (int ind = 0; ind < getChart().getCategoryPlot().getDatasetCount(); ind++) {
                    if (getChart().getCategoryPlot().getDataset(ind) == dataset) {
                        return ind;
                    }
                }
                return -1;
            }

            private void switchRendererLabels(final int i) {
                if (i >= 0) {
                    final CategoryItemRenderer renderer = getChart().getCategoryPlot().getRenderer(i);
                    renderer.setBaseItemLabelsVisible(!renderer.getBaseItemLabelsVisible());
                }
            }

            @Override
            public void chartMouseMoved(final ChartMouseEvent event) {
                // ignored for now
            }

        });
    }

    private void mouseDoubleClicked(final ChartMouseEvent event) {
        for (final IMultipleDecDoubleClickListener listener : getListeners(IMultipleDecDoubleClickListener.class)) {
            listener.doubleClick(new AnalysisDataEvent<ChartMouseEvent>(this, event));
        }
    }

    public void addAnalysisDoubleClickListener(final IMultipleDecDoubleClickListener l) {
        listenerList.add(IMultipleDecDoubleClickListener.class, l);
    }

    public void removeAnalysisDoubleClickListener(final IMultipleDecDoubleClickListener l) {
        listenerList.remove(IMultipleDecDoubleClickListener.class, l);
    }

}
