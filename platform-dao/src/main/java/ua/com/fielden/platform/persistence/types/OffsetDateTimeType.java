package ua.com.fielden.platform.persistence.types;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.*;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.CalendarTypeDescriptor;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.MutableMutabilityPlan;
import org.hibernate.type.descriptor.sql.BasicBinder;
import org.hibernate.type.descriptor.sql.BasicExtractor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;
import org.joda.time.DateTime;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Supplier;

import static java.sql.Types.TIMESTAMP;

/**
 * Custom Hibernate type mapping for {@link Date} <i>relative</i> to a specific time zone. It is relative in the sense that
 * {@link Date} values, which themselves carry no time zone information, are interpreted using a specific time zone.
 * For example, before a date is persisted into a DB it is usually formatted as a string (e.g., {@code 2020-03-03 12:00:00}),
 * which requires a time zone to be known. The inverse relationship applies when a date is retrieved from a DB: a formatted
 * string needs to be parsed relative to a specific time zone.
 * <p>
 * With this type mapping {@link Date} values are persisted with time portion excluding nanos.
 * Default Hibernate type {@link DateType} is not suitable as it does not store time,
 * {@link TimestampType} stores millisecond fractions (nanos), which does not play well with {@link Date} that doesn't
 * store nanos. For example, if a property is bound to a date picker component, nanos are lost, which is recognised as change.
 * 
 * @author TG Team
 */
class OffsetDateTimeType extends AbstractSingleColumnStandardBasicType<Date> implements IdentifierType<Date>, LiteralType<Date> {
    private static final long serialVersionUID = 1L;

    public OffsetDateTimeType(final Supplier<? extends TimeZone> timeZone) {
        super(
                new AbstractDateTimeSqlTypeDescriptor() {
                    @Override TimeZone timeZone() { return timeZone.get(); }
                },
                new AbstractDateTimeJavaTypeDescriptor() {
                    @Override TimeZone timeZone() { return timeZone.get(); }
                });
    }


    /**
     * This name acts as a type mapping lookup key. It will be used during registration of the mapping.
     * Hibernate documentation does not specify what this value should be to override the standard mapping.
     * Name {@code "java.util.Date"} works (before it was simply {@code "date"}).
     * See <a href='https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#basic-provided'>Hibernate 5.4 User Guide, 2.3.1. Hibernate-provided BasicTypes, Table 1. Standard BasicTypes</a> for more details.
     */
    @Override
    public String getName() {
        return Date.class.getCanonicalName();
    }

    @Override
    public Date stringToObject(final String xml) {
        return getJavaTypeDescriptor().fromString(xml);
    }

    @Override
    public String objectToSQLString(final Date value, final Dialect dialect) {
        return '\'' + new Timestamp(value.getTime()).toString() + '\'';
    }

    /**
     * SQL side of the {@link OffsetDateTimeType} type binding.
     *
     * @see OffsetDateTimeType
     */
    static abstract class AbstractDateTimeSqlTypeDescriptor implements SqlTypeDescriptor {

        AbstractDateTimeSqlTypeDescriptor() {
        }

        abstract TimeZone timeZone();

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
            // Unlike setTimestamp which can accept a Calendar, setObject does not convey any timezone context; therefore,
            // javaTypeDescriptor must perform timezone offsetting in unwrap(); hence, LocalDateTime is a MUST.
            return new BasicBinder<>(javaTypeDescriptor, this) {
                @Override
                protected void doBind(final PreparedStatement st, final X value, final int index, final WrapperOptions options) throws SQLException {
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
                    return javaTypeDescriptor.wrap(rs.getTimestamp(name, Calendar.getInstance(timeZone())), options);
                }

                @Override
                protected X doExtract(final CallableStatement statement, final int index, final WrapperOptions options) throws SQLException {
                    return javaTypeDescriptor.wrap(statement.getTimestamp(index, Calendar.getInstance(timeZone())), options);
                }

                @Override
                protected X doExtract(final CallableStatement statement, final String name, final WrapperOptions options) throws SQLException {
                    return javaTypeDescriptor.wrap(statement.getTimestamp(name, Calendar.getInstance(timeZone())), options);
                }
            };
        }

    }

    static abstract class AbstractDateTimeJavaTypeDescriptor extends AbstractTypeDescriptor<Date> {
        private static final long serialVersionUID = 1L;
        static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

        private static final class DateMutabilityPlan extends MutableMutabilityPlan<Date> {
            private static final long serialVersionUID = 1L;
            public static final DateMutabilityPlan INSTANCE = new DateMutabilityPlan();

            private DateMutabilityPlan() {}

            @Override
            public Date deepCopyNotNull(final Date value) {
                return new Timestamp(value.getTime());
            }
        }

        public AbstractDateTimeJavaTypeDescriptor() {
            super(Date.class, DateMutabilityPlan.INSTANCE);
        }

        abstract TimeZone timeZone();

        @Override
        public String toString(final Date value) {
            final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            dateFormat.setTimeZone(timeZone());
            return dateFormat.format(value);
        }

        @Override
        public Date fromString(final String string) {
            try {
                final DateFormat utcFormat = new SimpleDateFormat(DATE_FORMAT);
                utcFormat.setTimeZone(timeZone());
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
            final Calendar calendar = Calendar.getInstance(timeZone());
            calendar.setTime(value);
            return CalendarTypeDescriptor.INSTANCE.extractHashCode(calendar);
        }

        /**
         * <b>NOTE</b>: The only accepted {@code type} is {@link LocalDateTime}.
         */
        @Override
        public <X> X unwrap(final Date value, final Class<X> type, final WrapperOptions options) {
            if (value == null) {
                return null;
            }
            // force callers to use LocalDateTime exclusively to avoid subtle mistakes regarding timezones
            if (LocalDateTime.class == type) {
                return (X) value.toInstant().atZone(timeZone().toZoneId()).toLocalDateTime();
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
                case LocalDateTime ldt -> Date.from(ldt.atZone(timeZone().toZoneId()).toInstant());
                case Long n -> new Date(n);
                case Calendar cal -> new Date(cal.getTimeInMillis());
                default -> throw unknownUnwrap(value.getClass());
            };
        }

    }

}
