package ua.com.fielden.platform.swing.booking;

import java.util.Date;
import java.util.EventObject;

import ua.com.fielden.platform.entity.AbstractEntity;

public class BookingChangedEvent<T extends AbstractEntity<?>, ST extends AbstractEntity<?>> extends EventObject {

    private static final long serialVersionUID = -5791392694829183522L;

    private final BookingChangedEventType eventType;
    private final BookingStretchSide stretchSide;

    private final T entity;
    private final ST subEntity;
    private final BookingSeries<T, ST> series;
    private final Date from;
    private final Date to;

    public BookingChangedEvent(final BookingChartPanel<T, ST> source, final T entity, final ST subEntity, final BookingSeries<T, ST> series, final Date from, final Date to) {
        this(source, BookingChangedEventType.MOVE, null, entity, subEntity, series, from, to);
    }

    public BookingChangedEvent(final BookingChartPanel<T, ST> source, final BookingStretchSide side, final T entity, final ST subEntity, final BookingSeries<T, ST> series, final Date newValue) {
        this(source, BookingChangedEventType.STRETCH, side, entity, subEntity, series, newValue, null);
    }

    private BookingChangedEvent(final BookingChartPanel<T, ST> source, //
            final BookingChangedEventType eventType, //
            final BookingStretchSide stretchSide, //
            final T entity, //
            final ST subEntity,//
            final BookingSeries<T, ST> series, //
            final Date from, //
            final Date to) {
        super(source);
        this.eventType = eventType;
        this.stretchSide = stretchSide;
        this.entity = entity;
        this.subEntity = subEntity;
        this.series = series;
        this.from = from;
        this.to = to;
    }

    @SuppressWarnings("unchecked")
    @Override
    public BookingChartPanel<T, ST> getSource() {
        return (BookingChartPanel<T, ST>) super.getSource();
    }

    public BookingChangedEventType getEventType() {
        return eventType;
    }

    public BookingStretchSide getStretchSide() {
        return stretchSide;
    }

    public T getEntity() {
        return entity;
    }

    public ST getSubEntity() {
	return subEntity;
    }

    public BookingSeries<T, ST> getSeries() {
        return series;
    }

    public Date getFrom() {
        return from;
    }

    public Date getTo() {
        return to;
    }

    public Date getNewValue() {
        if (getStretchSide() != null) {
            return from;
        }
        return null;
    }

    public static enum BookingChangedEventType {
        MOVE, STRETCH;
    }

    public static enum BookingStretchSide {
        LEFT, RIGHT;
    }
}
