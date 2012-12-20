package ua.com.fielden.platform.swing.review.report.analysis.lifecycle;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriod;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.lifecycle.EntityPropertyLifecycle;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.swing.categorychart.ChartPanel;
import ua.com.fielden.platform.swing.timeline.ColoredTask;
import ua.com.fielden.platform.utils.EntityUtils;

public abstract class TimelineManagementControl <T extends AbstractEntity> implements ChartMouseListener, MouseListener, MouseMotionListener {
    private final ChartPanel localChartPanel;
    private final JFreeChart jfreechart;
    private final TaskSeriesCollection mainDataSet;
    private final LifecycleModel<T> chartEntryModel;

    private double itemWidth = -1;
    private boolean startedResizingOrMoving = false;
    private double finalMovePointX = 0;
    private ChartRenderingInfo info = null;;
    private double initialMovePointX = 0;
    private ResizeMargin resizeMargin = null;
    private XYItemEntity xyItemEntity = null;
    private int seriesIndex = 0;

    public TimelineManagementControl(final LifecycleModel<T> chartEntryModel) {
	this.chartEntryModel = chartEntryModel;
	this.mainDataSet = LifecycleChartFactory.createMainDataSet(chartEntryModel);
	jfreechart = LifecycleChartFactory.createAvailabilityChart(chartEntryModel, mainDataSet); // ChartFactory.createTimeSeriesChart("Series & Point Dragging Demo", "Date", "Price Per Unit", createDataset(), true, true, false);
	localChartPanel = new ChartPanel(jfreechart);

	localChartPanel.addChartMouseListener(this);
	localChartPanel.addMouseMotionListener(this);
	localChartPanel.addMouseListener(this);
	localChartPanel.setPreferredSize(new Dimension(750, 500));
	localChartPanel.setAutoscrolls(false);

	// TODO temporally disable "rectangle" mouse zooming:
	localChartPanel.setMouseZoomable(false);

	localChartPanel.setMouseWheelEnabled(true);
	this.info = localChartPanel.getChartRenderingInfo();

	jfreechart.removeLegend();
    }

    public abstract boolean canBeChanged(T entity, final Date start, final Date finish);
    public abstract void change(T entity, final Date start, final Date finish);

    public ChartPanel getLocalChartPanel() {
	return localChartPanel;
    }

    @Override
    public void chartMouseClicked(final ChartMouseEvent paramChartMouseEvent) {
    }

    @Override
    public void chartMouseMoved(final ChartMouseEvent paramChartMouseEvent) {
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
	movePoint(e);
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
    }

    @Override
    public void mouseExited(final MouseEvent e) {
	startedResizingOrMoving = false;
	initialMovePointX = 0;
	localChartPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
	if (!startedResizingOrMoving) {
	    final XYItemEntity newItemEntity = determineAnItemUnderACursor((ChartPanel) e.getSource(), e.getX(), e.getY());
	    if (!EntityUtils.equalsEx(newItemEntity, xyItemEntity)) {
		if (newItemEntity != null) {
		    seriesIndex = newItemEntity.getSeriesIndex();
		} else {
		    seriesIndex = -1;
		}

		xyItemEntity = newItemEntity;
		System.err.println("MOUSE MOVED:		xyItemEntity has been changed to == " + xyItemEntity);
	    }
	    provideCursor(e, false);
	}
    }

    @Override
    public void mousePressed(final MouseEvent e) {
	startedResizingOrMoving = true;
	initialMovePointX = determinePointX(e.getPoint());
	finalMovePointX = 0;

	resizeMargin = provideCursor(e, true);
	itemWidth = determineEntityWidth();
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
	// stop dragging on mouse released
	startedResizingOrMoving = false;
	initialMovePointX = 0;
	finalMovePointX = 0;
	itemWidth = -1;
	resizeMargin = provideCursor(e, false);
    }

    public void movePoint(final MouseEvent me) {
	try {
	    if (startedResizingOrMoving) {
		finalMovePointX = determinePointX(me.getPoint());

		final double difference = finalMovePointX - initialMovePointX;

		itemWidth = determineEntityWidth();
		System.out.println("itemWidth == " + itemWidth);

		final int itemIndex = xyItemEntity.getItem();
		final ColoredTask task = (ColoredTask) localTaskSeries().get(itemIndex);
		final TimePeriod oldPeriod = task.getDuration();
		System.out.println("Old period [" + oldPeriod.getStart() + "; " + oldPeriod.getEnd() + "].");
		final TimePeriod newPeriod = adjustPeriod(oldPeriod, resizeMargin, difference);
		final ColoredTask newTask = new ColoredTask(task.getDescription(), newPeriod, task.getColor(), task.getInfo());

		if (canBeChanged(chartEntryModel.getLifecycleData().get(seriesIndex).getEntity(), newPeriod.getStart(), newPeriod.getEnd())) {
		    change(chartEntryModel.getLifecycleData().get(seriesIndex).getEntity(), newPeriod.getStart(), newPeriod.getEnd());

		    localTaskSeries().remove(task);
		    localTaskSeries().add(newTask);
		    final int newTaskIndex = localTaskSeries().getItemCount() - 1;

		    jfreechart.fireChartChanged();
		    localChartPanel.updateUI();

		    System.out.println("newTaskIndex == " + newTaskIndex);

		    System.out.println("seriesIndex == " + seriesIndex);
		    xyItemEntity = findXyEntity(seriesIndex, newTaskIndex, this.info.getEntityCollection());
		    System.err.println("MOUSE DRAGGED:		xyItemEntity has been changed to == " + xyItemEntity);

		}
		initialMovePointX = finalMovePointX;
	    }
	} catch (final Exception e) {
	    e.printStackTrace();
	    System.out.println(e);
	}
    }

