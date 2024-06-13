package ua.com.fielden.platform.persistence.types;

import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;
import ua.com.fielden.platform.entity.query.ICompositeUserTypeInstantiate;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;

import java.util.Optional;

/**
 * Establishes mappings between:
 * <ul>
 *   <li> Java types and Hibernate types.
 *   <li> Interfaces and implementations of {@link IUserTypeInstantiate} and {@link ICompositeUserTypeInstantiate}.
 * </ul>
 *
 * A Hibernate type is an instance of either {@link UserType}, {@link Type} or {@link CompositeUserType}.
 * <p>
 * For each method that returns an optional result, the result will be empty if the supplied type has no corresponding
 * registered Hibernate type.
 */
public interface HibernateTypeMappings {

    /**
     * Given a subtype of {@link IUserTypeInstantiate} or {@link ICompositeUserTypeInstantiate}, returns an instance
     * of its implementation. Given any other type, returns the corresponding Hibernate type.
     */
    Optional<Object> getHibernateType(Class<?> type);

    static HibernateTypeMappings empty() {
        return type -> Optional.empty();
    }

}
