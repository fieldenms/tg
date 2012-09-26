package ua.com.fielden.platform.swing.review.report.analysis.lifecycle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RefineryUtilities;
import org.joda.time.DateTime;

import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.Entity;
import ua.com.fielden.platform.equery.lifecycle.EntityPropertyLifecycle;
import ua.com.fielden.platform.equery.lifecycle.IGroup;
import ua.com.fielden.platform.equery.lifecycle.IProperty;
import ua.com.fielden.platform.equery.lifecycle.IProperty.ITimeProperty;
import ua.com.fielden.platform.equery.lifecycle.IProperty.IValueProperty;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModelTest;
import ua.com.fielden.platform.swing.categorychart.ChartPanel;
import ua.com.fielden.platform.swing.utils.SimpleLauncher;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.swing.view.BaseFrame;
import ua.com.fielden.platform.swing.view.IEntityMasterCache;
import ua.com.fielden.platform.utils.Pair;

import com.jidesoft.plaf.LookAndFeelFactory;

public class TimelineManagementControlExample  extends AbstractUiApplication implements ChartMouseListener, MouseListener, MouseMotionListener {
    private LifecycleModel<Entity> chartEntryModel;
    private TaskSeriesCollection mainDataSet = new TaskSeriesCollection();

    ChartPanel localChartPanel = null;
    boolean canMove = false;
    double finalMovePointY = 0;
    ChartRenderingInfo info = null;;
    double initialMovePointY = 0;
    JFreeChart jfreechart = null;
    TimeSeries localTimeSeries = new TimeSeries("Series");
    TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
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

