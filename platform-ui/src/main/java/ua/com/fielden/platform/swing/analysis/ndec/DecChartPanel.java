package ua.com.fielden.platform.swing.analysis.ndec;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;

import ua.com.fielden.platform.swing.categorychart.AnalysisDoubleClickEvent;
import ua.com.fielden.platform.swing.categorychart.ChartPanel;

public class DecChartPanel extends ChartPanel {

    private static final long serialVersionUID = 7950397611951314191L;

    public DecChartPanel(final JFreeChart chart) {
	super(chart, 400, 300, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE,true, true, true, true, true, true, true);


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
	for (final IAnalysisDoubleClickListener listener : getListeners(IAnalysisDoubleClickListener.class)){
	    listener.doubleClick(new AnalysisDoubleClickEvent(this, event));
	}
    }

    public void addAnalysisDoubleClickListener(final IAnalysisDoubleClickListener l){
	listenerList.add(IAnalysisDoubleClickListener.class, l);
    }

    public void removeAnalysisDoubleClickListener(final IAnalysisDoubleClickListener l){
	listenerList.remove(IAnalysisDoubleClickListener.class, l);
    }

}
