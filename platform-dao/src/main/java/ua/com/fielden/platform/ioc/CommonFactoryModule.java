package ua.com.fielden.platform.ioc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.dao.factory.DaoFactory;
import ua.com.fielden.platform.dao2.MappingsGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;

/**
 * Hibernate driven module required for correct instantiation of entities.
 *
 * @author TG Team
 *
 */
public class CommonFactoryModule extends PropertyFactoryModule {

    public CommonFactoryModule(final Properties props, final Map<Class, Class> defaultHibernateTypes, final List<Class<? extends AbstractEntity>> applicationEntityTypes) throws Exception {
	super(props, defaultHibernateTypes, applicationEntityTypes);
    }

    public CommonFactoryModule(final SessionFactory sessionFactory, final MappingExtractor mappingExtractor, final MappingsGenerator mappingsGenerator) {
	super(sessionFactory, mappingExtractor, mappingsGenerator);
    }

    protected EntityFactory getEntityFactory() {
	return entityFactory;
    }

    protected DaoFactory getDaoFactory() {
	return daoFactory;
    }
}
