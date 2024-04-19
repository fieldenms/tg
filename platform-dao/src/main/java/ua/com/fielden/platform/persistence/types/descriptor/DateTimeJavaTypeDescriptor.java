package ua.com.fielden.platform.persistence.types.descriptor;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.CalendarTypeDescriptor;
import org.hibernate.type.descriptor.java.MutableMutabilityPlan;

public class DateTimeJavaTypeDescriptor extends AbstractTypeDescriptor<Date> {
    private static final long serialVersionUID = 1L;
    
    public static final DateTimeJavaTypeDescriptor INSTANCE = new DateTimeJavaTypeDescriptor();
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static class DateMutabilityPlan extends MutableMutabilityPlan<Date> {
        private static final long serialVersionUID = 1L;
        public static final DateMutabilityPlan INSTANCE = new DateMutabilityPlan();
		
		@Override
		public Date deepCopyNotNull(final Date value) {
			return new Timestamp( value.getTime() );
		}
	}

	public DateTimeJavaTypeDescriptor() {
		super( Date.class, DateMutabilityPlan.INSTANCE );
	}

	@Override
	public String toString(final Date value) {
		return new SimpleDateFormat(DATE_FORMAT).format( value );
	}

	@Override
	public Date fromString(final String string) {
		try {
			return new SimpleDateFormat(DATE_FORMAT).parse( string );
		}
		catch (final ParseException pe) {
			throw new HibernateException( "could not parse date string" + string, pe );
		}
	}

	@Override
	public boolean areEqual(final Date one, final Date another) {
		if ( one == another) {
			return true;
		}
		return !( one == null || another == null ) && one.getTime() == another.getTime();

	}

	@Override
	public int extractHashCode(final Date value) {
		Calendar calendar = java.util.Calendar.getInstance();
		calendar.setTime( value );
		return CalendarTypeDescriptor.INSTANCE.extractHashCode( calendar );
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <X> X unwrap(final Date value, final Class<X> type, final WrapperOptions options) {
		if (value == null) {
			return null;
		}
		if (Date.class == type) {
			return (X) value;
		}
		if (LocalDateTime.class == type) {
			return (X) value.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		}
		if (java.sql.Date.class.isAssignableFrom(type)) {
			return (X) (value instanceof java.sql.Date date ? date : new java.sql.Date(value.getTime()));
		}
		if (java.sql.Time.class.isAssignableFrom(type)) {
			return (X) (value instanceof java.sql.Time time ? time : new java.sql.Time(value.getTime()));
		}
		if (java.sql.Timestamp.class.isAssignableFrom(type)) {
			return (X) (value instanceof java.sql.Timestamp ts ? ts : new java.sql.Timestamp(value.getTime()));
		}
		if (Calendar.class.isAssignableFrom(type)) {
			final GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis(value.getTime());
			return (X) cal;
		}
		if (Long.class.isAssignableFrom(type) ) {
			return (X) Long.valueOf(value.getTime());
		}
		throw unknownUnwrap(type);
	}

	@Override
	public <X> Date wrap(final X value, final WrapperOptions options) {
		return switch (value) {
			case null -> null;
			/* we don't want Timestamp as it would make it cumbersome to check for java.util.Date values elsewhere;
			 * it would require doing Date.class.isAssignableFrom(value) instead of Date.class == value.getClass(),
			 * and Map<Class, ...> wouldn't work at all */
			case Timestamp ts -> new Date(ts.getTime());
			case Date date -> date;
			case LocalDateTime ldt -> Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
			case Long n -> new Date(n);
			case Calendar cal -> new Date(cal.getTimeInMillis());
			default -> throw unknownUnwrap(value.getClass());
		};
	}
}
