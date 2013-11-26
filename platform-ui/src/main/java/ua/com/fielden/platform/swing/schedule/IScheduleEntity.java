package ua.com.fielden.platform.swing.schedule;

import java.util.Date;

public interface IScheduleEntity<T> {

    Date getFrom(T entity);

    Date getTo(T entity);

    void setFrom(T entity, Date fromDate);

    void setTo(T entity, Date toDate);

    boolean canEditEntity(T entity);
}
