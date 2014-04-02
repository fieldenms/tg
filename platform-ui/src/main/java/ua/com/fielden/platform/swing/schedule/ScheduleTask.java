package ua.com.fielden.platform.swing.schedule;

import java.util.Date;

import org.jfree.data.gantt.Task;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriod;

import ua.com.fielden.platform.entity.AbstractEntity;

public class ScheduleTask<T extends AbstractEntity<?>> extends Task {

    private static final long serialVersionUID = 8561433637611687470L;

    private final ScheduleSeries<T> scheduleSeries;
    private final T entity;

    public ScheduleTask(final String description, final ScheduleSeries<T> scheduleSeries, final T entity) {
        super(description, scheduleSeries.getScheduleEntity().getFrom(entity), scheduleSeries.getScheduleEntity().getTo(entity));
        this.scheduleSeries = scheduleSeries;
        this.entity = entity;
    }

    @Override
    public TimePeriod getDuration() {
        return new SimpleTimePeriod(getFrom(), getTo());
    }

    @Override
    public void setDuration(final TimePeriod duration) {
        if (duration.getStart().before(duration.getEnd())) {
            scheduleSeries.getScheduleEntity().setFrom(entity, duration.getStart());
            scheduleSeries.getScheduleEntity().setTo(entity, duration.getEnd());
        }
    }

    public Date getFrom() {
        return scheduleSeries.getScheduleEntity().getFrom(entity);
    }

    public Date getTo() {
        return scheduleSeries.getScheduleEntity().getTo(entity);
    }

    public void setFrom(final Date date) {
        if (getTo().after(date)) {
            scheduleSeries.getScheduleEntity().setFrom(entity, date);
        }
    }

    public void setTo(final Date date) {
        if (getFrom().before(date)) {
            scheduleSeries.getScheduleEntity().setTo(entity, date);
        }
    }

    public boolean canEdit() {
        return scheduleSeries.getScheduleEntity().canEditEntity(entity);
    }

    public T getEntity() {
        return entity;
    }

    public ScheduleSeries<T> getScheduleSeries() {
        return scheduleSeries;
    }
}
