package ua.com.fielden.platform.swing.schedule;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

public abstract class SingleTypeScheduleEntity<T extends AbstractEntity<?>> implements IScheduleEntity<T> {

    private final String from;
    private final String to;
    private final Date defaultValue;

    public SingleTypeScheduleEntity(final Class<T> entityType, final String from, final String to, final Date defaultValue) {
	this.from = from;
	this.to = to;
	this.defaultValue = defaultValue;

	final Class<?> fromValueType = PropertyTypeDeterminator.determinePropertyType(entityType, from);
	final Class<?> toValueType = PropertyTypeDeterminator.determinePropertyType(entityType, to);

	if (!Date.class.isAssignableFrom(fromValueType)) {
	    throw new IllegalArgumentException("The type of from property is not Date!");
	}
	if (!Date.class.isAssignableFrom(toValueType)) {
	    throw new IllegalArgumentException("The type of to property is not Date!");
	}
	if (defaultValue != null && !Date.class.isAssignableFrom(defaultValue.getClass())) {
	    throw new IllegalArgumentException("The type of default value must be Date!");
	}
    }

    @Override
    public Date getFrom(final T entity) {
	return (Date)entity.get(from);
    }

    @Override
    public Date getTo(final T entity) {
	final Object value = entity.get(to);
	return value == null ? defaultValue : (Date) value;
    }

    @Override
    public void setFrom(final T entity, final Date fromDate) {
	entity.set(from, fromDate);
    }

    @Override
    public void setTo(final T entity, final Date toDate) {
	entity.set(to, toDate);
    }

}
