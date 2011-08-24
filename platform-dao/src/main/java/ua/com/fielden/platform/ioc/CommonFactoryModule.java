package ua.com.fielden.platform.ioc;

import java.util.Map;
import java.util.Properties;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao.factory.DaoFactory;
import ua.com.fielden.platform.entity.factory.EntityFactory;

/**
 * Hibernate driven module required for correct instantiation of entities.
 *
 * @author TG Team
 *
 */
public class CommonFactoryModule extends PropertyFactoryModule {

    public CommonFactoryModule(final Properties props, final Map<Class, Class> defaultHibernateTypes, final Class[] applicationEntityTypes, final boolean initFactories) throws Exception {
	super(props, defaultHibernateTypes, applicationEntityTypes);
	if (initFactories) {
	    daoFactory.setModule(this);
	    entityFactory.setModule(this);
	}
    }

    public CommonFactoryModule(final SessionFactory sessionFactory, final MappingExtractor mappingExtractor, final MappingsGenerator mappingsGenerator, final boolean initFactories) {
	super(sessionFactory, mappingExtractor, mappingsGenerator);
	if (initFactories) {
	    daoFactory.setModule(this);
	    entityFactory.setModule(this);
	}
    }

    protected EntityFactory getEntityFactory() {
	return entityFactory;
    }

    protected DaoFactory getDaoFactory() {
	return daoFactory;
    }
}
