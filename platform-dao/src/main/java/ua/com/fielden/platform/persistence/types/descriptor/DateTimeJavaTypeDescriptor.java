package ua.com.fielden.platform.persistence.types.descriptor;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
		if ( value == null ) {
			return null;
		}
		if ( java.sql.Date.class.isAssignableFrom( type ) ) {
			final java.sql.Date rtn = java.sql.Date.class.isInstance( value )
					? ( java.sql.Date ) value
					: new java.sql.Date( value.getTime() );
			return (X) rtn;
		}
		if ( java.sql.Time.class.isAssignableFrom( type ) ) {
			final java.sql.Time rtn = java.sql.Time.class.isInstance( value )
					? ( java.sql.Time ) value
					: new java.sql.Time( value.getTime() );
			return (X) rtn;
		}
		if ( java.sql.Timestamp.class.isAssignableFrom( type ) ) {
			final java.sql.Timestamp rtn = java.sql.Timestamp.class.isInstance( value )
					? ( java.sql.Timestamp ) value
					: new java.sql.Timestamp( value.getTime() );
			return (X) rtn;
		}
		if ( Date.class.isAssignableFrom( type ) ) {
			return (X) value;
		}
		if ( Calendar.class.isAssignableFrom( type ) ) {
			final GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis( value.getTime() );
			return (X) cal;
		}
		if ( Long.class.isAssignableFrom( type ) ) {
			return (X) Long.valueOf( value.getTime() );
		}
		throw unknownUnwrap( type );
	}
	
	@Override
	public <X> Date wrap(final X value, final WrapperOptions options) {
		if ( value == null ) {
			return null;
		}
		if ( Date.class.isInstance( value ) ) {
			return (Date) value;
		}

		if ( Long.class.isInstance( value ) ) {
			return new Date( (Long) value );
		}

		if ( Calendar.class.isInstance( value ) ) {
			return new Date( ( (Calendar) value ).getTimeInMillis() );
		}

		throw unknownWrap( value.getClass() );
	}
}
