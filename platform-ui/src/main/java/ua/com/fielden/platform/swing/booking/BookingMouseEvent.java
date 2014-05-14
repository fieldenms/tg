package ua.com.fielden.platform.swing.booking;

import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.EventObject;

import ua.com.fielden.platform.entity.AbstractEntity;

public class BookingMouseEvent<T extends AbstractEntity<?>, ST extends AbstractEntity<?>> extends EventObject {

    private static final long serialVersionUID = -8834579078406112024L;

    private final MouseEvent sourceEvent;
    private final BookingTask<T, ST> task;
    private final Date x;
    private final int y;

    public BookingMouseEvent(final BookingChartPanel<T, ST> source, final MouseEvent sourceEvent, final BookingTask<T, ST> task, final Date x, final int y) {
        super(source);
        this.sourceEvent = sourceEvent;
        this.task = task;
        this.x = x;
        this.y = y;
    }

    @SuppressWarnings("unchecked")
    @Override
    public BookingChartPanel<T, ST> getSource() {
        return (BookingChartPanel<T, ST>) super.getSource();
    }

    public BookingTask<T, ST> getTask() {
	return task;
    }

    public Date getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public MouseEvent getSourceEvent() {
        return sourceEvent;
    }

}
