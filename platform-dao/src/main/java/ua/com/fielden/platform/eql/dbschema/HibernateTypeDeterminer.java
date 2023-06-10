package ua.com.fielden.platform.eql.dbschema;

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.Map;

import org.hibernate.type.Type;
import org.hibernate.type.TypeResolver;
import org.hibernate.type.spi.TypeConfiguration;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.UserType;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.annotation.PersistentType;

/**
 * A utility class for mapping application java types to Hibernate persistent types.
 * 
 * @author TG Team
 *
 */
public class HibernateTypeDeterminer {
    
    private final Injector hibTypesInjector;
    private final Map<Class<?>, Object> hibTypesDefaults;
    private static final TypeResolver typeResolver = new TypeConfiguration().getTypeResolver();
    private static final Type H_LONG = typeResolver.basic("long");
    
    public HibernateTypeDeterminer(final Injector hibTypesInjector, final Map<Class<?>, Object> hibTypesDefaults) {
        this.hibTypesInjector = hibTypesInjector;
        this.hibTypesDefaults = hibTypesDefaults;
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

        if (hibTypesInjector != null && !Void.class.equals(hibernateUserTypeImplementor)) { // Hibernate type is definitely either IUserTypeInstantiate or ICompositeUserTypeInstantiate
            return hibTypesInjector.getInstance(hibernateUserTypeImplementor);
        } else {
            final Object defaultHibType = hibTypesDefaults.get(javaType);
            if (defaultHibType != null) { // default is provided for given property java type
                return defaultHibType;
            } else { // trying to mimic hibernate logic when no type has been specified - use hibernate's map of defaults
                return typeResolver.heuristicType(javaType.getName());
            }
        }
    }   
}
