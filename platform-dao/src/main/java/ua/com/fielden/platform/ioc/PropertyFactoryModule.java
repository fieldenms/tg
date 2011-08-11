package ua.com.fielden.platform.ioc;

import org.hibernate.SessionFactory;

import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao.factory.DaoFactory;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.IMetaPropertyFactory;
import ua.com.fielden.platform.entity.property.DaoMetaPropertyFactory;

import com.google.inject.Scopes;

/**
 * Hibernate driven module required for correct instantiation of entities.
 *
 * @author TG Team
 *
 */
public class PropertyFactoryModule extends TransactionalModule {

    protected final DaoFactory daoFactory;
    protected final EntityFactory entityFactory;

    public PropertyFactoryModule(final SessionFactory sessionFactory, final MappingExtractor mappingExtractor, final MappingsGenerator mappingsGenerator) {
	super(sessionFactory, mappingExtractor, mappingsGenerator);
	daoFactory = new DaoFactory(){};
	entityFactory = new EntityFactory(){};
    }

    @Override
    protected void configure() {
	super.configure();
	bind(EntityFactory.class).toInstance(entityFactory);
	// bind DaoFactory, which is needed purely for MetaPropertyFactory
	bind(DaoFactory.class).toInstance(daoFactory);
	// bind property factory
	bind(IMetaPropertyFactory.class).to(DaoMetaPropertyFactory.class).in(Scopes.SINGLETON);

    }
}
