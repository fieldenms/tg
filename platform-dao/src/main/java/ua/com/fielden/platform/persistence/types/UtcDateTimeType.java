package ua.com.fielden.platform.persistence.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.IdentifierType;
import org.hibernate.type.LiteralType;
import org.hibernate.type.MutableType;
import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;

/**
 * This is a user type that should be used to store properties of type {@link Date} in UTC.
 * For this to take effect, a corresponding property needs to be annotated with <code>@PersistentType(userType = IUtcDateTimeType.class)</code>
 *  
 * @author TG Team
 *
 */
public class UtcDateTimeType extends MutableType implements IdentifierType<Date>, LiteralType<Date>, IUtcDateTimeType {
    private static final long serialVersionUID = 1L;

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    @Override
    public Object get(final ResultSet rs, final String name) throws SQLException {
        final Timestamp value = rs.getTimestamp(name, Calendar.getInstance(UTC));
        return value != null ? new Date(value.getTime()) : null;
    }

    @Override
    public void set(final PreparedStatement st, final Object value, final int index) throws SQLException {
        final long millis = value instanceof Timestamp ? ((Timestamp) value).getTime() : ((java.util.Date) value).getTime();
        st.setTimestamp(index, new Timestamp(millis), Calendar.getInstance(UTC));
    }

    public Class<?> getReturnedClass() {
        return Date.class;
    }
    
    @Override
    public int sqlType() {
        return Types.TIMESTAMP;
    }

    @Override
    public boolean isEqual(final Object x, final Object y) {
        if (x == y) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }

        // DateTime is clever in handling Timestamp and Date, which are otherwise different objects
        // using DateTime for equality is just a simple trick to avoid type checking
        final DateTime xdate = new DateTime(x);
        final DateTime ydate = new DateTime(y);

        return xdate.equals(ydate);
    }

    @Override
    public int getHashCode(final Object x, final EntityMode entityMode) {
        return ((java.util.Date) x).hashCode() * 31;
    }

    public String getName() {
        return "date";
    }

    @Override
    public String toString(final Object val) {
        final DateFormat utcFormat = new SimpleDateFormat(DATE_FORMAT);
        utcFormat.setTimeZone(UTC);
        return utcFormat.format((java.util.Date) val);
    }

    @Override
    public Object deepCopyNotNull(final Object value) {
        return new Timestamp(((java.util.Date) value).getTime());
    }

    public Date stringToObject(final String xml) throws Exception {
        final DateFormat utcFormat = new SimpleDateFormat(DATE_FORMAT);
        utcFormat.setTimeZone(UTC);
        return utcFormat.parse(xml);
    }

    public String objectToSQLString(final Date value, final Dialect dialect) throws Exception {
        return '\'' + new Timestamp(value.getTime()).toString() + '\'';
    }

    @Override
    public Object fromStringValue(final String xml) throws HibernateException {
        try {
            final DateFormat utcFormat = new SimpleDateFormat(DATE_FORMAT);
            utcFormat.setTimeZone(UTC);
            return utcFormat.parse(xml);
        } catch (final ParseException pe) {
            throw new HibernateException("could not parse XML", pe);
        }
    }
    
    @Override
    public Object instantiate(final Object argument, final EntityFactory factory) {
        return argument;
    }
}
