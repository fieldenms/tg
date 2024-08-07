package ua.com.fielden.platform.eql.dbschema;

import org.hibernate.type.Type;
import org.hibernate.type.TypeResolver;
import org.hibernate.type.spi.TypeConfiguration;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.eql.dbschema.exceptions.DbSchemaException;
import ua.com.fielden.platform.persistence.types.HibernateTypeMappings;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

/**
 * A utility class for mapping application java types to Hibernate persistent types.
 * 
 * @author TG Team
 *
 */
public class HibernateTypeDeterminer {
    
    private static final TypeResolver typeResolver = new TypeConfiguration().getTypeResolver();
    private static final Type H_LONG = typeResolver.basic("long");

    private final HibernateTypeMappings hibernateTypeMappings;

    public HibernateTypeDeterminer(final HibernateTypeMappings hibernateTypeMappings) {
        this.hibernateTypeMappings = hibernateTypeMappings;
    }

    /**
     * Determines hibernate type corresponding to the provided java type.
     * 
     * @param javaType
     * @param persistentType
     * @return either an instance of {@link Type}, {@link UserType} or {@link CompositeUserType}
     */
    public Object getHibernateType(final Class<?> javaType, final PersistentType persistentType) {

        // if javaType represents a persistent entity then simply return ID type (which is LONG)
        if (isPersistedEntityType(javaType) || isUnionEntityType(javaType)) {
            return H_LONG;
        }
        
        // otherwise analyse string representation of the provided persistentType, which could be empty 
        final String hibernateTypeName = persistentType != null ? persistentType.value() : null;

        if (isNotEmpty(hibernateTypeName)) {
            return typeResolver.basic(hibernateTypeName);
        }

        // this point is reached if the string representation of persistentType was empty, so we need to analyse the userType
        final Class<?> hibernateUserTypeImplementor = persistentType != null ? persistentType.userType() : Void.class;

        if (!Void.class.equals(hibernateUserTypeImplementor)) { // Hibernate type is definitely either IUserTypeInstantiate or ICompositeUserTypeInstantiate
            return hibernateTypeMappings.getHibernateType(hibernateUserTypeImplementor).orElse(null);
        } else {
            return hibernateTypeMappings.getHibernateType(javaType)
                    .orElseGet(() -> {
                        try {
                            // this has been observed to fail internally sometimes
                            return typeResolver.heuristicType(javaType.getName());
                        } catch (final Exception e) {
                            throw new DbSchemaException("Couldn't determine Hibernate type of [%s]".formatted(javaType.getTypeName()), e);
                        }
                    });
        }
    }   
}
