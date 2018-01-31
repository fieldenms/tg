package ua.com.fielden.platform.persistence.types.descriptor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.CalendarTypeDescriptor;
import org.joda.time.DateTime;

import ua.com.fielden.platform.persistence.types.descriptor.DateTimeJavaTypeDescriptor.DateMutabilityPlan;

public class UtcDateTimeJavaTypeDescriptor extends AbstractTypeDescriptor<Date> {
    private static final long serialVersionUID = 1L;

    public static final UtcDateTimeJavaTypeDescriptor INSTANCE = new UtcDateTimeJavaTypeDescriptor();
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    public UtcDateTimeJavaTypeDescriptor() {
        super(Date.class, DateMutabilityPlan.INSTANCE);
    }

    @Override
    public String toString(final Date value) {
        final DateFormat utcFormat = new SimpleDateFormat(DATE_FORMAT);
        utcFormat.setTimeZone(UTC);
        return utcFormat.format(value);
    }

    @Override
    public Date fromString(String string) {
        try {
            final DateFormat utcFormat = new SimpleDateFormat(DATE_FORMAT);
            utcFormat.setTimeZone(UTC);
            return utcFormat.parse(string);
        } catch (final ParseException pe) {
            throw new HibernateException("could not parse date string" + string, pe);
        }
    }

    @Override
    public boolean areEqual(final Date one, final Date another) {
        if (one == another) {
            return true;
        }
        // DateTime is clever in handling Timestamp and Date, which are otherwise different objects
        // using DateTime for equality is just a simple trick to avoid type checking
        return !(one == null || another == null) && new DateTime(one).equals(new DateTime(another));
    }

    @Override
    public int extractHashCode(final Date value) {
        final Calendar calendar = java.util.Calendar.getInstance(UTC);
        calendar.setTime(value);
        return CalendarTypeDescriptor.INSTANCE.extractHashCode(calendar);
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public <X> X unwrap(Date value, Class<X> type, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (java.sql.Date.class.isAssignableFrom(type)) {
            final java.sql.Date rtn = java.sql.Date.class.isInstance(value)
                    ? (java.sql.Date) value
                    : new java.sql.Date(value.getTime());
            return (X) rtn;
        }
        if (java.sql.Time.class.isAssignableFrom(type)) {
            final java.sql.Time rtn = java.sql.Time.class.isInstance(value)
                    ? (java.sql.Time) value
                    : new java.sql.Time(value.getTime());
            return (X) rtn;
        }
        if (java.sql.Timestamp.class.isAssignableFrom(type)) {
            final java.sql.Timestamp rtn = java.sql.Timestamp.class.isInstance(value)
                    ? (java.sql.Timestamp) value
                    : new java.sql.Timestamp(value.getTime());
            return (X) rtn;
        }
        if (Date.class.isAssignableFrom(type)) {
            return (X) value;
        }
        if (Calendar.class.isAssignableFrom(type)) {
            final GregorianCalendar cal = new GregorianCalendar();
            cal.setTimeInMillis(value.getTime());
            return (X) cal;
        }
        if (Long.class.isAssignableFrom(type)) {
            return (X) Long.valueOf(value.getTime());
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> Date wrap(X value, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (Date.class.isInstance(value)) {
            return (Date) value;
        }

        if (Long.class.isInstance(value)) {
            return new Date((Long) value);
        }

        if (Calendar.class.isInstance(value)) {
            return new Date(((Calendar) value).getTimeInMillis());
        }

        throw unknownWrap(value.getClass());
    }
}
