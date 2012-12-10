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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriod;
import org.jfree.ui.RefineryUtilities;
import org.joda.time.DateTime;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.equery.lifecycle.EntityPropertyLifecycle;
import ua.com.fielden.platform.equery.lifecycle.IGroup;
import ua.com.fielden.platform.equery.lifecycle.IProperty;
import ua.com.fielden.platform.equery.lifecycle.IProperty.ITimeProperty;
import ua.com.fielden.platform.equery.lifecycle.IProperty.IValueProperty;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.equery.lifecycle.ValuedInterval;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.swing.categorychart.ChartPanel;
import ua.com.fielden.platform.swing.timeline.ColoredTask;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.IEntityMasterCache;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.jidesoft.plaf.LookAndFeelFactory;

public class TimelineManagementControlExample  extends AbstractUiApplication implements ChartMouseListener, MouseListener, MouseMotionListener {
    private final Module module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    private LifecycleModel<TimelineEntity> chartEntryModel;
    private TaskSeriesCollection mainDataSet = new TaskSeriesCollection();

    ChartPanel localChartPanel = null;
    boolean canMove = false;
    // double finalMovePointY = 0;
    double finalMovePointX = 0;
    ChartRenderingInfo info = null;;
    double initialMovePointX = 0;
    private ResizeMargin resizeMargin = null;
    JFreeChart jfreechart = null;

//    final TaskSeriesCollection mainDataSet = new TaskSeriesCollection();
//	if (chartEntryModel != null) {
//	    for (final EntityPropertyLifecycle<T> epl : chartEntryModel.getLifecycleData()) {
//		final TaskSeries taskSeries = new TaskSeries(epl.getInfo(true));
//
//		for (final ValuedInterval vi : epl.getIntervals()) {
//		    taskSeries.add(new ColoredTask("ColoredTask", new SimpleTimePeriod(vi.getFrom().toDate(), vi.getTo().toDate()), vi.getColor(), vi.getInfo()));
//		}
//		mainDataSet.add(taskSeries);
//	    }
//	}

    XYItemEntity xyItemEntity = null;

    @Override
    protected void beforeUiExposure(final String[] args, final SplashController splashController) throws Throwable {
	SwingUtilitiesEx.installNimbusLnFifPossible();
	com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
	LookAndFeelFactory.installJideExtension();

	chartEntryModel = createLifecycleModel();
	mainDataSet = LifecycleChartFactory.createMainDataSet(chartEntryModel);

	super.beforeUiExposure(args, splashController);
    }

    private TaskSeries localTaskSeries() {
	return mainDataSet.getSeries(0); // TODO take an appropriate series
    }

    /**
     * Example {@link EntityPropertyLifecycle} with some "x" offset.
     *
     * @param x
     * @return
     */
    public EntityPropertyLifecycle<TimelineEntity> createEPL(final String key, final int x) {
	final List<ValuedInterval> intervals = new ArrayList<ValuedInterval>();
	// not sorted:
	intervals.add(new ValuedInterval(date(36 + x), date(42 + x), "Value 1"));
	intervals.add(new ValuedInterval(date(2 + x), date(10 + x), "Value 2"));
	intervals.add(new ValuedInterval(date(42 + x), date(47 + x), "Value 4"));
	intervals.add(new ValuedInterval(date(10 + x), date(31 + x), "Value 2"));
	intervals.add(new ValuedInterval(date(31 + x), date(36 + x), "Value 3"));
	intervals.add(new ValuedInterval(date(47 + x), date(59 + x), "Value 2"));
	return new EntityPropertyLifecycle<TimelineEntity>(factory.newByKey(TimelineEntity.class, key), TimelineEntity.class, "monitoring", intervals, date(15), date(55));
    }

    public DateTime date(final int millis) {
	return new DateTime(2010, 1, 1, 0, 0, 0, millis);
    }

    public EntityPropertyLifecycle<TimelineEntity> createUnavailableEPL(final String key) {
	final List<ValuedInterval> intervals = new ArrayList<ValuedInterval>();
	intervals.add(new ValuedInterval(date(15), date(55), "Value 4"));
	return new EntityPropertyLifecycle<TimelineEntity>(factory.newByKey(TimelineEntity.class, key), TimelineEntity.class, "monitoring", intervals, date(15), date(55));
    }

