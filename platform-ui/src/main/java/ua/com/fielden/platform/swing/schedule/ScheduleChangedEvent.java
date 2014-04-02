package ua.com.fielden.platform.swing.schedule;

import java.util.Date;
import java.util.EventObject;

import ua.com.fielden.platform.entity.AbstractEntity;

public class ScheduleChangedEvent<T extends AbstractEntity<?>> extends EventObject {

    private static final long serialVersionUID = -5791392694829183522L;

    private final ScheduleChangedEventType eventType;
    private final ScheduleStretchSide stretchSide;

    private final T entity;
    private final ScheduleSeries<T> series;
    private final Date from;
    private final Date to;

    public ScheduleChangedEvent(final ScheduleChartPanel<T> source, final T entity, final ScheduleSeries<T> series, final Date from, final Date to) {
        this(source, ScheduleChangedEventType.MOVE, null, entity, series, from, to);
    }

    public ScheduleChangedEvent(final ScheduleChartPanel<T> source, final ScheduleStretchSide side, final T entity, final ScheduleSeries<T> series, final Date newValue) {
        this(source, ScheduleChangedEventType.STRETCH, side, entity, series, newValue, null);
    }

    private ScheduleChangedEvent(final ScheduleChartPanel<T> source, //
            final ScheduleChangedEventType eventType, //
            final ScheduleStretchSide stretchSide, //
            final T entity, //
            final ScheduleSeries<T> series, //
            final Date from, //
            final Date to) {
        super(source);
        this.eventType = eventType;
        this.stretchSide = stretchSide;
        this.entity = entity;
        this.series = series;
        this.from = from;
        this.to = to;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ScheduleChartPanel<T> getSource() {
        return (ScheduleChartPanel<T>) super.getSource();
    }

    public ScheduleChangedEventType getEventType() {
        return eventType;
    }

    public ScheduleStretchSide getStretchSide() {
        return stretchSide;
    }

    public T getEntity() {
        return entity;
    }

    public ScheduleSeries<T> getSeries() {
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

    public static enum ScheduleChangedEventType {
        MOVE, STRETCH;
    }

    public static enum ScheduleStretchSide {
        LEFT, RIGHT;
    }
}
