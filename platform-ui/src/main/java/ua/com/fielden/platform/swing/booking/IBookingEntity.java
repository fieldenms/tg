package ua.com.fielden.platform.swing.booking;

import java.util.Date;

import ua.com.fielden.platform.swing.booking.BookingChangedEvent.BookingChangedEventType;
import ua.com.fielden.platform.swing.booking.BookingChangedEvent.BookingStretchSide;
import ua.com.fielden.platform.utils.Pair;

public interface IBookingEntity<T, ST> {

    Date getFrom(T entity, ST subEntity);

    Date getTo(T entity, ST subEntity);

    void setFrom(T entity, ST subEntity, Date fromDate);

    void setTo(T entity, ST subEntity, Date toDate);

    Pair<Date, Date> getDuration(T entity, ST subEntity);

    void setDuration(T entity, ST subEntity, Date from, Date to);

    boolean canEditSubEntity(T entity, ST subEntity, BookingChangedEventType type, BookingStretchSide side);
}
