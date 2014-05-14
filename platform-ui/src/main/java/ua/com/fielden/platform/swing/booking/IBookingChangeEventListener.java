package ua.com.fielden.platform.swing.booking;

import java.util.EventListener;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IBookingChangeEventListener<T extends AbstractEntity<?>, ST extends AbstractEntity<?>> extends EventListener {

    void bookingChanged(BookingChangedEvent<T, ST> event);

}
