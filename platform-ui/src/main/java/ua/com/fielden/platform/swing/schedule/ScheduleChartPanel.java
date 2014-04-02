package ua.com.fielden.platform.swing.schedule;

import java.awt.Cursor;
import java.awt.Paint;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.SwingUtilities;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.gantt.XYTaskDataset;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.xy.XYDataset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.categorychart.ChartPanel;
import ua.com.fielden.platform.swing.schedule.ScheduleChangedEvent.ScheduleChangedEventType;
import ua.com.fielden.platform.swing.schedule.ScheduleChangedEvent.ScheduleStretchSide;
import ua.com.fielden.platform.utils.Pair;

public class ScheduleChartPanel<T extends AbstractEntity<?>> extends ChartPanel {

    private static final long serialVersionUID = -3358861548848789512L;

    private final Set<ScheduleSeries<T>> serieses = new TreeSet<ScheduleSeries<T>>(createSeriesComparator());
    private final List<T> data = new ArrayList<>();

    private IDomainLabelGenerator<T> labelGenerator = new DefaultDomainLabelGenerator<>();
    private ITooltipGenerator<T> tooltipGenerator = new DefaultTooltipGenerator<>();
    private double stretchFactor = 0.05;

    private boolean labelVisible = false;

    //Temporary parameters needed for dragging and stretching
    private ScheduleChangedEventType eventType = null;
    private ScheduleStretchSide stretchSide = null;
    private ScheduleTask<T> currentTask = null;
    private Date previousPosition = null;

