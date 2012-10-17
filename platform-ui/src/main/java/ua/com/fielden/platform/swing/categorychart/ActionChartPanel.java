package ua.com.fielden.platform.swing.categorychart;

import java.awt.event.ActionEvent;

import javax.swing.event.EventListenerList;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;

import ua.com.fielden.platform.equery.lifecycle.IProgressUpdater;
import ua.com.fielden.platform.selectioncheckbox.SelectionCheckBoxPanel.IAction;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.blocking.IBlockingLayerProvider;

/**
 * Class the extends the {@link ChartPanel} and adds the {@link ChartMouseListener} that handles double click event on the chart
 *
 * @author oleh
 *
 */
public class ActionChartPanel<M, T> extends ChartPanel {

    private static final long serialVersionUID = 4243160139520327156L;

    private final IChartFactory<M, T> chartFactory;
    private final IBlockingLayerProvider provider;

    private final EventListenerList listeners = new EventListenerList();

    private boolean labelVisible = false;

    private T chartType = null;
    private IAction postAction, preAction;

    /**
     * Should be used to get appropriate chart from list of charts created by {@link IChartFactory}. Please note that {@link IChartFactory} SHOULD be re-factored, see
     * {@link IChartFactory} j-docs for more details.
     */
    private final int indexOfAppropriateChart;

    /**
     * Creates new instance of the {@link ActionChartPanel} and adds the {@link ChartMouseListener} to the panel. See
     * {@link ChartPanel#ChartPanel(JFreeChart, int, int, int, int, int, int, boolean, boolean, boolean, boolean, boolean, boolean)} for more information
     *
     * @param chart
     * @param width
     * @param height
     * @param minimumDrawWidth
     * @param minimumDrawHeight
     * @param maximumDrawWidth
     * @param maximumDrawHeight
     * @param useBuffer
     * @param properties
     * @param save
     * @param print
     * @param zoom
     * @param tooltips
     */
    public ActionChartPanel(final IChartFactory<M, T> chartFactory, final IBlockingLayerProvider provider, final T type, final int indexOfAppropriateChart, final int width, final int height, final int minimumDrawWidth, final int minimumDrawHeight, final int maximumDrawWidth, final int maximumDrawHeight, final boolean useBuffer, final boolean properties, final boolean save, final boolean print, final boolean zoom, final boolean tooltips) {
	super(null, width, height, minimumDrawWidth, minimumDrawHeight, maximumDrawWidth, maximumDrawHeight, useBuffer, properties, save, print, zoom, tooltips);
	this.indexOfAppropriateChart = indexOfAppropriateChart;
	this.chartType = type;
	this.provider = provider;
	this.chartFactory = chartFactory;
	this.setMouseWheelEnabled(true);

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
			final CategoryItemRenderer renderer = plot.getRenderer();
			labelVisible = !labelVisible;
			renderer.setBaseItemLabelsVisible(labelVisible);
		    }
		}
	    }

	    @Override
	    public void chartMouseMoved(final ChartMouseEvent event) {
		// ignored for now
	    }

	});
    }

    /**
     * Override this method in order to provide custom double click event handler.
     *
     * @param chartEntity
     */
    public void mouseDoubleClicked(final ChartMouseEvent chartMouseEvent) {
	System.out.println(chartMouseEvent);
    }

    @Override
    public void setChart(final JFreeChart chart) {
	super.setChart(chart);
	if (chart != null && chart.getPlot() instanceof CategoryPlot) {
	    final CategoryPlot plot = chart.getCategoryPlot();
	    final CategoryItemRenderer renderer = plot.getRenderer();
	    renderer.setBaseItemLabelsVisible(labelVisible);
	}
	fireChartPanelChangedListener(new ChartPanelChangedEventObject(this, chart));
    }

    /**
     * Set new chart to this {@link ChartPanel}. That chart must be of the {@link CategoryChartTypes} type and depicts data specified with {@code model} parameter. If all parameter
     * is true then all series of the Category chart must be visible otherwise one must specify series indexes which must be visible.
     *
     * @param aggregates
     * @param type
     */
    public void setChart(final M model, final boolean all, final int... indexes) {
	chartFactory.setModel(model, all, indexes);
	updateChart();
    }

    /**
     * Set new chart to this {@link ChartPanel}. That chart must be of the {@link CategoryChartTypes} type.
     *
     * @param type
     */
    public void setChart(final T type) {
	chartType = type;
	updateChart();
    }

    public void updateChart() {
	final BlockingIndefiniteProgressLayer bipl = provider.getBlockingLayer();
	if (bipl == null) {
	    throw new RuntimeException("Null instead of blocking pane.");
	}
	final BlockingLayerCommand<Void> bl = new BlockingLayerCommand<Void>("Set chart action.", bipl) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		if (!bipl.isIncrementalLocking()) {
		    bipl.enableIncrementalLocking();
		}
		setMessage("Updating...");
		final boolean b = super.preAction();
		if (preAction != null) {
		    preAction.action();
		}
		return b;
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		chartFactory.initDatasets(chartType, new IProgressUpdater() {
		    @Override
		    public void updateProgress(final String message) {
			setMessage(message);
		    }
		});
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		setChart(chartFactory.getCharts(chartType).get(indexOfAppropriateChart));
		try {
		    if (postAction != null) {
			postAction.action();
		    }
		    super.postAction(value);
		} catch (final Exception e) {
		    e.printStackTrace();
		    // do nothing
		}
	    }

	    /**
	     * After default exception handling executed, post-actions should be performed to enable all necessary buttons, unlock layer etc.
	     */
	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		if (postAction != null) {
		    postAction.action();
		}
		super.postAction(null);
	    }
	};
	bl.actionPerformed(null);
    }

    public IBlockingLayerProvider getProvider() {
	return provider;
    }

    /**
     * Adds new {@link IChartPanelChangeListener} instance.
     *
     * @param listener
     */
    public void addChartPanelChangedListener(final IChartPanelChangeListener listener) {
	listeners.add(IChartPanelChangeListener.class, listener);
    }

    /**
     * Removes specified {@link IChartPanelChangeListener} from the listeners list.
     *
     * @param listener
     */
    public void removeChartPanelChangedListener(final IChartPanelChangeListener listener) {
	listeners.remove(IChartPanelChangeListener.class, listener);
    }

    /**
     * Fires {@link ChartPanelChangedEventObject} event for all registered {@link IChartPanelChangeListener}.
     *
     * @param event
     */
    protected void fireChartPanelChangedListener(final ChartPanelChangedEventObject event) {
	if (listeners == null) {
	    return;
	}
	final Object[] listenersArray = listeners.getListenerList();
	for (int i = listenersArray.length - 2; i >= 0; i -= 2) {
	    if (listenersArray[i] == IChartPanelChangeListener.class) {
		((IChartPanelChangeListener) listenersArray[i + 1]).chartPanelChanged(event);
	    }
	}

    }

    public T getChartType() {
	return chartType;
    }

    public IAction getPostAction() {
	return postAction;
    }

    public void setPostAction(final IAction postAction) {
	this.postAction = postAction;
    }

    public IAction getPreAction() {
	return preAction;
    }

    public void setPreAction(final IAction preAction) {
	this.preAction = preAction;
    }

    public IChartFactory<M, T> getChartFactory() {
	return chartFactory;
    }

    public int getIndexOfAppropriateChart() {
	return indexOfAppropriateChart;
    }
}