    /**
     * Creates testing lifecycle model.
     *
     * @return
     */
    private LifecycleModel<TimelineEntity> createLifecycleModel() {
	final List<EntityPropertyLifecycle<TimelineEntity>> ld = new ArrayList<EntityPropertyLifecycle<TimelineEntity>>();
	ld.add(createEPL("A0001", 0));
	ld.add(createEPL("A0002", 5)); // the same lifecycle just shifted to the right on 5 millis.
	ld.add(createEPL("A0003", -2));
	ld.add(createEPL("A0004", 2));
	ld.add(createEPL("A0005", 1));
	ld.add(createEPL("A0006", -1));

	ld.add(createUnavailableEPL("A0007"));

	final LifecycleModel<TimelineEntity> lm = new LifecycleModel<TimelineEntity>(TimelineEntity.class, date(15), date(55), ld, new LinkedHashMap<IProperty, Object>(), true){
	    @Override
	    protected IGroup<TimelineEntity> createGroupByValue(final IValueProperty property, final Object value, final LifecycleModel<TimelineEntity> parent, final List<Integer> indexes) {
		return null;
	    }

	    @Override
	    protected IGroup<TimelineEntity> createGroupByModelAndPeriod(final ITimeProperty timeProperty, final LifecycleModel<TimelineEntity> narrowedModel, final LifecycleModel<TimelineEntity> parent) {
		return null;
	    }

	    @Override
	    public Pair<? extends LifecycleModel<TimelineEntity>, ? extends LifecycleModel<TimelineEntity>> split(final DateTime moment, final boolean copy, final boolean full) {
		return null;
	    }

	    @Override
	    protected LifecycleModel<TimelineEntity> copy(final LinkedHashMap<IProperty, Object> extractedGroupingValues) {
		return null;
	    }

	};
	return lm;
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Throwable {
	final BaseFrame mainApplicationFrame = new BaseFrame("Timeline management control example", new HashMap<Class<? extends AbstractEntity<?>>, IEntityMasterCache>());

//	final ChartPanel localChartPanel = new ChartPanel(chart);
//	localChartPanel.addChartMouseListener(this);
//	localChartPanel.addMouseMotionListener(this);
//	localChartPanel.addMouseListener(this);

	jfreechart = LifecycleChartFactory.createAvailabilityChart(chartEntryModel, mainDataSet); // ChartFactory.createTimeSeriesChart("Series & Point Dragging Demo", "Date", "Price Per Unit", createDataset(), true, true, false);
	localChartPanel = new ChartPanel(jfreechart);
	localChartPanel.addChartMouseListener(this);
	localChartPanel.addMouseMotionListener(this);
	localChartPanel.addMouseListener(this);
	localChartPanel.setPreferredSize(new Dimension(750, 500));
	localChartPanel.setAutoscrolls(false);
	// localChartPanel.setMouseZoomable(false);
	localChartPanel.setMouseWheelEnabled(true);
	this.info = localChartPanel.getChartRenderingInfo();

/*	final XYPlot localXYPlot = (XYPlot) jfreechart.getPlot();
	final XYItemRenderer localXYItemRenderer = localXYPlot.getRenderer();

	localXYItemRenderer.setSeriesStroke(0, new BasicStroke(2.0F));

	final XYLineAndShapeRenderer localXYLineAndShapeRenderer = (XYLineAndShapeRenderer) localXYPlot.getRenderer();
	localXYLineAndShapeRenderer.setBaseShapesVisible(true);

	localXYLineAndShapeRenderer.setSeriesFillPaint(0, Color.white);
	localXYLineAndShapeRenderer.setUseFillPaint(true);
	localXYLineAndShapeRenderer.setLegendItemToolTipGenerator(new StandardXYSeriesLabelGenerator("Tooltip {0}"));
	final ValueAxis range = localXYPlot.getRangeAxis();
	range.setLowerBound(0); // set lower limit so that can't move in -ve
				// range
	range.setUpperBound(10000); // set upper limit as per app. needs
*/

//	final ChartPanel chartPanel = new ChartPanel(chart) {
//	    private static final long serialVersionUID = -4977378293075789317L;
//
//	    @Override
//	    public void mouseDragged(final MouseEvent mouseEvent) {
//	        super.mouseDragged(mouseEvent);
//
//		/*Insets insets = getInsets();
//		int x = (int) ((event.getX() - insets.left) / this.scaleX);
//		int y = (int) ((event.getY() - insets.top) / this.scaleY);
//
//		this.anchor = new Point2D.Double(x, y);
//		ChartEntity entity = null;
//		if (this.info != null) {
//		    EntityCollection entities = this.info.getEntityCollection();
//		    if (entities != null) {
//			entity = entities.getEntity(x, y);
//		    }
//		}*/
//
//	    }
//	};
//	chartPanel.addChartMouseListener(new ChartMouseListener() {
//	    @Override
//	    public void chartMouseClicked(final ChartMouseEvent event) {
//		if (event.getTrigger().isControlDown() && /*event.getTrigger().getClickCount() == 2 && */ !event.getTrigger().isConsumed()) {
//		    performCustomAction(new AnalysisDataEvent<>(mainApplicationFrame, event /*chartMouseEvent*/));
//		}
//
//		//if (event.getTrigger().getClickCount() == 2 && !event.getTrigger().isConsumed()) {
//		//    event.getTrigger().consume();
//		//    mouseDoubleClicked(event);
//		//} else if (event.getTrigger().getClickCount() == 1) {
//		//    event.getTrigger().consume();
//		//    if (getChart().getPlot() instanceof CategoryPlot) {
//		//	final CategoryPlot plot = getChart().getCategoryPlot();
//		//	final CategoryItemRenderer renderer = plot.getRenderer();
//		//	labelVisible = !labelVisible;
//		//	renderer.setBaseItemLabelsVisible(labelVisible);
//		//    }
//		//}
//	    }
//
//	    @Override
//	    public void chartMouseMoved(final ChartMouseEvent event) {
//		// ignored for now
//	    }
//
//	});

	mainApplicationFrame.setPreferredSize(new Dimension(1280, 800));
	mainApplicationFrame.add(localChartPanel);
	mainApplicationFrame.pack();

	RefineryUtilities.centerFrameOnScreen(mainApplicationFrame);
	SimpleLauncher.show("Timeline management control example", mainApplicationFrame, null);
    }

//    protected void performCustomAction(final AnalysisDataEvent<ChartMouseEvent> clickedData) {
//	final ChartEntity entity = clickedData.getData().getEntity();
//	System.err.println("CTRL + " + entity);
//	if (entity instanceof CategoryItemEntity) {
//	    createDoubleClickAction(createChoosenItem(((CategoryItemEntity) entity).getColumnKey())).actionPerformed(null);
//	} else if (entity instanceof CategoryLabelEntity) {
//	    createDoubleClickAction(createChoosenItem(((CategoryLabelEntity) entity).getKey())).actionPerformed(null);
//	}
//    }
//
//    private Pair<String, Object> createChoosenItem(final Comparable<?> columnKey) {
//	final EntityWrapper entityWrapper = (EntityWrapper) columnKey;
//	final List<String> categories = getModel().getChartAnalysisDataProvider().categoryProperties();
//	if(categories.size() == 1){
//	    return new Pair<String, Object>(categories.get(0), entityWrapper.getEntity());
//	}
//	return null;
//    }

    public static void main(final String[] args) {
	DOMConfigurator.configure("src/main/resources/log4j.xml");
	new TimelineManagementControlExample().launch(args);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    public void chartMouseClicked(final ChartMouseEvent paramChartMouseEvent) {
    }

    public void chartMouseMoved(final ChartMouseEvent paramChartMouseEvent) {
    }

/*    public XYDataset createDataset() {
       localTimeSeries.add(new Month(1, 2002), 500.19999999999999D);
       localTimeSeries.add(new Month(2, 2002), 694.10000000000002D);
       localTimeSeries.add(new Month(3, 2002), 734.39999999999998D);
       localTimeSeries.add(new Month(4, 2002), 453.19999999999999D);
       localTimeSeries.add(new Month(5, 2002), 200.19999999999999D);
       localTimeSeries.add(new Month(6, 2002), 345.60000000000002D);
       localTimeSeries.add(new Month(7, 2002), 500.19999999999999D);
       localTimeSeries.add(new Month(8, 2002), 694.10000000000002D);
       localTimeSeries.add(new Month(9, 2002), 734.39999999999998D);
       localTimeSeries.add(new Month(10, 2002), 453.19999999999999D);
       localTimeSeries.add(new Month(11, 2002), 500.19999999999999D);
       localTimeSeries.add(new Month(12, 2002), 345.60000000000002D);
       timeseriescollection.addSeries(localTimeSeries);
       return timeseriescollection;
    }*/

    public void mouseClicked(final MouseEvent e) {
    }

    public void mouseDragged(final MouseEvent e) {
       // at a time use one of them.
       // moveTimeSeries(e); // comment or uncomment to enable or disable series
                      // movement
       movePoint(e); // comment or uncomment to enable or disable selected
                   // point movement
    }

    public void mouseEntered(final MouseEvent e) {
    }

    public void mouseExited(final MouseEvent e) {
       canMove = false; // stop movement if cursor is moved out from the chart
                      // area
       initialMovePointX = 0;
       localChartPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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

	if ((entity != null) && (entity instanceof XYItemEntity)) {
	    return (XYItemEntity) entity;
	} else {
	    return null;
	}
    }

    public void mouseMoved(final MouseEvent e) {
	final XYItemEntity newItemEntity = determineAnItemUnderACursor((ChartPanel) e.getSource(), e.getX(), e.getY());
	if (!EntityUtils.equalsEx(newItemEntity, xyItemEntity)) {
	    xyItemEntity = newItemEntity;
	    System.err.println("		xyItemEntity has been changed to == " + xyItemEntity);
	}
	provideCursor(e, false);
    }

    private enum ResizeMargin {
	LEFT, RIGHT
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
	}
	return null;
    }

    public void mousePressed(final MouseEvent e) {
//       final int x = e.getX(); // initialized point whenenver mouse is pressed
//       final int y = e.getY();

//       final ChartPanel chartPanel = (ChartPanel) e.getSource();
//       final Insets insets = chartPanel.getInsets();
//       final int x = (int) ((e.getX() - insets.left) / chartPanel.getScaleX());
//       final int y = (int) ((e.getY() - insets.top) / chartPanel.getScaleY());


       initialMovePointX = determinePointX(e.getPoint()); // xy.getRangeAxis().java2DToValue(p.getX(), dataArea, xy.getRangeAxisEdge());
       canMove = true;

       resizeMargin = provideCursor(e, true);
       // localChartPanel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
    }

    public void mouseReleased(final MouseEvent e) {
       // stop dragging on mouse released
       canMove = false;
       initialMovePointX = 0;
       resizeMargin = provideCursor(e, false);
    }

    public void movePoint(final MouseEvent me) {
       try {
          if (canMove) {
             finalMovePointX = determinePointX(me.getPoint());

             final double difference = finalMovePointX - initialMovePointX;
             final double itemWidth = determineEntityWidth();
             // System.out.println("itemWidth == " + itemWidth);

             final double relational = difference / itemWidth;
             final double resizeRelationalValue = (relational > 1.0 && ResizeMargin.LEFT == resizeMargin) ? 1.0 : //
        	     			(relational < -1.0 && ResizeMargin.RIGHT == resizeMargin) ? -1.0 : relational;
             System.out.println("resizeRelationalValue == " + resizeRelationalValue * 100 + "%");
             System.out.println("resizeMargin == " + resizeMargin);


             final int itemIndex = xyItemEntity.getItem();
             final ColoredTask task = (ColoredTask) localTaskSeries().get(itemIndex);
             final TimePeriod oldPeriod = task.getDuration();
             final ColoredTask newTask = new ColoredTask(task.getDescription(), adjustPeriod(oldPeriod, resizeMargin, resizeRelationalValue), task.getColor(), task.getInfo());

             localTaskSeries().remove(task);
             localTaskSeries().add(newTask);

             jfreechart.fireChartChanged();
             localChartPanel.updateUI();

             initialMovePointX = finalMovePointX; // TODO

//           if (localTaskSeries().getValue(itemIndex).doubleValue()
//           + difference > xy.getRangeAxis().getRange().getLength()
//           || localTaskSeries().getValue(itemIndex).doubleValue()
//                 + difference < 0.0D) {
//        initialMovePointY = finalMovePointY;
//     }
//     // retrict movement for upper and lower limit (upper limit
//     // should be as per application needs)
//     final double targetPoint = localTaskSeries().getValue(itemIndex)
//           .doubleValue()
//           + difference;
//     if (targetPoint > 10000 || targetPoint < 0) {
//        return;
//     } else
//	 localTaskSeries().update(itemIndex, targetPoint);

          }
       } catch (final Exception e) {
	   e.printStackTrace();
	   System.out.println(e);
       }
    }

    private TimePeriod adjustPeriod(final TimePeriod oldPeriod, final ResizeMargin resizeMargin, final double resizeRelationalValue) {
	if (resizeMargin == null) {
	    throw new IllegalArgumentException("ResizeMargin could not be null.");
	}
	final Date adjustableDate = ResizeMargin.LEFT == resizeMargin ? oldPeriod.getStart() : oldPeriod.getEnd();
	final long duration = oldPeriod.getEnd().getTime() - oldPeriod.getStart().getTime();
	final long newDate = (long) (adjustableDate.getTime() + duration * resizeRelationalValue);
	return new SimpleTimePeriod(ResizeMargin.LEFT == resizeMargin ? newDate : oldPeriod.getStart().getTime(), ResizeMargin.LEFT == resizeMargin ? oldPeriod.getEnd().getTime() : newDate);
    }

    public double determineEntityWidth() {
	final Rectangle2D itemBounds = xyItemEntity.getArea().getBounds2D();
	return itemBounds.getWidth() * localChartPanel.getScaleX(); // x2 - x1; // itemWidth;
    }

    public double determinePointX(final Point pt) {
//	final XYPlot xy = jfreechart.getXYPlot();
//	final Rectangle2D dataArea = localChartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
//	final Point2D p = localChartPanel.translateScreenToJava2D(pt);
//	return xy.getRangeAxis().java2DToValue(p.getX(), dataArea, xy.getRangeAxisEdge());
	return pt.getX();
    }

//    public void moveTimeSeries(final MouseEvent me) {
//       try {
//          if (canMove) {
//             final Point pt = me.getPoint();
//             final XYPlot xy = jfreechart.getXYPlot();
//             final Rectangle2D dataArea = localChartPanel.getChartRenderingInfo()
//                   .getPlotInfo().getDataArea();
//             final Point2D p = localChartPanel.translateScreenToJava2D(pt);
//             finalMovePointY = xy.getRangeAxis().java2DToValue(p.getY(),
//                   dataArea, xy.getRangeAxisEdge());
//             final double difference = finalMovePointY - initialMovePointY;
//             for (int i = 0; i < localTaskSeries().getItemCount(); i++) {
//                if (localTaskSeries().getValue(i).doubleValue() + difference > xy
//                      .getRangeAxis().getRange().getLength()
//                      || localTaskSeries().getValue(i).doubleValue()
//                            + difference < 0.0D) {
//                   initialMovePointY = finalMovePointY;
//                }
//             }
//
//             // retrict movement for upper and lower limit (upper limit
//             // should be as per application needs)
//             for (int i = 0; i < localTaskSeries().getItemCount(); i++) {
//                final double targetPoint = localTaskSeries().getValue(i)
//                      .doubleValue()
//                      + difference;
//                if (targetPoint > 10000 || targetPoint < 0) {
//                   return;
//                }
//             }
//             for (int i = 0; i < localTaskSeries().getItemCount(); i++) {
//                final double targetPoint = localTaskSeries().getValue(i)
//                      .doubleValue()
//                      + difference;
//                localTaskSeries().update(i, targetPoint);
//             }
//             jfreechart.fireChartChanged();
//             localChartPanel.updateUI();
//             initialMovePointY = finalMovePointY;
//          }
//       } catch (final Exception e) {
//          System.out.println(e);
//       }
//    }

}
