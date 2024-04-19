package ua.com.fielden.platform.persistence.types;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.*;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.BasicBinder;
import org.hibernate.type.descriptor.sql.BasicExtractor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;
import ua.com.fielden.platform.persistence.types.descriptor.DateTimeJavaTypeDescriptor;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Date;

import static java.sql.Types.TIMESTAMP;

/**
 * Custom Hibernate type for persisting {@link Date} instances with time portion excluding nanos.
 * <p>
 * Default Hibernate type {@link DateType} is not suitable as it does not store time,
 * {@link TimestampType} stores millisecond fractions (nanos), which does not play well with {@link Date} that doesn't
 * store nanos. For example, if a property is bound to a date picker component, nanos are lost, which is recognised as change.
 * 
 * @author TG Team
 */
public class DateTimeType extends AbstractSingleColumnStandardBasicType<Date> implements IdentifierType<Date>, LiteralType<Date> {
    private static final long serialVersionUID = 1L;
    public static final DateTimeType INSTANCE = new DateTimeType();

    public DateTimeType() {
        // SqlTypeDescriptor, JavaTypeDescriptor
        super(DateTimeSqlTypeDescriptor.INSTANCE, DateTimeJavaTypeDescriptor.INSTANCE);
    }

    @Override
    public String getName() {
        /* This name acts as a type mapping lookup key. It will be used during registration of the mapping.
         * Hibernate documentation does not specify what this value should be to override the standard mapping.
         * java.util.Date works.
         * See Hibernate 5.4 User Guide, 2.3.1. Hibernate-provided BasicTypes, Table 1. Standard BasicTypes */
        return java.util.Date.class.getCanonicalName();
    }

    @Override
    public Date stringToObject(final String xml) throws Exception {
        return getJavaTypeDescriptor().fromString(xml); //  DateFormat.getDateInstance().parse(xml)
    }

    @Override
    public String objectToSQLString(final Date value, final Dialect dialect) throws Exception {
        return '\'' + new Timestamp(value.getTime()).toString() + '\'';
    }

    /**
     * SQL side of the {@link DateTimeType} type binding.
     *
     * @see DateTimeType
     */
    private static final class DateTimeSqlTypeDescriptor implements SqlTypeDescriptor {

        static final DateTimeSqlTypeDescriptor INSTANCE = new DateTimeSqlTypeDescriptor();

        private DateTimeSqlTypeDescriptor() {}

        @Override
        public int getSqlType() {
            return TIMESTAMP;
        }

        @Override
        public boolean canBeRemapped() {
            return true;
        }

        @Override
        public <X> ValueBinder<X> getBinder(final JavaTypeDescriptor<X> javaTypeDescriptor) {
            // We need LocalDateTime specifically for PostgreSQL, as java.util.Date will be typed as UNSPECIFIED.
            // setObject is required to accept LocalDateTime as TIMESTAMP (even PostgreSQL driver does so).
            return new BasicBinder<>(javaTypeDescriptor, this) {
                @Override
                protected void doBind(final PreparedStatement st, final X value, final int index, final WrapperOptions options) throws
                        SQLException {
                    final LocalDateTime date = javaTypeDescriptor.unwrap(value, LocalDateTime.class, options);
                    st.setObject(index, date, TIMESTAMP);
                }

                @Override
                protected void doBind(final CallableStatement st, final X value, final String name, final WrapperOptions options) throws SQLException {
                    final LocalDateTime date = javaTypeDescriptor.unwrap(value, LocalDateTime.class, options);
                    st.setObject(name, date, TIMESTAMP);
                }
            };
        }

        @Override
        public <X> ValueExtractor<X> getExtractor(final JavaTypeDescriptor<X> javaTypeDescriptor) {
            return new BasicExtractor<>(javaTypeDescriptor, this) {
                @Override
                protected X doExtract(final ResultSet rs, final String name, final WrapperOptions options) throws SQLException {
                    return javaTypeDescriptor.wrap(rs.getTimestamp(name), options);
                }

                @Override
                protected X doExtract(final CallableStatement statement, final int index, final WrapperOptions options) throws SQLException {
                    return javaTypeDescriptor.wrap(statement.getTimestamp(index), options);
                }

                @Override
                protected X doExtract(final CallableStatement statement, final String name, final WrapperOptions options) throws SQLException {
                    return javaTypeDescriptor.wrap(statement.getTimestamp(name), options);
                }
            };
        }

    }

}
