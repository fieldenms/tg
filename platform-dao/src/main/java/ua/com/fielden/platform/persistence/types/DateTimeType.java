package ua.com.fielden.platform.persistence.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.IdentifierType;
import org.hibernate.type.LiteralType;
import org.hibernate.type.MutableType;
import org.joda.time.DateTime;

/**
 * Custom Hibernate type for persisting {@link Date} instances with time portion excluding nanos.
 * <p>
 * The default Hibernate type <code>date</code> is not suitable as it does not store time, the <code>timestamp</code> stores millisecond fractions (nanos), which does not play well
 * with Date time.
 * <p>
 * For example, if a property is bound to a date picker component then nanos are lost, which is recognised as change.
 * 
 * @author 01es
 * 
 */
public class DateTimeType extends MutableType implements IdentifierType, LiteralType {
    private static final long serialVersionUID = 1L;

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public Object get(final ResultSet rs, final String name) throws SQLException {
        final Timestamp value = rs.getTimestamp(name);
        return value != null ? new Date(value.getTime()) : null;
    }

    public Class<?> getReturnedClass() {
        return Date.class;
    }

    @Override
    public void set(final PreparedStatement st, final Object value, final int index) throws SQLException {
        final DateTime xdate = value instanceof Timestamp ? new DateTime(((Timestamp) value).getTime()) : new DateTime(((java.util.Date) value).getTime());
        st.setTimestamp(index, new Timestamp(xdate.getMillis()));
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

        final DateTime xdate = new DateTime(x);
        final DateTime ydate = new DateTime(y);

        return xdate.equals(ydate);
    }

    @Override
    public int getHashCode(final Object x, final EntityMode entityMode) {
        return new Long(((java.util.Date) x).getTime()).hashCode();
    }

    public String getName() {
        return "date";
    }

    @Override
    public String toString(final Object val) {
        return new SimpleDateFormat(DATE_FORMAT).format((java.util.Date) val);
    }

    @Override
    public Object deepCopyNotNull(final Object value) {
        return new Timestamp(((java.util.Date) value).getTime());
    }

    public Object stringToObject(final String xml) throws Exception {
        return DateFormat.getDateInstance().parse(xml);
    }

    public String objectToSQLString(final Object value, final Dialect dialect) throws Exception {
        return '\'' + new Timestamp(((java.util.Date) value).getTime()).toString() + '\'';
    }

    @Override
    public Object fromStringValue(final String xml) throws HibernateException {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(xml);
        } catch (final ParseException pe) {
            throw new HibernateException("could not parse XML", pe);
        }
    }

}
