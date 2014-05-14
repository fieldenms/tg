package ua.com.fielden.platform.example.swing.booking;

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
import org.joda.time.DateTime;
import org.joda.time.Duration;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.booking.BookingChangedEvent.BookingChangedEventType;
import ua.com.fielden.platform.swing.booking.BookingChangedEvent.BookingStretchSide;
import ua.com.fielden.platform.swing.booking.BookingChartPanel;
import ua.com.fielden.platform.swing.booking.BookingMouseEvent;
import ua.com.fielden.platform.swing.booking.BookingSeries;
import ua.com.fielden.platform.swing.booking.BookingTask;
import ua.com.fielden.platform.swing.booking.IBookingChartMouseEventListener;
import ua.com.fielden.platform.swing.booking.IBookingPainter;
import ua.com.fielden.platform.swing.booking.SingleTypeBookingEntity;
import ua.com.fielden.platform.swing.schedule.IScheduleChangeEventListener;
import ua.com.fielden.platform.swing.schedule.ScheduleChangedEvent;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.utils.Pair;

import com.jidesoft.plaf.LookAndFeelFactory;

public class BookingChartPanelExample {

    private static BookingSeries<VehicleEntity, BookingEntity> actulaSeries;
    private static BookingSeries<VehicleEntity, BookingEntity> bookingSeries;

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
                final JFrame frame = new JFrame("Booking chart demo");
                final JLabel label = new JLabel("None");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new MigLayout("fill, insets 0", "[grow, fill]", "[grow, fill][]"));
                frame.add(createBookingChartPanel(label), "wrap");
                frame.add(label);
                frame.setPreferredSize(new Dimension(640, 480));
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    private static Component createBookingChartPanel(final JLabel taskBar) {
        final BookingChartPanel<VehicleEntity, BookingEntity> bookingChart = new BookingChartPanel<>();

        bookingChart.setChartName("Booking chart demo");
        bookingChart.setRangeAxisName("Range axis");
        bookingChart.setDomainAxisName("Domain axis");

        //marker value now.
        final Date now = new Date();

        actulaSeries = createActualSeries(now);
        bookingSeries = createBookingSeries(now);
        bookingChart.addBookingSeries(actulaSeries);
        bookingChart.addBookingSeries(bookingSeries);

        bookingChart.addMarker(createNowMarker(now));

        bookingChart.setData(VehicleDataStore.allData);

        bookingChart.addBookingChartMouseEventListener(createMouseListener(taskBar, now));
        //bookingChart.addScheduleChangedEventListener(createScheduleChangedListener(taskBar));

        return bookingChart;
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

    private static IBookingChartMouseEventListener<VehicleEntity, BookingEntity> createMouseListener(final JLabel taskBar, final Date now) {
        return new IBookingChartMouseEventListener<VehicleEntity, BookingEntity>() {

            private final Duration defaultDuration = Duration.standardDays(1);

	    @Override
	    public void mouseClick(final BookingMouseEvent<VehicleEntity, BookingEntity> event) {
		if(event.getTask() == null && event.getSourceEvent().getClickCount() == 2 && event.getX().after(now)) {
		    addNewSubTask(event);
		} else if (event.getTask() != null){
		    taskBar.setText(event.getTask().getEntity() + ", " + event.getTask().getSubEntity().getBookingStart());
		}
	    }

	    private void addNewSubTask(final BookingMouseEvent<VehicleEntity, BookingEntity> event) {
		final BookingTask<VehicleEntity, BookingEntity> firstBefore = event.getSource().getFirstTaskBefore(bookingSeries, event.getX(), event.getY());
		final BookingTask<VehicleEntity, BookingEntity> firstAfter = event.getSource().getFirstTaskAfter(bookingSeries, event.getX(), event.getY());
		final Date firstBeforeDate = firstBefore != null && firstBefore.getTo() != null && firstBefore.getTo().after(now) ? firstBefore.getTo() : now;
		final Date firstAfterDate = firstAfter != null && firstAfter.getFrom() != null ? firstAfter.getFrom() : new DateTime(event.getX().getTime()).plus(defaultDuration).toDate();
		DateTime start = null;
		DateTime end = null;
		if(firstAfterDate.getTime() - firstBeforeDate.getTime() >= defaultDuration.getMillis()) {
		    start = new DateTime(event.getX().getTime() - defaultDuration.getMillis() / 2);
		    if (start.isBefore(firstBeforeDate.getTime())) {
			start = new DateTime(firstBeforeDate.getTime());
		    }
		    end = start.plus(defaultDuration);
		    if(end.isAfter(firstAfterDate.getTime())) {
			end = new DateTime(firstAfterDate.getTime());
			start = end.minus(defaultDuration);
		    }
		} else {
		    start = new DateTime(firstBeforeDate.getTime());
		    end = new DateTime(firstAfterDate.getTime());
		}
		final BookingEntity booking = new BookingEntity().//
			setVehicleEntity(event.getSource().getEntity(event.getY())).//
			setBookingStart(start.toDate()).setBookingFinish(end.toDate());
		event.getSource().addTask(bookingSeries, event.getY(), booking);
	    }

	    @Override
	    public void mouseMove(final BookingMouseEvent<VehicleEntity, BookingEntity> event) {
		// TODO Auto-generated method stub

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

    private static BookingSeries<VehicleEntity, BookingEntity> createActualSeries(final Date now) {
        final SingleTypeBookingEntity<VehicleEntity, BookingEntity> entity = new SingleTypeBookingEntity<VehicleEntity, BookingEntity>(BookingEntity.class, "actStart", "actFinish", now, now, null) {

	    @Override
	    public boolean canEditSubEntity(final VehicleEntity entity, final BookingEntity subEntity, final BookingChangedEventType type, final BookingStretchSide side) {
		return false;
	    }
	};
        final BookingSeries<VehicleEntity, BookingEntity> series = new BookingSeries<>(entity);
        series.setCutOfFactor(0.5);
        series.setName("Actual series");
        series.setPainter(new IBookingPainter<VehicleEntity, BookingEntity>() {

            final Color color = new Color(39, 148, 216);

            @Override
            public List<Pair<String, Paint>> getAvailableLegendItems() {
                return Arrays.asList(new Pair<String, Paint>("Actual duration", color));
            }

	    @Override
	    public Paint getPainterFor(final VehicleEntity entity, final BookingEntity subEntity) {
		return color;
	    }
        });
        return series;
    }

    private static BookingSeries<VehicleEntity, BookingEntity> createBookingSeries(final Date now) {
        final SingleTypeBookingEntity<VehicleEntity, BookingEntity> entity = new SingleTypeBookingEntity<VehicleEntity, BookingEntity>(BookingEntity.class, "bookingStart", "bookingFinish", null, now, null){

	    @Override
	    public boolean canEditSubEntity(final VehicleEntity entity, final BookingEntity subEntity, final BookingChangedEventType type, final BookingStretchSide side) {
		if (BookingChangedEventType.MOVE == type) {
		    if(subEntity.getBookingStart().before(now) || subEntity.getBookingFinish().before(now)) {
			return false;
		    }
		} else {
		    if (BookingStretchSide.LEFT == side && now.after(subEntity.getBookingStart())) {
			return false;
		    } else if (BookingStretchSide.RIGHT == side && now.after(subEntity.getBookingFinish())) {
			return false;
		    }
		}
		return true;
	    }

        };


        final BookingSeries<VehicleEntity, BookingEntity> series = new BookingSeries<>(entity);
        series.setCutOfFactor(0.10);
        series.setName("Booking series");
        series.setPainter(new IBookingPainter<VehicleEntity, BookingEntity>() {

            final Color color = new Color(254, 214, 88);
            //final Color invalidColor = new Color(238, 83, 35);
            //final Color requestColor = new Color(27, 114, 25);

            @Override
            public List<Pair<String, Paint>> getAvailableLegendItems() {
                return Arrays.asList(new Pair<String, Paint>("Booking duration", color));
            }

	    @Override
	    public Paint getPainterFor(final VehicleEntity entity, final BookingEntity subEntity) {
		return color;
	    }
        });
        return series;
    }
}
