package ua.com.fielden.platform.swing.schedule;

import ua.com.fielden.platform.entity.AbstractEntity;

public class ScheduleSeries<T extends AbstractEntity<?>> {

    private final IScheduleEntity<T> scheduleEntity;

    private double cutOfFactor = 0.0;
    private ISchedulePainter<T> painter;
    private String name = "";

    public ScheduleSeries(final IScheduleEntity<T> scheduleEntity) {
	this.scheduleEntity = scheduleEntity;
    }

    public ScheduleSeries<T> setCutOfFactor(final double cutOfFactor) {
	this.cutOfFactor = cutOfFactor;
	return this;
    }

    public ScheduleSeries<T> setPainter(final ISchedulePainter<T> painter) {
	this.painter = painter;
	return this;
    }

    public ScheduleSeries<T> setName(final String name) {
	this.name = name;
	return this;
    }

    public ISchedulePainter<T> getPainter() {
	return painter;
    }

    public double getCutOfFactor() {
	return cutOfFactor;
    }

    public String getName() {
	return name;
    }

    public boolean isTaskVisible(final T entity) {
	return scheduleEntity.getFrom(entity) != null && scheduleEntity.getTo(entity) != null;
    }

    public IScheduleEntity<T> getScheduleEntity() {
	return scheduleEntity;
    }
}
