package ua.com.fielden.platform.swing.review.report.analysis.multipledec;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;

import ua.com.fielden.platform.swing.categorychart.ChartPanel;
import ua.com.fielden.platform.swing.review.report.analysis.view.AnalysisDataEvent;

public class DecChartPanel extends ChartPanel {

    private static final long serialVersionUID = 7950397611951314191L;

    public DecChartPanel(final JFreeChart chart) {
	super(chart, 400, 225, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE,true, true, true, true, true, true, true);


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
			switchRendererLabels(plot);
		    }
		}
	    }

	    private void switchRendererLabels(final CategoryPlot plot){
		for(int renderIndex = 0; renderIndex < plot.getRendererCount(); renderIndex++){
		    final CategoryItemRenderer renderer = plot.getRenderer(renderIndex);
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
	for (final IMultipleDecDoubleClickListener listener : getListeners(IMultipleDecDoubleClickListener.class)){
	    listener.doubleClick(new AnalysisDataEvent<ChartMouseEvent>(this, event));
	}
    }

    public void addAnalysisDoubleClickListener(final IMultipleDecDoubleClickListener l){
	listenerList.add(IMultipleDecDoubleClickListener.class, l);
    }

    public void removeAnalysisDoubleClickListener(final IMultipleDecDoubleClickListener l){
	listenerList.remove(IMultipleDecDoubleClickListener.class, l);
    }

}
