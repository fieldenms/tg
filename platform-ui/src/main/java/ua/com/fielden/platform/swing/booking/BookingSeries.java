package ua.com.fielden.platform.swing.booking;

import ua.com.fielden.platform.entity.AbstractEntity;

public class BookingSeries<T extends AbstractEntity<?>, ST extends AbstractEntity<?>> {

    private final IBookingEntity<T, ST> bookingEntity;

    private double cutOfFactor = 0.0;
    private IBookingPainter<T, ST> painter;
    private String name = "";

    public BookingSeries(final IBookingEntity<T, ST> bookingEntity) {
        this.bookingEntity = bookingEntity;
    }

    public BookingSeries<T, ST> setCutOfFactor(final double cutOfFactor) {
        this.cutOfFactor = cutOfFactor;
        return this;
    }

    public BookingSeries<T, ST> setPainter(final IBookingPainter<T, ST> painter) {
        this.painter = painter;
        return this;
    }

    public BookingSeries<T, ST> setName(final String name) {
        this.name = name;
        return this;
    }

    public IBookingPainter<T, ST> getPainter() {
        return painter;
    }

    public double getCutOfFactor() {
        return cutOfFactor;
    }

    public String getName() {
        return name;
    }

    public boolean isTaskVisible(final T entity, final ST subEntity) {
        return bookingEntity.getFrom(entity, subEntity) != null && bookingEntity.getTo(entity, subEntity) != null;
    }

    public IBookingEntity<T, ST> getBookingEntity() {
        return bookingEntity;
    }
}