    private TimePeriod adjustPeriod(final TimePeriod oldPeriod, final ResizeMargin resizeMargin, final double difference) {
	if (resizeMargin == null) {
	    throw new IllegalArgumentException("ResizeMargin could not be null.");
	}

        final double relational = difference / itemWidth;
        final double resizeRelationalValue = (relational > 1.0 && ResizeMargin.LEFT == resizeMargin) ? 1.0 : //
   	     			(relational < -1.0 && ResizeMargin.RIGHT == resizeMargin) ? -1.0 : relational;
        System.out.println("resizeRelationalValue == " + resizeRelationalValue * 100 + "%");
        System.out.println("resizeMargin == " + resizeMargin);

	final long oldLeft = oldPeriod.getStart().getTime();
	final long oldRight = oldPeriod.getEnd().getTime();
	final long duration = oldRight - oldLeft;
	return new SimpleTimePeriod(	ResizeMargin.LEFT == resizeMargin || ResizeMargin.MIDDLE == resizeMargin ? adjust(oldLeft, duration * resizeRelationalValue) : oldLeft,
					ResizeMargin.RIGHT == resizeMargin || ResizeMargin.MIDDLE == resizeMargin ? adjust(oldRight, duration * resizeRelationalValue) : oldRight);
    }

    private long adjust(final long date, final double by) {
	final long newDate = (long) (date + by);
	return newDate;
    }

    public double determineEntityWidth() {
	final Rectangle2D itemBounds = xyItemEntity.getArea().getBounds2D();
	return itemBounds.getWidth() * localChartPanel.getScaleX(); // x2 - x1; // itemWidth;
    }

    public double determinePointX(final Point pt) {
	return pt.getX();
    }

    /**
     * Determines an entity currently situated under a cursor.
     *
     * @param chartPanel
     * @param x0
     * @param y0
     * @return
     */
    private XYItemEntity determineAnItemUnderACursor(final ChartPanel chartPanel, final int x0, final int y0) {
	final Insets insets = chartPanel.getInsets();
	final int x = (int) ((x0 - insets.left) / chartPanel.getScaleX());
	final int y = (int) ((y0 - insets.top) / chartPanel.getScaleY());

	final ChartEntity entity = this.info.getEntityCollection().getEntity(x, y);

	return xyEntity(entity);
    }

    private XYItemEntity findXyEntity(final int seriesIndex, final int itemIndex, final EntityCollection entityCollection) {
	for (final Object entity : entityCollection.getEntities()) {
	    if ((entity != null) && (entity instanceof XYItemEntity)) {
		final XYItemEntity xyEntity = (XYItemEntity) entity;
		if (xyEntity.getSeriesIndex() == seriesIndex && xyEntity.getItem() == itemIndex) {
		    return xyEntity;
		}
	    }
	}
	return null;
    }

    private XYItemEntity xyEntity(final ChartEntity entity) {
	if ((entity != null) && (entity instanceof XYItemEntity)) {
	    return (XYItemEntity) entity;
	} else {
	    return null;
	}
    }


    private enum ResizeMargin {
	LEFT, RIGHT, MIDDLE
    }

    public ResizeMargin provideCursor(final MouseEvent e, final boolean pressed) {
	final int gap = 3;
	if (xyItemEntity == null) {
	    localChartPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	} else if (!EntityUtils.equalsEx(xyItemEntity, determineAnItemUnderACursor((ChartPanel) e.getSource(), e.getX() - gap, e.getY()))) {
	    // the left boundary of xyItemEntity has been found
	    localChartPanel.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
	    return ResizeMargin.LEFT;
	} else if (!EntityUtils.equalsEx(xyItemEntity, determineAnItemUnderACursor((ChartPanel) e.getSource(), e.getX() + gap, e.getY()))) {
	    // the right boundary of xyItemEntity has been found
	    localChartPanel.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
	    return ResizeMargin.RIGHT;
	} else {
	    localChartPanel.setCursor(pressed ? new Cursor(Cursor.MOVE_CURSOR) : new Cursor(Cursor.HAND_CURSOR));
	    if (pressed) {
		return ResizeMargin.MIDDLE;
	    }
	}
	return null;
    }

    private TaskSeries localTaskSeries() {
	return mainDataSet.getSeries(seriesIndex); // TODO take an appropriate series
    }

    public List<T> getEntities() {
	final List<T> entities = new ArrayList<T>();
	for (final EntityPropertyLifecycle<T> epl : chartEntryModel.getLifecycleData()) {
	    entities.add(epl.getEntity());
	}
	return entities;
    }
}
