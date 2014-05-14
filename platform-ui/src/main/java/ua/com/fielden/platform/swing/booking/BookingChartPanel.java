package ua.com.fielden.platform.swing.booking;

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
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.gantt.XYTaskDataset;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriod;
import org.jfree.data.xy.XYDataset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.booking.BookingChangedEvent.BookingChangedEventType;
import ua.com.fielden.platform.swing.booking.BookingChangedEvent.BookingStretchSide;
import ua.com.fielden.platform.swing.categorychart.ChartPanel;
import ua.com.fielden.platform.utils.Pair;

public class BookingChartPanel<T extends AbstractEntity<?>, ST extends AbstractEntity<?>> extends ChartPanel {

    private static final long serialVersionUID = -3358861548848789512L;

    private final Set<BookingSeries<T, ST>> serieses = new TreeSet<BookingSeries<T, ST>>(createSeriesComparator());
    private final List<Pair<T, List<ST>>> data = new ArrayList<>();

    private IDomainLabelGenerator<T> labelGenerator = new DefaultDomainLabelGenerator<>();
    private ITooltipGenerator<T, ST> tooltipGenerator = new DefaultTooltipGenerator<>();
    private double stretchFactor = 0.05;

    private boolean labelVisible = false;

    //Temporary parameters needed for dragging and stretching
    private BookingChangedEventType eventType = null;
    private BookingStretchSide stretchSide = null;
    private BookingTaskSeries<T, ST> currentSeries = null;
    private BookingTask<T, ST> currentTask = null;
    private Date previousPosition = null;