    public ScheduleChartPanel() {
        super(null, 640, 480, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true, true, true, true, true, true);
        setChart(createChart());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void mouseClicked(final MouseEvent e) {
        super.mouseClicked(e);
        final Pair<Date, String> coordinates = getCoordinates(e.getPoint());
        final Pair<T, ScheduleSeries<T>> entitySeries = getEntityWithSeries(e.getPoint());
        for (final IScheduleChartMouseEventListener<T> l : getListeners(IScheduleChartMouseEventListener.class)) {
            l.mouseClick(new ScheduleMouseEvent<T>(this, e, entitySeries.getKey(), entitySeries.getValue(), coordinates.getKey(), coordinates.getValue()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void mouseMoved(final MouseEvent e) {
        super.mouseMoved(e);
        final Pair<Date, String> coordinates = getCoordinates(e.getPoint());
        final ScheduleTask<T> task = getTask(e.getPoint());
        if (task != null && task.canEdit()) {
            if (closeToRight(task.getFrom(), task.getTo(), coordinates.getKey())) {
                setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            } else if (closeToLeft(task.getFrom(), task.getTo(), coordinates.getKey())) {
                setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
            } else {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        final Pair<T, ScheduleSeries<T>> entityWithSeries = getEntityWithSeries(e.getPoint());
        for (final IScheduleChartMouseEventListener<T> l : getListeners(IScheduleChartMouseEventListener.class)) {
            l.mouseMove(new ScheduleMouseEvent<T>(this, e, entityWithSeries.getKey(), entityWithSeries.getValue(), coordinates.getKey(), coordinates.getValue()));
        }
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        final ScheduleTask<T> task = getTask(e.getPoint());
        if (task != null && task.canEdit() && SwingUtilities.isLeftMouseButton(e)) {
            final Pair<Date, String> coordinates = getCoordinates(e.getPoint());
            currentTask = task;
            previousPosition = coordinates.getKey();
            if (closeToRight(currentTask.getFrom(), currentTask.getTo(), coordinates.getKey())) {
                eventType = ScheduleChangedEventType.STRETCH;
                stretchSide = ScheduleStretchSide.RIGHT;
            } else if (closeToLeft(currentTask.getFrom(), currentTask.getTo(), coordinates.getKey())) {
                eventType = ScheduleChangedEventType.STRETCH;
                stretchSide = ScheduleStretchSide.LEFT;
            } else {
                eventType = ScheduleChangedEventType.MOVE;
            }
        } else {
            currentTask = null;
            eventType = null;
            stretchSide = null;
            previousPosition = null;
            super.mousePressed(e);
        }
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        if (currentTask != null) {
            final Pair<Date, String> coordinates = getCoordinates(e.getPoint());
            final long delta = coordinates.getKey().getTime() - previousPosition.getTime();
            if (eventType == ScheduleChangedEventType.MOVE) {
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                final Date newLeftValue = new Date(currentTask.getFrom().getTime() + delta);
                final Date newRightValue = new Date(currentTask.getTo().getTime() + delta);
                currentTask.setDuration(new SimpleTimePeriod(newLeftValue, newRightValue));
                fireScheduleChangedEvent(new ScheduleChangedEvent<>(this, currentTask.getEntity(), currentTask.getScheduleSeries(), newLeftValue, newRightValue));
            } else if (eventType == ScheduleChangedEventType.STRETCH) {
                Date newValue = null;
                if (stretchSide == ScheduleStretchSide.LEFT) {
                    newValue = new Date(currentTask.getFrom().getTime() + delta);
                    currentTask.setFrom(newValue);
                } else {
                    newValue = new Date(currentTask.getTo().getTime() + delta);
                    currentTask.setTo(newValue);
                }
                fireScheduleChangedEvent(new ScheduleChangedEvent<>(this, stretchSide, currentTask.getEntity(), currentTask.getScheduleSeries(), newValue));
            }
            previousPosition = coordinates.getKey();
            getChart().setNotify(true);
            getRangeAxis().configure();
        } else {
            super.mouseDragged(e);
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        if (currentTask != null) {
            if (eventType == ScheduleChangedEventType.MOVE) {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
            currentTask = null;
            eventType = null;
            stretchSide = null;
            previousPosition = null;
        } else {
            super.mouseReleased(e);
        }
    }

    public boolean isLabelVisible() {
        return labelVisible;
    }

    public void setLabelVisible(final boolean labelVisible) {
        this.labelVisible = labelVisible;
    }

    public void setTooltipGenerator(final ITooltipGenerator<T> tooltipGenerator) {
        if (tooltipGenerator == null) {
            throw new NullPointerException("Tooltip generator can not be null");
        }
        this.tooltipGenerator = tooltipGenerator;
    }

    public ITooltipGenerator<T> getTooltipGenerator() {
        return tooltipGenerator;
    }

    private Pair<T, ScheduleSeries<T>> getEntityWithSeries(final Point point) {
        final ScheduleTask<T> task = getTask(point);
        if (task != null) {
            return new Pair<T, ScheduleSeries<T>>(task.getEntity(), task.getScheduleSeries());
        }
        return new Pair<T, ScheduleSeries<T>>(null, null);
    }

    private XYItemEntity getXYItem(final Point point) {
        final ChartEntity entity = getEntityForPoint(point.x, point.y);
        if (entity instanceof XYItemEntity) {
            return (XYItemEntity) entity;
        }
        return null;
    }

    private ScheduleTask<T> getTask(final Point p) {
        final XYItemEntity e = getXYItem(p);
        if (e != null) {
            return getTask(e.getDataset(), e.getSeriesIndex(), e.getItem());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private ScheduleTask<T> getTask(final XYDataset dataset, final int series, final int item) {
        return (ScheduleTask<T>) ((XYTaskDataset) dataset).getTasks().getSeries(series).get(item);
    }

    private Pair<Date, String> getCoordinates(final Point point) {
        final XYPlot xyPlot = getPlot();
        final Rectangle2D dataArea = getChartRenderingInfo().getPlotInfo().getDataArea();
        final Point2D p = translateScreenToJava2D(point);
        final Date x = new Date((long) getRangeAxis().java2DToValue(p.getX(), dataArea, xyPlot.getRangeAxisEdge()));
        final String y = getDomainAxis().valueToString(Math.round(getDomainAxis().java2DToValue(p.getY(), dataArea, xyPlot.getDomainAxisEdge())));
        return new Pair<Date, String>(x, y);
    }

    private boolean closeToLeft(final Date from, final Date to, final Date date) {
        final Date time = new Date((long) (from.getTime() + stretchFactor * (to.getTime() - from.getTime())));
        return date.after(from) && date.before(time);
    }

    private boolean closeToRight(final Date from, final Date to, final Date date) {
        final Date time = new Date((long) (to.getTime() - stretchFactor * (to.getTime() - from.getTime())));
        return date.after(time) && date.before(to);
    }

    @SuppressWarnings("unchecked")
    private void fireScheduleChangedEvent(final ScheduleChangedEvent<T> scheduleChangedEvent) {
        for (final IScheduleChangeEventListener<T> l : listenerList.getListeners(IScheduleChangeEventListener.class)) {
            l.scheduleChanged(scheduleChangedEvent);
        }
    }

    private SymbolAxis getDomainAxis() {
        return (SymbolAxis) getPlot().getDomainAxis();
    }

    private DateAxis getRangeAxis() {
        return (DateAxis) getPlot().getRangeAxis();
    }

    public void addScheduleChangedEventListener(final IScheduleChangeEventListener<T> l) {
        listenerList.add(IScheduleChangeEventListener.class, l);
    }

    public void removeScheduleChangedEventListener(final IScheduleChangeEventListener<T> l) {
        listenerList.remove(IScheduleChangeEventListener.class, l);
    }

    public void addScheduleChartMouseEventListener(final IScheduleChartMouseEventListener<T> l) {
        listenerList.add(IScheduleChartMouseEventListener.class, l);
    }

    public void removeScheduleChartMouseEventListener(final IScheduleChartMouseEventListener<T> l) {
        listenerList.remove(IScheduleChartMouseEventListener.class, l);
    }

    /**
     * Returns the comparotor for series set that
     * 
     * @return
     */
    private Comparator<? super ScheduleSeries<T>> createSeriesComparator() {
        return new Comparator<ScheduleSeries<T>>() {

            @Override
            public int compare(final ScheduleSeries<T> o1, final ScheduleSeries<T> o2) {
                if (o1.getCutOfFactor() > o2.getCutOfFactor()) {
                    return -1;
                } else if (o1.getCutOfFactor() < o2.getCutOfFactor()) {
                    return 1;
                }
                return 0;
            }
        };
    }

    private JFreeChart createChart() {

        //Generating axis.
        final ValueAxis valueAxis = createValueAxis("");
        final ValueAxis domainAxis = createDomainAxis("");

        //Generating renderers.
        final XYBarRenderer[] renderers = createRenderers();

        //Generate datasets.
        final XYDataset[] datasets = createDatasets();

        //Generating plot.
        final XYPlot plot = new XYPlot(null, domainAxis, valueAxis, null);
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        plot.setFixedLegendItems(createLegend());
        for (int datasetIndex = 0; datasetIndex < datasets.length; datasetIndex++) {
            plot.setDataset(datasetIndex, datasets[datasetIndex]);
            plot.setRenderer(datasetIndex, renderers[datasetIndex]);
        }

        //Generating chart.
        final JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        ChartUtilities.applyCurrentTheme(chart);

        return chart;
    }

    private LegendItemCollection createLegend() {
        final LegendItemCollection legend = new LegendItemCollection();
        for (final ScheduleSeries<T> series : serieses) {
            legend.addAll(createLegendColllection(series));
        }
        return legend;
    }

    private LegendItemCollection createLegendColllection(final ScheduleSeries<T> series) {
        final LegendItemCollection legend = new LegendItemCollection();
        for (final Pair<String, Paint> legendItem : series.getPainter().getAvailableLegendItems()) {
            legend.add(new LegendItem(legendItem.getKey(), legendItem.getValue()));
        }
        return legend;
    }

    private XYTaskDataset[] createDatasets() {
        final XYTaskDataset[] datasets = new XYTaskDataset[serieses.size()];
        int seriesCount = 0;
        final Iterator<ScheduleSeries<T>> seriesIterator = serieses.iterator();
        while (seriesIterator.hasNext()) {
            datasets[seriesCount++] = createDatasetFor(seriesIterator.next());
        }
        return datasets;
    }

    private XYTaskDataset createDatasetFor(final ScheduleSeries<T> series) {
        final TaskSeriesCollection collection = new TaskSeriesCollection();
        for (final T entity : data) {
            final TaskSeries newSeries = new TaskSeries(series.getName());
            if (series.isTaskVisible(entity)) {
                newSeries.add(new ScheduleTask<T>(series.getName(), series, entity));
            }
            collection.add(newSeries);
        }
        return new XYTaskDataset(collection);
    }

    private XYBarRenderer[] createRenderers() {
        final XYBarRenderer[] renderers = new XYBarRenderer[serieses.size()];
        int seriesCount = 0;
        final Iterator<ScheduleSeries<T>> seriesIterator = serieses.iterator();
        while (seriesIterator.hasNext()) {
            renderers[seriesCount++] = createRendererFor(seriesIterator.next());
        }
        return renderers;
    }

    private XYBarRenderer createRendererFor(final ScheduleSeries<T> series) {
        return new XYBarRenderer() {

            private static final long serialVersionUID = -2136238382011312334L;

            {
                setMargin(series.getCutOfFactor());
                setUseYInterval(true);
                setBaseToolTipGenerator(createTooltipGenerator());
                //setBaseItemLabelsVisible(true);
                //setBaseItemLabelGenerator(createLabelGenerator());
                //setNegativeItemLabelPositionFallback(position)
            }

            @Override
            public Paint getSeriesPaint(final int ind) {
                return series.getPainter().getPainterFor(data.get(ind));
            }

            private XYToolTipGenerator createTooltipGenerator() {
                return new XYToolTipGenerator() {

                    @Override
                    public String generateToolTip(final XYDataset dataset, final int seriesInd, final int item) {
                        final ScheduleTask<T> task = getTask(dataset, seriesInd, item);
                        return tooltipGenerator.getTooltip(task.getEntity(), series);
                    }
                };
            }

            private XYItemLabelGenerator createLabelGenerator() {
                return new XYItemLabelGenerator() {

                    @Override
                    public String generateLabel(final XYDataset dataset, final int series, final int item) {
                        final ScheduleTask<T> task = getTask(dataset, series, item);
                        if (task == currentTask || labelVisible) {
                        }
                        return null;
                    }
                };
            }

        };
    }

    private ValueAxis createDomainAxis(final String label) {
        final String[] domainNames = new String[data.size()];
        for (int entityIndex = 0; entityIndex < data.size(); entityIndex++) {
            domainNames[entityIndex] = labelGenerator.getDoaminName(data.get(entityIndex));
        }
        final SymbolAxis symbolAxis = new SymbolAxis(label, domainNames);
        symbolAxis.setGridBandsVisible(false);
        return symbolAxis;
    }

    private ValueAxis createValueAxis(final String label) {
        return new DateAxis(label);
    }

    public ScheduleChartPanel<T> addScheduleSeries(final ScheduleSeries<T> scheduleSeries) {
        if (serieses.add(scheduleSeries)) {
            updateChartWithNewSeries(scheduleSeries);
        }
        return this;
    }

    private void updateChartWithNewSeries(final ScheduleSeries<T> scheduleSeries) {
        int indexOfSeries = 0;
        final Iterator<ScheduleSeries<T>> iterator = serieses.iterator();
        while (iterator.hasNext() && !iterator.next().equals(scheduleSeries)) {
            indexOfSeries++;
        }
        final XYTaskDataset dataset = createDatasetFor(scheduleSeries);
        final XYBarRenderer renderer = createRendererFor(scheduleSeries);
        insertDatasets(dataset, renderer, indexOfSeries);
    }

    private void insertDatasets(final XYTaskDataset dataset, final XYBarRenderer renderer, final int indexOfSeries) {
        shiftDatasetRight(indexOfSeries);
        shiftRenderersRight(indexOfSeries);
        getPlot().setDataset(indexOfSeries, dataset);
        getPlot().setRenderer(indexOfSeries, renderer);
        getPlot().setFixedLegendItems(createLegend());
    }

    private void shiftRenderersRight(final int indexOfSeries) {
        final XYPlot plot = getPlot();
        for (int i = getPlot().getRendererCount() - 1; i >= indexOfSeries; i--) {
            plot.setRenderer(i + 1, plot.getRenderer(i));
        }
    }

    private void shiftDatasetRight(final int indexOfSeries) {
        final XYPlot plot = getPlot();
        for (int i = plot.getDatasetCount() - 1; i >= indexOfSeries; i--) {
            plot.setDataset(i + 1, plot.getDataset(i));
        }
    }

    /**
     * Set the {@link ITooltipGenerator}. Throws {@link NullPointerException} if the lableGenerator parameter is null.
     * 
     * @param labelGenerator
     * @return
     */
    public ScheduleChartPanel<T> setLabelGenerator(final IDomainLabelGenerator<T> labelGenerator) {
        if (labelGenerator == null) {
            throw new NullPointerException("Label generator can not be null");
        }
        this.labelGenerator = labelGenerator;
        return this;
    }

    private XYPlot getPlot() {
        return getChart().getXYPlot();
    }

    /**
     * Adds the range marker to the chart.
     * 
     * @param marker
     * @return
     */
    public ScheduleChartPanel<T> addMarker(final Marker marker) {
        getPlot().addRangeMarker(marker);
        return this;
    }

    public void setChartName(final String chartName) {
        getChart().setTitle(chartName);
    }

    public void setRangeAxisName(final String rangeAxisName) {
        getPlot().getRangeAxis().setLabel(rangeAxisName);
    }

    public void setDomainAxisName(final String domainAxisName) {
        getPlot().getDomainAxis().setLabel(domainAxisName);
    }

    public void setData(final List<T> data) {
        this.data.clear();
        this.data.addAll(data == null ? new ArrayList<T>() : data);
        final XYPlot plot = getPlot();
        final XYDataset[] datasets = createDatasets();
        for (int datasetIndex = 0; datasetIndex < datasets.length; datasetIndex++) {
            plot.setDataset(datasetIndex, datasets[datasetIndex]);
        }
        plot.setDomainAxis(createDomainAxis(plot.getDomainAxis().getLabel()));
    }

    public List<T> getData() {
        return Collections.unmodifiableList(data);
    }
}
