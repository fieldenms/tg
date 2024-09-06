package ua.com.fielden.platform.ioc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;

/**
 * Hibernate driven module required for correct instantiation of entities.
 *
 * @author TG Team
 *
 */
public class CommonFactoryModule extends PropertyFactoryModule {

    public CommonFactoryModule(final Properties props, final Map<Class, Class> defaultHibernateTypes, final List<Class<? extends AbstractEntity<?>>> applicationEntityTypes) {
        super(props, defaultHibernateTypes, applicationEntityTypes);
    }

    protected EntityFactory getEntityFactory() {
        return entityFactory;
    }

}
