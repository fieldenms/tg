package ua.com.fielden.platform.swing.booking;

import java.util.EventListener;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IBookingChartMouseEventListener<T extends AbstractEntity<?>, ST extends AbstractEntity<?>> extends EventListener {

    void mouseClick(BookingMouseEvent<T, ST> event);

    void mouseMove(BookingMouseEvent<T, ST> event);

}