    /**
     * Creates testing lifecycle model.
     *
     * @return
     */
    public static LifecycleModel<Entity> createLifecycleModel() {
	final LifecycleModelTest lmt = new LifecycleModelTest();
	final List<EntityPropertyLifecycle<Entity>> ld = new ArrayList<EntityPropertyLifecycle<Entity>>();
	ld.add(lmt.createEPL("A0001", 0));
	ld.add(lmt.createEPL("A0002", 5)); // the same lifecycle just shifted to the right on 5 millis.
	ld.add(lmt.createEPL("A0003", -2));
	ld.add(lmt.createEPL("A0004", 2));
	ld.add(lmt.createEPL("A0005", 1));
	ld.add(lmt.createEPL("A0006", -1));

	ld.add(lmt.createUnavailableEPL("A0007"));

	final LifecycleModel<Entity> lm = new LifecycleModel<Entity>(lmt.date(15), lmt.date(55), ld, new LinkedHashMap<IProperty, Object>(), true){
	    @Override
	    protected IGroup<Entity> createGroupByValue(final IValueProperty property, final Object value, final LifecycleModel<Entity> parent, final List<Integer> indexes) {
		return null;
	    }

	    @Override
	    protected IGroup<Entity> createGroupByModelAndPeriod(final ITimeProperty timeProperty, final LifecycleModel<Entity> narrowedModel, final LifecycleModel<Entity> parent) {
		return null;
	    }

	    @Override
	    public Pair<? extends LifecycleModel<Entity>, ? extends LifecycleModel<Entity>> split(final DateTime moment, final boolean copy, final boolean full) {
		return null;
	    }

	    @Override
	    protected LifecycleModel<Entity> copy(final LinkedHashMap<IProperty, Object> extractedGroupingValues) {
		return null;
	    }

	};
	return lm;
    }

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Throwable {
	final BaseFrame mainApplicationFrame = new BaseFrame("Timeline management control example", new HashMap<Class<? extends AbstractEntity<?>>, IEntityMasterCache>());

	final JFreeChart chart = LifecycleChartFactory.createAvailabilityChart(chartEntryModel, mainDataSet);

//	final ChartPanel localChartPanel = new ChartPanel(chart);
//	localChartPanel.addChartMouseListener(this);
//	localChartPanel.addMouseMotionListener(this);
//	localChartPanel.addMouseListener(this);

	jfreechart = chart; // ChartFactory.createTimeSeriesChart("Series & Point Dragging Demo", "Date", "Price Per Unit", createDataset(), true, true, false);
	localChartPanel = new ChartPanel(jfreechart);
	localChartPanel.addChartMouseListener(this);
	localChartPanel.addMouseMotionListener(this);
	localChartPanel.addMouseListener(this);
	localChartPanel.setPreferredSize(new Dimension(750, 500));
	localChartPanel.setAutoscrolls(false);
	localChartPanel.setMouseZoomable(false);
	this.info = localChartPanel.getChartRenderingInfo();
	final XYPlot localXYPlot = (XYPlot) jfreechart.getPlot();
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

    public XYDataset createDataset() {
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
    }

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
       initialMovePointY = 0;
       localChartPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    public void mouseMoved(final MouseEvent e) {
    }

    public void mousePressed(final MouseEvent e) {
       final int x = e.getX(); // initialized point whenenver mouse is pressed
       final int y = e.getY();
       final EntityCollection entities = this.info.getEntityCollection();
       final ChartMouseEvent cme = new ChartMouseEvent(jfreechart, e, entities
             .getEntity(x, y));
       final ChartEntity entity = cme.getEntity();
       if ((entity != null) && (entity instanceof XYItemEntity)) {
          xyItemEntity = (XYItemEntity) entity;
       } else if (!(entity instanceof XYItemEntity)) {
          xyItemEntity = null;
          return;
       }
       if (xyItemEntity == null) {
          return; // return if not pressed on any series point
       }
       final Point pt = e.getPoint();
       final XYPlot xy = jfreechart.getXYPlot();
       final Rectangle2D dataArea = localChartPanel.getChartRenderingInfo()
             .getPlotInfo().getDataArea();
       final Point2D p = localChartPanel.translateScreenToJava2D(pt);
       initialMovePointY = xy.getRangeAxis().java2DToValue(p.getY(), dataArea,
             xy.getRangeAxisEdge());
       canMove = true;
       localChartPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void mouseReleased(final MouseEvent e) {
       // stop dragging on mouse released
       canMove = false;
       initialMovePointY = 0;
       localChartPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    public void movePoint(final MouseEvent me) {
       try {
          if (canMove) {
             final int itemIndex = xyItemEntity.getItem();
             final Point pt = me.getPoint();
             final XYPlot xy = jfreechart.getXYPlot();
             final Rectangle2D dataArea = localChartPanel.getChartRenderingInfo()
                   .getPlotInfo().getDataArea();
             final Point2D p = localChartPanel.translateScreenToJava2D(pt);
             finalMovePointY = xy.getRangeAxis().java2DToValue(p.getY(),
                   dataArea, xy.getRangeAxisEdge());
             final double difference = finalMovePointY - initialMovePointY;
             if (localTimeSeries.getValue(itemIndex).doubleValue()
                   + difference > xy.getRangeAxis().getRange().getLength()
                   || localTimeSeries.getValue(itemIndex).doubleValue()
                         + difference < 0.0D) {
                initialMovePointY = finalMovePointY;
             }
             // retrict movement for upper and lower limit (upper limit
             // should be as per application needs)
             final double targetPoint = localTimeSeries.getValue(itemIndex)
                   .doubleValue()
                   + difference;
             if (targetPoint > 10000 || targetPoint < 0) {
                return;
             } else
                localTimeSeries.update(itemIndex, targetPoint);

             jfreechart.fireChartChanged();
             localChartPanel.updateUI();
             initialMovePointY = finalMovePointY;
          }
       } catch (final Exception e) {
          System.out.println(e);
       }
    }

    public void moveTimeSeries(final MouseEvent me) {
       try {
          if (canMove) {
             final Point pt = me.getPoint();
             final XYPlot xy = jfreechart.getXYPlot();
             final Rectangle2D dataArea = localChartPanel.getChartRenderingInfo()
                   .getPlotInfo().getDataArea();
             final Point2D p = localChartPanel.translateScreenToJava2D(pt);
             finalMovePointY = xy.getRangeAxis().java2DToValue(p.getY(),
                   dataArea, xy.getRangeAxisEdge());
             final double difference = finalMovePointY - initialMovePointY;
             for (int i = 0; i < localTimeSeries.getItemCount(); i++) {
                if (localTimeSeries.getValue(i).doubleValue() + difference > xy
                      .getRangeAxis().getRange().getLength()
                      || localTimeSeries.getValue(i).doubleValue()
                            + difference < 0.0D) {
                   initialMovePointY = finalMovePointY;
                }
             }

             // retrict movement for upper and lower limit (upper limit
             // should be as per application needs)
             for (int i = 0; i < localTimeSeries.getItemCount(); i++) {
                final double targetPoint = localTimeSeries.getValue(i)
                      .doubleValue()
                      + difference;
                if (targetPoint > 10000 || targetPoint < 0) {
                   return;
                }
             }
             for (int i = 0; i < localTimeSeries.getItemCount(); i++) {
                final double targetPoint = localTimeSeries.getValue(i)
                      .doubleValue()
                      + difference;
                localTimeSeries.update(i, targetPoint);
             }
             jfreechart.fireChartChanged();
             localChartPanel.updateUI();
             initialMovePointY = finalMovePointY;
          }
       } catch (final Exception e) {
          System.out.println(e);
       }
    }

}
