package ua.com.fielden.platform.ioc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao.factory.DaoFactory2;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;

/**
 * Hibernate driven module required for correct instantiation of entities.
 *
 * @author TG Team
 *
 */
public class CommonFactoryModule2 extends PropertyFactoryModule2 {

    public CommonFactoryModule2(final Properties props, final Map<Class, Class> defaultHibernateTypes, final List<Class<? extends AbstractEntity>> applicationEntityTypes) throws Exception {
	super(props, defaultHibernateTypes, applicationEntityTypes);
    }

    public CommonFactoryModule2(final SessionFactory sessionFactory, final MappingsGenerator mappingsGenerator) {
	super(sessionFactory, mappingsGenerator);
    }

    protected EntityFactory getEntityFactory() {
	return entityFactory;
    }

    protected DaoFactory2 getDaoFactory() {
	return daoFactory;
    }
}