    public BookingChartPanel() {
        super(null, 640, 480, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true, true, true, true, true, true);
        setChart(createChart());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void mouseClicked(final MouseEvent e) {
        super.mouseClicked(e);
        final Pair<Date, Integer> coordinates = getCoordinates(e.getPoint());
        final BookingTask<T, ST> task = getTask(e.getPoint());
        for (final IBookingChartMouseEventListener<T, ST> l : getListeners(IBookingChartMouseEventListener.class)) {
            l.mouseClick(new BookingMouseEvent<T, ST>(this, e, task, coordinates.getKey(), coordinates.getValue().intValue()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void mouseMoved(final MouseEvent e) {
        super.mouseMoved(e);
        final Pair<Date, Integer> coordinates = getCoordinates(e.getPoint());
        final BookingTask<T, ST> task = getTask(e.getPoint());
        if (task != null) {
            if (closeToRight(task.getFrom(), task.getTo(), coordinates.getKey()) && task.canEdit(BookingChangedEventType.STRETCH, BookingStretchSide.RIGHT)) {
                setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            } else if (closeToLeft(task.getFrom(), task.getTo(), coordinates.getKey()) && task.canEdit(BookingChangedEventType.STRETCH, BookingStretchSide.LEFT)) {
                setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
            } else {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        for (final IBookingChartMouseEventListener<T, ST> l : getListeners(IBookingChartMouseEventListener.class)) {
            l.mouseMove(new BookingMouseEvent<T, ST>(this, e, task, coordinates.getKey(), coordinates.getValue().intValue()));
        }
    }

    @Override
    public void mousePressed(final MouseEvent e) {
	final BookingTask<T, ST> task = getTask(e.getPoint());
	final BookingTaskSeries<T, ST> series = getSeries(e.getPoint());
        if (task != null && SwingUtilities.isLeftMouseButton(e)) {
            final Pair<Date, Integer> coordinates = getCoordinates(e.getPoint());
            currentSeries = series;
            currentTask = task;
            previousPosition = coordinates.getKey();
            if (closeToRight(task.getFrom(), task.getTo(), coordinates.getKey()) && task.canEdit(BookingChangedEventType.STRETCH, BookingStretchSide.RIGHT)) {
                eventType = BookingChangedEventType.STRETCH;
                stretchSide = BookingStretchSide.RIGHT;
            } else if (closeToLeft(task.getFrom(), task.getTo(), coordinates.getKey()) && task.canEdit(BookingChangedEventType.STRETCH, BookingStretchSide.LEFT)) {
                eventType = BookingChangedEventType.STRETCH;
                stretchSide = BookingStretchSide.LEFT;
            } else if(task.canEdit(BookingChangedEventType.MOVE, null)){
                eventType = BookingChangedEventType.MOVE;
            }
        } else {
            currentSeries = null;
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
            final Pair<Date, Integer> coordinates = getCoordinates(e.getPoint());
            final long delta = coordinates.getKey().getTime() - previousPosition.getTime();
            if (eventType == BookingChangedEventType.MOVE) {
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                moveCurrentTask(delta);
            } else if (eventType == BookingChangedEventType.STRETCH) {
                if (stretchSide == BookingStretchSide.LEFT) {
                    stretchLeftSideOfCurrentTask(delta);
                } else {
                    stretchRightSideOfCurrentTask(delta);
                }
            }
            previousPosition = coordinates.getKey();
            getChart().setNotify(true);
            getRangeAxis().configure();
        } else {
            super.mouseDragged(e);
        }
    }

    private void stretchRightSideOfCurrentTask(final long delta) {
	final BookingTask<T, ST> tail = currentSeries.higher(currentTask);
	final TimePeriod prevDuration = currentTask.getDuration();
	final Date newTo = new Date(currentTask.getTo().getTime() + delta);
	if (tail == null || tail.getFrom().after(newTo)) {
	    currentTask.setTo(newTo);
	} else {
	    currentTask.setTo(new Date(currentTask.getTo().getTime() + moveTaskRight(delta, tail)));
	}
	final TimePeriod newDuration = currentTask.getDuration();
	if(!prevDuration.equals(newDuration)) {
	    fireBookingChangedEvent(new BookingChangedEvent<>(this, currentTask.getEntity(), currentTask.getSubEntity(), currentTask.getBookingSeries(), newDuration.getStart(), newDuration.getEnd()));
	}
    }

    private void stretchLeftSideOfCurrentTask(final long delta) {
	final BookingTask<T, ST> head = currentSeries.lower(currentTask);
	final TimePeriod prevDuration = currentTask.getDuration();
	final Date newFrom = new Date(currentTask.getFrom().getTime() + delta);
	if (head == null || head.getTo().before(newFrom)) {
	    currentTask.setFrom(newFrom);
	} else {
	    currentTask.setFrom(new Date(currentTask.getFrom().getTime() + moveTaskLeft(delta, head)));
	}
	final TimePeriod newDuration = currentTask.getDuration();
	if(!prevDuration.equals(newDuration)) {
	    fireBookingChangedEvent(new BookingChangedEvent<>(this, currentTask.getEntity(), currentTask.getSubEntity(), currentTask.getBookingSeries(), newDuration.getStart(), newDuration.getEnd()));
	}
    }

    private void moveCurrentTask(final long delta) {
	if (delta < 0) {
	    moveTaskLeft(delta, currentTask);
	} else {
	    moveTaskRight(delta, currentTask);
	}
    }

    private long moveTaskRight(final long delta, final BookingTask<T, ST> task) {
	final BookingTask<T, ST> tail = currentSeries.higher(task);
	final TimePeriod prevDuration = task.getDuration();
	final long rightDelta;
	if (tail == null) {
	    rightDelta = delta;
	} else {
	    if (tail.getFrom().after(new Date(prevDuration.getEnd().getTime() + delta))) {
		rightDelta = delta;
	    } else {
		rightDelta = moveTaskLeft(delta, tail);
	    }
	}
	final Date newLeftValue = new Date(task.getFrom().getTime() + delta);
	final Date newRightValue = new Date(task.getTo().getTime() + rightDelta);
	if(task.canEdit(BookingChangedEventType.MOVE, null)) {
	    task.setDuration(new SimpleTimePeriod(newLeftValue, newRightValue));
	} else if (task.canEdit(BookingChangedEventType.STRETCH, BookingStretchSide.LEFT)) {
	    task.setFrom(newRightValue);
	}
	final TimePeriod newDuration = task.getDuration();
	if(!prevDuration.equals(newDuration)) {
	    fireBookingChangedEvent(new BookingChangedEvent<>(this, currentTask.getEntity(), currentTask.getSubEntity(), currentTask.getBookingSeries(), newDuration.getStart(), newDuration.getEnd()));
	}
	return newDuration.getEnd().getTime() - prevDuration.getEnd().getTime();
    }

    private long moveTaskLeft(final long delta, final BookingTask<T, ST> task) {
	final BookingTask<T, ST> head = currentSeries.lower(task);
	final TimePeriod prevDuration = task.getDuration();
	final long leftDelta;
	if (head == null) {
	    leftDelta = delta;
	} else {
	    if (head.getTo().before(new Date(prevDuration.getStart().getTime() + delta))) {
		leftDelta = delta;
	    } else {
		leftDelta = moveTaskLeft(delta, head);
	    }
	}
	final Date newLeftValue = new Date(task.getFrom().getTime() + leftDelta);
	final Date newRightValue = new Date(task.getTo().getTime() + delta);
	if(task.canEdit(BookingChangedEventType.MOVE, null)) {
	    task.setDuration(new SimpleTimePeriod(newLeftValue, newRightValue.before(newLeftValue) ? newLeftValue : newRightValue));
	} else if (task.canEdit(BookingChangedEventType.STRETCH, BookingStretchSide.RIGHT)) {
	    task.setTo(newRightValue);
	}
	final TimePeriod newDuration = task.getDuration();
	if(!prevDuration.equals(newDuration)) {
	    fireBookingChangedEvent(new BookingChangedEvent<>(this, currentTask.getEntity(), currentTask.getSubEntity(), currentTask.getBookingSeries(), newLeftValue, newRightValue));
	}
	return newDuration.getEnd().getTime() - prevDuration.getEnd().getTime();
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        if (currentTask != null) {
            if (eventType == BookingChangedEventType.MOVE) {
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

    public void setTooltipGenerator(final ITooltipGenerator<T, ST> tooltipGenerator) {
        if (tooltipGenerator == null) {
            throw new NullPointerException("Tooltip generator can not be null");
        }
        this.tooltipGenerator = tooltipGenerator;
    }

    public ITooltipGenerator<T, ST> getTooltipGenerator() {
        return tooltipGenerator;
    }

    private XYItemEntity getXYItem(final Point point) {
        final ChartEntity entity = getEntityForPoint(point.x, point.y);
        if (entity instanceof XYItemEntity) {
            return (XYItemEntity) entity;
        }
        return null;
    }

    private BookingTask<T, ST> getTask(final Point p) {
        final XYItemEntity e = getXYItem(p);
        if (e != null) {
            return getTask(e.getDataset(), e.getSeriesIndex(), e.getItem());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private BookingTaskSeries<T, ST> getSeries(final Point p) {
	final XYItemEntity e = getXYItem(p);
        if (e != null) {
            return (BookingTaskSeries<T,ST>)((XYTaskDataset) e.getDataset()).getTasks().getSeries(e.getSeriesIndex());
        }
        return null;
    }

    private Integer itemIndex(final Point p) {
	final XYItemEntity e = getXYItem(p);
        if (e != null) {
            return Integer.valueOf(e.getItem());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private BookingTask<T, ST> getTask(final XYDataset dataset, final int series, final int item) {
        return (BookingTask<T, ST>) ((XYTaskDataset) dataset).getTasks().getSeries(series).get(item);
    }

    private Pair<Date, Integer> getCoordinates(final Point point) {
        final XYPlot xyPlot = getPlot();
        final Rectangle2D dataArea = getChartRenderingInfo().getPlotInfo().getDataArea();
        final Point2D p = translateScreenToJava2D(point);
        final Date x = new Date((long) getRangeAxis().java2DToValue(p.getX(), dataArea, xyPlot.getRangeAxisEdge()));
        final int y = (int)Math.round(getDomainAxis().java2DToValue(p.getY(), dataArea, xyPlot.getDomainAxisEdge()));
        return new Pair<>(x, Integer.valueOf(y));
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
    private void fireBookingChangedEvent(final BookingChangedEvent<T, ST> bookingChangedEvent) {
        for (final IBookingChangeEventListener<T, ST> l : listenerList.getListeners(IBookingChangeEventListener.class)) {
            l.bookingChanged(bookingChangedEvent);
        }
    }

    private SymbolAxis getDomainAxis() {
        return (SymbolAxis) getPlot().getDomainAxis();
    }

    private DateAxis getRangeAxis() {
        return (DateAxis) getPlot().getRangeAxis();
    }

    public void addBookingChangedEventListener(final IBookingChangeEventListener<T, ST> l) {
        listenerList.add(IBookingChangeEventListener.class, l);
    }

    public void removeBookingChangedEventListener(final IBookingChangeEventListener<T, ST> l) {
        listenerList.remove(IBookingChangeEventListener.class, l);
    }

    public void addBookingChartMouseEventListener(final IBookingChartMouseEventListener<T, ST> l) {
        listenerList.add(IBookingChartMouseEventListener.class, l);
    }

    public void removeBookingChartMouseEventListener(final IBookingChartMouseEventListener<T, ST> l) {
        listenerList.remove(IBookingChartMouseEventListener.class, l);
    }

    /**
     * Returns the comparotor for series set that
     *
     * @return
     */
    private Comparator<? super BookingSeries<T, ST>> createSeriesComparator() {
        return new Comparator<BookingSeries<T, ST>>() {

            @Override
            public int compare(final BookingSeries<T, ST> o1, final BookingSeries<T, ST> o2) {
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
        for (final BookingSeries<T, ST> series : serieses) {
            legend.addAll(createLegendColllection(series));
        }
        return legend;
    }

    private LegendItemCollection createLegendColllection(final BookingSeries<T, ST> series) {
        final LegendItemCollection legend = new LegendItemCollection();
        for (final Pair<String, Paint> legendItem : series.getPainter().getAvailableLegendItems()) {
            legend.add(new LegendItem(legendItem.getKey(), legendItem.getValue()));
        }
        return legend;
    }

    private XYTaskDataset[] createDatasets() {
        final XYTaskDataset[] datasets = new XYTaskDataset[serieses.size()];
        int seriesCount = 0;
        final Iterator<BookingSeries<T, ST>> seriesIterator = serieses.iterator();
        while (seriesIterator.hasNext()) {
            datasets[seriesCount++] = createDatasetFor(seriesIterator.next());
        }
        return datasets;
    }

    private XYTaskDataset createDatasetFor(final BookingSeries<T, ST> series) {
        final TaskSeriesCollection collection = new TaskSeriesCollection();
        for (final Pair<T, List<ST>> entries : data) {
            collection.add(createSeriesFor(series, entries.getKey(), entries.getValue()));
        }
        return new XYTaskDataset(collection);
    }

    private TaskSeries createSeriesFor(final BookingSeries<T, ST> series, final T entity, final List<ST> subEntities) {
	final BookingTaskSeries<T, ST> newSeries = new BookingTaskSeries<>(series.getName());
	for (final ST subEntity : subEntities) {
	    if (series.isTaskVisible(entity, subEntity)) {
		newSeries.add(new BookingTask<T, ST>(series.getName(), series, entity, subEntity));
	    }
	}
	return newSeries;
    }

    private XYBarRenderer[] createRenderers() {
	XYBarRenderer.setDefaultShadowsVisible(false);
        final XYBarRenderer[] renderers = new XYBarRenderer[serieses.size()];
        int seriesCount = 0;
        final Iterator<BookingSeries<T, ST>> seriesIterator = serieses.iterator();
        while (seriesIterator.hasNext()) {
            renderers[seriesCount++] = createRendererFor(seriesIterator.next());
        }
        return renderers;
    }

    private XYBarRenderer createRendererFor(final BookingSeries<T, ST> series) {
        return new XYBarRenderer() {

            private static final long serialVersionUID = -2136238382011312334L;

            {
                setMargin(series.getCutOfFactor());
                setUseYInterval(true);
                setBaseToolTipGenerator(createTooltipGenerator());
                setDrawBarOutline(true);
                //setBaseItemLabelsVisible(true);
                //setBaseItemLabelGenerator(createLabelGenerator());
                //setNegativeItemLabelPositionFallback(position)
            }

            @Override
            public Paint getItemPaint(final int row, final int column) {
        	return series.getPainter().getPainterFor(data.get(row).getKey(), data.get(row).getValue().get(column));
            }

            private XYToolTipGenerator createTooltipGenerator() {
                return new XYToolTipGenerator() {

                    @Override
                    public String generateToolTip(final XYDataset dataset, final int seriesInd, final int item) {
                        final BookingTask<T, ST> task = getTask(dataset, seriesInd, item);
                        return tooltipGenerator.getTooltip(task.getEntity(), task.getSubEntity(), series);
                    }
                };
            }

//            private XYItemLabelGenerator createLabelGenerator() {
//                return new XYItemLabelGenerator() {
//
//                    @Override
//                    public String generateLabel(final XYDataset dataset, final int series, final int item) {
//                        final BookingTask<T, ST> task = getTask(dataset, series, item);
//                        if (task == currentTask || labelVisible) {
//                        }
//                        return null;
//                    }
//                };
//            }

        };
    }

    private ValueAxis createDomainAxis(final String label) {
        final String[] domainNames = new String[data.size()];
        for (int entityIndex = 0; entityIndex < data.size(); entityIndex++) {
            domainNames[entityIndex] = labelGenerator.getDoaminName(data.get(entityIndex).getKey());
        }
        final SymbolAxis symbolAxis = new SymbolAxis(label, domainNames);
        symbolAxis.setGridBandsVisible(false);
        return symbolAxis;
    }

    private ValueAxis createValueAxis(final String label) {
        return new DateAxis(label);
    }

    public BookingChartPanel<T, ST> addBookingSeries(final BookingSeries<T, ST> series) {
        if (serieses.add(series)) {
            updateChartWithNewSeries(series);
        }
        return this;
    }

    private void updateChartWithNewSeries(final BookingSeries<T, ST> series) {
        int indexOfSeries = 0;
        final Iterator<BookingSeries<T, ST>> iterator = serieses.iterator();
        while (iterator.hasNext() && !iterator.next().equals(series)) {
            indexOfSeries++;
        }
        final XYTaskDataset dataset = createDatasetFor(series);
        final XYBarRenderer renderer = createRendererFor(series);
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
    public BookingChartPanel<T, ST> setLabelGenerator(final IDomainLabelGenerator<T> labelGenerator) {
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
    public BookingChartPanel<T, ST> addMarker(final Marker marker) {
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

    public void setData(final List<Pair<T, List<ST>>> data) {
        this.data.clear();
        this.data.addAll(data == null ? new ArrayList<Pair<T, List<ST>>>() : data);
        final XYPlot plot = getPlot();
        final XYDataset[] datasets = createDatasets();
        for (int datasetIndex = 0; datasetIndex < datasets.length; datasetIndex++) {
            plot.setDataset(datasetIndex, datasets[datasetIndex]);
        }
        plot.setDomainAxis(createDomainAxis(plot.getDomainAxis().getLabel()));
    }

    public List<Pair<T, List<ST>>> getData() {
        return Collections.unmodifiableList(data);
    }

    public T getEntity(final int seriesIndex) {
	return data.get(seriesIndex).getKey();
    }

    public ST getSubTask(final int seriesIndex, final int itemIndex) {
	return data.get(seriesIndex).getValue().get(itemIndex);
    }

    public void addTask(final BookingSeries<T, ST> series, final int seriesIndex, final ST subTask) {
	//Adding task to the list.
	data.get(seriesIndex).getValue().add(subTask);
	//Adding task to dataset.
	final XYTaskDataset dataset = (XYTaskDataset)getDatasetFor(series);
	final TaskSeries taskSeries = dataset.getTasks().getSeries(seriesIndex);
	taskSeries.add(new BookingTask<T, ST>(series.getName(), series, data.get(seriesIndex).getKey(), subTask));
    }

    @SuppressWarnings("unchecked")
    public BookingTask<T, ST> getFirstTaskBefore(final BookingSeries<T, ST> bSeries, final Date date, final int seriesIndex) {
	final XYDataset dataset = getDatasetFor(bSeries);
	if(dataset == null) {
	    return null;
	}
	final TaskSeries series = ((XYTaskDataset) dataset).getTasks().getSeries(seriesIndex);
	BookingTask<T, ST> firstTaskBefore = null;
	for(int itemInd = 0; itemInd < series.getItemCount(); itemInd++) {
	    final BookingTask<T, ST> currTask = (BookingTask<T, ST>)series.get(itemInd);
	    if(currTask.getTo() != null && currTask.getTo().before(date) &&
		    (firstTaskBefore == null || firstTaskBefore.getTo().before(currTask.getTo()))) {
		firstTaskBefore = currTask;
	    }
	}
	return firstTaskBefore;
    }

    @SuppressWarnings("unchecked")
    public BookingTask<T, ST> getFirstTaskAfter(final BookingSeries<T, ST> bSeries, final Date date, final int seriesIndex) {
	final XYDataset dataset = getDatasetFor(bSeries);
	if(dataset == null) {
	    return null;
	}
	final TaskSeries series = ((XYTaskDataset) dataset).getTasks().getSeries(seriesIndex);
	BookingTask<T, ST> firstTaskAfter = null;
	for(int itemInd = 0; itemInd < series.getItemCount(); itemInd++) {
	    final BookingTask<T, ST> currTask = (BookingTask<T, ST>)series.get(itemInd);
	    if(currTask.getFrom() != null && currTask.getFrom().after(date) &&
		    (firstTaskAfter == null || firstTaskAfter.getFrom().after(currTask.getFrom()))) {
		firstTaskAfter = currTask;
	    }
	}
	return firstTaskAfter;
    }

    public XYDataset getDatasetFor(final BookingSeries<T, ST> series) {
	final Iterator<BookingSeries<T, ST>> iter = serieses.iterator();
	int seriesIndex = 0;
	while(iter.hasNext() && iter.next() != series) {
	    seriesIndex++;
	}
	return seriesIndex == serieses.size() ? null : getPlot().getDataset(seriesIndex);
    }
}
