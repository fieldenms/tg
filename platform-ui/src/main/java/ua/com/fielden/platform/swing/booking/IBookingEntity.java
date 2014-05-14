package ua.com.fielden.platform.swing.booking;

import java.util.Date;

import ua.com.fielden.platform.swing.booking.BookingChangedEvent.BookingChangedEventType;
import ua.com.fielden.platform.swing.booking.BookingChangedEvent.BookingStretchSide;

public interface IBookingEntity<T, ST> {

    Date getFrom(T entity, ST subEntity);

    Date getTo(T entity, ST subEntity);

    void setFrom(T entity, ST subEntity, Date fromDate);

    void setTo(T entity, ST subEntity, Date toDate);

    boolean canEditSubEntity(T entity, ST subEntity, BookingChangedEventType type, BookingStretchSide side);
}
