package ua.com.fielden.platform.swing.schedule;

import java.util.EventListener;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IScheduleChangeEventListener<T extends AbstractEntity<?>> extends EventListener {

    void scheduleChanged(ScheduleChangedEvent<T> event);

}
