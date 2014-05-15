package ua.com.fielden.platform.swing.booking;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.Pair;

public abstract class SingleTypeBookingEntity<T extends AbstractEntity<?>, ST extends AbstractEntity<?>> implements IBookingEntity<T, ST> {

    private final String from;
    private final String to;
    private final Date defaultValue;
    private final Date rightLimit;
    private final Date leftLimit;

    public SingleTypeBookingEntity(final Class<ST> entityType, final String from, final String to, final Date defaultValue, final Date leftLimit, final Date rightLimit) {
        this.from = from;
        this.to = to;
        this.defaultValue = defaultValue;
        this.leftLimit = leftLimit;
        this.rightLimit = rightLimit;

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
        if(rightLimit != null && leftLimit != null && leftLimit.after(rightLimit)) {
            throw new IllegalArgumentException("The left limit must be before the right limit!");
        }
    }

    @Override
    public Date getFrom(final T entity, final ST subEntity) {
        return (Date) subEntity.get(from);
    }

    @Override
    public Date getTo(final T entity, final ST subEntity) {
	final Object value = subEntity.get(to);
        return value == null && (getFrom(entity, subEntity) != null && getFrom(entity, subEntity).before(defaultValue)) ? defaultValue : (Date) value;
    }

    @Override
    public void setFrom(final T entity, final ST subEntity, final Date fromDate) {
	if((leftLimit == null || leftLimit.before(fromDate)) && (rightLimit == null || rightLimit.after(fromDate))) {
	    if(fromDate.before(getTo(entity, subEntity))){
		subEntity.set(from, fromDate);
	    }
	} else if (leftLimit != null && leftLimit.after(fromDate)) {
	    subEntity.set(from, leftLimit);
	} else if (rightLimit != null && rightLimit.before(fromDate)) {
	    subEntity.set(from, rightLimit);
	}
    }

    @Override
    public void setTo(final T entity, final ST subEntity, final Date toDate) {
        if((leftLimit == null || leftLimit.before(toDate)) && (rightLimit == null || rightLimit.after(toDate))) {
            if (toDate.after(getFrom(entity, subEntity))) {
        	subEntity.set(to, toDate);
            }
        } else if (leftLimit != null && leftLimit.after(toDate)) {
	    subEntity.set(to, leftLimit);
	} else if (rightLimit != null && rightLimit.before(toDate)) {
	    subEntity.set(to, rightLimit);
	}
    }

    @Override
    public Pair<Date, Date> getDuration(final T entity, final ST subEntity) {
        return new Pair<>(getFrom(entity, subEntity), getTo(entity, subEntity));
    }

    @Override
    public void setDuration(final T entity, final ST subEntity, final Date fromDate, final Date toDate) {
	if(fromDate.after(toDate)) {
	    throw new IllegalArgumentException("The start must be >= end!");
	}
	if ((leftLimit == null || leftLimit.before(fromDate)) && (rightLimit ==  null || rightLimit.after(toDate))) {
	    subEntity.set(from, fromDate);
	    subEntity.set(to, toDate);
	} else if (leftLimit != null && leftLimit.after(fromDate)) {
	    final long delta = leftLimit.getTime() - fromDate.getTime();
	    subEntity.set(from, leftLimit);
	    subEntity.set(to, new Date(toDate.getTime() + delta));
	} else if(rightLimit != null && rightLimit.before(toDate)) {
	    final long delta = rightLimit.getTime() - toDate.getTime();
	    subEntity.set(from, new Date(fromDate.getTime() + delta));
	    subEntity.set(to, rightLimit);
	}
    }
}
