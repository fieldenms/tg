package ua.com.fielden.platform.eql.dbschema;

import static ua.com.fielden.platform.utils.Pair.pair;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;

import ua.com.fielden.platform.utils.Pair;

/**
 * A utility class to identify JDBC SQL types that correspond to Hibernate types.
 * 
 * @author TG Team
 *
 */
public class HibernateToJdbcSqlTypeCorrespondence {

    public static int jdbcSqlTypeFor(final Type hibernateType) {
        return hibernateType.sqlTypes(null)[0];
    }

    public static int jdbcSqlTypeFor(final UserType hibernateUserType) {
        return hibernateUserType.sqlTypes()[0];
    }
    
    public static List<Pair<String, Integer>> jdbcSqlTypeFor(final CompositeUserType compositeUserType) {
        final List<Pair<String, Integer>> result = new ArrayList<>();

        for (int index = 0; index < compositeUserType.getPropertyNames().length; index++) {
            result.add(pair(compositeUserType.getPropertyNames()[index], jdbcSqlTypeFor(compositeUserType.getPropertyTypes()[index])));
        }

        return result;
    }
}