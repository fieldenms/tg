package ua.com.fielden.platform.persistence.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.AbstractStandardBasicType;
import org.hibernate.type.IdentifierType;
import org.hibernate.type.LiteralType;
import org.hibernate.type.descriptor.sql.TimestampTypeDescriptor;

import ua.com.fielden.platform.persistence.types.descriptor.DateTimeJavaTypeDescriptor;

/**
 * Custom Hibernate type for persisting {@link Date} instances with time portion excluding nanos.
 * <p>
 * The default Hibernate type <code>date</code> is not suitable as it does not store time, the <code>timestamp</code> stores millisecond fractions (nanos), which does not play well
 * with Date time.
 * <p>
 * For example, if a property is bound to a date picker component then nanos are lost, which is recognised as change.
 * 
 * @author TG Team
 * 
 */
public class DateTimeType extends AbstractStandardBasicType<Date> implements IdentifierType<Date>, LiteralType<Date> {
    private static final long serialVersionUID = 1L;

    public DateTimeType() {
        // SqlTypeDescriptor, JavaTypeDescriptor
        super(TimestampTypeDescriptor.INSTANCE, DateTimeJavaTypeDescriptor.INSTANCE);
    }

    
    @Override
    public Object get(final ResultSet rs, final String name, final SharedSessionContractImplementor session) throws SQLException {
        final Timestamp value = rs.getTimestamp(name);
        return value != null ? new Date(value.getTime()) : null;
    }

    @Override
    public void set(final PreparedStatement st, final Date value, final int index, final SharedSessionContractImplementor session) throws SQLException {
        final long millis = value.getTime();
        st.setTimestamp(index, new Timestamp(millis));
    }

    @Override
    public String getName() {
        return "date";
    }

    
    @Override
    public Date stringToObject(final String xml) throws Exception {
        return getJavaTypeDescriptor().fromString(xml); //  DateFormat.getDateInstance().parse(xml)
    }

    @Override
    public String objectToSQLString(final Date value, final Dialect dialect) throws Exception {
        return '\'' + new Timestamp(value.getTime()).toString() + '\'';
    }


    @Override
    public void nullSafeSet(final PreparedStatement preparedStatement, final Object value, final int index, final boolean[] settable, final SharedSessionContractImplementor session) throws SQLException {
        if (null == value) {
            preparedStatement.setNull(index, getSqlTypeDescriptor().getSqlType());
        } else {
            set(preparedStatement, (Date) value, index, session);
        }
        
    }

}
