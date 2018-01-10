package ua.com.fielden.platform.persistence.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.AbstractStandardBasicType;
import org.hibernate.type.IdentifierType;
import org.hibernate.type.LiteralType;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.persistence.types.descriptor.UtcDateTimeJavaTypeDescriptor;
import ua.com.fielden.platform.persistence.types.descriptor.UtcTimestampSqlTypeDescriptor;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;

/**
 * This is a user type that should be used to store properties of type {@link Date} in UTC.
 * For this to take effect, a corresponding property needs to be annotated with <code>@PersistentType(userType = IUtcDateTimeType.class)</code>
 *  
 * @author TG Team
 *
 */
public class UtcDateTimeType extends AbstractStandardBasicType<Date> implements IdentifierType<Date>, LiteralType<Date>, IUtcDateTimeType {
    private static final long serialVersionUID = 1L;

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    public UtcDateTimeType() {
        // SqlTypeDescriptor, JavaTypeDescriptor
        super(UtcTimestampSqlTypeDescriptor.INSTANCE, UtcDateTimeJavaTypeDescriptor.INSTANCE);
    }
    
    @Override
    public Object get(final ResultSet rs, final String name, final SharedSessionContractImplementor session) throws SQLException {
        final Timestamp value = rs.getTimestamp(name, Calendar.getInstance(UTC));
        return value != null ? new Date(value.getTime()) : null;
    }

    @Override
    public void set(final PreparedStatement st, final Date value, final int index, final SharedSessionContractImplementor session) throws SQLException {
        final long millis = value.getTime();
        st.setTimestamp(index, new Timestamp(millis), Calendar.getInstance(UTC));
    }

    public String getName() {
        return "date";
    }

    public Date stringToObject(final String xml) throws Exception {
        return getJavaTypeDescriptor().fromString(xml);
    }

    public String objectToSQLString(final Date value, final Dialect dialect) throws Exception {
        return '\'' + new Timestamp(value.getTime()).toString() + '\'';
    }

    @Override
    public Object instantiate(final Object argument, final EntityFactory factory) {
        return argument;
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
