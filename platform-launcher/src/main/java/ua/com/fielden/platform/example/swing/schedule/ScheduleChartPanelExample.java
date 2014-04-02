package ua.com.fielden.platform.example.swing.schedule;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Paint;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import net.miginfocom.swing.MigLayout;

import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.schedule.IScheduleChangeEventListener;
import ua.com.fielden.platform.swing.schedule.IScheduleChartMouseEventListener;
import ua.com.fielden.platform.swing.schedule.ISchedulePainter;
import ua.com.fielden.platform.swing.schedule.MultipleTypeScheduleEntity;
import ua.com.fielden.platform.swing.schedule.ScheduleChangedEvent;
import ua.com.fielden.platform.swing.schedule.ScheduleChartPanel;
import ua.com.fielden.platform.swing.schedule.ScheduleMouseEvent;
import ua.com.fielden.platform.swing.schedule.ScheduleSeries;
import ua.com.fielden.platform.swing.schedule.SingleTypeScheduleEntity;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.utils.Pair;

import com.jidesoft.plaf.LookAndFeelFactory;

public class ScheduleChartPanelExample {

    public static void main(final String[] args) {
        SwingUtilitiesEx.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (final LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(laf.getName())) {
                        try {
                            UIManager.setLookAndFeel(laf.getClassName());
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                com.jidesoft.utils.Lm.verifyLicense("Fielden Management Services", "Rollingstock Management System", "xBMpKdqs3vWTvP9gxUR4jfXKGNz9uq52");
                LookAndFeelFactory.installJideExtension();
                final JFrame frame = new JFrame("Scedule chart demo");
                final JLabel label = new JLabel("None");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new MigLayout("fill, insets 0", "[grow, fill]", "[grow, fill][]"));
                frame.add(createScheduleChartPanel(label), "wrap");
                frame.add(label);
                frame.setPreferredSize(new Dimension(640, 480));
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    private static Component createScheduleChartPanel(final JLabel taskBar) {
        final ScheduleChartPanel<? extends AbstractEntity<?>> schedule = new ScheduleChartPanel<AbstractEntity<?>>();

        schedule.setChartName("Schedule chart demo");
        schedule.setRangeAxisName("Range axis");
        schedule.setDomainAxisName("Domain axis");

        //marker value now.
        final Date now = new Date();

        schedule.addScheduleSeries(createActualSeries(now));
        schedule.addScheduleSeries(createEstimateSeries(now));

        schedule.addMarker(createNowMarker(now));

        schedule.setData(WorkOrderDataStore.dataAll);

        schedule.addScheduleChartMouseEventListener(createMoseListener(taskBar));
        schedule.addScheduleChangedEventListener(createScheduleChangedListener(taskBar));

        return schedule;
    }

    private static IScheduleChangeEventListener<AbstractEntity<?>> createScheduleChangedListener(final JLabel taskBar) {
        return new IScheduleChangeEventListener<AbstractEntity<?>>() {

            @Override
            public void scheduleChanged(final ScheduleChangedEvent<AbstractEntity<?>> event) {
                taskBar.setText(event.getEntity() + ": (" + event.getSeries().getScheduleEntity().getFrom(event.getEntity()) + ", "
                        + event.getSeries().getScheduleEntity().getTo(event.getEntity()) + ")");
            }

        };
    }

    private static IScheduleChartMouseEventListener<AbstractEntity<?>> createMoseListener(final JLabel taskBar) {
        return new IScheduleChartMouseEventListener<AbstractEntity<?>>() {

            @Override
            public void mouseClick(final ScheduleMouseEvent<AbstractEntity<?>> event) {
                System.out.println("==========mouse clicked==========");
                System.out.println("Coordinates: " + event.getX() + " : " + event.getY());
                if (event.getEntity() != null) {
                    System.out.println("Entity: " + event.getEntity() + " Series name: " + event.getSeries().getName());
                }
                System.out.println("=================================");

            }

            @Override
            public void mouseMove(final ScheduleMouseEvent<AbstractEntity<?>> event) {
                taskBar.setText("Coordinates: " + event.getX() + " : " + event.getY());
            }

        };
    }

    private static Marker createNowMarker(final Date now) {
        final ValueMarker valuemarker = new ValueMarker(now.getTime());
        valuemarker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        valuemarker.setPaint(Color.BLACK);
        valuemarker.setLabel("Today");
        valuemarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
        valuemarker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
        return valuemarker;
    }

    private static ScheduleSeries<AbstractEntity<?>> createActualSeries(final Date now) {
        final MultipleTypeScheduleEntity entity = new MultipleTypeScheduleEntity();
        entity.setScheduleSeries(WorkOrderEntity.class, new SingleTypeScheduleEntity<WorkOrderEntity>(WorkOrderEntity.class, "actualStart", "actualFinish", now) {

            @Override
            public boolean canEditEntity(final WorkOrderEntity entity) {
                return false;
            }

        });
        final ScheduleSeries<AbstractEntity<?>> series = new ScheduleSeries<AbstractEntity<?>>(entity);
        series.setCutOfFactor(0.5);
        series.setName("Actual series");
        series.setPainter(new ISchedulePainter<AbstractEntity<?>>() {

            final Color color = new Color(39, 148, 216);

            @Override
            public Paint getPainterFor(final AbstractEntity<?> entity) {
                return color;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Pair<String, Paint>> getAvailableLegendItems() {
                return Arrays.asList(new Pair<String, Paint>("Actual duration", color));
            }
        });
        return series;
    }

    private static ScheduleSeries<AbstractEntity<?>> createEstimateSeries(final Date now) {
        final MultipleTypeScheduleEntity entity = new MultipleTypeScheduleEntity();
        entity.setScheduleSeries(WorkOrderEntity.class, new SingleTypeScheduleEntity<WorkOrderEntity>(WorkOrderEntity.class, "earlyStart", "earlyFinish", now) {

            @Override
            public boolean canEditEntity(final WorkOrderEntity entity) {
                return true;
            }

        })//
        .setScheduleSeries(WorkRequest.class, new SingleTypeScheduleEntity<WorkRequest>(WorkRequest.class, "requestStart", "requestFinish", now) {

            @Override
            public boolean canEditEntity(final WorkRequest entity) {
                return true;
            }

        });

        final ScheduleSeries<AbstractEntity<?>> series = new ScheduleSeries<AbstractEntity<?>>(entity);
        series.setCutOfFactor(0.10);
        series.setName("Early series");
        series.setPainter(new ISchedulePainter<AbstractEntity<?>>() {

            final Color color = new Color(254, 214, 88);
            final Color invalidColor = new Color(238, 83, 35);
            final Color requestColor = new Color(27, 114, 25);

            @Override
            public Paint getPainterFor(final AbstractEntity<?> entity) {
                if (entity instanceof WorkOrderEntity) {
                    if (isValid((WorkOrderEntity) entity)) {
                        return color;
                    } else {
                        return invalidColor;
                    }
                } else {
                    return requestColor;
                }
            }

            private boolean isValid(final WorkOrderEntity entity) {
                final WorkRequest request = entity.getWorkRequest();
                final Date date = entity.getEarlyFinish() == null ? now : entity.getEarlyFinish();
                if (request.getRequestFinish().before(date)) {
                    return false;
                }
                return true;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Pair<String, Paint>> getAvailableLegendItems() {
                return Arrays.asList(new Pair<String, Paint>("Estimated duration", color), new Pair<String, Paint>("Work requst duration", requestColor));
            }
        });
        return series;
    }
}
