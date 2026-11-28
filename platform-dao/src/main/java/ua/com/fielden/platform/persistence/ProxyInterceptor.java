package ua.com.fielden.platform.persistence;

import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;

import java.io.Serializable;

import org.apache.logging.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.hibernate.type.Type;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.keygen.KeyNumber;

/**
 * This is a thread-safe implementation of the global Hibernate intercepter for correct handling of entities enhanced with Guice(CGLIB) method intercepter.
 *
 * @author TG Team
 */
public class ProxyInterceptor extends EmptyInterceptor {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = getLogger(ProxyInterceptor.class);

    private final transient EntityFactory factory;

    public ProxyInterceptor(final EntityFactory factory) {
        this.factory = factory;
    }

    /**
     * Determines the correct class name for enhanced instances.
     */
    @Override
    public String getEntityName(final Object object) {
        if (object instanceof AbstractEntity) {
            return ((AbstractEntity<?>) object).getType().getName();
        }
        return super.getEntityName(object);
    }

    /**
     * Instantiates entities using {@link EntityFactory} instead of the default constructor.
     */
    @Override
    public Object instantiate(final String entityName, final EntityMode entityMode, final Serializable id) {
        LOGGER.debug(format("instantiating: %s for id = %s", entityName, id));
        try {
            final Class<AbstractEntity<?>> entityType = (Class<AbstractEntity<?>>) Class.forName(entityName);
            // KeyNumber relies on being loaded via a Hibernate session with a lock option (refer KeyNumberDao for more details)
            if (KeyNumber.class.isAssignableFrom(entityType)) {
                return factory.newEntity(entityType, (Long) id);
            } else {
                return factory.newEntityLightweight(entityType, (Long) id);
            }
        } catch (final ClassNotFoundException ex) {
            LOGGER.fatal(ex);
            throw Result.failure(ex);
        }
    }

    /// Union entities are mapped as components, which Hibernate instantiates itself, bypassing [#instantiate(String, EntityMode, Serializable)].
    /// They are initialised by [EntityFactory] here, as part of loading the entity that owns them.
    ///
    @Override
    public boolean onLoad(final Object entity, final Serializable id, final Object[] state, final String[] propertyNames, final Type[] types) {
        for (final Object value : state) {
            if (value instanceof AbstractUnionEntity union) {
                factory.initPlainEntity(union);
            }
        }
        return false;
    }

}
