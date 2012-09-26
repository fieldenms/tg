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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
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
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
* @author Vimal Goyal
*
*/

@SuppressWarnings("serial")
public class SeriesAndPointDragAndMove extends ApplicationFrame implements
      ChartMouseListener, MouseListener, MouseMotionListener {

   public static void main(final String[] paramArrayOfString) {
      final SeriesAndPointDragAndMove seriesAndPointDragAndMove = new SeriesAndPointDragAndMove(
            "Series & Point Dragging Demo");
      seriesAndPointDragAndMove.pack();
      RefineryUtilities.centerFrameOnScreen(seriesAndPointDragAndMove);
      seriesAndPointDragAndMove.setVisible(true);
   }

   boolean canMove = false;
   double finalMovePointY = 0;
   ChartRenderingInfo info = null;;
   double initialMovePointY = 0;
   JFreeChart jfreechart = null;
   ChartPanel localChartPanel = null;
   TimeSeries localTimeSeries = new TimeSeries("Series");
   TimeSeriesCollection timeseriescollection = new TimeSeriesCollection();
   XYItemEntity xyItemEntity = null;

   public SeriesAndPointDragAndMove(final String paramString) {
      super(paramString);
      jfreechart = ChartFactory.createTimeSeriesChart(
            "Series & Point Dragging Demo", "Date", "Price Per Unit",
            createDataset(), true, true, false);
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

      final XYLineAndShapeRenderer localXYLineAndShapeRenderer = (XYLineAndShapeRenderer) localXYPlot
            .getRenderer();
      localXYLineAndShapeRenderer.setBaseShapesVisible(true);

      localXYLineAndShapeRenderer.setSeriesFillPaint(0, Color.white);
      localXYLineAndShapeRenderer.setUseFillPaint(true);
      localXYLineAndShapeRenderer
            .setLegendItemToolTipGenerator(new StandardXYSeriesLabelGenerator(
                  "Tooltip {0}"));
      final ValueAxis range = localXYPlot.getRangeAxis();
      range.setLowerBound(0); // set lower limit so that can't move in -ve
                        // range
      range.setUpperBound(10000); // set upper limit as per app. needs
      setContentPane(localChartPanel);
   }

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
