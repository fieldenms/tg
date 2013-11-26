package ua.com.fielden.platform.swing.schedule;

import java.util.EventListener;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IScheduleChartMouseEventListener<T extends AbstractEntity<?>> extends EventListener {

    void mouseClick(ScheduleMouseEvent<T> event);

    void mouseMove(ScheduleMouseEvent<T> event);

}
