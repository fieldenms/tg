package ua.com.fielden.platform.swing.booking;

import java.util.Date;

import org.jfree.data.gantt.Task;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriod;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.booking.BookingChangedEvent.BookingChangedEventType;
import ua.com.fielden.platform.swing.booking.BookingChangedEvent.BookingStretchSide;

public class BookingTask<T extends AbstractEntity<?>, ST extends AbstractEntity<?>> extends Task {

    private static final long serialVersionUID = 8561433637611687470L;

    private final BookingSeries<T, ST> bookingSeries;
    private final T entity;
    private ST subEntity;

    public BookingTask(final String description, final BookingSeries<T, ST> bookingSeries, final T entity, final ST subEntity) {
        super(description, bookingSeries.getBookingEntity().getFrom(entity, subEntity), bookingSeries.getBookingEntity().getTo(entity, subEntity));
        this.bookingSeries = bookingSeries;
        this.entity = entity;
        this.subEntity = subEntity;
    }

    @Override
    public TimePeriod getDuration() {
        return new SimpleTimePeriod(getFrom(), getTo());
    }

    @Override
    public void setDuration(final TimePeriod duration) {
	bookingSeries.getBookingEntity().setDuration(entity, subEntity, duration.getStart(), duration.getEnd());
    }

    public Date getFrom() {
        return bookingSeries.getBookingEntity().getFrom(entity, subEntity);
    }

    public Date getTo() {
        return bookingSeries.getBookingEntity().getTo(entity, subEntity);
    }

    public void setFrom(final Date date) {
	bookingSeries.getBookingEntity().setFrom(entity, subEntity, date);
    }

    public void setTo(final Date date) {
	bookingSeries.getBookingEntity().setTo(entity, subEntity, date);
    }

    public boolean canEdit(final BookingChangedEventType type, final BookingStretchSide side) {
        return bookingSeries.getBookingEntity().canEditSubEntity(entity, subEntity, type, side);
    }

    public T getEntity() {
        return entity;
    }

    public ST getSubEntity() {
	return subEntity;
    }

    public BookingSeries<T, ST> getBookingSeries() {
        return bookingSeries;
    }

    public void replaceSubEntity(final ST subEntity) {
	this.subEntity = subEntity;
    }
}
