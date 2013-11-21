package ua.com.fielden.platform.swing.schedule;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

public class ScheduleSeries<T extends AbstractEntity<?>, V extends Comparable<?>> {

    private final Class<T> entityType;
    private final Class<V> valueType;
    private final String fromValue;
    private final String toValue;
    private final V defaultValue;

    private double cutOfFactor;
    private ISchedulePainter<T> painter;

    @SuppressWarnings("unchecked")
    public ScheduleSeries(final Class<T> entityType, final String fromValue, final String toValue, final V defaultValue) {
	this.entityType = entityType;
	this.fromValue = fromValue;
	this.toValue = toValue;
	this.defaultValue = defaultValue;

	final Class<?> fromValueType = PropertyTypeDeterminator.determinePropertyType(entityType, fromValue);
	final Class<?> toValueType = PropertyTypeDeterminator.determinePropertyType(entityType, toValue);

	if (!toValueType.equals(fromValueType)) {
	    throw new IllegalArgumentException("The type of from value property is not the same as the type of to property!");
	}
	if (defaultValue != null && !defaultValue.getClass().equals(fromValueType)) {
	    throw new IllegalArgumentException("The type of default value must be same as the type of from and to properties!");
	}
	this.valueType = (Class<V>)fromValueType;
    }

    public ScheduleSeries<T, V> setCutOfFactor(final double cutOfFactor) {
	this.cutOfFactor = cutOfFactor;
	return this;
    }

    public ScheduleSeries<T, V> setPainter(final ISchedulePainter<T> painter) {
	this.painter = painter;
	return this;
    }

    public Class<V> getValueType() {
	return valueType;
    }

    public double getCutOfFactor() {
	return cutOfFactor;
    }
}
