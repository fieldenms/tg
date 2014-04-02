package ua.com.fielden.platform.swing.schedule;

import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.EventObject;

import ua.com.fielden.platform.entity.AbstractEntity;

public class ScheduleMouseEvent<T extends AbstractEntity<?>> extends EventObject {

    private static final long serialVersionUID = -8834579078406112024L;

    private final MouseEvent sourceEvent;
    private final T entity;
    private final ScheduleSeries<T> series;
    private final Date x;
    private final String y;

    public ScheduleMouseEvent(final ScheduleChartPanel<T> source, final MouseEvent sourceEvent, final T entity, final ScheduleSeries<T> series, final Date x, final String y) {
        super(source);
        this.sourceEvent = sourceEvent;
        this.entity = entity;
        this.series = series;
        this.x = x;
        this.y = y;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ScheduleChartPanel<T> getSource() {
        return (ScheduleChartPanel<T>) super.getSource();
    }

    public T getEntity() {
        return entity;
    }

    public ScheduleSeries<T> getSeries() {
        return series;
    }

    public Date getX() {
        return x;
    }

    public String getY() {
        return y;
    }

    public MouseEvent getSourceEvent() {
        return sourceEvent;
    }
}
