package ua.com.fielden.platform.eql.dbschema;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;
import ua.com.fielden.platform.eql.exceptions.EqlStage3ProcessingException;
import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.utils.Pair;

import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.lang3.StringUtils.substringBefore;
import static ua.com.fielden.platform.utils.Pair.pair;
import static ua.com.fielden.platform.utils.StreamUtils.zip;

/**
 * A utility class to interact with mappings between JDBC SQL types and Hibernate types.
 * 
 * @author TG Team
 */
public final class HibernateToJdbcSqlTypeCorrespondence {

    /**
     * Returns the <i>generic</i> name of the database type corresponding to the given type code.
     * <p>
     * A generic type name is an unparameterised name. Examples of generic names and their parameterised counterparts:
     * <ul>
     *   <li> {@code numeric} -- {@code numeric(10, 2)}
     *   <li> {@code char} -- {@code char(255)}
     * </ul>
     * This method exists to overcome the limitation of Hibernate in providing access only to parameterised names.
     *
     * @param sqlType  a type code from {@link Types}
     * @param dialect  the dialect providing type mappings
     * @throws HibernateException  if there is no type name associated with the given SQL type
     */
    // funnily enough Hibernate itself performs such conversion in certain Dialect implementations
    public static String genericSqlTypeName(final int sqlType, final Dialect dialect) {
        final var map = GENERIC_TYPE_NAMES.computeIfAbsent(dialect, $ -> new ConcurrentHashMap<>());
        return map.computeIfAbsent(sqlType, $ -> substringBefore(dialect.getTypeName(sqlType), '('));
    }
    // where
    private static final Map<Dialect, Map<Integer, String>> GENERIC_TYPE_NAMES = new ConcurrentHashMap<>(1); // expect at most 1 dialect

    /**
     * Infers the name of a Hibernate type to use in a {@code CAST} SQL expression.
     *
     * @param dialect  the SQL dialect to use to resolve the SQL type
     */
    public static String sqlCastTypeName(final Object hibernateType, final Dialect dialect) {
        final int sqlType = switch (hibernateType) {
            case Type t -> jdbcSqlTypeFor(t);
            case UserType t -> jdbcSqlTypeFor(t);
            case null, default -> throw new IllegalArgumentException("Unexpected Hibernate type: %s".formatted(hibernateType));
        };

        return genericSqlTypeName(sqlType, dialect);
    }

    public static int jdbcSqlTypeFor(final Type hibernateType) {
        // NOTE Hibernate doesn't complain about null but that is not documented anywhere
        return hibernateType.sqlTypes(null)[0];
    }

    /**
     * Returns the {@linkplain Types SQL type code} of <b>the first column</b> mapped by given Hibernate user type.
     */
    public static int jdbcSqlTypeFor(final UserType hibernateUserType) {
        return hibernateUserType.sqlTypes()[0];
    }

    public static List<Pair<String, Integer>> jdbcSqlTypeFor(final CompositeUserType compositeUserType) {
        return zip(Arrays.stream(compositeUserType.getPropertyNames()), Arrays.stream(compositeUserType.getPropertyTypes()),
                (name, type) -> pair(name, jdbcSqlTypeFor(type)))
                .toList();
    }

    private HibernateToJdbcSqlTypeCorrespondence() {}

}
